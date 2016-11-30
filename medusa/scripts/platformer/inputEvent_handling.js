function handle(duringReplay) {
	p = objectMap.getPlayerObject(e.getInstanceID());
	
	if (p != null)
	{
		switch (e.getInput())
		{
			case "LEFT PRESSED":
			{
				p.setLeftPressed(true);
				break;
			}
			case "RIGHT PRESSED":
			{
				p.setRightPressed(true);
				break;
			}
			case "JUMP PRESSED":
			{
				p.setJumpPressed(true);
				break;
			}
			case "LEFT RELEASED":
			{
				p.setLeftPressed(false);
				break;
			}
			case "RIGHT RELEASED":
			{
				p.setRightPressed(false);
				break;
			}
			case "JUMP RELEASED":
			{
				p.setJumpPressed(false);
				break;
			}
			default:
				break;
		}
	}
	else if (!duringReplay)
	{
		switch(e.getInput())
		{

			case "START RECORD":
			{
				replayManager.startRecording();
				break;
			}
			case "STOP RECORD":
			{
				replayManager.stopRecording();
				break;
			}
			case "PLAYBACK60":
			{
				replayManager.playReplay(60);
				break;
			}
			case "PLAYBACK30":
			{
				replayManager.playReplay(30);
				break;
			}
			case "PLAYBACK120":
			{
				replayManager.playReplay(120);
				break;
			}
			default:
				break;
		}
	}
}