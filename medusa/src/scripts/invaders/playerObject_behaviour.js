Bullet = Java.type("engine.gameObjects.Bullet");

movementDirection = 0;

if (leftPressed)
	movementDirection -= 1;
if (rightPressed)
	movementDirection += 1;

hSpeed = movementSpeed * movementDirection;

if (movementDirection > 0)
{
	if (instance.checkForPhysicalCollision(x + width
			+ hSpeed, y, 1, height))
	{
		// x = Math.round(x);
		while(!instance.checkForPhysicalCollision(x + width
				+ movementDirection, y, 1, height))
		{
			x += movementDirection;
		}
		hSpeed = 0;
	}
}
else if (movementDirection < 0)
{
	if (instance.checkForPhysicalCollision(x + hSpeed
			- 1, y, 1, height))
	{
		// x = Math.round(x);
		while(!instance.checkForPhysicalCollision(x
				+ movementDirection
				- 1, y, 1, height))
		{
			x += movementDirection;
		}
		hSpeed = 0;
	}
}

if (jumpPressed && Bullet.getCount() == 0) {
	instance.addToMap(new Bullet(x + (width / 8) * 3, y - 10, player.parentInstanceID));
}

x += hSpeed;

//handle Non-Physical collisions
colliding = instance.getColliding(player);

CollisionEvent = Java.type("engine.gameEvents.CollisionEvent");

for (i = 0; i < colliding.size(); i++) {
	e = colliding.get(i);
	
	instance.queueEvent(new CollisionEvent(instance.getCurrentTime() + 1,
							player.parentInstanceID, player.getID(), e.getID()), false);
}