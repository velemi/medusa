package engine.gameObjects;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import engine.core.GameInstance;
import processing.core.PApplet;

/**
 * A player-controllable game object
 * 
 * @author Jordan Neal
 */
public class PlayerObject extends GameObject implements RenderableObject, Cloneable, MovingObject
{
	private static final long serialVersionUID = 6154481363016678189L;
	
	private int[] color = { (int) (Math.random() * 255),
			(int) (Math.random() * 255), (int) (Math.random() * 255) };
	
	SpawnPoint spawn = null;
	
	static final float DEFAULT_X = 120;
	static final float DEFAULT_Y = 100;
			
	// == SIZE == //
	
	/** Default height, in pixels, of a PlayerObject */
	static final int DEFAULT_HEIGHT = 40;
	
	/** Default width, in pixels, of a PlayerObject */
	static final int DEFAULT_WIDTH = 30;
	
	// == BASIC MOVEMENT == //
	
	/** True if the left movement key is being pressed */
	private boolean leftPressed = false;
	
	/** True if the right movement key is being pressed */
	private boolean rightPressed = false;
	
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
	private boolean jumpPressed = false;
	
	/** True if this PlayerObject can currently jump */
	private boolean canJump = false;
	
	public synchronized PlayerObject clone()
	{
		PlayerObject cloneObject = (PlayerObject) super.clone();
		
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
	
	public synchronized void becomeCopyOf(PlayerObject other)
	{
		becomeCopyOf((GameObject) other);
		
		this.color[0] = other.color[0];
		this.color[1] = other.color[1];
		this.color[2] = other.color[2];
	}
	
	/*
	 * Defines behavior to be run when rendering this PlayerObject.
	 * (non-Javadoc)
	 * @see engine.GameObject#display()
	 */
	public synchronized void display(PApplet parent)
	{
		parent.fill(color[0], color[1], color[2]);
		parent.stroke(0);
		parent.rect(this.x, this.y, this.width, this.height);
		
	}
	
	public void setLeftPressed(boolean leftPressed)
	{
		this.leftPressed = leftPressed;
	}
	
	public void setRightPressed(boolean rightPressed)
	{
		this.rightPressed = rightPressed;
	}
	
	public void setJumpPressed(boolean jumpPressed)
	{
		this.jumpPressed = jumpPressed;
	}
	
	/**
	 * Checks to see if this PlayerObject is about to collide with any other
	 * game objects, and acts accordingly.
	 */
	private void handlePhysicalCollisions(GameInstance parent)
	{
		ArrayList<GameObject> standingOn = parent.getPhysicalCollisions(x, this.bottomBorder() + 1, width, 1);
		
		if(!standingOn.isEmpty()){
			for (int i = 0; i < standingOn.size(); i++) {
				GameObject floor = standingOn.get(i);
				if (floor instanceof HorizontalMovingBlock) {
					this.hSpeed += ((HorizontalMovingBlock) floor).movementSpeed
							* ((HorizontalMovingBlock) floor).movementDirection;
				} 
			}
		}
		
		// horizontal collision
		if (movementDirection > 0) {
			if (parent.checkForPhysicalCollision(this.rightBorder() + hSpeed, y, 1, height)) {
				//x = Math.round(x);
				while(!parent.checkForPhysicalCollision(this.rightBorder() + movementDirection, y, 1, height)) {
					x += movementDirection;
				}
				hSpeed = 0;
			}
		} else if (movementDirection < 0) {
			if (parent.checkForPhysicalCollision(this.leftBorder() + hSpeed - 1, y, 1, height)) {
				//x = Math.round(x);
				while(!parent.checkForPhysicalCollision(this.leftBorder() + movementDirection
						- 1, y, 1, height)) {
					x += movementDirection;
				}
				hSpeed = 0;
			}
		}
		
		
		
		int vDirection = (int) Math.signum(vSpeed);
		// vertical collision
		if (vDirection > 0) {
			if (parent.checkForPhysicalCollision(x, this.bottomBorder() + vSpeed, width, 1)) {
				//y = Math.round(y); 
				while(!parent.checkForPhysicalCollision(x, this.bottomBorder() + vDirection, width, 1)) {
					y += vDirection;
				}
				vSpeed = 0;
			}
		} else if (vDirection < 0) {
			if (parent.checkForPhysicalCollision(x, this.topBorder() + vSpeed - 1, width, 1)) {
				//y = Math.round(y);
				while(!parent.checkForPhysicalCollision(x, this.topBorder() + vDirection - 1, width, 1)) {
					y += vDirection;
				}
				vSpeed = 0;
			}
		}
		
		
	}

	private synchronized void handleNonPhysicalCollisions(GameInstance parent)
	{
		ConcurrentHashMap<UUID, GameObject> collisions = parent.getTouching(this);
		
		for(Map.Entry<UUID, GameObject> entry : collisions.entrySet()) {
			if(entry.getValue() instanceof EffectOnContact) {
				((EffectOnContact) entry.getValue()).effectOnContact(this);
			}
		}
	}
	
	/** Updates this PlayerObject's position based on its current state. */
	public synchronized void doPhysics(GameInstance parent)
	{
		float sX = x;
		float sY = y;
		
		movementDirection = 0;
		if (leftPressed)
			movementDirection -= 1;
		if (rightPressed)
			movementDirection += 1;
			
		hSpeed = movementSpeed * movementDirection;
		
		if (vSpeed < maxFallSpeed)
			vSpeed += gravity;
			
		canJump = false;
		if (parent.checkForPhysicalCollision(x, y + height + 1, width, 1)) {
			canJump = true;
		}
		
		if (canJump && jumpPressed) {
			vSpeed = -1 * jumpSpeed;
		}
		
		// handle any incoming collisions
		handlePhysicalCollisions(parent);
		
		// move the player object
		x += hSpeed;
		y += vSpeed;
		
		// If player object did move
		if (sX != x || sY != y)
		{
			// Trigger sending PLAYER_OBJECT_UPDATE
			// TODO ...
		}
		
		handleNonPhysicalCollisions(parent);
	}
	
	public static PlayerObject createNew()
	{
		return new PlayerObject(DEFAULT_X, DEFAULT_Y);
	}

	public synchronized void kill()
	{
		if (spawn != null) {
			x = spawn.x;
			y = spawn.y;
		} else {
			x = 0;
			y = 0;
		}
		
		hSpeed = 0;
		vSpeed = 0;
		
		movementDirection = 0;
		
		canJump = false;
		
		
	}
}
