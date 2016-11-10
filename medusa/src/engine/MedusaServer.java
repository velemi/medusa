package engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.UUID;
import engine.gameEvents.CollisionEvent;
import engine.gameEvents.DeathEvent;
import engine.gameEvents.GameEvent;
import engine.gameEvents.InputEvent;
import engine.gameEvents.NullEvent;
import engine.gameEvents.SpawnEvent;
import engine.gameEvents.eventManagement.EventHandler;
import engine.gameObjects.Block;
import engine.gameObjects.DeathZone;
import engine.gameObjects.GameObject;
import engine.gameObjects.HorizontalMovingBlock;
import engine.gameObjects.PlayerObject;
import engine.gameObjects.SpawnPoint;
import engine.gameObjects.objectClasses.Killable;
import engine.gameObjects.objectClasses.MovingObject;
import engine.gameObjects.objectClasses.Spawnable;
import engine.network.NetworkHandler;
import engine.network.messages.ClientDisconnectMessage;
import engine.network.messages.GameEventMessage;
import engine.network.messages.NewClientMessage;
import engine.time.Timeline;
import processing.core.PApplet;

/**
 * Game server for Medusa Engine
 * 
 * @author Jordan Neal
 */
public class MedusaServer extends GameInstance
{
	public static final boolean DEBUG = GameInstance.DEBUG;
	
	private class ServerEventHandler implements EventHandler
	{
		@Override
		public void handleEvent(GameEvent e)
		{
			switch (e.getEventType())
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
			
			if ((objects[0] instanceof PlayerObject)
					&& (objects[1] instanceof DeathZone))
			{
				eventManager.queueEvent(new DeathEvent(e, e.getTimeStamp()
						+ 1, getInstanceID(), objects[0].getID()));
			}
			else if ((objects[1] instanceof PlayerObject)
					&& (objects[0] instanceof DeathZone))
			{
				eventManager.queueEvent(new DeathEvent(e, e.getTimeStamp()
						+ 1, getInstanceID(), objects[1].getID()));
			}
		}
		
