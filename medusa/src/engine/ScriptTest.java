package engine;

import engine.gameObjects.Block;
import engine.gameObjects.HorizontalMovingBlock;
import engine.gameObjects.SpawnPoint;

public class ScriptTest
{
	
	public static void main(String[] args)
	{
		HorizontalMovingBlock h = new HorizontalMovingBlock(0, 0);
		
		ScriptManager.bindArgument("h", h);
		ScriptManager.bindArgument("HorizontalMovingBlock", HorizontalMovingBlock.class);
		ScriptManager.bindArgument("Block", Block.class);
		ScriptManager.bindArgument("SpawnPoint", SpawnPoint.class);
		
		ScriptManager.loadScript("src/scripts/testScript.js");
	}
	
}
