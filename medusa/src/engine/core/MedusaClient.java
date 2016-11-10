package engine.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import engine.gameEvents.CollisionEvent;
import engine.gameEvents.DeathEvent;
import engine.gameEvents.EventHandler;
import engine.gameEvents.GameEvent;
import engine.gameEvents.InputEvent;
import engine.gameEvents.SpawnEvent;
import engine.gameObjects.DeathZone;
import engine.gameObjects.GameObject;
import engine.gameObjects.Killable;
import engine.gameObjects.MovingObject;
import engine.gameObjects.PlayerObject;
import engine.gameObjects.Spawnable;
import engine.network.DataPattern;
import engine.network.NetworkHandler;
import processing.core.PApplet;

/**
 * Game client for Medusa Engine
 * 
 * @author Jordan Neal
 */
public class MedusaClient extends GameInstance
{
	public static final boolean 	DEBUG = GameInstance.DEBUG;
	
	/** This game client's PlayerObject */
	private PlayerObject playerObject;
	
	private class ClientEventHandler implements EventHandler
	{
		@Override
		public void handleEvent(GameEvent e)
		{
			switch(e.getEventType())
			{
				case "CollisionEvent":
				{
					handle((CollisionEvent) e);
					break;
				}
				case "InputEvent":
				{
					handle((InputEvent) e);
					break;
				}
				case "DeathEvent":
				{
					handle((DeathEvent) e);
					break;
				}
				case "SpawnEvent":
				{
					handle((SpawnEvent) e);
					break;
				}
				default:
					break;
			}
		}
		
		private void handle(CollisionEvent e)
		{
			GameObject[] objects = new GameObject[2];
			
			objects[0] = gameObjectMap.get(e.getIDs()[0]);
			objects[1] = gameObjectMap.get(e.getIDs()[1]);
			
			if ((objects[0] instanceof PlayerObject) && (objects[1] instanceof DeathZone))
			{
				eventManager.queueEvent(
						new DeathEvent(e, e.getTimeStamp() + 1, 3, getInstanceID(), objects[0].getID()));
			}
			else if ((objects[1] instanceof PlayerObject) && (objects[0] instanceof DeathZone))
			{
				eventManager.queueEvent(
						new DeathEvent(e, e.getTimeStamp() + 1, 3, getInstanceID(), objects[1].getID()));
			}
		}
		
		private void handle(InputEvent e)
		{
			switch(e.getInput())
			{
				case "LEFT PRESSED":
				{
					playerObject.setLeftPressed(true);
					break;
				}
				case "RIGHT PRESSED":
				{
					playerObject.setRightPressed(true);
					break;
				}
				case "JUMP PRESSED":
				{
					playerObject.setJumpPressed(true);
					break;
				}
				case "LEFT RELEASED":
				{
					playerObject.setLeftPressed(false);
					break;
				}
				case "RIGHT RELEASED":
				{
					playerObject.setRightPressed(false);
					break;
				}
				case "JUMP RELEASED":
				{
					playerObject.setJumpPressed(false);
					break;
				}
				default:
					break;
			}
		}
		
		private void handle(DeathEvent e)
		{
			GameObject object = gameObjectMap.get(e.getObjectID());
			
			if (object instanceof Killable)
			{
				((Killable) object).kill();
				removeFromMap(object);
				
				if (object instanceof PlayerObject)
					eventManager.queueEvent(new SpawnEvent(e, e.getTimeStamp() 
							+ PlayerObject.DEFAULT_RESPAWN, 4, getInstanceID(), object));
			}
		}
		
		private void handle(SpawnEvent e)
		{
			GameObject object = e.getObject();
			
			if (object instanceof Spawnable)
			{
				((Spawnable) object).spawn();
				
				if (!gameObjectMap.contains(object))
				{
					gameObjectMap.put(object.getID(), object);
				}
			}
		}
	}
	
	private ServerHandler gameServerHandler;
	
	/**
	 * A subclass which handles I/O between this game client and the game
	 * server.
	 * 
	 * @author Jordan Neal
	 */
	class ServerHandler extends NetworkHandler
	{
		String serverHostname;
		
		@Override
		protected void disconnect()
		{
			super.disconnect();
		}
		
