package engine.gameEvents;

import engine.gameObjects.GameObject;

public class SpawnEvent extends GameEvent
{
	private static final long serialVersionUID = -2382267984785947092L;
	
	GameObject object;
	
	public SpawnEvent(long ts, int p, GameObject object)
	{
		super(ts, p);
		
		this.object = object;
	}
	
	public GameObject getObject()
	{
		return object;
	}
}
