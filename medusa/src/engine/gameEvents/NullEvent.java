package engine.gameEvents;

import java.util.UUID;

public class NullEvent extends GameEvent
{
	private static final long serialVersionUID = 2263375812712767408L;
	
	private static final int DEFAULT_PRIORITY = 0;
	
	public NullEvent(long ts, UUID instanceID)
	{
		this(ts, DEFAULT_PRIORITY, instanceID);
	}
	
	public NullEvent(long ts, int priority, UUID instanceID)
	{
		super(ts, priority, instanceID);
	}
}