		class ClientOutputThread extends NetworkOutputThread
		{
			PlayerObject p = null;
			
			public ClientOutputThread()
			{
				outgoingPattern = DataPattern.PLAYER_OBJECT_UPDATE;
			}
			
			protected void sendData()
			{
				switch (outgoingPattern) {
					case PLAYER_OBJECT_UPDATE :	//PLAYER_OBJECT_UPDATE
						sendPlayerObject();
						break;
					default:
						break;
				}
			}
			
			private void sendPlayerObject()
			{
				if(p == null)
					p = playerObject.clone();
				else
					p.becomeCopyOf(playerObject);
				
				try {
					networkOutput.reset();
					networkOutput.writeObject(p);
				} catch (EOFException e) {
					System.err.println("EOFException occurred when trying to write to the"
							+ " server - disconnecting & shutting down");
					disconnect();
				} catch (SocketException e) {
					System.err.println("SocketException has occurred when trying to write to the"
							+ " server - disconnecting & shutting down");
					disconnect();
				} catch (IOException e) {
					System.err.println("IOException has occurred when trying to write to the"
							+ " server - disconnecting & shutting down");
					disconnect();
				}
			}
		}
		
		class ClientInputThread extends NetworkInputThread
		{
			Map<UUID, GameObject> g = null;
			
			protected void receiveData()
			{
				switch (incomingPattern) {
					case GAME_OBJECTS_UPDATE :
						receiveUpdatedObjects();
						break;
					case GAME_OBJECTS_UPDATE_ALL :
						receiveAllObjects();
						break;
					default:
						break;
				}
			}
			
			@SuppressWarnings("unchecked")
			private void receiveUpdatedObjects()
			{
				try {
					g = (Map<UUID, GameObject>) networkInput.readObject();
					//System.out.println("got g: "+g);
					g.remove(playerObject.getID());
					
					gameObjectMap.putAll(g);
					
					
					if (playerObject.isAlive())
						gameObjectMap.put(playerObject.getID(), playerObject);
				} catch (ClassNotFoundException e) {
					System.err.println("ClassNotFoundException occurred when trying to read from the"
							+ " server - disconnecting & shutting down");
					disconnect();
				} catch (SocketException e) {
					System.err.println("SocketException occurred when trying to read from the"
							+ " server - disconnecting & shutting down");
					disconnect();
				} catch (IOException e) {
					System.err.println("IOException occurred when trying to read from the"
							+ " server - disconnecting & shutting down");
					disconnect();
				} 
			}
			
			@SuppressWarnings("unchecked")
			private void receiveAllObjects()
			{
				try {
					g = (Map<UUID, GameObject>) networkInput.readObject();
					
					g.remove(playerObject.getID());
					
					gameObjectMap.clear();
					gameObjectMap.putAll(g);
					gameObjectMap.put(playerObject.getID(), playerObject);
				} catch (ClassNotFoundException e) {
					System.err.println("ClassNotFoundException occurred when trying to read from the"
							+ " server - disconnecting & shutting down");
					disconnect();
				} catch (SocketException e) {
					System.err.println("SocketException occurred when trying to read from the"
							+ " server - disconnecting & shutting down");
					disconnect();
				} catch (IOException e) {
					System.err.println("IOException occurred when trying to read from the"
							+ " server - disconnecting & shutting down");
					disconnect();
				} 
			}
		}
		
		private ServerHandler(Socket sock)
		{
			super(sock);
		}
		
		protected void createThreads()
		{
			outputThread = new ClientOutputThread();
			inputThread = new ClientInputThread();
		}

		protected void socketSetup(Socket sock)
		{
			if (!GameInstance.DEBUG) {
				System.out.print("Enter the server's IP address: ");
				Scanner scanner = new Scanner(System.in);
				serverHostname = scanner.next();
				scanner.close();
			} else {
				serverHostname = "localhost";
			}
			
			try {
				connectionSocket = new Socket(serverHostname, SERVER_PORT);
				
				networkOutput = new ObjectOutputStream(this.connectionSocket.getOutputStream());
				networkInput = new ObjectInputStream(this.connectionSocket.getInputStream());
				
				System.out.println("Successfully established connection to the server.");
				connected = true;
			} catch (UnknownHostException e) {
				System.err.println("UnknownHostException occurred when trying to connect to server"
						+ " - aborting & shutting down");
			} catch (IOException e) {
				System.err.println("IOException occurred when trying to connect to server"
						+ " - aborting & shutting down");
			}
		}

