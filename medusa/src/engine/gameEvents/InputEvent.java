package engine.gameEvents;

import java.util.UUID;
import engine.gameObjects.PlayerObject;

public class InputEvent extends GameEvent
{
	private static final long serialVersionUID = -336984021575435098L;
	
	private static final int DEFAULT_PRIORITY = 10;
	
	private PlayerObject player;
	
	private String input;
	
	public InputEvent(long ts, UUID instanceID, String input, PlayerObject player)
	{
		this(ts, 0, instanceID, input, player);
	}
	
	public InputEvent(long ts, int minusPriority, UUID instanceID, String input, PlayerObject player)
	{
		this(null, ts, minusPriority, instanceID, input, player);
	}
	
	public InputEvent(GameEvent parent, long ts, UUID instanceID, String input, PlayerObject player)
	{
		this(parent, ts, DEFAULT_PRIORITY, instanceID, input, player);
	}
	
	public InputEvent(GameEvent parent, long ts, int priority, UUID instanceID,
			String input, PlayerObject player)
	{
		super(parent, ts, DEFAULT_PRIORITY + priority, instanceID);
		
		this.input = input;
		this.player = player;
	}
	
	public String getInput()
	{
		return input;
	}
	
	public PlayerObject getPlayer()
	{
		return player;
	}
}
