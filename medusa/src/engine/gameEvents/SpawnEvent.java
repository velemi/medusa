package engine.gameEvents;

import java.util.UUID;

public class SpawnEvent extends GameEvent
{
	private static final long serialVersionUID = -2382267984785947092L;
	
	UUID objectID;
	
	public SpawnEvent(long ts, int p, UUID id)
	{
		super(ts, p);
		
		this.objectID = id;
	}
}
