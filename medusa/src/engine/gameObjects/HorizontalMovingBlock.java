package engine.gameObjects;

import engine.GameInstance;
import engine.ScriptManager;
import engine.gameObjects.objectClasses.PhysicsObject;

public class HorizontalMovingBlock extends Block implements PhysicsObject
{
	private static final long serialVersionUID = 693212084271323344L;
	
	float movementSpeed = 4f;
	
	float hSpeed = 0;
	
	int movementDirection = 1;
	
	public HorizontalMovingBlock(int sX, int sY)
	{
		super(sX, sY);
		
		height = 10;
	}
	
	private void exposeScriptingObjects(GameInstance instance)
	{
		ScriptManager.bindArgument("movementDirection", movementDirection);
		ScriptManager.bindArgument("movementSpeed", movementSpeed);
		ScriptManager.bindArgument("hSpeed", hSpeed);
		ScriptManager.bindArgument("x", x);
		ScriptManager.bindArgument("y", y);
		ScriptManager.bindArgument("height", height);
		ScriptManager.bindArgument("width", width);
		
		// objects that will not have their values updated after running the script
		ScriptManager.bindArgument("instance", instance);
		ScriptManager.bindArgument("this", this);
	}
	
	private void updateValues()
	{
		movementDirection = ((Number) ScriptManager.retrieveValue("movementDirection")).intValue();
		movementSpeed = ((Number) ScriptManager.retrieveValue("movementSpeed")).floatValue();
		hSpeed = ((Number) ScriptManager.retrieveValue("hSpeed")).floatValue();
		x = ((Number) ScriptManager.retrieveValue("x")).floatValue();
		y = ((Number) ScriptManager.retrieveValue("y")).floatValue();
		height = ((Number) ScriptManager.retrieveValue("height")).floatValue();
		width = ((Number) ScriptManager.retrieveValue("width")).floatValue();
	}
	
	@Override
	public synchronized void doPhysics(GameInstance instance)
	{
		ScriptManager.lock();
		exposeScriptingObjects(instance);
		ScriptManager.loadScript("scripts/platformer/movingBlock_behaviour.js");
		updateValues();
		ScriptManager.clearBindings();
		ScriptManager.unlock();
	}

	public int getMovementDirection()
	{
		return movementDirection;
	}
	
	public float getMovementSpeed()
	{
		return movementSpeed;
	}

	public void setMovementDirection(int movementDirection)
	{
		this.movementDirection = movementDirection;
	}
	
}
