//print(movementDirection);
//print(leftPressed);

movementDirection = 0;

if (leftPressed)
	movementDirection -= 1;
if (rightPressed)
	movementDirection += 1;

//print(movementDirection);

hSpeed = movementSpeed * movementDirection;

if (vSpeed < maxFallSpeed)
	vSpeed += gravity;

canJump = false;
if (instance.checkForPhysicalCollision(x, y + height + 1, width, 1)) {
	canJump = true;
}

if (canJump && jumpPressed) {
	vSpeed = -1 * jumpSpeed;
}

standingOn = instance.getPhysicalCollisions(x, y + height
		+ 1, width, 1);

HorizontalMovingBlock = Java.type("engine.gameObjects.HorizontalMovingBlock");

if (!standingOn.isEmpty())
{
	for (i = 0; i < standingOn.size(); i++)
	{
		floor = standingOn.get(i);
		if (floor instanceof HorizontalMovingBlock)
		{
			this.hSpeed += floor.getMovementSpeed()
					* floor.getMovementDirection();
		}
	}
}

// horizontal collision
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

vDirection = java.lang.Math.signum(vSpeed);
// vertical collision
if (vDirection > 0)
{
	if (instance.checkForPhysicalCollision(x, y + height
			+ vSpeed, width, 1))
	{
		// y = Math.round(y);
		while(!instance.checkForPhysicalCollision(x, y + height
				+ vDirection, width, 1))
		{
			y += vDirection;
		}
		vSpeed = 0;
	}
}
else if (vDirection < 0)
{
	if (instance.checkForPhysicalCollision(x, y + vSpeed
			- 1, width, 1))
	{
		// y = Math.round(y);
		while(!instance.checkForPhysicalCollision(x, y
				+ vDirection - 1, width, 1))
		{
			y += vDirection;
		}
		vSpeed = 0;
	}
}

//print(x + "\n");

x = x + hSpeed;
y += vSpeed;

//print(x + '\n');