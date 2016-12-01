package engine.gameObjects;

import java.util.UUID;
import engine.GameInstance;
import engine.ScriptManager;
import engine.gameObjects.objectClasses.Killable;
import engine.gameObjects.objectClasses.PhysicsObject;
import engine.gameObjects.objectClasses.RenderableObject;

public class Bullet extends GameObject implements PhysicsObject, Killable, RenderableObject
{
	private static final long serialVersionUID = -1641518259302327880L;

	static int count = 0;
	
	UUID parentInstanceID;
	
	static final float DEFAULT_WIDTH = 10;
	static final float DEFAULT_HEIGHT = 10;
	
	int speed = 10;
	
	public Bullet(float sX, float sY, UUID parentInstanceID)
	{
		super(sX, sY);
		width = DEFAULT_WIDTH;
		height = DEFAULT_HEIGHT;
		
		this.parentInstanceID = parentInstanceID;
		
		count++;
	}
	
	public UUID getParentInstanceID()
	{
		return this.parentInstanceID;
	}
	
	public static int getCount()
	{
		return count;
	}
	
	private void exposeScriptingObjects(GameInstance instance)
	{
		ScriptManager.bindArgument("x", x);
		ScriptManager.bindArgument("y", y);
		ScriptManager.bindArgument("height", height);
		ScriptManager.bindArgument("width", width);
		ScriptManager.bindArgument("speed", speed);
		ScriptManager.bindArgument("count", count);
		
		ScriptManager.bindArgument("instance", instance);
		ScriptManager.bindArgument("bullet", this);
	}
	
	private void updateValues()
	{
		x = ((Number) ScriptManager.retrieveValue("x")).floatValue();
		y = ((Number) ScriptManager.retrieveValue("y")).floatValue();
		height = ((Number) ScriptManager.retrieveValue("height")).floatValue();
		width = ((Number) ScriptManager.retrieveValue("width")).floatValue();
		speed = ((Number) ScriptManager.retrieveValue("speed")).intValue();
		count = ((Number) ScriptManager.retrieveValue("count")).intValue();
	}
	
	@Override
	public void doPhysics(GameInstance instance)
	{
		ScriptManager.lock();
		exposeScriptingObjects(instance);
		
		ScriptManager.loadScript("invaders/bullet_behaviour.js");
		
		updateValues();
		ScriptManager.clearBindings();
		ScriptManager.unlock();
	}

	@Override
	public void kill()
	{
		count--;
	}

	@Override
	public void display(GameInstance parent)
	{
		parent.fill(200,200,0);
		parent.stroke(200,200,0);
		parent.rect(x, y, width, height);
	}
	
}
