package engine.gameEvents;

import java.io.Serializable;

public abstract class GameEvent implements Serializable, Comparable<GameEvent>
{
	private static final long serialVersionUID = 2310896639535068630L;
	
	private long timeStamp;
	private int priority;
	
	public GameEvent(long ts, int p)
	{
		this.timeStamp = ts;
		this.priority = p;
	}
	
	@Override
	public int compareTo(GameEvent o)
	{
		if(this.timeStamp == o.timeStamp)
		{
			if (this.priority == o.priority)
				return 0;
			else if (this.priority > o.priority)
				return 1;
			else
				return -1;
		}
		else if (this.timeStamp > o.timeStamp)
		{
			return 1;
		}
		else
		{
			return -1;
		}
	}
	
	public String getEventType()
	{
		return this.getClass().getSimpleName();
	}
	
	public long getTimeStamp()
	{
		return this.timeStamp;
	}
	
	public int getPriority()
	{
		return this.priority;
	}
}
