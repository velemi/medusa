package engine.gameEvents;

import java.util.UUID;

public class DeathEvent extends GameEvent
{
	private static final long serialVersionUID = 689651224454257902L;
	
	private UUID objectID;
	
	public DeathEvent(long ts, int p, UUID instanceID, UUID objectID)
	{
		super(ts, p, instanceID);
		
		this.objectID = objectID;
	}
	
	public DeathEvent(GameEvent parent, long ts, int p, UUID instanceID, UUID objectID)
	{
		super(parent, ts, p, instanceID);
		
		this.objectID = objectID;
	}
	
	public UUID getObjectID()
	{
		return objectID;
	}
}
