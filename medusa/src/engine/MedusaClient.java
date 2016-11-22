package engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import engine.gameEvents.CollisionEvent;
import engine.gameEvents.DeathEvent;
import engine.gameEvents.GameEvent;
import engine.gameEvents.InputEvent;
import engine.gameEvents.NullEvent;
import engine.gameEvents.SpawnEvent;
import engine.gameEvents.eventManagement.EventHandler;
import engine.gameObjects.DeathZone;
import engine.gameObjects.GameObject;
import engine.gameObjects.PlayerObject;
import engine.gameObjects.objectClasses.Killable;
import engine.gameObjects.objectClasses.MovingObject;
import engine.gameObjects.objectClasses.Spawnable;
import engine.network.NetworkHandler;
import engine.network.messages.ClientDisconnectMessage;
import engine.network.messages.GameEventMessage;
import engine.network.messages.NetworkMessage;
import engine.network.messages.NewClientMessage;
import engine.time.Timeline;
import processing.core.PApplet;

/**
 * Game client for Medusa Engine
 * 
 * @author Jordan Neal
 */
public class MedusaClient extends GameInstance
{
	public static final boolean DEBUG = GameInstance.DEBUG;
	
	/** This game client's PlayerObject */
	private PlayerObject playerObject;
	
	private class ClientEventHandler implements EventHandler
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
				default:
					break;
			}
		}
		
		private void handle(CollisionEvent e)
		{
			GameObject[] objects = new GameObject[2];
			
			objects[0] = objectMap.getObject(e.getIDs()[0]);
			objects[1] = objectMap.getObject(e.getIDs()[1]);
			
			if ((objects[0] instanceof PlayerObject)
					&& (objects[1] instanceof DeathZone) /*&& !replayManager.playing*/)
			{
				queueEvent(new DeathEvent(e, e.getTimeStamp()
						+ 1, getInstanceID(), objects[0].getID()), false);
			}
			else if ((objects[1] instanceof PlayerObject)
					&& (objects[0] instanceof DeathZone) /*&& !replayManager.playing*/)
			{
				queueEvent(new DeathEvent(e, e.getTimeStamp()
						+ 1, getInstanceID(), objects[1].getID()), false);
			}
		}
		
		private void handle(InputEvent e)
		{
			PlayerObject p = objectMap.getPlayerObject(e.getInstanceID());
			
			if (p != null)
			{
//				if (e.getPlayer() != null)
//				{
//					p.x = e.getPlayer().x;
//					p.y = e.getPlayer().y;
//				}
				
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
			GameObject object = objectMap.getObject(e.getObjectID());
			
			if (object instanceof Killable)
			{
				((Killable) object).kill();
				removeFromMap(object);
				
				if (object instanceof PlayerObject /*&& !replayManager.playing*/)
					queueEvent(new SpawnEvent(e, e.getTimeStamp()
							+ PlayerObject.DEFAULT_RESPAWN, getInstanceID(), object), false);
			}
		}
		
		private void handle(SpawnEvent e)
		{
			GameObject object = e.getObject();
			
			if (object instanceof Spawnable)
			{
				((Spawnable) object).spawn();
				
				if (!objectMap.contains(object))
				{
					addToMap(object);
				}
			}
		}
	}
	
	@Override
	public void queueEvent(GameEvent e, boolean propagate)
	{
		//System.out.println(e);
		
		eventManager.queueEvent(e);
		
		if (propagate)
			serverHandler.queueMessage(new GameEventMessage(e));
	}
	
	private ClientLogicThread clientLogicThread = new ClientLogicThread(this);
	
	/**
	 * The thread which handles the game logic loop
	 * 
	 * @author Jordan Neal
	 */
	private class ClientLogicThread extends Thread
	{
		GameInstance gameInstance;
		
		public ClientLogicThread(GameInstance instance)
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
					queueEvent(new NullEvent(currentTime, instanceID), true);
					currentTime++;
					
					//System.out.println("TIME: " + gameTimeline.getTime());
					eventManager.handleEvents(currentTime);
					
//					synchronized (gameObjectMap)
//					{
						for (GameObject moveObject : objectMap.getObjectsOfClass(MovingObject.class))
						{
							if (!(moveObject instanceof PlayerObject))
							{
								((MovingObject) moveObject).doPhysics(gameInstance);
							}
							else if (((PlayerObject) moveObject).isAlive())
							{
								((PlayerObject) moveObject).doPhysics(gameInstance);
							}
						}
//					}
				}
			}
		}
	}
	
	private ServerHandler serverHandler;
	
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
		
		class ClientInputThread extends NetworkInputThread
		{
			@Override
			protected void respondToMessage()
			{
				switch (incomingMessage.getMessageType())
				{
					case "GameEventMessage":
					{
						GameEvent incomingEvent = ((GameEventMessage) incomingMessage).getEvent();
						
						queueEvent(incomingEvent, false);
						
						break;
					}
					case "ClientDisconnectMessage":
					{
						UUID disconnectedClient = ((ClientDisconnectMessage) incomingMessage).getClientID();
						
						eventManager.removeQueue(disconnectedClient);
						
						removeFromMap(objectMap.getPlayerObject(disconnectedClient));
						
						break;
					}
					case "NewClientMessage":
					{
						PlayerObject newPlayer = ((NewClientMessage) incomingMessage).getPlayer();
						
						addToMap(newPlayer);
						
						break;
					}
					default:
						break;
				}
			}
			
		}
		
		public ServerHandler(Socket sock)
		{
			super(sock);
		}
		
		protected void createThreads()
		{
			outputThread = new NetworkOutputThread();
			inputThread = new ClientInputThread();
		}
		
		protected void socketSetup(Socket sock)
		{
			if (!GameInstance.DEBUG)
			{
				System.out.print("Enter the server's IP address: ");
				Scanner scanner = new Scanner(System.in);
				serverHostname = scanner.next();
				scanner.close();
			}
			else
			{
				System.out.println("DEBUG IS ON: AUTOMATICALLY CONNECTING TO SERVER ON LOCALHOST");
				serverHostname = "localhost";
			}
			
			try
			{
				connectionSocket = new Socket(serverHostname, SERVER_PORT);
				
				networkOutput = new ObjectOutputStream(this.connectionSocket.getOutputStream());
				networkInput = new ObjectInputStream(this.connectionSocket.getInputStream());
				
				System.out.println("Successfully established connection to the server.");
				connected = true;
			}
			catch (UnknownHostException e)
			{
				System.err.println("UnknownHostException occurred when trying to connect to server"
						+ " - aborting & shutting down");
			}
			catch (IOException e)
			{
				System.err.println("IOException occurred when trying to connect to server"
						+ " - aborting & shutting down");
			}
		}
		
		@SuppressWarnings("unchecked")
		protected void initDataTransactions()
		{
			try
			{
				PriorityBlockingQueue<GameEvent> events = (PriorityBlockingQueue<GameEvent>) networkInput.readObject();
				
				// long serverGVT = networkInput.readLong();
				
				gameTimeline = new Timeline(networkInput.readLong(), 1000000000L
						/ TARGET_FRAMERATE);
						
				playerObject = (PlayerObject) networkInput.readObject();
				instanceID = playerObject.getParentInstanceID();
				
				for (GameEvent e : events)
				{
					queueEvent(e, false);
					// eventManager.queueEvent(new NullEvent(e.getTimeStamp(),
					// instanceID));
				}
				
				eventManager.registerHandler(new ClientEventHandler(), new String[ ] {
						"CollisionEvent", "InputEvent", "DeathEvent", "SpawnEvent" });
				
//				while (eventManager.getGVT() < serverGVT)
//				{
//					queueEvent(new NullEvent(eventManager.getGVT() + 1, instanceID), false);
//				}
				
				HashMap<UUID, GameObject> g = (HashMap<UUID, GameObject>) networkInput.readObject();
				
				for (GameObject o : g.values())
				{
					addToMap(o);
				}
				
				addToMap(playerObject);
			}
			catch (ClassNotFoundException e)
			{
				System.err.println("ClassNotFoundException occurred while performing initial"
						+ " communication with server - disconnecting & shutting down");
				disconnect();
			}
			catch (IOException e)
			{
				System.err.println("IOException occurred while performing initial communcation"
						+ " with server - disconnecting & shutting down");
			}
		}
		
	}
	
	void sendMessageToServer(NetworkMessage message)
	{
		serverHandler.queueMessage(message);
	}
	
	/*
	 * Defines behavior to be run once when keys are pressed. May run repeatedly
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
			queueEvent(new InputEvent(gameTimeline.getTime() + 1, getInstanceID(), inputString, playerObject), true);
	}
	
	/*
	 * Defines behavior to be run once when keys are released. (non-Javadoc)
	 * @see processing.core.PApplet#keyReleased()
	 */
	public void keyReleased()
	{
		String inputString = "";
		
		if (key == CODED)
		{
			if (keyCode == LEFT)
			{
				inputString = "LEFT RELEASED";
			}
			else if (keyCode == RIGHT)
			{
				inputString = "RIGHT RELEASED";
			}
		}
		else if (key == ' ')
		{
			inputString = "JUMP RELEASED";
		}
		
		if (!inputString.equals(""))
			queueEvent(new InputEvent(gameTimeline.getTime() + 1, getInstanceID(), inputString, playerObject), true);
	}
	
	@Override
	public void setup()
	{	
		// Try to establish a connection to the server by instantiating a server
		// handler
		serverHandler = new ServerHandler(new Socket());
		
		if (serverHandler.connected)
		{
			// if the connection was successful, start server handler's threads,
			// and logic thread
			serverHandler.startThreads();
			clientLogicThread.start();
		}
		else
		{
			// if the connection was not successful, print a failure message
			// before exiting
			System.out.println("Connection to server failed.");
		}
	}
	
	public void runClient()
	{
		PApplet.main("engine.MedusaClient");
	}
	
	public static void main(String[] args)
	{
		MedusaClient medusaClient = new MedusaClient();
		medusaClient.runClient();
	}
}
