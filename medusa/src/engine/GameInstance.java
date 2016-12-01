package engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import engine.gameEvents.NullEvent;
import engine.gameEvents.SpawnEvent;
import engine.gameEvents.eventManagement.EventHandler;
import engine.gameEvents.eventManagement.EventManager;
import engine.gameObjects.GameObject;
import engine.gameObjects.GameObjectSet;
import engine.gameObjects.PlayerObject;
import engine.gameObjects.SpawnPoint;
import engine.gameObjects.objectClasses.PhysicsObject;
import engine.gameObjects.objectClasses.RenderableObject;
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
	public static final boolean DEBUG = false;
	
	public static final int TARGET_FRAMERATE = 60;
	
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 800;
	
	/** The port number to be used by the server */
	public static final int SERVER_PORT = 7734;
	
	protected GameInstance thisInstance = this;
	
	protected ReentrantReadWriteLock exeLock = new ReentrantReadWriteLock(true);
	
	String gameTitle = "";

	protected UUID instanceID;

	public ReplayManager replayManager = new ReplayManager(this);
	
	public EventManager eventManager = new EventManager();
	
	GameObjectSet objectMap = new GameObjectSet();
	
	Timeline gameTimeline;
	
	long currentTime;
	
	long inputTime = 0;
	
	int inputCount = 0;
	
	ConcurrentLinkedQueue<String> inputLog = new ConcurrentLinkedQueue<String>();
	
	ConcurrentLinkedQueue<String> handledLog = new ConcurrentLinkedQueue<String>();
	
	private Object deepClone(Object object)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public int getPlayerCount()
	{
		return objectMap.getPlayerCount();
	}
	
	@SuppressWarnings("unchecked")
	protected void displayLogs()
	{
		pauseExeAndTime();
		ConcurrentLinkedQueue<String> iLog = (ConcurrentLinkedQueue<String>) deepClone(inputLog);
		ConcurrentLinkedQueue<String> hLog = (ConcurrentLinkedQueue<String>) deepClone(handledLog);
		
		System.out.println("Log for instance " + getInstanceID() + ", t=" + currentTime + ": ");
		
		System.out.println("Inputs received: (" + iLog.size() + ")");
		while (!iLog.isEmpty())
		{
			System.out.println(iLog.poll());
		}
		
		System.out.println("\nInputs handled: (" + hLog.size() + ")");
		while (!hLog.isEmpty())
		{
			System.out.println(hLog.poll());
		}
		
		resumeExeAndTime();
	}
	
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

	public long getCurrentTime()
	{
		return currentTime;
	}

	public void setGameTitle(String title)
	{
		synchronized(gameTitle)
		{
			gameTitle = title;
		}
	}
	
	public String getGameTitle()
	{
		synchronized(gameTitle)
		{
			return gameTitle;
		}
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

	public abstract void queueEvent(GameEvent e, boolean propagate);
	
	/*
	 * Defines PApplet settings for size() and smooth() values. (non-Javadoc)
	 * @see processing.core.PApplet#settings()
	 */
	public void settings()
	{
		size(SCREEN_WIDTH, SCREEN_HEIGHT);
	}
	
	private void drawGrid(float gridSize)
	{
		fill(25);
		stroke(25);
		
		for (int i = 1; i < SCREEN_WIDTH / gridSize; i++)
		{
			line(0, i * gridSize, SCREEN_WIDTH, i * gridSize);
		}
		
		for (int i = 1; i < SCREEN_HEIGHT / gridSize; i++)
		{
			line(i * gridSize, 0, i * gridSize, SCREEN_HEIGHT);
		}
	}
	
	/*
	 * Defines behavior to be run once per animation frame of the PApplet.
	 * (non-Javadoc)
	 * @see processing.core.PApplet#draw()
	 */
	public void draw()
	{
		// render the frame & gameObjects
		if(getGameTitle().equals("invaders"))
		{
			background(0);
			drawGrid(50);
		}
		else
		{
			background(204);
		}
		
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
		
		long l = getCurrentTime();
		long r = gameTimeline.getTime();
		
		String logic = "Logical Time: " + l;
		String real =  "Game Time: " + l;
		String off = "Time Offset: " + (r - l);
		
		fill(0,100,0);
		stroke(0,100,0);
		textSize(10);
		text(real + "   " + logic + "   " + off, 5, SCREEN_HEIGHT - 5);
	}

	public class ReplayManager
	{
		private GameReplay replay = null;
		
		public GameObjectSet rObjects = null;
		
		private Timeline replayTimeline;
		
		private boolean recording = false;
		
		private long fps;
		
		private long rCurrentTime;
		
		private boolean playing = false;
		
		private GameInstance gameInstance;
		
		public ReplayManager(GameInstance i)
		{
			gameInstance = i;
		}
		
		public long getReplayTime()
		{
			if (replayTimeline != null)
			{
				return replayTimeline.getTime();
			}
			
			return -1L;
		}
		
		public boolean isPlaying()
		{
			return playing;
		}
		
		public int getReplayPlayerCount()
		{
			return rObjects.getPlayerCount();
		}
		
		public boolean isRecording()
		{
			return recording;
		}
		
		public void playReplay(long fps)
		{
			if (replay != null && replay.isComplete() && !playing)
			{
				this.fps = fps;
				
				new ReplayLogicThread().start();
			}
		}
		
		public void startRecording()
		{
			new RStartThread().start();
		}
		
		public void stopRecording()
		{
			new RStopThread().start();
		}
		
		private void handle(CollisionEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			if (getGameTitle().equals("platform"))
				ScriptManager.loadScript("scripts/platformer/collisionEvent_handling.js");
			else if (getGameTitle().equals("invaders"))
				ScriptManager.loadScript("scripts/invaders/collisionEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", true);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
		
		private void handle(DeathEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			if (getGameTitle().equals("platform"))
				ScriptManager.loadScript("scripts/platformer/deathEvent_handling.js");
			else if (getGameTitle().equals("invaders"))
				ScriptManager.loadScript("scripts/invaders/deathEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", true);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
		
		private void handle(DespawnEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			ScriptManager.loadScript("scripts/platformer/despawnEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", true);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
		
		private void handle(InputEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", rObjects);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("replayManager", replayManager);
			
			ScriptManager.loadScript("scripts/platformer/inputEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", true);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
		
		private void handle(SpawnEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			ScriptManager.loadScript("scripts/platformer/spawnEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", true);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
		
		private void handleFromQueue(long cTime, ConcurrentLinkedQueue<GameEvent> queue)
		{
			while(queue.peek() != null && (queue.peek().getTimeStamp() <= cTime))
			{
				replayHandle(queue.poll());
			}
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
				replayTimeline = 
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
	}

	protected class CoreEventHandler implements EventHandler
	{
		@Override
		public void handleEvent(GameEvent e)
		{
			//System.out.println(e.getEventType());
			
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
				default:
					break;
			}
		}
		
		private void handle(CollisionEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			if (getGameTitle().equals("platform"))
				ScriptManager.loadScript("scripts/platformer/collisionEvent_handling.js");
			else if (getGameTitle().equals("invaders"))
				ScriptManager.loadScript("scripts/invaders/collisionEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
		
		private void handle(InputEvent e)
		{
			PlayerObject p = objectMap.getPlayerObject(e.getInstanceID());
			
			String h = e.getInstanceID() + ", ts="+ e.getTimeStamp() + ", count=" + (e.getPriority() - 10)
					+ ": " + e.getInput() + "@t=" + currentTime + " -> ";
			
			
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("replayManager", replayManager);
			
			ScriptManager.loadScript("scripts/platformer/inputEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
			
			if (p != null)
				h = h + "l:" + p.isLeftPressed() + ", r:" + p.isRightPressed() + ", j:" 
						+ p.isJumpPressed();
			
			handledLog.add(h);
		}
		
		private void handle(DeathEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			if (getGameTitle().equals("platform"))
				ScriptManager.loadScript("scripts/platformer/deathEvent_handling.js");
			else if (getGameTitle().equals("invaders"))
				ScriptManager.loadScript("scripts/invaders/deathEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
		
		private void handle(SpawnEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			ScriptManager.loadScript("scripts/platformer/spawnEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
		
		private void handle(DespawnEvent e)
		{
			ScriptManager.lock();
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			ScriptManager.loadScript("scripts/platformer/despawnEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
			ScriptManager.unlock();
		}
	}

	protected class CoreLogicThread extends Thread
	{
		public void run()
		{
			while(true)
			{
				long newTime = gameTimeline.getTime();
				
				while(newTime > currentTime)
				{
					exeLock.readLock().lock();
					
					NullEvent n = new NullEvent(currentTime, instanceID);
					queueEvent(n, true);
					
					exeLock.readLock().unlock();
					
					boolean handled = false;
					
					while (!handled)
					{
						exeLock.readLock().lock();
						
						handled = eventManager.handleEvents(currentTime);
						
						exeLock.readLock().unlock();
					}
					
					exeLock.readLock().lock();
					
					currentTime++;
					
					for (GameObject moveObject : objectMap.getObjectsOfClass(PhysicsObject.class))
					{
						if (!(moveObject instanceof PlayerObject))
						{
							((PhysicsObject) moveObject).doPhysics(thisInstance);
						}
						else if (((PlayerObject) moveObject).isAlive())
						{
							((PlayerObject) moveObject).doPhysics(thisInstance);
						}
					}
					
					exeLock.readLock().unlock();
				}
				
			}
		}
	}
}
