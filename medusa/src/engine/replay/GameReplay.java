package engine.replay;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;
import engine.gameEvents.EndReplayEvent;
import engine.gameEvents.GameEvent;
import engine.gameEvents.eventManagement.EventHandler;
import engine.gameEvents.eventManagement.EventManager;
import engine.gameObjects.GameObjectSet;

public class GameReplay
{
	boolean complete = false;
	
	GameObjectSet replayObjects;
	
	ConcurrentLinkedQueue<GameEvent> replayEvents = new ConcurrentLinkedQueue<GameEvent>();
	
	long startTime, endTime;
	
	boolean recording = true;
	
	RecordingEventHandler recHandler = new RecordingEventHandler();
	
	public GameReplay(GameObjectSet objects, EventManager events, long sTime)
	{
		this.replayObjects = (GameObjectSet) deepClone(objects);
		
		PriorityBlockingQueue<GameEvent> e = events.getAllEvents();
		
		while (!e.isEmpty())
		{
			this.replayEvents.add(e.poll());
		}
		
		this.startTime = sTime;
		
		events.registerHandler(recHandler, new String[] 
				{"CollisionEvent", "InputEvent", "DeathEvent", "SpawnEvent", "DespawnEvent"});
		
	}
	
	public void stopRecording(long eTime)
	{
		recording = false;
		
		endTime = eTime;
		
		replayEvents.add(new EndReplayEvent(endTime, Integer.MAX_VALUE, null));
		
		complete = true;
	}
	
	public boolean isComplete()
	{
		return complete;
	}
	
	@SuppressWarnings("unchecked")
	public ConcurrentLinkedQueue<GameEvent> getEventQueue()
	{
		return (ConcurrentLinkedQueue<GameEvent>) deepClone(replayEvents);
	}
	
	public GameObjectSet getObjects()
	{
		return (GameObjectSet) deepClone(replayObjects);
	}
	
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
	
	private class RecordingEventHandler implements EventHandler
	{

		@Override
		public void handleEvent(GameEvent e)
		{
			if (recording && e != null)
			{
				replayEvents.add(e);
			}
		}
		
	}
	
	public long getStartTime()
	{
		return this.startTime;
	}
	
	public long getEndTime()
	{
		return this.endTime;
	}
}
