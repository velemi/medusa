package engine.gameEvents;

import java.util.UUID;
import engine.gameObjects.GameObject;

public class DespawnEvent extends GameEvent
{
private static final long serialVersionUID = -2382267984785947092L;
	
	private static final int DEFAULT_PRIORITY = 4;
	
	GameObject object;
	
	public DespawnEvent(long ts, UUID instanceID, GameObject object)
	{
		this(ts, DEFAULT_PRIORITY, instanceID, object);
	}
	
	public DespawnEvent(long ts, int priority, UUID instanceID, GameObject object)
	{
		this(null, ts, priority, instanceID, object);
	}
	
	public DespawnEvent(GameEvent parent, long ts, UUID instanceID,
			GameObject object)
	{
		this(parent, ts, DEFAULT_PRIORITY, instanceID, object);
	}
	
	public DespawnEvent(GameEvent parent, long ts, int priority, UUID instanceID,
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
