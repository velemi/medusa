package engine.gameEvents;

import java.util.UUID;

public class NullEvent extends GameEvent
{
	private static final long serialVersionUID = 2263375812712767408L;

	public NullEvent(long ts, int p, UUID instanceID)
	{
		super(ts, p, instanceID);
	}
}
