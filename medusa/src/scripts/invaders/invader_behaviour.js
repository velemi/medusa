GameInstance = Java.type("engine.GameInstance");
CollisionEvent = Java.type("engine.gameEvents.CollisionEvent");
Invader = Java.type("engine.gameObjects.Invader");
PlayerObject = Java.type("engine.gameObjects.PlayerObject");
TextObject = Java.type("engine.gameObjects.TextObject");
ArrayList = Java.type("java.util.ArrayList");

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
		if (pCount > 0) {
			y += 20;
			
			if (y > 773 )
			{
				// remove all the enemies from the map
				objects = new ArrayList(objectMap.getFullMap().values());
				
				for (i = 0; i < objects.size(); i++)
				{
					o = objects.get(i);
					
					if (o instanceof Invader || o instanceof PlayerObject) {
						instance.removeFromMap(o);
					}
				}
				
				// add a game over message to the map
				instance.addToMap(new TextObject("YOU LOSE", 30, 300, 300, 200, 0, 0));
			}
		}
		
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