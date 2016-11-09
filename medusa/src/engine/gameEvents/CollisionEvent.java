package engine.gameEvents;

import java.util.UUID;

public class CollisionEvent extends GameEvent
{
	private static final long serialVersionUID = -4431184999816546485L;
	
	private UUID[] objectIDs = new UUID[2];
	
	public CollisionEvent(long ts, int p, UUID id1, UUID id2)
	{
		super(ts, p);
		
		this.objectIDs[0] = id1;
		this.objectIDs[1] = id2;
	}
	
	public UUID[] getIDs()
	{
		return objectIDs;
	}
}
