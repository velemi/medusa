package engine.objectModel;

import processing.core.PApplet;

/**
 * A generic "block" game object
 * 
 * @author Jordan Neal
 */
public class Block extends GameObject implements RenderableObject
{
	private static final long serialVersionUID = 6909549408650446901L;
	
	/** Default height, in pixels, of a Block object */
	static final float DEFAULT_HEIGHT = 50;
	
	/** Default width, in pixels, of a Block object */
	static final float DEFAULT_WIDTH = 50;
	
	/**
	 * Constructor which initializes the Block object
	 * 
	 * @param sX
	 *            The starting X-coordinate of this Block object's upper left
	 *            corner.
	 * @param sY
	 *            The starting Y-coordinate of this Block object's upper left
	 *            corner.
	 */
	public Block(int sX, int sY)
	{
		super(sX, sY);
		this.height = Block.DEFAULT_HEIGHT;
		this.width = Block.DEFAULT_WIDTH;
		
		this.setPhysicalCollision(true);
	}
	
	/*
	 * Defines rendering behavior for Block objects
	 * (non-Javadoc)
	 * @see engine.GameObject#display()
	 */
	public synchronized void display(PApplet parent)
	{
		parent.fill(0);
		parent.stroke(100);
		parent.rect(x, y, width, height);
	}
}
