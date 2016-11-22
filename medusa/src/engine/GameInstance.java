package engine;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import engine.gameEvents.GameEvent;
import engine.gameEvents.eventManagement.EventManager;
import engine.gameObjects.GameObject;
import engine.gameObjects.GameObjectSet;
import engine.gameObjects.PlayerObject;
import engine.gameObjects.SpawnPoint;
import engine.gameObjects.objectClasses.RenderableObject;
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
	
	protected UUID instanceID;
	
	long currentTime;
	
	Timeline gameTimeline;
	
	public EventManager eventManager = new EventManager();
	
	GameObjectSet objectMap = new GameObjectSet();
	
	public abstract void queueEvent(GameEvent e, boolean propagate);
	
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
		return objectMap.getColliding(o, false);
	}
	
	public ArrayList<GameObject> getPhysicalCollisions(double x, double y,
			double w, double h)
	{
		return objectMap.getColliding(x, y, w, h, true);
	}
	
	public boolean checkForPhysicalCollision(double x, double y, double w,
			double h)
	{
		return objectMap.checkPhysCollision(x, y, w, h);
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
		// render the frame & gameObjects
		background(204);
		
		for (GameObject o : objectMap.getObjectsOfClass(RenderableObject.class))
		{
			((RenderableObject) o).display(this);
		}
	}
	
	public long getCurrentTime()
	{
		return currentTime;
	}
	
//	public ReplayManager replayManager;
//	
//	public class ReplayManager
//	{
//		GameInstance instance;
//		
//		boolean record = false;
//		
//		public boolean playing = false;
//		
//		long startTime;
//		long endTime;
//		
//		Timeline replayTimeline;
//		Timeline mainTimeline;
//		
//		ConcurrentHashMap<UUID, GameObject> objectStart = new ConcurrentHashMap<UUID, GameObject>();
//		ConcurrentHashMap<UUID, GameObject> objectEnd = new ConcurrentHashMap<UUID, GameObject>();
//		
//		PriorityBlockingQueue<GameEvent> eventQueue = new PriorityBlockingQueue<GameEvent>();
//		
//		public ReplayManager(GameInstance instance)
//		{
//			this.instance = instance;
//			
//			eventManager.registerHandler(new ReplayEventHandler(), new String[] {
//					"NullEvent", "CollisionEvent", "InputEvent", "DeathEvent", "SpawnEvent" ,
//					"EndReplayEvent" });
//		}
//		
//		public void startRecording()
//		{
//			System.out.println("Starting a recording.");
//			startTime = currentTime;
//			objectStart.clear();
//			synchronized (objectMapLock)
//			{
//				for (GameObject o : gameObjectMap.values())
//				{
//					
//					
//					objectStart.put(o.getID(), o.clone());
//					//System.out.println(o.clone());
//				}
//			}
//			eventQueue.clear();
//			
//			replayTimeline = new Timeline(startTime, 1000000000L / TARGET_FRAMERATE);
//			replayTimeline.pause();
//			
//			record = true;
//		}
//		
//		public void stopRecording()
//		{
//			System.out.println("Stopping recording.");
//			objectEnd.clear();
//			synchronized (objectMapLock)
//			{
//				for (GameObject o : gameObjectMap.values())
//				{
//					objectEnd.put(o.getID(), o.clone());
//				}
//			}
//			endTime = currentTime;
//			
//			record = false;
//			
//			gameTimeline.resume();
//			
//			beginPlayback();
//		}
//		
//		private void beginPlayback()
//		{
//			System.out.println("Beginning playback.");
//			
//			gameObjectMap = new ConcurrentHashMap<UUID, GameObject>(objectStart);
//			
//			
//			
//			gameTimeline.pause();
//			mainTimeline = new Timeline(gameTimeline);
//			
//			System.out.println(startTime);
//			
//			while(!eventQueue.isEmpty())
//			{
//				GameEvent e = eventQueue.poll();
//				System.out.println(e.getTimeStamp() + ": "+ e);
//				queueEvent(e, false);
//				
//			}
//			
//			GameEvent f = new EndReplayEvent(endTime, 0, instanceID);
//			System.out.println(f.getTimeStamp() + ": "+ f);
//			queueEvent(f, false);
//			
//			replayTimeline.resume();
//			currentTime = replayTimeline.getTime();
//			
//			//gameTimeline.resume();
//			
//			playing = true;
//			
//			while (playing)
//			{
//				long newTime = replayTimeline.getTime();
//				
//				//System.out.println("cur:" + currentTime);
//				
//				//System.out.println(newTime);
//				
//				eventManager.handleEvents(currentTime);
//				
//				while(newTime > currentTime)
//				{
////					if (!replayManager.playing)
////						queueEvent(new NullEvent(currentTime, instanceID), true);
//					
//					currentTime++;
//					
//					// System.out.println("TIME: " + gameTimeline.getTime());
//					
//					synchronized (gameObjectMap)
//					{
//						for (MovingObject moveObject : movingObjects.values())
//						{
//							if (!(moveObject instanceof PlayerObject))
//							{
//								moveObject.doPhysics(instance);
//							}
//							else if (((PlayerObject) moveObject).isAlive())
//							{
//								((PlayerObject) moveObject).doPhysics(instance);
//							}
//						}
//					}
//				}
//			}
//		}
//		
//		private void endPlayback()
//		{
//			gameObjectMap = new ConcurrentHashMap<UUID, GameObject>(objectEnd);
//			
//			gameTimeline = mainTimeline;
//			
//			currentTime = endTime;
//			gameTimeline.resume();
//			
//			playing = false;
//		}
//		
//		private class ReplayEventHandler implements EventHandler
//		{
//			
//			@Override
//			public void handleEvent(GameEvent e)
//			{
//				if (record)
//				{
//					if(!(e instanceof InputEvent || e instanceof EndReplayEvent))
//					{
//						eventQueue.add(e);
//					}
//				}
//				
//				switch (e.getEventType())
//				{
//					case "InputEvent":
//					{
//						handle((InputEvent) e);
//						break;
//					}
//					case "EndReplayEvent":
//					{
//						handle((EndReplayEvent) e);
//						break;
//					}
//					default:
//						break;
//				}
//				
//			}
//			
//			private void handle(EndReplayEvent e)
//			{
//				endPlayback();
//			}
//
//			private void handle(InputEvent e)
//			{
//				//System.out.println(e.getInput());
//
//				switch(e.getInput())
//				{
//					case "START RECORDING":
//					{
//						startRecording();
//						break;
//					}
//					case "STOP RECORDING":
//					{
//						stopRecording();
//						break;
//					}
//					default:
//						eventQueue.add(e);
//						break;
//				}
//			}
//		}
//	}
}