		@SuppressWarnings("unchecked")
		protected void initDataTransactions()
		{
			try {
				gameTimeline = new Timeline(networkInput.readLong(), 1000000000L / TARGET_FRAMERATE);
				
				playerObject = (PlayerObject) networkInput.readObject();
				gameObjectMap = (ConcurrentHashMap<UUID, GameObject>) networkInput.readObject();
				gameObjectMap.put(playerObject.getID(), playerObject);
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFoundException occurred while performing initial"
						+ " communication with server - disconnecting & shutting down");
				disconnect();
			} catch (IOException e) {
				System.err.println("IOException occurred while performing initial communcation"
						+ " with server - disconnecting & shutting down");
			}
		}

		
	}
	
	void sendMessageToServer(DataPattern message)
	{
		gameServerHandler.queueMessage(message);
	}
	
	private ClientLogicThread clientLogicThread = new ClientLogicThread(this);
	
	/** The thread which handles the game logic loop
	 * 
	 * @author Jordan Neal
	 */
	private class ClientLogicThread extends Thread
	{
		GameInstance client;
		
		public ClientLogicThread(GameInstance client)
		{
			this.client = client;
		}
		
		public void run()
		{
			currentTime = gameTimeline.getTime();
			while(true) {
				long newTime = gameTimeline.getTime();
				
				while (newTime > currentTime)
				{
					currentTime++;
					
					eventManager.handleEvents(currentTime);
					
					if (playerObject.isAlive())
						playerObject.doPhysics(client);
				}
			}
		}
	}

	
	/* Defines behavior to be run once when keys are pressed. May run repeatedly
	 * (after system-dependent delay) if keys are held. (non-Javadoc)
	 * @see processing.core.PApplet#keyPressed()
	 */
	public void keyPressed()
	{
		String inputString = "";
		
		if (key == CODED)
		{
			if (keyCode == LEFT)
			{
				inputString = "LEFT PRESSED";
			}
			else if (keyCode == RIGHT)
			{
				inputString = "RIGHT PRESSED";
			}
		}
		else if (key == ' ')
		{
			inputString = "JUMP PRESSED";
		}
		
		if (!inputString.equals(""))
			eventManager.queueEvent(new InputEvent(gameTimeline.getTime(), 1, getInstanceID(), inputString));
	}
	
	/* Defines behavior to be run once when keys are released. (non-Javadoc)
	 * @see processing.core.PApplet#keyReleased()
	 */
	public void keyReleased()
	{
		String inputString = "";
		
		
		if (key == CODED) {
			if (keyCode == LEFT) {
				inputString = "LEFT RELEASED";
			} else if (keyCode == RIGHT) {
				inputString = "RIGHT RELEASED";
			}
		} else if (key == ' ') {
			inputString = "JUMP RELEASED";
		}
		
		if (!inputString.equals(""))
			eventManager.queueEvent(new InputEvent(gameTimeline.getTime(), 1, getInstanceID(), inputString));
	}
	
	@Override
	public void setup()
	{
		eventManager.registerHandler(new ClientEventHandler(), new String[]
				{"CollisionEvent", "InputEvent", "DeathEvent", "SpawnEvent"});
		
		// Try to establish a connection to the server by instantiating a server handler
		gameServerHandler = new ServerHandler(new Socket());
		
		if (gameServerHandler.connected) {
			
			// if the connection was successful, start server handler's threads, and logic thread
			gameServerHandler.startThreads();
			clientLogicThread.start();
			
		} else {
			
			// if the connection was not successful, print a failure message before exiting
			System.out.println("Connection to server failed.");
		}
	}
	
	public void runClient()
	{
		PApplet.main("engine.core.MedusaClient");
	}
	
	public static void main(String[] args)
	{
		MedusaClient medusaClient = new MedusaClient();
		medusaClient.runClient();
	}
}
