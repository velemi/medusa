package engine.gameObjects;

public class SpawnPoint extends GameObject
{	
	private static final long serialVersionUID = 3958834212303800225L;
	
	public SpawnPoint(int sX, int sY)
	{
		super(sX, sY);
		
		this.setPhysicalCollision(false);
	}
}
