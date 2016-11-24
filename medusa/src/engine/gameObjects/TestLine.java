package engine.gameObjects;

import engine.gameObjects.objectClasses.RenderableObject;
import processing.core.PApplet;

public class TestLine extends GameObject implements RenderableObject
{
	private static final long serialVersionUID = 6871617644663307574L;
	
	/** Default height, in pixels, of a Block object */
	static final float DEFAULT_HEIGHT = 300;
	
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
	public TestLine(int sX, int sY)
	{
		super(sX, sY);
		this.height = TestLine.DEFAULT_HEIGHT;
	}
	
	/*
	 * Defines rendering behavior for Block objects (non-Javadoc)
	 * @see engine.GameObject#display()
	 */
	public synchronized void display(PApplet parent)
	{
		parent.fill(0);
		parent.stroke(200,0,0);
		parent.line(x, y, x, y+height);
	}
}
