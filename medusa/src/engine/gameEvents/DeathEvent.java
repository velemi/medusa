package engine.gameEvents;

import java.util.UUID;

public class DeathEvent extends GameEvent
{
	private static final long serialVersionUID = 689651224454257902L;
	
	private static final int DEFAULT_PRIORITY = 3;
	
	private UUID objectID;
	
	public DeathEvent(long ts, UUID instanceID, UUID objectID)
	{
		this(ts, DEFAULT_PRIORITY, instanceID, objectID);
	}
	
	public DeathEvent(long ts, int priority, UUID instanceID, UUID objectID)
	{
		this(null, ts, priority, instanceID, objectID);
	}
	
	public DeathEvent(GameEvent parent, long ts, UUID instanceID, UUID objectID)
	{
		this(parent, ts, DEFAULT_PRIORITY, instanceID, objectID);
	}
	
	public DeathEvent(GameEvent parent, long ts, int priority, UUID instanceID,
			UUID objectID)
	{
		super(parent, ts, priority, instanceID);
		
		this.objectID = objectID;
	}
	
	public UUID getObjectID()
	{
		return objectID;
	}
}
