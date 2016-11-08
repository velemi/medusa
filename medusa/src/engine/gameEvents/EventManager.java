package engine.gameEvents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class EventManager
{
	ConcurrentHashMap<String, List<EventHandler>> handlerMap;
	
	PriorityBlockingQueue<GameEvent> eventQueue;
	
	public EventManager()
	{
		handlerMap = new ConcurrentHashMap<String, List<EventHandler>>();
		eventQueue = new PriorityBlockingQueue<GameEvent>();
	}
	
	public void registerHandler(EventHandler handler, String[] eventTypes)
	{
		for (String eventType : eventTypes)
		{
			List<EventHandler> handlerList = 
					handlerMap.putIfAbsent(eventType, 
							Collections.synchronizedList(new ArrayList<EventHandler>()));
			
			if (!handlerList.contains(handler))
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
		eventQueue.add(e);
		
		//TODO send a copy of the event to other machines
	}
	
	private void dispatchEvent(GameEvent e)
	{
		List<EventHandler> handlerList = handlerMap.get(e.getEventType());
		
		synchronized(handlerList)
		{
			for (EventHandler h : handlerList)
			{
				h.handleEvent(e);
			}
		}
	}
	
	public void handleEvents(long ts)
	{
		//TODO
	}
}
