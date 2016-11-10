package engine.gameEvents;

import java.util.UUID;

public class InputEvent extends GameEvent
{
	private static final long serialVersionUID = -336984021575435098L;
	
	private String input;
	
	public InputEvent(long ts, int p, UUID instanceID, String input)
	{
		super(ts, p, instanceID);
		
		this.input = input;
	}
	
	public InputEvent(GameEvent parent, long ts, int p, UUID instanceID, String input)
	{
		super(parent, ts, p, instanceID);
		
		this.input = input;
	}
	
	public String getInput()
	{
		return input;
	}
}
