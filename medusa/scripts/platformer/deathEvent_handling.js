Killable = Java.type("engine.gameObjects.objectClasses.Killable");
PlayerObject = Java.type("engine.gameObjects.PlayerObject");
SpawnEvent = Java.type("engine.gameEvents.SpawnEvent");

function handle(duringReplay) {
	object = objectMap.getObject(e.getObjectID());

	if (object instanceof Killable)
	{
		object.kill();
		instance.removeFromMap(object);

		if ((object instanceof PlayerObject) && !duringReplay)
			instance.queueEvent(new SpawnEvent(e, e.getTimeStamp()
					+ PlayerObject.DEFAULT_RESPAWN, instance.getInstanceID(), object), false);
	}
}