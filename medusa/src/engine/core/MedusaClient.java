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
import engine.objectModel.PlayerObject;
import engine.network.DataPattern;
import engine.network.NetworkHandler;
import engine.objectModel.GameObject;
import processing.core.PApplet;

/**
 * Game client for Medusa Engine
 * 
 * @author Jordan Neal
 */
public class MedusaClient extends GameInstance
{
	public static final boolean 	DEBUG = GameInstance.DEBUG;
	
	private static final int 		SERVER_PORT = MedusaServer.SERVER_PORT;
	
	/** This game client's PlayerObject */
	private PlayerObject playerObject;
	
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
	
	private GameLogicThread gameLogicThread = new GameLogicThread(this);
	
	/** The thread which handles the game logic loop
	 * 
	 * @author Jordan Neal
	 */
	private class GameLogicThread extends Thread
	{
		GameInstance client;
		public GameLogicThread(GameInstance client)
		{
			this.client = client;
		}
		
		public void run()
		{
			while(true) {
				playerObject.doPhysics(client);
				
				// TODO Get rid of this throttling after implementing proper
				// timing stuff
				try {
					Thread.sleep(16);
				} catch (InterruptedException e) {
					e.printStackTrace();
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
		if (key == CODED) {
			if (keyCode == LEFT) {
				playerObject.setLeftPressed(true);
			} else if (keyCode == RIGHT) {
				playerObject.setRightPressed(true);
			}
		} else if (key == ' ') {
			playerObject.setJumpPressed(true);
		}
	}
	
	/* Defines behavior to be run once when keys are released. (non-Javadoc)
	 * @see processing.core.PApplet#keyReleased()
	 */
	public void keyReleased()
	{
		if (key == CODED) {
			if (keyCode == LEFT) {
				playerObject.setLeftPressed(false);
			} else if (keyCode == RIGHT) {
				playerObject.setRightPressed(false);
			}
		} else if (key == ' ') {
			playerObject.setJumpPressed(false);
		}
	}
	
	@Override
	public void setup()
	{
		// Try to establish a connection to the server by instantiating a server handler
		gameServerHandler = new ServerHandler(new Socket());
		
		if (gameServerHandler.connected) {
			
			// if the connection was successful, start server handler's threads, and logic thread
			gameServerHandler.startThreads();
			gameLogicThread.start();
			
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
