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
	
	private static long turnTime = -1L;
	private static long rTurnTime = turnTime;
	
	private long thisLastTurned = turnTime;
	
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
		if(!instance.replayManager.isPlaying())
		{
			ScriptManager.bindArgument("turnTime", turnTime);
		}
		else
		{
			ScriptManager.bindArgument("turnTime", rTurnTime);
		}
		ScriptManager.bindArgument("thisLastTurned", thisLastTurned);
		ScriptManager.bindArgument("movementDelay", movementDelay);
		ScriptManager.bindArgument("fleetSpeed", fleetSpeed);
		ScriptManager.bindArgument("movementDirection", movementDirection);
		
		ScriptManager.bindArgument("instance", instance);
	}
	
	private void updateValues(GameInstance instance)
	{
		x = ((Number) ScriptManager.retrieveValue("x")).floatValue();
		y = ((Number) ScriptManager.retrieveValue("y")).floatValue();
		height = ((Number) ScriptManager.retrieveValue("height")).floatValue();
		width = ((Number) ScriptManager.retrieveValue("width")).floatValue();
		if(!instance.replayManager.isPlaying())
			turnTime = ((Number) ScriptManager.retrieveValue("turnTime")).longValue();
		else
			rTurnTime = ((Number) ScriptManager.retrieveValue("turnTime")).longValue();
		thisLastTurned = ((Number) ScriptManager.retrieveValue("thisLastTurned")).longValue();
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
		
		updateValues(instance);
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
