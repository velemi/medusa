package engine.gameObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import engine.gameObjects.objectClasses.RenderableObject;

public class GameObjectSet
{
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	private HashMap<UUID, GameObject> objectMap = new HashMap<UUID, GameObject>();
	private HashMap<UUID, PlayerObject> playerObjects = new HashMap<UUID, PlayerObject>();
	
	private ArrayList<GameObject> renderables = new ArrayList<GameObject>();
	
	public GameObjectSet()
	{
		
	}
	
	public void addToSet(GameObject o)
	{
		lock.writeLock().lock();
		
		if (o != null)
		{
			objectMap.put(o.objectID, o);
			
			if (o instanceof PlayerObject)
				playerObjects.put(((PlayerObject) o).parentInstanceID, (PlayerObject) o);
			if (o instanceof RenderableObject)
				renderables.add(o);
		}
		
		lock.writeLock().unlock();
	}
	
	public void removeFromSet(GameObject o)
	{
		lock.writeLock().lock();
		
		if (o != null)
		{
			objectMap.remove(o.objectID);
			
			if (o instanceof PlayerObject)
				playerObjects.remove(((PlayerObject) o).parentInstanceID);
			if (o instanceof RenderableObject)
				renderables.remove((RenderableObject) o);
		}
		
		lock.writeLock().unlock();
	}
	
	public GameObject getObject(UUID objectID)
	{
		lock.readLock().lock();
		
		GameObject r = objectMap.get(objectID);
		
		lock.readLock().unlock();
		
		return r;
	}
	
	public PlayerObject getPlayerObject(UUID instanceID)
	{
		lock.readLock().lock();
		
		PlayerObject r = playerObjects.get(instanceID);
		
		lock.readLock().unlock();
		
		return r;
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<GameObject> getObjectsOfClass(Class<?> t)
	{
		lock.readLock().lock();
		
		ArrayList<GameObject> objects;
		
		if (t.equals(RenderableObject.class))
			objects = (ArrayList<GameObject>) this.renderables.clone();
		else 
		{
			objects = new ArrayList<GameObject>();
			
			for (GameObject o : objectMap.values())
			{
				if (o.isType(t))
					objects.add(o);
			}
		}
			
		lock.readLock().unlock();
		
		return objects;
	}
}
