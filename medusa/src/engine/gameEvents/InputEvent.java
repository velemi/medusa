package engine.gameEvents;

import java.util.UUID;

public class InputEvent extends GameEvent
{
	private static final long serialVersionUID = -336984021575435098L;
	
	private static final int DEFAULT_PRIORITY = 1;
	
	private String input;
	
	public InputEvent(long ts, UUID instanceID, String input)
	{
		this(ts, DEFAULT_PRIORITY, instanceID, input);
	}
	
	public InputEvent(long ts, int priority, UUID instanceID, String input)
	{
		this(null, ts, priority, instanceID, input);
	}
	
	public InputEvent(GameEvent parent, long ts, UUID instanceID, String input)
	{
		this(parent, ts, DEFAULT_PRIORITY, instanceID, input);
	}
	
	public InputEvent(GameEvent parent, long ts, int priority, UUID instanceID,
			String input)
	{
		super(parent, ts, priority, instanceID);
		
		this.input = input;
	}
	
	public String getInput()
	{
		return input;
	}
}
