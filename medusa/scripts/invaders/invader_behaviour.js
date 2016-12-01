GameInstance = Java.type("engine.GameInstance");

cTime = instance.getCurrentTime();

if (cTime % movementDelay == 0)
{
	if (cTime >= turnTime && turnTime > thisLastTurned) 
	{
		y += 20;
		
		movementDirection = movementDirection * -1;
		
		thisLastTurned = cTime;
	}
	else 
	{
		hSpeed = fleetSpeed * movementDirection;
		
		x += hSpeed;
		
		if (movementDirection > 0) {
			if (x + width > GameInstance.SCREEN_WIDTH - 20){
				turnTime = cTime + movementDelay;
			}
		}
		else if (movementDirection < 0) {
			if (x < 20){
				turnTime = cTime + movementDelay;
			}
		}
	}
}