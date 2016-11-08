package engine.gameEvents;

import java.io.Serializable;

public abstract class GameEvent implements Serializable
{
	private static final long serialVersionUID = 2310896639535068630L;
	
	long timeStamp;
	int priority;
	
	public GameEvent(long ts, int p)
	{
		this.timeStamp = ts;
		this.priority = p;
	}
	
	public String getEventType()
	{
		return this.getClass().getSimpleName();
	}
}
