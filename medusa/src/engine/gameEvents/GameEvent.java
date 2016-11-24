package engine.gameEvents;

import java.io.Serializable;
import java.util.UUID;

public abstract class GameEvent implements Serializable, Comparable<GameEvent>
{
	private static final long serialVersionUID = 2310896639535068630L;
	
	private GameEvent parentEvent;
	private long timeStamp;
	private int priority;
	private UUID instanceID;
	
	private int age;
	
	public GameEvent(long ts, int p, UUID instanceID)
	{
		this(null, ts, p, instanceID);
	}
	
	public GameEvent(GameEvent parent, long ts, int p, UUID instanceID)
	{
		this.parentEvent = parent;
		this.timeStamp = ts;
		this.priority = p;
		this.instanceID = instanceID;
		
		if (this.parentEvent == null)
		{
			this.age = 0;
		}
		else
		{
			this.age = this.parentEvent.age;
		}
	}
	
	@Override
	public int compareTo(GameEvent o)
	{
		if (this.timeStamp == o.timeStamp)
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
	
	public boolean isType(Class<?> t)
	{
		return t.isInstance(this);
	}
	
	public String getEventType()
	{
		return this.getClass().getSimpleName();
	}
	
	public GameEvent getParentEvent()
	{
		return this.parentEvent;
	}
	
	public long getTimeStamp()
	{
		return this.timeStamp;
	}
	
	public int getPriority()
	{
		return this.priority;
	}
	
	public void setPriority(int priority)
	{
		this.priority = priority;
	}
	
	public UUID getInstanceID()
	{
		return this.instanceID;
	}
	
	public int getAge()
	{
		return this.age;
	}
}
