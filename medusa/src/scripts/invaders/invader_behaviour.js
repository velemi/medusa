GameInstance = Java.type("engine.GameInstance");

if (!instance.replayManager.isPlaying())
{
	cTime = instance.getCurrentTime();
	pCount = instance.getPlayerCount();
}
else
{
	cTime = instance.replayManager.getReplayTime();
	pCount = instance.replayManager.getReplayPlayerCount();
}

if (cTime % movementDelay == 0)
{
	if (cTime >= turnTime && turnTime > thisLastTurned) 
	{
		if (pCount > 0)
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