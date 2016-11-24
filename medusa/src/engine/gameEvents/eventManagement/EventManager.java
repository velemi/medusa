package engine.gameEvents.eventManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import engine.gameEvents.GameEvent;
import engine.gameEvents.InputEvent;
import engine.gameEvents.NullEvent;

public class EventManager
{
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	ConcurrentHashMap<String, List<EventHandler>> handlerMap;
	ConcurrentHashMap<UUID, EventQueue> eventQueues = new ConcurrentHashMap<UUID, EventQueue>();
	
	EventQueue readyQueue = new EventQueue(null);
	
	//PriorityBlockingQueue<GameEvent> readyEventQueue;
	
	private ArrayList<GameEvent> tempList = new ArrayList<GameEvent>();
	
	private long gvt;
	
	public EventManager()
	{
		handlerMap = new ConcurrentHashMap<String, List<EventHandler>>();
		//readyEventQueue = new PriorityBlockingQueue<GameEvent>();
	}
	
	public void lockManager()
	{
		lock.writeLock().lock();
	}
	
	public void unlockManager()
	{
		lock.writeLock().unlock();
	}
	
	public long getGVT()
	{
		//lock.readLock().lock();
		
		long r = this.gvt;
		
		//lock.readLock().unlock();
		
		return r;
	}
	
	public void setGVT(long gvt)
	{
		//lock.writeLock().lock();
		
		this.gvt = gvt;
		
		//lock.writeLock().unlock();
	}
	
	public ConcurrentHashMap<UUID, EventQueue> getQueues()
	{
		//lock.readLock().lock();
		
		ConcurrentHashMap<UUID, EventQueue> r = eventQueues;
		
		//lock.readLock().unlock();
		
		return r;
	}
	
	public PriorityBlockingQueue<GameEvent> getAllEvents()
	{
		//lock.readLock().lock();
		
		PriorityBlockingQueue<GameEvent> allEvents = new PriorityBlockingQueue<GameEvent>();
		
		for(EventQueue q : eventQueues.values())
		{
			allEvents.addAll(q.queue);
		}
		
		//lock.readLock().unlock();
		
		return allEvents;
	}
	
	public void registerHandler(EventHandler handler, String[] eventTypes)
	{
		//lock.writeLock().lock();
		
		for (String eventType : eventTypes)
		{
			handlerMap.putIfAbsent(eventType, Collections.synchronizedList(new ArrayList<EventHandler>()));
			
			List<EventHandler> handlerList = handlerMap.get(eventType);
			
			if (handlerList != null && !handlerList.contains(handler))
			{
				handlerList.add(handler);
			}
		}
		
		//lock.writeLock().unlock();
	}
	
	public void unregisterHandler(EventHandler handler, String[] eventTypes)
	{
		//lock.writeLock().lock();
		
		for (String eventType : eventTypes)
		{
			if (handlerMap.containsKey(eventType))
			{
				List<EventHandler> handlerList = handlerMap.get(eventType);
				
				handlerList.remove(handler);
				
				if (handlerList.isEmpty())
				{
					handlerMap.remove(eventType);
				}
			}
		}
		
		//lock.writeLock().unlock();
	}
	
	public void addQueue(UUID instanceID)
	{
		//lock.writeLock().lock();
		
		if (!eventQueues.containsKey(instanceID))
		{
			eventQueues.put(instanceID, new EventQueue(instanceID));
		}
		
		//lock.writeLock().unlock();
	}
	
	public void removeQueue(UUID instanceID)
	{
		//lock.writeLock().lock();
		
		eventQueues.remove(instanceID);
		
		//lock.writeLock().unlock();
	}
	
	public void queueEvent(GameEvent e)
	{
		//lock.writeLock().lock();
		
		UUID instance = e.getInstanceID();
		
		//createQueueIfNeeded(instance);
		
		if (e instanceof InputEvent)
		{
			synchronized (tempList)
			{
				tempList.clear();
				tempList.addAll(readyQueue.getQueue());
				for (GameEvent other : tempList)
				{
					if (other instanceof InputEvent
							&& other.getTimeStamp() == e.getTimeStamp()
							&& other.getPriority() >= e.getPriority()
							&& other.getInstanceID().equals(e.getInstanceID()))
					{
						e.setPriority(other.getPriority() + 1);
					}
				}
			}
		}
		
		
		if(e.getTimeStamp() >= gvt)
			eventQueues.get(instance).add(e);
		else
			readyQueue.add(e);
		
		
		//readyQueue.add(e);
		
		//lock.writeLock().unlock();
	}
	
	private void dispatchToHandlers(GameEvent e)
	{
		List<EventHandler> handlerList = handlerMap.get(e.getEventType());
		
		if (handlerList != null)
		{
			for (EventHandler h : handlerList)
			{
				h.handleEvent(e);
			}
			
		}
	}
	
	private void printState()
	{
		System.out.println("----------------------");
		System.out.println("GVT = " + gvt);
		System.out.println();
		for (EventQueue q : eventQueues.values())
		{
			System.out.println(q.toString());
		}
	}
	
	private void waitForNullEvents()
	{
		boolean nullsReceived = true;
		
		//printState();
		
		long lastPrint = 0;
		
		do
		{
			//lock.readLock().lock();
			
			nullsReceived = true;
			
			for (EventQueue q : eventQueues.values())
			{
				
				if (q.peek() == null || 
						!(q.peek().isType(NullEvent.class) && q.getFirstTimestamp() <= gvt))
				{
					nullsReceived = false;
					
					//System.out.println("Null not found for " + q.instanceID);
				}
				else if (q.getFirstTimestamp() < gvt)
				{
					readyQueue.add(q.poll());
					nullsReceived = false;
				}
				
			}
			
			//lock.readLock().unlock();
			
			// TODO make thread sleep until more events have been queued?
			if (!nullsReceived)
			{
				if (lastPrint < gvt)
				{
					printState();
					lastPrint = gvt;
				}
			}
		}
		while(!nullsReceived);
		
		printState();
	}
	
	private void prepareEvents(long cTime)
	{
		while (gvt <= cTime)
		{
			waitForNullEvents();
			
			//lock.writeLock().lock();
			
			for (EventQueue q : eventQueues.values())
			{
				while (q.peek() != null && q.getFirstTimestamp() <= gvt)
				{
					readyQueue.add(q.poll());
				}
			}
			
			gvt++;
			
			//lock.writeLock().unlock();
		}
	}
	
	public void handleEvents(long currentTime)
	{
		prepareEvents(currentTime);
		
		while(readyQueue.peek() != null && (readyQueue.getFirstTimestamp() <= currentTime))
		{
			dispatchToHandlers(readyQueue.poll());
		}
	}
}
