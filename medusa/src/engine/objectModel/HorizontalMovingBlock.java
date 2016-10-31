package engine.objectModel;

import engine.core.GameInstance;

public class HorizontalMovingBlock extends Block implements MovingObject
{
	private static final long serialVersionUID = 693212084271323344L;
	
	float movementSpeed = 4f;
	
	float hSpeed = 0;
	
	private int movementDirection = 1;

	public HorizontalMovingBlock(int sX, int sY)
	{
		super(sX, sY);
	}

	@Override
	public synchronized void doMovement(GameInstance parent)
	{
		hSpeed = movementSpeed * movementDirection;
		
		if (movementDirection > 0) {
			if ((parent.checkForPhysicalCollision(x + hSpeed + width, y, 1, height)) 
					|| (x + hSpeed + width - 1) > GameInstance.SCREEN_WIDTH) {
				x = Math.round(x);
				while((!parent.checkForPhysicalCollision(x + movementDirection + width + 1, y, 1, height))
						&& (x + movementDirection + width - 1) < GameInstance.SCREEN_WIDTH) {
					x += movementDirection;
				}
				hSpeed = 0;
				
				movementDirection = -1;
			}
		} else if (movementDirection < 0) {
			if ((parent.checkForPhysicalCollision(x + hSpeed - 1, y, 1, height) ) 
					|| (x + hSpeed - 1) < 0) {
				x = Math.round(x);
				while((!parent.checkForPhysicalCollision(x + movementDirection
						- 1, y, 1, height)) && (x + movementDirection - 1) > 0) {
					x += movementDirection;
				}
				hSpeed = 0;
				
				movementDirection = 1;
			}
		}
		
		x += hSpeed;
	}

}
