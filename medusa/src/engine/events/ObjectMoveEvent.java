package engine.events;

import java.util.UUID;

public class ObjectMoveEvent extends GameEvent
{
	UUID objectID;
	float startX, startY, deltaX, deltaY;
	
	public ObjectMoveEvent(long ts, UUID objectID, float sX, float sY, float dX, float dY)
	{
		this.timestamp = ts;
		this.objectID = objectID;
		this.startX = sX;
		this.startY = sY;
		this.deltaX = dX;
		this.deltaY = dY;
	}
}
