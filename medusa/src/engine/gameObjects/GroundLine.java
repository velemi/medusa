package engine.gameObjects;

import engine.GameInstance;
import engine.gameObjects.objectClasses.RenderableObject;

public class GroundLine extends GameObject implements RenderableObject
{
	private static final long serialVersionUID = 1L;
	
	public GroundLine(float y)
	{
		super(0, y);
	}

	@Override
	public void display(GameInstance parent)
	{
		parent.fill(0,255,0);
		parent.stroke(0,255,0);
		parent.line(0, y, GameInstance.SCREEN_WIDTH, y);
	}
	
}
