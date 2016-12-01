package engine.gameObjects;

import engine.GameInstance;
import engine.ScriptManager;
import engine.gameObjects.objectClasses.Killable;
import engine.gameObjects.objectClasses.PhysicsObject;
import engine.gameObjects.objectClasses.RenderableObject;

public class Invader extends GameObject implements PhysicsObject, RenderableObject, Killable
{
	private static final long serialVersionUID = 1L;
	
	static int DEFAULT_HEIGHT = 20;
	
	static int DEFAULT_WIDTH = 40;
	
	private static long turnTime = 0L;
	
	private static float movementDelay = 30;
	
	private int movementDirection = 1;
	
	private static float fleetSpeed = 5f;
	
	public Invader(int sX, int sY)
	{
		super(sX, sY);
		height = DEFAULT_HEIGHT;
		width = DEFAULT_WIDTH;
	}

	@Override
	public void display(GameInstance parent)
	{
		ScriptManager.lock();
		
		ScriptManager.bindArgument("parent", parent);
		ScriptManager.bindArgument("x", x);
		ScriptManager.bindArgument("y", y);
		ScriptManager.bindArgument("width", width);
		ScriptManager.bindArgument("height", height);
		
		ScriptManager.loadScript("scripts/invaders/invader_display.js");
		
		ScriptManager.clearBindings();
		
		ScriptManager.unlock();
	}
	
	private void exposeScriptingObjects(GameInstance instance)
	{
		ScriptManager.bindArgument("x", x);
		ScriptManager.bindArgument("y", y);
		ScriptManager.bindArgument("width", width);
		ScriptManager.bindArgument("height", height);
		ScriptManager.bindArgument("turnTime", turnTime);
		ScriptManager.bindArgument("movementDelay", movementDelay);
		ScriptManager.bindArgument("fleetSpeed", fleetSpeed);
		ScriptManager.bindArgument("movementDirection", movementDirection);
		
		ScriptManager.bindArgument("instance", instance);
	}
	
	private void updateValues()
	{
		x = ((Number) ScriptManager.retrieveValue("x")).floatValue();
		y = ((Number) ScriptManager.retrieveValue("y")).floatValue();
		height = ((Number) ScriptManager.retrieveValue("height")).floatValue();
		width = ((Number) ScriptManager.retrieveValue("width")).floatValue();
		turnTime = ((Number) ScriptManager.retrieveValue("turnTime")).longValue();
		fleetSpeed = ((Number) ScriptManager.retrieveValue("fleetSpeed")).floatValue();
		movementDirection = ((Number) ScriptManager.retrieveValue("movementDirection")).intValue();
		movementDelay = ((Number) ScriptManager.retrieveValue("movementDelay")).floatValue();
	}
	
	@Override
	public void doPhysics(GameInstance instance)
	{
		ScriptManager.lock();
		exposeScriptingObjects(instance);
		
		ScriptManager.loadScript("scripts/invaders/invader_behaviour.js");
		
		updateValues();
		ScriptManager.clearBindings();
		ScriptManager.unlock();
	}

	@Override
	public void kill()
	{
		if (movementDelay > 3f)
		{
			movementDelay -= 1.5f;
		}
		
		if (movementDelay < 3f)
		{
			movementDelay = 3f;
		}
	}
}
