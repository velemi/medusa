package engine.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import engine.gameObjects.Block;
import engine.gameObjects.DeathZone;
import engine.gameObjects.GameObject;
import engine.gameObjects.HorizontalMovingBlock;
import engine.gameObjects.MovingObject;
import engine.gameObjects.PlayerObject;
import engine.gameObjects.SpawnPoint;
import engine.network.DataPattern;
import engine.network.NetworkHandler;
import processing.core.PApplet;

/**
 * Game server for Medusa Engine
 * 
 * @author Jordan Neal
 */
public class MedusaServer extends GameInstance
{
	public static final boolean DEBUG = GameInstance.DEBUG;
	
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
			
			try {
				serverSocket = new ServerSocket(7734);
			} catch (IOException e) {
				if(DEBUG)
					System.err.println("IOException has occurred when trying to create the "
							+ "ServerSocket - no action taken");
			}
		}
		
		public void run()
		{
			if (serverSocket != null) {
				System.out.println("Server is now ready to receive clients.\r\n");
				
				// start accepting incoming connections
				while(true) {
					
					ClientHandler newClient = null;
					try {
						newClient = new ClientHandler(serverSocket.accept());
					} catch (IOException e) {
						if(DEBUG)
							System.err.println("IOException occurred while waiting for a connection");
					}	
					
					if (newClient != null) {
						synchronized (clientList) {
							clientList.add(newClient);
						}
						newClient.startThreads();
					}
					
				}
			} else {
				if(DEBUG)
					System.err.println("Error: Could not start connectionListener - "
						+ "serverSocket was NULL.");
			}
		}
	}
	
	/**
	 * The list of GameClientHandler threads. Should contain exactly one for each active
	 * game client connection.
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
		
		/**
		 * disconnect from this client, and remove any gameObjects belonging to
		 * it from the game state
		 */
		@Override
		protected void disconnect()
		{
			super.disconnect();
			gameObjectMap.remove(playerObject.getID());
			
			synchronized(clientList) {
				clientList.remove(this);
				
				for (ClientHandler client : clientList) {
					client.outputThread.outgoingPattern = DataPattern.GAME_OBJECTS_UPDATE_ALL;
				}
			}
		}
		
		class ServerOutputThread extends NetworkOutputThread
		{
			ConcurrentHashMap<UUID, GameObject> g = new ConcurrentHashMap<UUID, GameObject>();
			
			public ServerOutputThread()
			{
				outgoingPattern = DataPattern.GAME_OBJECTS_UPDATE;
			}
			
			protected void sendData()
			{
				switch (outgoingPattern) {
					case GAME_OBJECTS_UPDATE :
						sendUpdatedObjects();
						break;
					case GAME_OBJECTS_UPDATE_ALL :
						sendAllObjects();
						break;
					default:
						break;
				}
			}
			
			/**
			 * Sends a map containing a copy of all updated objects to the client
			 */
			void sendUpdatedObjects()
			{
				// TODO - currently just sends anything that implements MovingObject, fix that
				
				g.clear();
				synchronized (gameObjectMap) {
					for (Map.Entry<UUID, GameObject> entry : gameObjectMap.entrySet()) {
						GameObject value = entry.getValue();
						
						if(value instanceof MovingObject) {
							
							UUID key = value.getID();
							
							g.put(key, value);
						}
					}
				}
				
				try {
					networkOutput.reset();
					//System.out.println("about to write g: "+ g);
					networkOutput.writeObject(g);
				} catch (EOFException e) {
					if (DEBUG)
						System.err.println("EOFException has occurred when trying to write updated objects"
								+ " to the client - disconnecting");
					disconnect();
				} catch (SocketException e) {
					if (DEBUG)
						System.err.println("SocketException has occurred when trying to write updated objects"
								+ " to the client - disconnecting");
					disconnect();
				} catch (IOException e) {
					if (DEBUG)
						System.err.println("IOException has occurred when trying to write updated objects"
								+ " to the client - disconnecting");
					disconnect();
				}
			}
			
			/**
			 * Sends a map containing a copy of all objects to the client
			 */
			void sendAllObjects()
			{
				// Clear the copy map
				g.clear();
				
				// Put all elements from gameObjectMap into the copy map
				g.putAll(gameObjectMap);
				
				// TODO - remove this once proper waiting for outgoing messages implemented
				outgoingPattern = DataPattern.GAME_OBJECTS_UPDATE;
				
				try {
					// Reset the output stream
					networkOutput.reset();
					
					// Write the copy map to the client
					networkOutput.writeObject(g);
					
				} catch (EOFException e) {
					if (DEBUG)
						System.err.println("EOFException has occurred when trying to write all objects"
								+ " to the client - disconnecting");
					disconnect();
				} catch (SocketException e) {
					if (DEBUG)
						System.err.println("SocketException has occurred when trying to write all objects"
								+ " to the client - disconnecting");
					disconnect();
				} catch (IOException e) {
					if (DEBUG)
						System.err.println("IOException has occurred when trying to write all objects"
								+ " to the client - disconnecting");
					disconnect();
				}
			}
		}
		
		class ServerInputThread extends NetworkInputThread
		{
			PlayerObject p = null;

			@Override
			protected void receiveData()
			{
				switch (incomingPattern) {
					case PLAYER_OBJECT_UPDATE :
						receivePlayerObject();
						break;
					default:
						break;
				}
			}

			private void receivePlayerObject()
			{
				try {
					p = (PlayerObject) networkInput.readObject();
					
					synchronized (playerObject) {
						playerObject.becomeCopyOf(p);
					}
				} catch (ClassNotFoundException e) {
					if(DEBUG)
						System.err.println("ClassNotFoundException occurred when trying to read from the"
							+ " client - disconnecting");
					disconnect();
				} catch (SocketException e) {
					if(DEBUG)
						System.err.println("SocketException occurred when trying to read from the"
							+ " client - disconnecting");
					disconnect();
				} catch (IOException e) {
					if(DEBUG)
						System.err.println("IOException occurred when trying to read from the"
							+ " client - disconnecting");
					disconnect();
				}
			}
		}
		
		protected ClientHandler(Socket sock)
		{
			super(sock);
		}
		
		protected void createThreads()
		{
			outputThread = new ServerOutputThread();
			inputThread = new ServerInputThread();
		}
		
		protected void socketSetup(Socket sock)
		{
			connectionSocket = sock;
			
			try {
				networkOutput = new ObjectOutputStream(this.connectionSocket.getOutputStream());
				networkInput = new ObjectInputStream(this.connectionSocket.getInputStream());
				
				connected = true;
			} catch(SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		protected void initDataTransactions()
		{
			System.out.println("Accepted new client");
			
			try {
				
				playerObject = createNewPlayer();
				networkOutput.writeObject(playerObject);
				networkOutput.writeObject(gameObjectMap);
				
				gameObjectMap.put(playerObject.getID(), playerObject);
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	
	/** Performs initial setup of GameObjects for this server */
	private void setUpGameObjects()
	{
		synchronized (gameObjectMap) {
			
			// blocks
			addToMap(new Block(50, 100));
			addToMap(new Block(200, 100));
			addToMap(new Block(264, 187));
			for (int i = 1; i < 11; i++) {
				addToMap(new Block(i * 50, 300));
			}
			addToMap(new Block(50, 245));
			addToMap(new Block(500, 245));
			
			// deathZones
			for (int i = -2; i < 9; i++) {
				addToMap(new DeathZone(100 * i, 900));
				addToMap(new DeathZone(100 * i, -200));
				addToMap(new DeathZone(-200, i * 100));
				addToMap(new DeathZone(900, i * 100));
			}
			
			// spawnPoints
			addToMap(new SpawnPoint(204, 55));
			addToMap(new SpawnPoint(52, 40));
			addToMap(new SpawnPoint(260, 130));
			
			//moving platform
			addToMap(new HorizontalMovingBlock(400,500));
			addToMap(new HorizontalMovingBlock(320, 187));
		}
	}
	
	private ServerLogicThread serverLogicThread = new ServerLogicThread(this);
	
	/** The thread which handles the game logic loop
	 * 
	 * @author Jordan Neal
	 */
	private class ServerLogicThread extends Thread
	{
		GameInstance client;
		public ServerLogicThread(GameInstance client)
		{
			this.client = client;
		}
		
		public void run()
		{
			while(true) {
				
				synchronized (gameObjectMap) {
					for (Map.Entry<UUID, GameObject> entry : gameObjectMap.entrySet()) {
						GameObject object = entry.getValue();
						
						if ((object instanceof MovingObject)
								&& !(object instanceof PlayerObject)) {
							((MovingObject) object).doPhysics(client);
						}
					}
				}
				// TODO Get rid of this throttling after implementing proper
				// timing stuff
				try {
					Thread.sleep(16);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
//				System.out.println(System.nanoTime());
			}
		}
	}
	
	@Override
	public void setup()
	{
		setUpGameObjects();
		
		gameTimeline = new Timeline(1000000L / TARGET_FRAMERATE);
		
		ConnectionListener connectionListener = new ConnectionListener();
		connectionListener.start();
		serverLogicThread.start();
	}
	
	/** Runs this game server */
	public void runServer()
	{
		PApplet.main("engine.core.MedusaServer");
	}
	
	public static void main(String[] args)
	{
		MedusaServer gameServer = new MedusaServer();
		
		gameServer.runServer();
	}
	
}
