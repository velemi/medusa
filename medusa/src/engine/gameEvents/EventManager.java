package engine.gameEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class EventManager
{
	ConcurrentHashMap<String, List<EventHandler>> handlerMap;
	
	ConcurrentHashMap<UUID, PriorityBlockingQueue<GameEvent>> instanceQueues;
	
	PriorityBlockingQueue<GameEvent> eventQueue;
	
	long GVT;
	
	public EventManager()
	{
		handlerMap = new ConcurrentHashMap<String, List<EventHandler>>();
		instanceQueues = new ConcurrentHashMap<UUID, PriorityBlockingQueue<GameEvent>>();
		eventQueue = new PriorityBlockingQueue<GameEvent>();
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
	
	public void queueEvent(GameEvent e)
	{
		// instanceQueues.putIfAbsent(e.getInstanceID(),
		// new PriorityBlockingQueue<GameEvent>());
		//
		// PriorityBlockingQueue<GameEvent> instanceQueue =
		// instanceQueues.get(e.getInstanceID());
		//
		// instanceQueue.add(e);
		
		// TODO make sure only one copy of the "same" event is queued for the
		// same timestamp, at least for InputEvents
		
		eventQueue.add(e);
	}
	
	private void dispatchToHandlers(GameEvent e)
	{
		List<EventHandler> handlerList = handlerMap.get(e.getEventType());
		
		synchronized (handlerList)
		{
			for (EventHandler h : handlerList)
			{
				h.handleEvent(e);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private long calcGVT()
	{
		long newGVT = Long.MAX_VALUE;
		
		for (PriorityBlockingQueue<GameEvent> instQueue : instanceQueues.values())
		{
			long ts = instQueue.peek().getTimeStamp();
			if (ts < newGVT)
			{
				newGVT = ts;
			}
		}
		
		return newGVT;
	}
	
	public void handleEvents(long currentTime)
	{
		
		// TODO rewrite once events are handled across all machines?
		while(eventQueue.peek() != null
				&& (eventQueue.peek().getTimeStamp() <= currentTime))
		{
			dispatchToHandlers(eventQueue.poll());
		}
	}
}