		private void handle(InputEvent e)
		{
			PlayerObject p = playerObjects.get(e.getInstanceID());
			
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
							+ PlayerObject.DEFAULT_RESPAWN, getInstanceID(), object));
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
					addToMap(object);
				}
			}
		}
	}
	
	@Override
	public void queueEvent(GameEvent e, boolean propagate)
	{
		eventManager.queueEvent(e);
		
		if (propagate)
		{
			synchronized (clientList)
			{
				for (ClientHandler client : clientList)
				{
					if (client.clientInstanceID != e.getInstanceID())
					{
						client.queueMessage(new GameEventMessage(e));
					}
				}
			}
		}
	}
	
	private ServerLogicThread serverLogicThread = new ServerLogicThread(this);
	
	/**
	 * The thread which handles the game logic loop
	 * 
	 * @author Jordan Neal
	 */
	private class ServerLogicThread extends Thread
	{
		GameInstance gameInstance;
		
		public ServerLogicThread(GameInstance instance)
		{
			this.gameInstance = instance;
		}
		
		public void run()
		{
			currentTime = gameTimeline.getTime();
			while(true)
			{
				long newTime = gameTimeline.getTime();
				
				while(newTime > currentTime)
				{
					currentTime++;
					
					eventManager.handleEvents(currentTime);
					
					synchronized (gameObjectMap)
					{
						for (MovingObject moveObject : movingObjects.values())
						{
							if (!(moveObject instanceof PlayerObject))
							{
								moveObject.doPhysics(gameInstance);
							}
							else if (((PlayerObject) moveObject).isAlive())
							{
								((PlayerObject) moveObject).doPhysics(gameInstance);
							}
						}
					}
					
					queueEvent(new NullEvent(currentTime, instanceID), true);
				}
			}
		}
	}
	
	/**
	 * A thread which listens for incoming game client connections. Upon a
	 * successful connection, it then starts a GameClientHandler for that
	 * client, and adds it to the list of GameClientHandlers.
	 * 
	 * @author Jordan Neal
	 */
	private class ConnectionListener extends Thread
	{
		
		/** The ServerSocket object for accepting incoming connections. */
		ServerSocket serverSocket;
		
		/** Constructor tries to set up the ServerSocket */
		private ConnectionListener()
		{
			serverSocket = null;
			try
			{
				serverSocket = new ServerSocket(7734);
			}
			catch (IOException e)
			{
				if (DEBUG)
					System.err.println("IOException has occurred when trying to create the "
							+ "ServerSocket - no action taken");
			}
		}
		
		public void run()
		{
			if (serverSocket != null)
			{
				System.out.println("Server is now ready to receive clients.\r\n");
				
				// start accepting incoming connections
				while(true)
				{
					
					ClientHandler newClient = null;
					try
					{
						newClient = new ClientHandler(serverSocket.accept());
					}
					catch (IOException e)
					{
						if (DEBUG)
							System.err.println("IOException occurred while waiting for a connection");
					}
					
					if (newClient != null)
					{
						synchronized (clientList)
						{
							// TODO send new client message to other clients
							// ...
							
							clientList.add(newClient);
						}
						newClient.startThreads();
					}
					
				}
			}
			else
			{
				if (DEBUG)
					System.err.println("Error: Could not start connectionListener - "
							+ "serverSocket was NULL.");
			}
		}
	}
	
	/**
	 * The list of GameClientHandler threads. Should contain exactly one for
	 * each active game client connection.
	 */
	private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();
	
	/**
	 * A subclass which handles I/O between this game server and a single game
	 * client.
	 * 
	 * @author Jordan Neal
	 */
	private class ClientHandler extends NetworkHandler
	{
		private PlayerObject playerObject;
		
		private UUID clientInstanceID;
		
		/**
		 * disconnect from this client, and remove any gameObjects belonging to
		 * it from the game state
		 */
		@Override
		protected void disconnect()
		{
			super.disconnect();
			removeFromMap(playerObject);
			
			synchronized (clientList)
			{
				clientList.remove(this);
				
				for (ClientHandler client : clientList)
				{
					client.queueMessage(new ClientDisconnectMessage(playerObject.getParentInstanceID()));
				}
			}
		}
		
		class ServerInputThread extends NetworkInputThread
		{
			@Override
			protected void respondToMessage()
			{
				switch (incomingMessage.getMessageType())
				{
					case "GameEventMessage":
					{
						GameEvent incomingEvent = ((GameEventMessage) incomingMessage).getEvent();
						
						queueEvent(incomingEvent, true);
						
						break;
					}
					case "ClientDisconnectMessage":
					{
						UUID disconnectedClient = ((ClientDisconnectMessage) incomingMessage).getClientID();
						
						// TODO remove events belonging to that client?
						
						removeFromMap(playerObjects.get(disconnectedClient));
						
						for (ClientHandler client : clientList)
						{
							if (client.clientInstanceID.equals(disconnectedClient))
							{
								clientList.remove(client);
							}
							else
							{
								client.queueMessage(incomingMessage);
							}
						}
						
						break;
					}
					case "NewClientMessage":
					{
						
						break;
					}
					default:
						break;
				}
			}
		}
		
		public ClientHandler(Socket sock)
		{
			super(sock);
		}
		
		protected void createThreads()
		{
			outputThread = new NetworkOutputThread();
			inputThread = new ServerInputThread();
		}
		
		protected void socketSetup(Socket sock)
		{
			connectionSocket = sock;
			
			try
			{
				networkOutput = new ObjectOutputStream(this.connectionSocket.getOutputStream());
				networkInput = new ObjectInputStream(this.connectionSocket.getInputStream());
				
				connected = true;
			}
			catch (SocketException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		protected void initDataTransactions()
		{
			System.out.println("Accepted new client");
			
			try
			{
				networkOutput.writeLong(gameTimeline.getTime());
				
				// TODO send new client all events currently queued on server
				
				clientInstanceID = UUID.randomUUID();
				
				playerObject = createNewPlayer();
				playerObject.setParentInstanceID(clientInstanceID);
				networkOutput.writeObject(playerObject);
				
				for (ClientHandler client : clientList)
				{
					if (!client.clientInstanceID.equals(clientInstanceID))
					{
						client.queueMessage(new NewClientMessage(playerObject));
					}
				}
				
				networkOutput.writeObject(gameObjectMap);
				
				addToMap(playerObject);
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/** Performs initial setup of GameObjects for this server */
	private void setUpGameObjects()
	{
		synchronized (gameObjectMap)
		{
			
			// blocks
			addToMap(new Block(50, 100));
			addToMap(new Block(200, 100));
			addToMap(new Block(264, 187));
			for (int i = 1; i < 11; i++)
			{
				addToMap(new Block(i * 50, 300));
			}
			addToMap(new Block(50, 245));
			addToMap(new Block(500, 245));
			
			// deathZones
			for (int i = -2; i < 9; i++)
			{
				addToMap(new DeathZone(100 * i, 900));
				addToMap(new DeathZone(100 * i, -200));
				addToMap(new DeathZone(-200, i * 100));
				addToMap(new DeathZone(900, i * 100));
			}
			
			// spawnPoints
			addToMap(new SpawnPoint(204, 55));
			addToMap(new SpawnPoint(52, 40));
			addToMap(new SpawnPoint(260, 130));
			
			// moving platform
			addToMap(new HorizontalMovingBlock(400, 500));
			addToMap(new HorizontalMovingBlock(320, 187));
		}
	}
	
	@Override
	public void setup()
	{
		instanceID = UUID.randomUUID();
		
		setUpGameObjects();
		
		eventManager.registerHandler(new ServerEventHandler(), new String[ ] {
				"CollisionEvent", "InputEvent", "DeathEvent", "SpawnEvent" });
				
		gameTimeline = new Timeline(1000000000L / TARGET_FRAMERATE);
		
		ConnectionListener connectionListener = new ConnectionListener();
		connectionListener.start();
		serverLogicThread.start();
	}
	
	/** Runs this game server */
	public void runServer()
	{
		PApplet.main("engine.time.MedusaServer");
	}
	
	public static void main(String[] args)
	{
		MedusaServer gameServer = new MedusaServer();
		gameServer.runServer();
	}
}
