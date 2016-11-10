package engine.gameEvents;

import java.util.UUID;

public class CollisionEvent extends GameEvent
{
	private static final long serialVersionUID = -4431184999816546485L;
	
	private static final int DEFAULT_PRIORITY = 2;
	
	private UUID[] objectIDs = new UUID[2];
	
	public CollisionEvent(long ts, UUID instanceID, UUID objectID1,
			UUID objectID2)
	{
		this(ts, DEFAULT_PRIORITY, instanceID, objectID1, objectID2);
	}
	
	public CollisionEvent(long ts, int priority, UUID instanceID,
			UUID objectID1, UUID objectID2)
	{
		this(null, ts, priority, instanceID, objectID1, objectID2);
	}
	
	public CollisionEvent(GameEvent parent, long ts, UUID instanceID,
			UUID objectID1, UUID objectID2)
	{
		this(parent, ts, DEFAULT_PRIORITY, instanceID, objectID1, objectID2);
	}
	
	public CollisionEvent(GameEvent parent, long ts, int priority,
			UUID instanceID, UUID objectID1, UUID objectID2)
	{
		super(parent, ts, priority, instanceID);
		
		this.objectIDs[0] = objectID1;
		this.objectIDs[1] = objectID2;
	}
	
	public UUID[] getIDs()
	{
		return objectIDs;
	}
}
