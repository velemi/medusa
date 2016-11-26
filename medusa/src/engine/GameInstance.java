package engine;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import engine.gameEvents.CollisionEvent;
import engine.gameEvents.DeathEvent;
import engine.gameEvents.DespawnEvent;
import engine.gameEvents.GameEvent;
import engine.gameEvents.InputEvent;
import engine.gameEvents.SpawnEvent;
import engine.gameEvents.eventManagement.EventManager;
import engine.gameObjects.GameObject;
import engine.gameObjects.GameObjectSet;
import engine.gameObjects.PlayerObject;
import engine.gameObjects.SpawnPoint;
import engine.gameObjects.objectClasses.Killable;
import engine.gameObjects.objectClasses.PhysicsObject;
import engine.gameObjects.objectClasses.RenderableObject;
import engine.gameObjects.objectClasses.Spawnable;
import engine.replay.GameReplay;
import engine.time.Timeline;
import processing.core.PApplet;

/**
 * An abstract class defining structure and behavior for classes which act as or
 * reflect an instance of a game
 * 
 * @author Jordan Neal
 */
public abstract class GameInstance extends PApplet
{
	public static final boolean DEBUG = true;
	
	public static final int TARGET_FRAMERATE = 60;
	
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 800;
	
	/** The port number to be used by the server */
	public static final int SERVER_PORT = 7734;
	
	protected ReentrantReadWriteLock exeLock = new ReentrantReadWriteLock(true);
	
	public ReplayManager replayManager = new ReplayManager(this);
	
	public EventManager eventManager = new EventManager();
	
	GameObjectSet objectMap = new GameObjectSet();
	
	protected UUID instanceID;
	
	Timeline gameTimeline;
	
	long currentTime;
	
	public abstract void queueEvent(GameEvent e, boolean propagate);
	
	protected void pauseExeAndTime()
	{
		exeLock.writeLock().lock();
		
		gameTimeline.pause();
	}
	
	protected void resumeExeAndTime()
	{
		gameTimeline.resume();
		
		exeLock.writeLock().unlock();
	}
	
	public UUID getInstanceID()
	{
		return this.instanceID;
	}
	
	public void addToMap(GameObject object)
	{
		objectMap.addToSet(object);
	}
	
	public void removeFromMap(GameObject object)
	{
		objectMap.removeFromSet(object);
	}
	
	public PlayerObject createNewPlayer()
	{
		ArrayList<GameObject> spawns = objectMap.getObjectsOfClass(SpawnPoint.class);
		
		Random r = new Random();
		
		int index = r.nextInt(spawns.size());
		
		SpawnPoint spawn = (SpawnPoint) spawns.get(index);
		PlayerObject newPlayer = null;
		
		if (spawn != null)
		{
			newPlayer = new PlayerObject(spawn);
		}
		else
		{
			newPlayer = new PlayerObject();
		}
		
		return newPlayer;
	}
	
	public ArrayList<GameObject> getColliding(GameObject o)
	{
		if (!replayManager.playing)
			return objectMap.getColliding(o, false);
		else
			return replayManager.rObjects.getColliding(o, false);
	}
	
	public ArrayList<GameObject> getPhysicalCollisions(double x, double y,
			double w, double h)
	{
		if (!replayManager.playing)
			return objectMap.getColliding(x, y, w, h, true);
		else
			return replayManager.rObjects.getColliding(x, y, w, h, true);
	}
	
	public boolean checkForPhysicalCollision(double x, double y, double w,
			double h)
	{
		if (!replayManager.playing)
			return objectMap.checkPhysCollision(x, y, w, h);
		else
			return replayManager.rObjects.checkPhysCollision(x, y, w, h);
	}
	
	/*
	 * Defines PApplet settings for size() and smooth() values. (non-Javadoc)
	 * @see processing.core.PApplet#settings()
	 */
	public void settings()
	{
		size(SCREEN_WIDTH, SCREEN_HEIGHT);
	}
	
