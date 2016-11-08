package engine.gameEvents;

public class InputEvent extends GameEvent
{
	private static final long serialVersionUID = -336984021575435098L;
	
	String input;
	
	public InputEvent(long ts, int p, String input)
	{
		super(ts, p);
		
		this.input = input;
	}
}
