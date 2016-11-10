package engine.gameEvents;

import java.util.UUID;
import engine.gameObjects.GameObject;

public class SpawnEvent extends GameEvent
{
	private static final long serialVersionUID = -2382267984785947092L;
	
	GameObject object;
	
	public SpawnEvent(long ts, int p, UUID instanceID, GameObject object)
	{
		super(ts, p, instanceID);
		
		this.object = object;
	}
	
	public SpawnEvent(GameEvent parent, long ts, int p, UUID instanceID, GameObject object)
	{
		super(parent, ts, p, instanceID);
		
		this.object = object;
	}
	
	public GameObject getObject()
	{
		return object;
	}
}
