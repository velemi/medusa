PlayerObject = Java.type("engine.gameObjects.PlayerObject");
Bullet = Java.type("engine.gameObjects.Bullet");
Invader = Java.type("engine.gameObjects.Invader");
DeathEvent = Java.type("engine.gameEvents.DeathEvent");

function handle(duringReplay) {
	
	if (!duringReplay) 
	{
		object1 = objectMap.getObject(e.getIDs()[0]);
		object2 = objectMap.getObject(e.getIDs()[1]);
		
		if ((object1 instanceof PlayerObject) && (object2 instanceof Invader))
		{
			instance.queueEvent(new DeathEvent(e, e.getTimeStamp()
					+ 1, instance.getInstanceID(), object1.getID()), false);
		}
		
		if ((object1 instanceof Bullet) && (object2 instanceof Invader))
		{
			instance.queueEvent(new DeathEvent(e, e.getTimeStamp()
					+ 1, instance.getInstanceID(), object1.getID()), false);

			instance.queueEvent(new DeathEvent(e, e.getTimeStamp()
					+ 1, instance.getInstanceID(), object2.getID()), false);
		}
	} 
	else 
	{
		// do nothing, I guess
	}
}