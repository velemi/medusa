package engine.gameObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GameObjectSet
{
	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
	
	private HashMap<UUID, GameObject> objectMap = new HashMap<UUID, GameObject>();
	private HashMap<UUID, PlayerObject> playerObjects = new HashMap<UUID, PlayerObject>();
	
//	private ArrayList<GameObject> renderables = new ArrayList<GameObject>();
//	private ArrayList<GameObject> spawnPoints = new ArrayList<GameObject>();
	
	public GameObjectSet()
	{
		
	}
	
	public GameObjectSet(GameObjectSet source)
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
//			if (o instanceof RenderableObject && !renderables.)
//				renderables.add(o);
//			if (o instanceof SpawnPoint)
//				spawnPoints.add(o);
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
//			if (o instanceof RenderableObject)
//				renderables.remove((RenderableObject) o);
//			if (o instanceof SpawnPoint)
//				spawnPoints.remove(o);
		}
		
		lock.writeLock().unlock();
	}
	
	public HashMap<UUID, GameObject> getFullMap()
	{
		lock.readLock().lock();
		
		@SuppressWarnings("unchecked")
		HashMap<UUID, GameObject> o = (HashMap<UUID, GameObject>) objectMap.clone();
		
		lock.readLock().unlock();
		
		return o;
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
	
	public ArrayList<GameObject> getObjectsOfClass(Class<?> t)
	{
		lock.readLock().lock();
		
		ArrayList<GameObject> objects = new ArrayList<GameObject>();
		
		for (GameObject o : objectMap.values())
		{
			if (o.isType(t))
				objects.add(o);
		}

		lock.readLock().unlock();
		
		return objects;
	}
	
	public boolean contains(GameObject o)
	{
		boolean r = false;
		
		lock.readLock().lock();
		
		r = objectMap.containsValue(o);
		
		lock.readLock().unlock();
		
		return r;
	}
	
	public ArrayList<GameObject> getColliding(GameObject o, boolean physOnly)
	{	
		return getColliding(o.x, o.y, o.width, o.height, physOnly);
	}
	
	public ArrayList<GameObject> getColliding(double x, double y, double w, double h, boolean physOnly)
	{
		lock.readLock().lock();
		
		ArrayList<GameObject> c = new ArrayList<GameObject>();
		
		for (GameObject e : objectMap.values())
		{
			if (e.intersects(x, y, w, h) && (!physOnly || e.hasPhysicalCollision()))
			{
				c.add(e);
			}
		}

		lock.readLock().unlock();
		
		return c;
	}
	
	public boolean checkPhysCollision(double x, double y, double w, double h)
	{
		lock.readLock().lock();
		
		boolean result = false;
		
		for (GameObject e : objectMap.values())
		{
			if (e.intersects(x, y, w, h) && e.hasPhysicalCollision())
				result = true;
		}
		
		lock.readLock().unlock();
		
		return result;
	}
}
