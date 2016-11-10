package engine.network.messages;

import java.io.Serializable;

public abstract class NetworkMessage implements Serializable
{
	private static final long serialVersionUID = -18937852386929141L;
	
	public String getMessageType()
	{
		return this.getClass().getSimpleName();
	}
}
