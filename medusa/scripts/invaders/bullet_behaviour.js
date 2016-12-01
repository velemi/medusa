y -= speed;

CollisionEvent = Java.type("engine.gameEvents.CollisionEvent");
DeathEvent = Java.type("engine.gameEvents.DeathEvent");

if (y <= -10)
{
	instance.queueEvent(new DeathEvent(instance.getCurrentTime() + 1,
							bullet.parentInstanceID, bullet.getID()), false);
}

colliding = instance.getColliding(bullet);

for (i = 0; i < colliding.size(); i++) {
	e = colliding.get(i);
	
	instance.queueEvent(new CollisionEvent(instance.getCurrentTime() + 1,
							bullet.parentInstanceID, bullet.getID(), e.getID()), false);
}