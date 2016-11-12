package engine.gameEvents.eventManagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import engine.gameEvents.GameEvent;
import engine.gameEvents.InputEvent;

public class EventManager
{
	ConcurrentHashMap<String, List<EventHandler>> handlerMap;
	
	ConcurrentHashMap<UUID, InstanceEventQueue> instances = new ConcurrentHashMap<UUID, InstanceEventQueue>();
	
	@SuppressWarnings("unused")
	private class InstanceEventQueue
	{
		PriorityBlockingQueue<GameEvent> events = new PriorityBlockingQueue<GameEvent>();
		
		UUID instanceID;
		
		public InstanceEventQueue(UUID instanceID)
		{
			this.instanceID = instanceID;
		}
		
		public void add(GameEvent e)
		{
			events.add(e);
		}
		
		public long getFirstTimestamp()
		{
			return events.peek().getTimeStamp();
		}
		
		public GameEvent peek()
		{
			return events.poll();
		}
		
		public GameEvent poll()
		{
			return events.poll();
		}

		public boolean isEmpty()
		{
			return events.isEmpty();
		}

		public void addAll(ArrayList<GameEvent> tempRemoved)
		{
			events.addAll(tempRemoved);
		}
	}
	
	PriorityBlockingQueue<GameEvent> readyEventQueue;
	
	private ArrayList<GameEvent> tempList = new ArrayList<GameEvent>();
	
	private long gvt;
	
	public EventManager()
	{
		handlerMap = new ConcurrentHashMap<String, List<EventHandler>>();
		instances = new ConcurrentHashMap<UUID, InstanceEventQueue>();
		readyEventQueue = new PriorityBlockingQueue<GameEvent>();
		
	}
	
	public long getGVT()
	{
		return this.gvt;
	}
	
	public void setGVT(long gvt)
	{
		this.gvt = gvt;
	}
	
	public PriorityBlockingQueue<GameEvent> getAllEvents()
	{
		PriorityBlockingQueue<GameEvent> allEvents = new PriorityBlockingQueue<GameEvent>();
		
		for(InstanceEventQueue q : instances.values())
		{
			allEvents.addAll(q.events);
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
	
	public synchronized void removeQueue(UUID instanceID)
	{
		instances.remove(instanceID);
	}
	
	public void queueEvent(GameEvent e)
	{
		if (e instanceof InputEvent)
		{
			
			synchronized (tempList)
			{
				tempList.clear();
				tempList.addAll(readyEventQueue);
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
		
		readyEventQueue.add(e);
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
	
//	private synchronized void updateGVT()
//	{
//		long newGVT = Long.MAX_VALUE;
////		for (PriorityQueue<GameEvent> currentQueue : instanceQueues.values())
////		{
////			if (currentQueue != null && !currentQueue.isEmpty())
////				System.out.print(currentQueue.peek().getInstanceID() + " : "
////						+ currentQueue.peek().getTimeStamp() + " | ");
////						
////			long currentLow = gvt;
////			
////			tempRemoved.clear();
////			
////			boolean queueDone = false;
////			
////			while(!queueDone)
////			{
////				GameEvent e = currentQueue.peek();
////				
////				if (e == null)
////				{
////					queueDone = true;
////				}
////				else if (e instanceof NullEvent && e.getTimeStamp() <= newGVT)
////				{
////					currentLow = e.getTimeStamp();
////					tempRemoved.add(currentQueue.poll());
////				}
////				else
////				{
////					queueDone = true;
////				}
////			}
////			
////			 if (currentLow < newGVT)
////				 newGVT = currentLow;
////			 
////			 currentQueue.addAll(tempRemoved);
////		}
//		
//		for (InstanceEventQueue currentQueue : instances.values())
//		{
////			if (currentQueue != null && !currentQueue.isEmpty())
////				System.out.print(currentQueue.peek().getInstanceID() + " : "
////						+ currentQueue.peek().getTimeStamp() + " | ");
//						
//			long currentLow = gvt;
//			
//			tempRemoved.clear();
//			
//			boolean queueDone = false;
//			
//			while(!queueDone)
//			{
//				GameEvent e = currentQueue.peek();
//				
//				if (e == null)
//				{
//					queueDone = true;
//				}
//				else if (e instanceof NullEvent && e.getTimeStamp() <= newGVT)
//				{
//					currentLow = e.getTimeStamp();
//					tempRemoved.add(currentQueue.poll());
//				}
//				else
//				{
//					queueDone = true;
//				}
//			}
//			
//			 if (currentLow < newGVT)
//				 newGVT = currentLow;
//			 
//			 if (tempRemoved != null && tempRemoved.isEmpty())
//				 currentQueue.addAll(tempRemoved);
//		}
//		System.out.println();
//		
//		if (newGVT > gvt)
//			gvt = newGVT;
//	}
	
//	private void updateReadyEvents()
//	{
//		updateGVT();
//		
////		for (PriorityQueue<GameEvent> q : instanceQueues.values())
////		{
////			if (q != null)
////			{
////				while(q.peek() != null && q.peek().getTimeStamp() <= gvt)
////				{
////					readyEventQueue.add(q.poll());
////				} 
////			}
////		}
//		
//		for (InstanceEventQueue q : instances.values())
//		{
//			while(!q.isEmpty() && q.peek().getTimeStamp() <= gvt)
//			{
//				readyEventQueue.add(q.poll());
//			}
//		}
//	}
	
	public void handleEvents(long currentTime)
	{
		//System.out.println(getGVT());
		
		//updateReadyEvents();
		
		while(readyEventQueue.peek() != null
				&& (readyEventQueue.peek().getTimeStamp() <= currentTime))
		{
			dispatchToHandlers(readyEventQueue.poll());
		}
	}
}
