package engine.gameObjects;

import java.util.UUID;
import engine.GameInstance;
import engine.ScriptManager;
import engine.gameObjects.objectClasses.Killable;
import engine.gameObjects.objectClasses.PhysicsObject;
import engine.gameObjects.objectClasses.RenderableObject;
import engine.gameObjects.objectClasses.Spawnable;

/**
 * A player-controllable game object
 * 
 * @author Jordan Neal
 */
public class PlayerObject
		extends GameObject 
		implements RenderableObject, Cloneable, PhysicsObject, Spawnable, Killable
{
	private static final long serialVersionUID = 6154481363016678189L;
	
	private int[] color = { (int) (Math.random() * 255),
			(int) (Math.random() * 255), (int) (Math.random() * 255) };
			
	UUID parentInstanceID;
	
	SpawnPoint spawn = null;
	private boolean alive = true;
	
	public static final long DEFAULT_RESPAWN = 90;
	
	static final float DEFAULT_X = 120;
	static final float DEFAULT_Y = 100;
	
	// == SIZE == //
	
	/** Default height, in pixels, of a PlayerObject */
	static int DEFAULT_HEIGHT = 40;
	
	/** Default width, in pixels, of a PlayerObject */
	static int DEFAULT_WIDTH = 30;
	
	// == BASIC MOVEMENT == //
	
	/** True if the left movement key is being pressed */
	private Boolean leftPressed = false;
	
	/** True if the right movement key is being pressed */
	private Boolean rightPressed = false;
	
	/** The default horizontal movement speed of a player object */
	private static final float DEFAULT_MOVE_SPEED = 5;
	
	/** The default maximum fall speed of a PlayerObject */
	private static final float DEFAULT_MAX_FALL_SPEED = 15;
	
	/** The default acceleration due to gravity of a PlayerObject */
	private static final float DEFAULT_GRAVITY = 0.2f;
	
	/** This PlayerObject's horizontal movement speed */
	private float movementSpeed = DEFAULT_MOVE_SPEED;
	
	/**
	 * An integer value which stores the PlayerObject's horizontal movement
	 * direction. Recalculated every time doMovement() is called, based on the
	 * state of key inputs. -1 = LEFT, 0 = NO MOVEMENT, 1 = RIGHT
	 */
	private int movementDirection = 0;
	
	/** This PlayerObject's current horizontal speed */
	private float hSpeed = 0;
	
	/** This PlayerObject's current vertical speed */
	private float vSpeed = 0;
	
	/** The maximum speed at which this PlayerObject can fall */
	private float maxFallSpeed = DEFAULT_MAX_FALL_SPEED;
	
	/** This PlayerObject's acceleration due to gravity */
	private float gravity = DEFAULT_GRAVITY;
	
	// == JUMPING == //
	
	/** The default jump speed of a PlayerObject */
	private static final float DEFAULT_JUMP_SPEED = 7;
	
	/** This PlayerObject's jump speed */
	private float jumpSpeed = DEFAULT_JUMP_SPEED;
	
	/** Should get set to TRUE if the jump key is being pressed */
	private Boolean jumpPressed = false;
	
	/** True if this PlayerObject can currently jump */
	private boolean canJump = false;
	
	public synchronized PlayerObject clone()
	{
		PlayerObject cloneObject = (PlayerObject) super.clone();
		
		cloneObject.alive = this.alive;
		cloneObject.leftPressed = this.leftPressed;
		cloneObject.rightPressed = this.rightPressed;
		cloneObject.movementSpeed = this.movementSpeed;
		cloneObject.movementDirection = this.movementDirection;
		cloneObject.hSpeed = this.hSpeed;
		cloneObject.vSpeed = this.vSpeed;
		cloneObject.maxFallSpeed = this.maxFallSpeed;
		cloneObject.gravity = this.gravity;
		cloneObject.jumpSpeed = this.jumpSpeed;
		cloneObject.jumpPressed = this.jumpPressed;
		cloneObject.canJump = this.canJump;
		
		return cloneObject;
	}
	
	public PlayerObject()
	{
		this(DEFAULT_X, DEFAULT_Y);
	}
	
	/**
	 * Constructor which initializes the PlayerObject
	 * 
	 * @param sX
	 *            The starting X-coordinate of this PlayerObject's upper left
	 *            corner.
	 * @param sY
	 *            The starting Y-coordinate of this PlayerObject's upper left
	 *            corner.
	 */
	public PlayerObject(float sX, float sY)
	{
		super(sX, sY);
		this.height = PlayerObject.DEFAULT_HEIGHT;
		this.width = PlayerObject.DEFAULT_WIDTH;
		
		this.setPhysicalCollision(false);
	}
	
	public PlayerObject(SpawnPoint s)
	{
		this(s.x, s.y);
		
		spawn = s;
	}
	
	public static void setDefaultSize(int width, int height)
	{
		PlayerObject.DEFAULT_HEIGHT = height;
		PlayerObject.DEFAULT_WIDTH = width;
	}
	
	public UUID getParentInstanceID()
	{
		return this.parentInstanceID;
	}
	
	public void setParentInstanceID(UUID id)
	{
		this.parentInstanceID = id;
	}
	
	/*
	 * Defines behavior to be run when rendering this PlayerObject.
	 * (non-Javadoc)
	 * @see engine.GameObject#display()
	 */
	public void display(GameInstance parent)
	{
		ScriptManager.lock();
		
		ScriptManager.bindArgument("parent", parent);
		ScriptManager.bindArgument("r", color[0]);
		ScriptManager.bindArgument("g", color[1]);
		ScriptManager.bindArgument("b", color[2]);
		ScriptManager.bindArgument("x", x);
		ScriptManager.bindArgument("y", y);
		ScriptManager.bindArgument("width", width);
		ScriptManager.bindArgument("height", height);
		
		if (parent.getGameTitle().equals("platform"))
			ScriptManager.loadScript("platformer/playerObject_display.js");
		else if (parent.getGameTitle().equals("invaders"))
			ScriptManager.loadScript("invaders/playerObject_display.js");
		
		ScriptManager.clearBindings();
		
		ScriptManager.unlock();
	}
	
	public void setLeftPressed(boolean leftPressed)
	{
		synchronized (this.leftPressed)
		{
			this.leftPressed = leftPressed;
		}
	}
	
	public void setRightPressed(boolean rightPressed)
	{
		synchronized (this.rightPressed)
		{
			this.rightPressed = rightPressed;
		}
	}
	
	public void setJumpPressed(boolean jumpPressed)
	{
		synchronized (this.jumpPressed)
		{
			this.jumpPressed = jumpPressed;
		}
	}
	
	private void exposeScriptingObjects(GameInstance instance)
	{	
		ScriptManager.bindArgument("movementDirection", movementDirection);
		ScriptManager.bindArgument("leftPressed", leftPressed);
		ScriptManager.bindArgument("rightPressed", rightPressed);
		ScriptManager.bindArgument("jumpPressed", jumpPressed);
		ScriptManager.bindArgument("movementSpeed", movementSpeed);
		ScriptManager.bindArgument("hSpeed", hSpeed);
		ScriptManager.bindArgument("vSpeed", vSpeed);
		ScriptManager.bindArgument("maxFallSpeed", maxFallSpeed);
		ScriptManager.bindArgument("gravity", gravity);
		ScriptManager.bindArgument("canJump", canJump);
		ScriptManager.bindArgument("jumpSpeed", jumpSpeed);
		ScriptManager.bindArgument("x", x);
		ScriptManager.bindArgument("y", y);
		ScriptManager.bindArgument("height", height);
		ScriptManager.bindArgument("width", width);
		
		// objects that will not have their values updated after running the script
		ScriptManager.bindArgument("instance", instance);
		ScriptManager.bindArgument("player", this);
	}
	
	private void updateValues()
	{
		movementDirection = ((Number) ScriptManager.retrieveValue("movementDirection")).intValue();
		leftPressed = (boolean) ScriptManager.retrieveValue("leftPressed");
		rightPressed = (boolean) ScriptManager.retrieveValue("rightPressed");
		jumpPressed = (boolean) ScriptManager.retrieveValue("jumpPressed");
		movementSpeed = ((Number) ScriptManager.retrieveValue("movementSpeed")).floatValue();
		hSpeed = ((Number) ScriptManager.retrieveValue("hSpeed")).floatValue();
		vSpeed = ((Number) ScriptManager.retrieveValue("vSpeed")).floatValue();
		maxFallSpeed = ((Number) ScriptManager.retrieveValue("maxFallSpeed")).floatValue();
		gravity = ((Number) ScriptManager.retrieveValue("gravity")).floatValue();
		jumpSpeed = ((Number) ScriptManager.retrieveValue("jumpSpeed")).floatValue();
		canJump = (boolean) ScriptManager.retrieveValue("canJump");
		x = ((Number) ScriptManager.retrieveValue("x")).floatValue();
		y = ((Number) ScriptManager.retrieveValue("y")).floatValue();
		height = ((Number) ScriptManager.retrieveValue("height")).floatValue();
		width = ((Number) ScriptManager.retrieveValue("width")).floatValue();
	}
	
	/** Updates this PlayerObject's position based on its current state. */
	public synchronized void doPhysics(GameInstance instance)
	{
		ScriptManager.lock();
		exposeScriptingObjects(instance);
		
		if (instance.getGameTitle().equals("platform"))
			ScriptManager.loadScript("platformer/playerObject_behaviour.js");
		else if (instance.getGameTitle().equals("invaders"))
			ScriptManager.loadScript("invaders/playerObject_behaviour.js");
		
		updateValues();
		ScriptManager.clearBindings();
		ScriptManager.unlock();
	}
	
	public static PlayerObject createNew()
	{
		return new PlayerObject(DEFAULT_X, DEFAULT_Y);
	}
	
	public synchronized void kill()
	{
		alive = false;
	}
	
	public void revive()
	{
		alive = true;
	}
	
	public boolean isAlive()
	{
		return this.alive;
	}
	
	@Override
	public void spawn()
	{
		alive = true;
		
		if (spawn != null)
		{
			x = spawn.x;
			y = spawn.y;
		}
		else
		{
			x = 0;
			y = 0;
		}
		
		leftPressed = false;
		rightPressed = false;
		jumpPressed = false;
		
		hSpeed = 0;
		vSpeed = 0;
		
		movementDirection = 0;
		
		canJump = false;
	}

	public int getMovementDirection()
	{
		return movementDirection;
	}

	public void setMovementDirection(int movementDirection)
	{
		this.movementDirection = movementDirection;
	}

	public float getHSpeed()
	{
		return hSpeed;
	}

	public void setHSpeed(float hSpeed)
	{
		this.hSpeed = hSpeed;
	}

	public float getVSpeed()
	{
		return vSpeed;
	}

	public void setVSpeed(float vSpeed)
	{
		this.vSpeed = vSpeed;
	}

	public boolean isLeftPressed()
	{
		return leftPressed;
	}

	public boolean isRightPressed()
	{
		return rightPressed;
	}

	public boolean isJumpPressed()
	{
		return jumpPressed;
	}
}
