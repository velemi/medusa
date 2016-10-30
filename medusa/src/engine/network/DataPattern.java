package engine.network;

/**
 * An enumeration where each constant represents a specific pattern of data that 
 * NetworkConnectionHandlers can send to one another, to alert the other to the type and format of 
 * the incoming data.
 * 
 * @author Jordan Neal
 */
public enum DataPattern
{
	INVALID_PATTERN,		// an invalid data pattern
	GAME_OBJECTS_UPDATE, 	// will be sending updated game objects
	GAME_OBJECTS_UPDATE_ALL,		// will be sending all current game objects
	PLAYER_OBJECT_UPDATE
}
