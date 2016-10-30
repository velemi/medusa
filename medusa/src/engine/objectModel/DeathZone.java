package engine.objectModel;

import processing.core.PApplet;

public class DeathZone extends GameObject implements EffectOnContact, RenderableObject
{
	private static final long serialVersionUID = 4872896885224403682L;
	
	/** Default height, in pixels, of a DeathZone object */
	static final float DEFAULT_HEIGHT = 100;
	
	/** Default width, in pixels, of a DeathZone object */
	static final float DEFAULT_WIDTH = 100;

	private static final boolean DEBUG = false;
	
	public DeathZone(int sX, int sY)
	{
		super(sX, sY);
		this.height = DeathZone.DEFAULT_HEIGHT;
		this.width = DeathZone.DEFAULT_WIDTH;
		
		this.setPhysicalCollision(false);
	}

	@Override
	public void effectOnContact(GameObject inContact)
	{
		if (inContact instanceof PlayerObject) {
			((PlayerObject) inContact).kill();
		}
	}

	@Override
	public void display(PApplet parent)
	{
		if (DeathZone.DEBUG) {
			parent.fill(200, 0, 0);
			parent.stroke(200, 0, 0);
			parent.rect(this.x, this.y, this.width, this.height);
		}
		
	}
	
}
