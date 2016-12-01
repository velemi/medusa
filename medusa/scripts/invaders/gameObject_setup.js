GameInstance = Java.type("engine.GameInstance");
SpawnPoint = Java.type("engine.gameObjects.SpawnPoint");
Invader = Java.type("engine.gameObjects.Invader");
PlayerObject = Java.type("engine.gameObjects.PlayerObject");
Block = Java.type("engine.gameObjects.Block");
GroundLine = Java.type("engine.gameObjects.GroundLine");

// invaders
for (i = 0; i < 12; i++){
	instance.addToMap(new Invader(i * 60 + 20, 80));
	instance.addToMap(new Invader(i * 60 + 20, 120));
}

// spawn point
instance.addToMap(new SpawnPoint(390, 762));

// set size of player objects (since this game uses a different size than the default)
PlayerObject.setDefaultSize(40, 18);

// blocks from platform game so I can re-use that collision code
instance.addToMap(new Block(-51, 762));
instance.addToMap(new Block(GameInstance.SCREEN_WIDTH + 1, 762));

// "ground line"
instance.addToMap(new GroundLine(783));