	/*
	 * Defines behavior to be run once per animation frame of the PApplet.
	 * (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
	public void draw()
	{
		//System.out.print("start - ");
		
		// render the frame & gameObjects
		background(204);
		
		if (replayManager.isRecording())
		{
			fill(200,0,0);
			stroke(200,0,0);
			rect(SCREEN_WIDTH - 75, 50, 25, 25);
		}
		
		if (replayManager.playing)
		{
			fill(0,200,0);
			stroke(0,200,0);
			beginShape();
			vertex(SCREEN_WIDTH - 75, 50);
			vertex(SCREEN_WIDTH - 50, 62.5f);
			vertex(SCREEN_WIDTH - 75, 75);
			vertex(SCREEN_WIDTH - 75, 50);
			endShape();
			
			for (GameObject o : replayManager.rObjects.getObjectsOfClass(RenderableObject.class))
			{
				((RenderableObject) o).display(this);
			}
		}
		else
		{
			for (GameObject o : objectMap.getObjectsOfClass(RenderableObject.class))
			{
				((RenderableObject) o).display(this);
			}
		}
		
		//System.out.println("end");
	}
	
	public long getCurrentTime()
	{
		return currentTime;
	}
	
	protected class ReplayManager
	{
		private GameReplay replay = null;
		
		public GameObjectSet rObjects = null;
		
		private boolean recording = false;
		
		private long fps;
		
		private long rCurrentTime;
		
		private boolean playing = false;
		
		private GameInstance gameInstance;
		
		public ReplayManager(GameInstance i)
		{
			gameInstance = i;
		}
		
		private class RStartThread extends Thread
		{
			public void run()
			{
				pauseExeAndTime();
				
				recording = true;
				
				replay = new GameReplay(objectMap, eventManager, currentTime);
				
				resumeExeAndTime();
			}
		}
		
		public void startRecording()
		{
			new RStartThread().start();
		}
		
		private class RStopThread extends Thread
		{
			public void run()
			{
				pauseExeAndTime();
				
				replay.stopRecording(currentTime);
				
				recording = false;
				
				resumeExeAndTime();
			}
		}
		
		public void stopRecording()
		{
			new RStopThread().start();
		}
		
		public boolean isRecording()
		{
			return recording;
		}
		
		public boolean isPlaying()
		{
			return playing;
		}
		
		private void replayHandle(GameEvent e)
		{
			switch (e.getEventType())
			{
				case "CollisionEvent":
				{
					handle((CollisionEvent) e);
					break;
				}
				case "InputEvent":
				{
					handle((InputEvent) e);
					break;
				}
				case "DeathEvent":
				{
					handle((DeathEvent) e);
					break;
				}
				case "SpawnEvent":
				{
					handle((SpawnEvent) e);
					break;
				}
				case "DespawnEvent":
				{
					handle((DespawnEvent) e);
					break;
				}
				case "EndReplayEvent":
				{
					playing = false;
					break;
				}
				default:
					break;
			}
		}
		
		private void handle(CollisionEvent e)
		{
			GameObject[] objects = new GameObject[2];
			
			objects[0] = rObjects.getObject(e.getIDs()[0]);
			objects[1] = rObjects.getObject(e.getIDs()[1]);
		}
		
		private void handle(InputEvent e)
		{
			PlayerObject p = rObjects.getPlayerObject(e.getInstanceID());
			
			if (p != null)
			{
				switch (e.getInput())
				{
					case "LEFT PRESSED":
					{
						p.setLeftPressed(true);
						break;
					}
					case "RIGHT PRESSED":
					{
						p.setRightPressed(true);
						break;
					}
					case "JUMP PRESSED":
					{
						p.setJumpPressed(true);
						break;
					}
					case "LEFT RELEASED":
					{
						p.setLeftPressed(false);
						break;
					}
					case "RIGHT RELEASED":
					{
						p.setRightPressed(false);
						break;
					}
					case "JUMP RELEASED":
					{
						p.setJumpPressed(false);
						break;
					}
					default:
						break;
				}
			}
		}
		
		private void handle(DeathEvent e)
		{
			GameObject object = rObjects.getObject(e.getObjectID());
			
			if (object instanceof Killable)
			{
				((Killable) object).kill();
				removeFromMap(object);
			}
		}
		
		private void handle(SpawnEvent e)
		{
			GameObject object = e.getObject();
			
			if (object instanceof Spawnable)
			{
				((Spawnable) object).spawn();
				
				if (!rObjects.contains(object))
				{
					rObjects.addToSet(object);
				}
			}
		}
		
		private void handle(DespawnEvent e)
		{
			GameObject object = e.getObject();
			
			if (rObjects.contains(object))
			{
				rObjects.removeFromSet(object);
			}
		}
		
		private void handleFromQueue(long cTime, ConcurrentLinkedQueue<GameEvent> queue)
		{
			while(queue.peek() != null && (queue.peek().getTimeStamp() <= cTime))
			{
				replayHandle(queue.poll());
			}
		}
		
		private class ReplayLogicThread extends Thread
		{
			public ReplayLogicThread()
			{
				this.setName("ReplayLogicThread");
			}
			
			public void run()
			{
				pauseExeAndTime();
				
				// get replay object state
				rObjects = replay.getObjects();
				
				// get replay event queue
				ConcurrentLinkedQueue<GameEvent> rEvents = replay.getEventQueue();
				
				playing = true;
				
				// create replay timeline
				Timeline replayTimeline = 
						new Timeline(replay.getStartTime(), 1000000000L / fps);
				
				rCurrentTime = replay.getStartTime();
				
				while(playing)
				{
					// get newTime
					long newTime = replayTimeline.getTime();
					
					while(newTime > rCurrentTime)
					{
						// handle replay events for rCurrentTime
						handleFromQueue(rCurrentTime, rEvents);
						
						rCurrentTime++;
						
						// do physics stuff or whatever
						for (GameObject moveObject : rObjects.getObjectsOfClass(PhysicsObject.class))
						{
							if (!(moveObject instanceof PlayerObject))
							{
								((PhysicsObject) moveObject).doPhysics(gameInstance);
							}
							else if (((PlayerObject) moveObject).isAlive())
							{
								((PlayerObject) moveObject).doPhysics(gameInstance);
							}
						}
					}
				}
				
				resumeExeAndTime();
			}
		}
		
		public void playReplay(long fps)
		{
			if (replay != null && replay.isComplete() && !playing)
			{
				this.fps = fps;
				
				new ReplayLogicThread().start();
			}
		}
	}
}
