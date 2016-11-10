package engine.time;

public class Timeline
{
	// The Timeline object that this Timeline is anchored to. If null, the Timeline
	// is assumed to be anchored to real time.
	private Timeline anchorTimeline = null;
	
	// The point on the anchor Timeline which is considered time = 0 for this Timeline
	private long origin;
	
	// The number of ticks of the anchoring Timeline which equals one tick on this Timeline
	private long tickSize;
	
	/** 
	 * Constructor which creates a new Timeline object, anchored to real time, with the
	 * specified tick size (measured in nanoseconds).
	 */
	public Timeline(long tickSize)
	{
		this(0, tickSize);
	}

	public Timeline(long currentTime, long tickSize)
	{
		this.origin = System.nanoTime() - (currentTime * tickSize);
		
		this.tickSize = tickSize;
	}
	
	/** 
	 * Constructor which creates a new Timeline object, anchored to the specified
	 * Timeline object, and with the specified tick size.
	 */
	public Timeline(Timeline anchor, long tickSize)
	{
		if (anchor != null) {
			this.anchorTimeline = anchor;
			this.origin = anchor.getTime();
			this.tickSize = tickSize;
		}
		else {
			this.origin = System.nanoTime();
			this.tickSize = tickSize;
		}
	}
	
	/**
	 * Returns the current time, relative to this Timeline object.
	 * 
	 * @return		the number of ticks between now and the Timeline's origin point
	 */
	public long getTime()
	{
		if(anchorTimeline == null)	//if anchored to real time
		{
			return (System.nanoTime() - this.origin) / this.tickSize;
		}
		else	//if anchored to another timeline
		{
			return (anchorTimeline.getTime() - this.origin) / this.tickSize;
		}
	}
	
	public Timeline getAnchorTimeline()
	{
		return anchorTimeline;
	}

	public void setAnchorTimeline(Timeline anchorTimeline)
	{
		this.anchorTimeline = anchorTimeline;
	}

	public long getOrigin()
	{
		return origin;
	}

	public void setOrigin(long origin)
	{
		this.origin = origin;
	}

	public long getTickSize()
	{
		return tickSize;
	}

	public void setTickSize(long tickSize)
	{
		this.tickSize = tickSize;
	}
}
