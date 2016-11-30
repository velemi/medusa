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
import engine.gameEvents.DespawnEvent;
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
import engine.gameObjects.objectClasses.PhysicsObject;
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
			//System.out.println(e.getEventType());
			
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
				case "DespawnEvent":
				{
					handle((DespawnEvent) e);
					break;
				}
				default:
					break;
			}
		}
		
		private void handle(CollisionEvent e)
		{
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			ScriptManager.loadScript("scripts/platformer/collisionEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
		}
		
		private void handle(InputEvent e)
		{
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("replayManager", replayManager);
			
			ScriptManager.loadScript("scripts/platformer/inputEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
		}
		
		private void handle(DeathEvent e)
		{
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			ScriptManager.loadScript("scripts/platformer/deathEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
		}
		
		private void handle(SpawnEvent e)
		{
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			ScriptManager.loadScript("scripts/platformer/spawnEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
		}
		
		private void handle(DespawnEvent e)
		{
			ScriptManager.bindArgument("objectMap", objectMap);
			ScriptManager.bindArgument("e", e);
			ScriptManager.bindArgument("instance", thisInstance);
			
			ScriptManager.loadScript("scripts/platformer/despawnEvent_handling.js");
			
			ScriptManager.invokeFunction("handle", false);
			
			ScriptManager.clearBindings();
		}
	}
	
	@Override
	public void queueEvent(GameEvent e, boolean propagate)
	{
		exeLock.readLock().lock();
		
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
		
		exeLock.readLock().unlock();
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
			this.setName("Server Logic Thread");
			this.gameInstance = instance;
		}
		
		public void run()
		{
			System.out.println("Server's id is: " + instanceID);
			
			currentTime = gameTimeline.getTime();
			while(true)
			{
				long newTime = gameTimeline.getTime();
				
				while(newTime > currentTime)
				{
					exeLock.readLock().lock();
					
					NullEvent n = new NullEvent(currentTime, instanceID);
					queueEvent(n, true);
					
					exeLock.readLock().unlock();
					
					boolean handled = false;
					
					while (!handled)
					{
						exeLock.readLock().lock();
						
						handled = eventManager.handleEvents(currentTime);
						
						exeLock.readLock().unlock();
					}
					
					exeLock.readLock().lock();
					
					currentTime++;
					
					for (GameObject moveObject : objectMap.getObjectsOfClass(PhysicsObject.class))
					{
						if (!(moveObject instanceof PlayerObject))
						{
							((PhysicsObject) moveObject).doPhysics(gameInstance);
						}
						else if (((PlayerObject) moveObject).isAlive())
						{
							((PlayerObject) moveObject).doPhysics(gameInstance);
						}
					}
					
					exeLock.readLock().unlock();
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
			this.setName("Connection Listener");
			
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
			
			//removeFromMap(playerObject);
			eventManager.queueEvent(new DespawnEvent(currentTime, instanceID, playerObject));
			
			eventManager.removeQueue(clientInstanceID);
			
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
				exeLock.readLock().lock();
				
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
						
						eventManager.removeQueue(disconnectedClient);
						eventManager.queueEvent(new DespawnEvent(currentTime, instanceID, 
								objectMap.getPlayerObject(disconnectedClient)));
						
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
				
				exeLock.readLock().unlock();
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
		
		private void initClientTimeline() throws IOException
		{
			networkOutput.writeLong(gameTimeline.getTime());
		}
		
		private void initClientObjects() throws IOException
		{
			clientInstanceID = UUID.randomUUID();
			
			playerObject = createNewPlayer();
			playerObject.setParentInstanceID(clientInstanceID);
			networkOutput.writeObject(playerObject);
			
			networkOutput.writeObject(objectMap.getFullMap());
			eventManager.queueEvent(new SpawnEvent(currentTime, instanceID, playerObject));
		}
		
		private void initClientEvents() throws IOException
		{
			eventManager.addQueue(clientInstanceID);
			NullEvent n = new NullEvent(currentTime, clientInstanceID);
			
			eventManager.queueEvent(n);
			
			networkOutput.writeObject(eventManager.getQueues());
			networkOutput.writeLong(eventManager.getGVT());
		}
		
		protected void initDataTransactions()
		{
			exeLock.writeLock().lock();
			
			System.out.println("Accepted new client");
			
			try
			{
				initClientTimeline();
				
				initClientObjects();
				
				initClientEvents();
				
				for (ClientHandler client : clientList)
				{
					if (!client.clientInstanceID.equals(clientInstanceID))
					{
						client.queueMessage(new NewClientMessage(playerObject));
					}
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			exeLock.writeLock().unlock();
		}
	}
	
	/** Performs initial setup of GameObjects for this server */
	private void setUpGameObjects()
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
		addToMap(new HorizontalMovingBlock(100, 500));
	}
	
	@Override
	public void setup()
	{
		instanceID = UUID.randomUUID();
		
		setUpGameObjects();
		
		eventManager.registerHandler(new ServerEventHandler(), new String[ ] {
				"CollisionEvent", "InputEvent", "DeathEvent", "SpawnEvent", "DespawnEvent" });
		
		eventManager.addQueue(instanceID);
		eventManager.setGVT(0);
				
		gameTimeline = new Timeline(1000000000L / TARGET_FRAMERATE);
		
		ConnectionListener connectionListener = new ConnectionListener();
		connectionListener.start();
		serverLogicThread.start();
	}
	
	/*
	 * Defines behavior to be run once when keys are pressed. May run repeatedly
	 * (after system-dependent delay) if keys are held. (non-Javadoc)
	 * @see processing.core.PApplet#keyPressed()
	 */
	public void keyPressed()
	{
		if (!replayManager.isPlaying())
		{
			String inputString = "";
			if (key == 'R' || key == 'r')
			{
				if (!replayManager.isRecording())
				{
					inputString = "START RECORD";
				}
				else
				{
					inputString = "STOP RECORD";
				}
			}
			else if (key == 'K' || key == 'k')
			{
				inputString = "PLAYBACK60";
			}
			else if (key == 'J' || key == 'j')
			{
				inputString = "PLAYBACK30";
			}
			else if (key == 'L' || key == 'l')
			{
				inputString = "PLAYBACK120";
			}
			
			if (!inputString.equals(""))
				queueEvent(new InputEvent(gameTimeline.getTime() + 1, getInstanceID(), inputString, null), true);
		}
	}
	
	/** Runs this game server */
	public void runServer()
	{
		PApplet.main("engine.MedusaServer");
	}
	
	public static void main(String[] args)
	{
		MedusaServer gameServer = new MedusaServer();
		gameServer.runServer();
	}
}
