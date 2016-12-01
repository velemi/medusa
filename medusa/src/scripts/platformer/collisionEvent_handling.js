PlayerObject = Java.type("engine.gameObjects.PlayerObject");
DeathZone = Java.type("engine.gameObjects.DeathZone");
DeathEvent = Java.type("engine.gameEvents.DeathEvent");

function handle(duringReplay) {
	
	object1 = objectMap.getObject(e.getIDs()[0]);
	object2 = objectMap.getObject(e.getIDs()[1]);
	
	if (!duringReplay) 
	{
		if ((object1 instanceof PlayerObject) && (object2 instanceof DeathZone))
		{
			instance.queueEvent(new DeathEvent(e, e.getTimeStamp()
					+ 1, instance.getInstanceID(), object1.getID()), false);
		}
		else if ((object2 instanceof PlayerObject) && (object1 instanceof DeathZone))
		{
			instance.queueEvent(new DeathEvent(e, e.getTimeStamp()
					+ 1, instance.getInstanceID(), object2.getID()), false);
		}
	} 
	else 
	{
		// do nothing, I guess
	}
}