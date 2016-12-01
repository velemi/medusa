if (h.isType(HorizontalMovingBlock)) {
	print("Success");
}

if (h.isType(Block)) {
	print("Success");
}

if (!h.isType(SpawnPoint)) {
	print("Success");
}

GameInstance = Java.type("engine.GameInstance");

print(GameInstance.SCREEN_WIDTH);