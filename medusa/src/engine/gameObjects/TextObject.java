package engine.gameObjects;

import engine.GameInstance;
import engine.gameObjects.objectClasses.RenderableObject;

public class TextObject extends GameObject implements RenderableObject
{
	private static final long serialVersionUID = 1L;
	
	int[] color = { 0, 0, 0 };
	
	String text;
	float size;
	
	public TextObject(String text, float size, float sX, float sY)
	{
		super(sX,sY);
		
		this.text = text;
		this.size = size;
	}
	
	public TextObject(String text, float size, float sX, float sY, int r, int g, int b)
	{
		this(text, size, sX, sY);
		
		this.color = new int[]{r, g, b};
	}

	@Override
	public void display(GameInstance parent)
	{
		parent.fill(color[0],color[1],color[2]);
		parent.stroke(color[0],color[1],color[2]);
		
		parent.textSize(size);
		parent.text(text, x, y);
	}
	
}
