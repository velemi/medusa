package engine.gameObjects;

import engine.GameInstance;
import engine.gameObjects.objectClasses.MovingObject;

public class HorizontalMovingBlock extends Block implements MovingObject
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
	
	@Override
	public synchronized void doPhysics(GameInstance parent)
	{
		hSpeed = movementSpeed * movementDirection;
		
		if (movementDirection > 0)
		{
			if ((parent.checkForPhysicalCollision(this.rightBorder() + hSpeed
					+ 1, y, 1, height))
					|| (x + hSpeed + width - 1) > GameInstance.SCREEN_WIDTH)
			{
				x = Math.round(x);
				while((!parent.checkForPhysicalCollision(this.rightBorder()
						+ movementDirection + 1, y, 1, height))
						&& (x + movementDirection + width
								- 1) < GameInstance.SCREEN_WIDTH)
				{
					x += movementDirection;
				}
				hSpeed = 0;
				
				movementDirection = -1;
			}
		}
		else if (movementDirection < 0)
		{
			if ((parent.checkForPhysicalCollision(this.leftBorder() + hSpeed
					- 1, y, 1, height))
					|| (x + hSpeed - 1) < 0)
			{
				x = Math.round(x);
				while((!parent.checkForPhysicalCollision(this.leftBorder()
						+ movementDirection
						- 1, y, 1, height)) && (x + movementDirection - 1) > 0)
				{
					x += movementDirection;
				}
				hSpeed = 0;
				
				movementDirection = 1;
			}
		}
		
		x += hSpeed;
	}
	
}
