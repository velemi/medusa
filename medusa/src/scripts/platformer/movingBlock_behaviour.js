GameInstance = Java.type("engine.GameInstance");

hSpeed = movementSpeed * movementDirection;

if (movementDirection > 0) {
	if ((instance.checkForPhysicalCollision(x + width + hSpeed + 1, y,
			1, height))
			|| (x + hSpeed + width - 1) > GameInstance.SCREEN_WIDTH) {
		x = Math.round(x);
		while ((!instance.checkForPhysicalCollision(x + width
				+ movementDirection + 1, y, 1, height))
				&& (x + movementDirection + width - 1) < GameInstance.SCREEN_WIDTH) {
			x += movementDirection;
		}
		hSpeed = 0;

		movementDirection = -1;
	}
} else if (movementDirection < 0) {
	if ((instance.checkForPhysicalCollision(x + hSpeed - 1, y,
			1, height))
			|| (x + hSpeed - 1) < 0) {
		x = Math.round(x);
		while ((!instance.checkForPhysicalCollision(x
				+ movementDirection - 1, y, 1, height))
				&& (x + movementDirection - 1) > 0) {
			x += movementDirection;
		}
		hSpeed = 0;

		movementDirection = 1;
	}
}

x += hSpeed;