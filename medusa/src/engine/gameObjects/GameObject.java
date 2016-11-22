package engine.gameObjects;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.UUID;

/**
 * A generic game object
 * 
 * @author Jordan Neal
 */
public class GameObject
		extends Rectangle2D.Float implements Serializable, Cloneable
{
	private static final long serialVersionUID = 3542363890442782476L;
	
	protected UUID objectID = UUID.randomUUID();
	
	/**
	 * True if this GameObject should have physical collision with
	 * PlayerObjects.
	 */
	private boolean physicalCollision = false;
	
	public GameObject()
	{
		this.x = 0;
		this.y = 0;
	}
	
	public GameObject(float sX, float sY)
	{
		this.x = sX;
		this.y = sY;
	}
	
	public boolean isType(Class<?> t)
	{
		if(t.isInstance(this))
			return true;
		
		return false;
	}
	
	public synchronized void becomeCopyOf(GameObject other)
	{
		this.x = other.x;
		this.y = other.y;
		this.height = other.height;
		this.width = other.width;
		
		this.physicalCollision = other.physicalCollision;
		
		this.objectID = other.objectID;
	}
	
	public synchronized GameObject clone()
	{
		GameObject cloneObject = (GameObject) super.clone();
		
		cloneObject.physicalCollision = this.physicalCollision;
		cloneObject.objectID = this.objectID;
		
		return (cloneObject);
	}
	
	public synchronized UUID getID()
	{
		return objectID;
	}
	
	public synchronized float topBorder()
	{
		return (this.y);
	}
	
	public synchronized float bottomBorder()
	{
		return (this.y + this.height);
	}
	
	public synchronized float leftBorder()
	{
		return (this.x);
	}
	
	public synchronized float rightBorder()
	{
		return (this.x + this.width);
	}
	
	/**
	 * Gets the value of physicalCollision for this GameObject
	 * 
	 * @return playerCollide
	 */
	public synchronized boolean hasPhysicalCollision()
	{
		return this.physicalCollision;
	}
	
	/**
	 * Sets the value of physicalCollision for this GameObject
	 * 
	 * @param collide
	 *            what playerCollide should be set to
	 */
	public synchronized void setPhysicalCollision(boolean collide)
	{
		this.physicalCollision = collide;
	}
}
