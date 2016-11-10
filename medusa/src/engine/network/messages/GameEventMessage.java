package engine.network.messages;

import engine.gameEvents.GameEvent;

public class GameEventMessage extends NetworkMessage
{
	private static final long serialVersionUID = 8709574354224710269L;
	
	private GameEvent event;
	
	public GameEventMessage(GameEvent event)
	{
		this.event = event;
	}
	
	public GameEvent getEvent()
	{
		return this.event;
	}
}
