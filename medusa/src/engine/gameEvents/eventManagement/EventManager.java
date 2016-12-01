package engine.gameEvents.eventManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import engine.gameEvents.GameEvent;
import engine.gameEvents.NullEvent;

public class EventManager
{
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	ConcurrentHashMap<String, List<EventHandler>> handlerMap;
	ConcurrentHashMap<UUID, EventQueue> eventQueues = new ConcurrentHashMap<UUID, EventQueue>();
	
	EventQueue readyQueue = new EventQueue(null);
	
	private long gvt;
	
	public EventManager()
	{
		handlerMap = new ConcurrentHashMap<String, List<EventHandler>>();
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
		long r = this.gvt;
		return r;
	}
	
	public void setGVT(long gvt)
	{
		this.gvt = gvt;
	}
	
	public ConcurrentHashMap<UUID, EventQueue> getQueues()
	{
		ConcurrentHashMap<UUID, EventQueue> r = eventQueues;
		return r;
	}
	
	public PriorityBlockingQueue<GameEvent> getAllEvents()
	{
		PriorityBlockingQueue<GameEvent> allEvents = new PriorityBlockingQueue<GameEvent>();
		
		for(EventQueue q : eventQueues.values())
		{
			allEvents.addAll(q.queue);
		}
		
		return allEvents;
	}
	
	public void registerHandler(EventHandler handler, String[] eventTypes)
	{
		for (String eventType : eventTypes)
		{
			handlerMap.putIfAbsent(eventType, Collections.synchronizedList(new ArrayList<EventHandler>()));
			
			List<EventHandler> handlerList = handlerMap.get(eventType);
			
			if (handlerList != null && !handlerList.contains(handler))
			{
				handlerList.add(handler);
			}
		}
	}
	
	public void unregisterHandler(EventHandler handler, String[] eventTypes)
	{
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
	}
	
	public synchronized void addQueue(UUID instanceID)
	{
		if (!eventQueues.containsKey(instanceID))
			eventQueues.put(instanceID, new EventQueue(instanceID));
	}
	
	public synchronized void removeQueue(UUID instanceID)
	{
		eventQueues.remove(instanceID);
	}
	
	private boolean queueExists(UUID i)
	{
		return eventQueues.containsKey(i);
	}
	
	public synchronized void queueEvent(GameEvent e)
	{
		UUID instance = e.getInstanceID();
		
		if (queueExists(instance))
		{
//			if (e instanceof InputEvent)
//			{
//				synchronized (tempList)
//				{
//					tempList.clear();
//					tempList.addAll(readyQueue.getQueue());
//					for (GameEvent other : tempList)
//					{
//						if (other instanceof InputEvent
//								&& other.getTimeStamp() == e.getTimeStamp()
//								&& other.getPriority() >= e.getPriority()
//								&& other.getInstanceID().equals(e.getInstanceID()))
//						{
//							e.setPriority(other.getPriority() + 1);
//						}
//					}
//				}
//			}
			
			eventQueues.get(instance).add(e);
			
//			if(e.getTimeStamp() >= gvt)
//				eventQueues.get(instance).add(e);
//			else
//				readyQueue.add(e);
		}
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
	
	@SuppressWarnings("unused")
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
	
	private boolean nullsReady()
	{
		boolean nullsReady = true;
		
		for (EventQueue q : eventQueues.values())
		{
			while (q.peek() != null && q.getFirstTimestamp() < gvt)
				readyQueue.add(q.poll());
			
			if (q.peek() != null && 
					(!q.peek().isType(NullEvent.class) || q.getFirstTimestamp() > gvt))
			{
				nullsReady = false;
			}
		}
		
		return nullsReady;
	}
	
	private void readyEvents(long cTime)
	{
		while (gvt <= cTime && nullsReady())
		{
			for (EventQueue q : eventQueues.values())
			{
				while (q.peek() != null && q.getFirstTimestamp() <= gvt)
					readyQueue.add(q.poll());
			}
			
			gvt++;
		}
	}
	
	public boolean handleEvents(long currentTime)
	{
		readyEvents(currentTime);
		
		handleFromQueue(currentTime, readyQueue);
		
		if (gvt <= currentTime)
			return false;
		
		return true;
	}
	
	public void handleFromQueue(long cTime, EventQueue queue)
	{
		while(queue.peek() != null && (queue.getFirstTimestamp() <= cTime))
		{
			dispatchToHandlers(queue.poll());
		}
	}
}
