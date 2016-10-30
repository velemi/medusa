package engine.core;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import engine.objectModel.GameObject;
import engine.objectModel.PlayerObject;
import engine.objectModel.SpawnPoint;
import processing.core.PApplet;

/**
 * An abstract class defining structure and behavior for classes which act as or
 * reflect an instance of a game
 * 
 * @author Jordan Neal
 */
public abstract class GameInstance extends PApplet
{
	public static final boolean DEBUG = true;
	
	public static final int SCREEN_WIDTH = 800, SCREEN_HEIGHT = 800;
	
	ConcurrentHashMap<UUID, GameObject> gameObjectMap = new ConcurrentHashMap<UUID, GameObject>();
	
	ConcurrentLinkedQueue<SpawnPoint> spawnPoints = new ConcurrentLinkedQueue<SpawnPoint>();
	
	public void addToMap(GameObject object)
	{
		if(object != null)
			gameObjectMap.put(object.getID(), object);
		
		if(object instanceof SpawnPoint)
			spawnPoints.add((SpawnPoint) object);
	}
	
	public PlayerObject createNewPlayer()
	{
		SpawnPoint spawn = spawnPoints.poll();
		PlayerObject newPlayer = null;
		
		if (spawn != null) 
		{
			newPlayer = new PlayerObject(spawn);
			spawnPoints.add(spawn);
		}
		else
		{
			newPlayer = new PlayerObject();
		}
		
		return newPlayer;
	}
	
	public ConcurrentHashMap<UUID, GameObject> getTouching(GameObject theObject)
	{
		ConcurrentHashMap<UUID, GameObject> result = new ConcurrentHashMap<UUID, GameObject>();
		
		for(Map.Entry<UUID, GameObject> entry : gameObjectMap.entrySet()) {
			if (entry.getValue().intersects(theObject)) {
				result.put(entry.getKey(), entry.getValue().clone());
			}
		}
		
		return result;
	}
	
	public boolean checkForPhysicalCollision(double x, double y)
	{
		boolean result = false;
		
		for (Map.Entry<UUID, GameObject> entry : gameObjectMap.entrySet()) {
			if (entry.getValue().contains(x, y) && entry.getValue().hasPhysicalCollision()) {
				result = true;
			}
		}
		
		return result;
	}
	
	public boolean checkForPhysicalCollision(double x, double y, double w, double h)
	{
		boolean result = false;
		
		for (Map.Entry<UUID, GameObject> entry : gameObjectMap.entrySet()) {
			if (entry.getValue().intersects(x, y, w, h) && entry.getValue().hasPhysicalCollision()) {
				result = true;
			}
		}
		
		return result;
	}
}
