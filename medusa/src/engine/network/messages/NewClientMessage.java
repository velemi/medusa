package engine.network.messages;

import engine.gameObjects.PlayerObject;

public class NewClientMessage extends NetworkMessage
{
	private static final long serialVersionUID = -277803654717665590L;
	
	private PlayerObject player;
	
	public NewClientMessage(PlayerObject player)
	{
		this.player = player;
	}
	
	public PlayerObject getPlayer()
	{
		return this.player;
	}
}
