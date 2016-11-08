package engine.gameEvents;

import java.util.UUID;

public class DeathEvent extends GameEvent
{
	private static final long serialVersionUID = 689651224454257902L;
	
	UUID objectID;
	
	public DeathEvent(long ts, int p, UUID id)
	{
		super(ts, p);
		
		this.objectID = id;
	}
}
