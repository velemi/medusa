package engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.UUID;
import engine.gameEvents.DespawnEvent;
import engine.gameEvents.GameEvent;
import engine.gameEvents.InputEvent;
import engine.gameEvents.NullEvent;
import engine.gameEvents.SpawnEvent;
import engine.gameObjects.PlayerObject;
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
	
	private ServerLogicThread serverLogicThread = new ServerLogicThread();

	/**
	 * The list of GameClientHandler threads. Should contain exactly one for
	 * each active game client connection.
	 */
	private ArrayList<ClientHandler> clientList = new ArrayList<ClientHandler>();

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
					if (!client.clientInstanceID.equals(e.getInstanceID()))
					{
						client.queueMessage(new GameEventMessage(e));
					}
				}
			}
		}
		
		exeLock.readLock().unlock();
	}
	
	private void selectGame()
	{
		Scanner in = new Scanner(System.in);
		
		while(gameTitle.equals(""))
		{
			System.out.println("Select the game for this server to run:");
			System.out.println("1: Platform     (Any number of players)");
			System.out.println("2: Invaders!    (Intended for one player)");
			
			System.out.print("\nSelection: ");
			
			String inString = in.nextLine();
			
			int select = -1;
			
			try
			{
				select = Integer.parseInt(inString);
			}
			catch (NumberFormatException e)
			{
				
			}
			
			if (select == 1)
				setGameTitle("platform");
			else if (select == 2)
				setGameTitle("invaders");
			
			if (gameTitle.equals(""))
				System.out.println("\nPlease make a valid selection.\n");
		}
		
		in.close();
	}

	/** Performs initial setup of GameObjects for this server */
	private void setUpGameObjects()
	{
		ScriptManager.lock();
		ScriptManager.bindArgument("instance", thisInstance);
		
		if (getGameTitle().equals("platform"))
			ScriptManager.loadScript("src/scripts/platformer/gameObject_setup.js");
		else if (getGameTitle().equals("invaders"))
			ScriptManager.loadScript("src/scripts/invaders/gameObject_setup.js");
		
		ScriptManager.clearBindings();
		ScriptManager.unlock();
	}

	@Override
	public void setup()
	{
		selectGame();
		
		System.out.println("\nNow starting game \"" + getGameTitle()
				+ "\"...\n\n");
		
		instanceID = UUID.randomUUID();
		
		setUpGameObjects();
		
		eventManager.registerHandler(new CoreEventHandler(), new String[ ] {
				"CollisionEvent", "InputEvent", "DeathEvent", "SpawnEvent",
				"DespawnEvent" });
		
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
	@SuppressWarnings("unused")
	public void keyPressed()
	{
		long curTime = gameTimeline.getTime();
		
		if (inputTime < curTime)
		{
			inputTime = curTime;
			
			inputCount = 0;
		}
		
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
			else if ((key == 'B' || key == 'b') && DEBUG)
			{
				displayLogs();
			}
			
			if (!inputString.equals(""))
			{
				queueEvent(new InputEvent(inputTime + 1, inputCount,
						getInstanceID(), inputString, null), true);
				
				inputLog.add(getInstanceID() + ", ts=" + (inputTime + 1) + ", count=" + inputCount
						+ ": " + inputString + "@t=" + inputTime);
				
				inputCount++;
			}
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

	/**
	 * The thread which handles the game logic loop
	 * 
	 * @author Jordan Neal
	 */
	private class ServerLogicThread extends CoreLogicThread
	{
		
		public ServerLogicThread()
		{
			this.setName("Server Logic Thread");
		}
		
		public void run()
		{
			System.out.println("Server's id is: " + instanceID);
			
			currentTime = gameTimeline.getTime();
			
			super.run();
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
				networkOutput.writeObject(getGameTitle());
				
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
}
