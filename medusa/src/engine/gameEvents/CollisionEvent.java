package engine.gameEvents;

import java.util.UUID;

public class CollisionEvent extends GameEvent
{
	private static final long serialVersionUID = -4431184999816546485L;
	
	UUID id1, id2;
	
	public CollisionEvent(long ts, int p, UUID id1, UUID id2)
	{
		super(ts, p);
		
		this.id1 = id1;
		this.id2 = id2;
	}
}
