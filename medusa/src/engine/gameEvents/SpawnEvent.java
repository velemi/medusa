package engine.gameEvents;

import java.util.UUID;
import engine.gameObjects.GameObject;

public class SpawnEvent extends GameEvent
{
	private static final long serialVersionUID = -2382267984785947092L;
	
	private static final int DEFAULT_PRIORITY = 4;
	
	GameObject object;
	
	public SpawnEvent(long ts, UUID instanceID, GameObject object)
	{
		this(ts, DEFAULT_PRIORITY, instanceID, object);
	}
	
	public SpawnEvent(long ts, int priority, UUID instanceID, GameObject object)
	{
		this(null, ts, priority, instanceID, object);
	}
	
	public SpawnEvent(GameEvent parent, long ts, UUID instanceID,
			GameObject object)
	{
		this(parent, ts, DEFAULT_PRIORITY, instanceID, object);
	}
	
	public SpawnEvent(GameEvent parent, long ts, int priority, UUID instanceID,
			GameObject object)
	{
		super(parent, ts, priority, instanceID);
		
		this.object = object;
	}
	
	public GameObject getObject()
	{
		return object;
	}
}
