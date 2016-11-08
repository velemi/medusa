package engine.gameEvents;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager
{
	ConcurrentHashMap<String, ArrayList<EventHandler>> handlerMap;
	
	public EventManager()
	{
		handlerMap = new ConcurrentHashMap<String, ArrayList<EventHandler>>();
	}
	
	public void registerHandler(EventHandler handler, String[] eventTypes)
	{
		//TODO
	}
	
	public void raiseEvent(GameEvent e)
	{
		//TODO
	}
}
