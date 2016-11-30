Spawnable = Java.type("engine.gameObjects.objectClasses.Spawnable");

function handle(duringReplay) {
	object = e.getObject();
	
	if (object instanceof Spawnable)
	{
		object.spawn();
		
		if (!objectMap.contains(object))
		{
			instance.addToMap(object);
		}
	}
}