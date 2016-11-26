package engine.gameEvents.eventManagement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import engine.gameEvents.GameEvent;
import engine.gameEvents.NullEvent;

public class EventQueue implements Serializable
{
	private static final long serialVersionUID = 4698026313285744914L;

	PriorityBlockingQueue<GameEvent> queue = new PriorityBlockingQueue<GameEvent>();
	
	UUID instanceID;
	
	public EventQueue(UUID instanceID)
	{
		this.instanceID = instanceID;
	}
	
	public UUID getInstanceID()
	{
		return this.instanceID;
	}
	
	public boolean containsNullForTS(long ts)
	{
		//lock.readLock().lock();
		
		boolean r = false;
		
		ArrayList<GameEvent> a = new ArrayList<GameEvent>(queue);
		
		for (GameEvent e : a)
		{
			if (e.getTimeStamp() == ts && e instanceof NullEvent)
			{
				r = true;
			}
		}
		
		//lock.readLock().unlock();
		
		return r;
	}
	
	public PriorityBlockingQueue<GameEvent> getQueue()
	{
		//lock.readLock().lock();
		
		PriorityBlockingQueue<GameEvent> b = new PriorityBlockingQueue<GameEvent>(queue);
		
		//lock.readLock().unlock();
		
		return b;
	}
	
	public String toString()
	{
		//lock.readLock().lock();
		
		String a = "EventQueue " + instanceID.toString() + ": ";
		
		PriorityBlockingQueue<GameEvent> b = new PriorityBlockingQueue<GameEvent>(queue);
		
		while(!b.isEmpty())
		{
			GameEvent e = b.poll();
			
			a = a + "[" + e.getEventType() + ", ts=" + e.getTimeStamp() + "]";
		}
		
		//lock.readLock().unlock();
		
		return a;
	}
	
	public void add(GameEvent e)
	{
		//lock.writeLock().lock();
		
		queue.add(e);
		
		//lock.writeLock().unlock();
	}
	
	public long getFirstTimestamp()
	{
		//lock.readLock().lock();
		
		long r = queue.peek().getTimeStamp();
		
		//lock.readLock().unlock();
		
		return r;
	}
	
	public GameEvent peek()
	{
		//lock.readLock().lock();
		
		GameEvent r = queue.peek();
		
		//lock.readLock().unlock();
		
		return r;
	}
	
	public GameEvent poll()
	{
		//lock.writeLock().lock();
		
		GameEvent r = queue.poll();
		
		//lock.writeLock().unlock();
		
		return r;
	}

	public boolean isEmpty()
	{
		//lock.readLock().lock();
		
		boolean r = queue.isEmpty();
		
		//lock.readLock().unlock();
		
		return r;
	}

	public void addAll(ArrayList<GameEvent> t)
	{
		//lock.writeLock().lock();
		
		queue.addAll(t);
		
		//lock.writeLock().unlock();
	}
}
