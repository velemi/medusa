package engine.network;

import java.util.UUID;

public class ClientDisconnectMessage extends NetworkMessage
{
	private static final long serialVersionUID = 5514542393673271067L;
	
	private UUID clientID;
	
	public ClientDisconnectMessage(UUID cID)
	{
		this.clientID = cID;
	}
	
	public UUID getClientID()
	{
		return this.clientID;
	}
}
