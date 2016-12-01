Killable = Java.type("engine.gameObjects.objectClasses.Killable");
PlayerObject = Java.type("engine.gameObjects.PlayerObject");
Invader = Java.type("engine.gameObjects.Invader");
TextObject = Java.type("engine.gameObjects.TextObject")
ArrayList = Java.type("java.util.ArrayList");

function handle(duringReplay) {
	object = objectMap.getObject(e.getObjectID());

	if (object instanceof Killable)
	{
		object.kill();
		instance.removeFromMap(object);

		if ((object instanceof PlayerObject))
		{
			// remove all the enemies from the map
			objects = new ArrayList(objectMap.getFullMap().values());
			
			for (i = 0; i < objects.size(); i++)
			{
				o = objects.get(i);
				
				if (o instanceof Invader) {
					instance.removeFromMap(o);
				}
			}
			
			// add a game over message to the map
			instance.addToMap(new TextObject("YOU LOSE", 30, 300, 300, 200, 0, 0));
		}
		else if (object instanceof Invader)
		{
			objects = new ArrayList(objectMap.getFullMap().values());
			
			count = 0;
			
			for (i = 0; i < objects.size(); i++)
			{
				o = objects.get(i);
				
				if (o instanceof Invader) {
					count++;
				}
			}
			
			if (count <= 0)
				instance.addToMap(new TextObject("YOU WIN", 30, 300, 300, 200, 0, 0));
		}
	}
}