Block = Java.type("engine.gameObjects.Block");
DeathZone = Java.type("engine.gameObjects.DeathZone");
SpawnPoint = Java.type("engine.gameObjects.SpawnPoint");
HorizontalMovingBlock = Java.type("engine.gameObjects.HorizontalMovingBlock");

// blocks
instance.addToMap(new Block(50, 100));
instance.addToMap(new Block(200, 100));
instance.addToMap(new Block(264, 187));
for (i = 1; i < 11; i++)
{
	instance.addToMap(new Block(i * 50, 300));
}
instance.addToMap(new Block(50, 245));
instance.addToMap(new Block(500, 245));

// deathZones
for (i = -2; i < 9; i++)
{
	instance.addToMap(new DeathZone(100 * i, 900));
	instance.addToMap(new DeathZone(100 * i, -200));
	instance.addToMap(new DeathZone(-200, i * 100));
	instance.addToMap(new DeathZone(900, i * 100));
}

// spawnPoints
instance.addToMap(new SpawnPoint(204, 55));
instance.addToMap(new SpawnPoint(52, 40));
instance.addToMap(new SpawnPoint(260, 130));

// moving platforms
instance.addToMap(new HorizontalMovingBlock(400, 500));
instance.addToMap(new HorizontalMovingBlock(320, 187));
instance.addToMap(new HorizontalMovingBlock(100, 500));