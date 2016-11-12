package engine.gameEvents;

import java.util.UUID;

public class EndReplayEvent extends GameEvent
{
	private static final long serialVersionUID = -637874176586633249L;

	public EndReplayEvent(long ts, int p, UUID instanceID)
	{
		super(ts, p, instanceID);
	}

}
