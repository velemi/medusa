package engine.gameEvents;

import java.util.UUID;

public class CollisionEvent extends GameEvent
{
	private static final long serialVersionUID = -4431184999816546485L;
	
	private UUID[] objectIDs = new UUID[2];
	
	public CollisionEvent(long ts, int p, UUID instanceID, UUID objectID1, UUID objectID2)
	{
		super(ts, p, instanceID);
		
		this.objectIDs[0] = objectID1;
		this.objectIDs[1] = objectID2;
	}
	
	public CollisionEvent(GameEvent parent, long ts, int p, UUID instanceID, UUID objectID1, UUID objectID2)
	{
		super(parent, ts, p, instanceID);
		
		this.objectIDs[0] = objectID1;
		this.objectIDs[1] = objectID2;
	}
	
	public UUID[] getIDs()
	{
		return objectIDs;
	}
}
