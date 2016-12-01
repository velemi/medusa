package engine;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import engine.gameEvents.DespawnEvent;
import engine.gameEvents.GameEvent;
import engine.gameEvents.InputEvent;
import engine.gameEvents.SpawnEvent;
import engine.gameEvents.eventManagement.EventQueue;
import engine.gameObjects.GameObject;
import engine.gameObjects.PlayerObject;
import engine.network.NetworkHandler;
import engine.network.messages.ClientDisconnectMessage;
import engine.network.messages.GameEventMessage;
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
	
	private PlayerObject playerObject;
	
	private ClientLogicThread clientLogicThread = new ClientLogicThread();

	private ServerHandler serverHandler;

	@Override
	public void queueEvent(GameEvent e, boolean propagate)
	{
		//System.out.println(e);
		
		eventManager.queueEvent(e);
		
		if (propagate)
			serverHandler.queueMessage(new GameEventMessage(e));
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
			else if ((key == 'B' || key == 'b') && DEBUG)
			{
				displayLogs();
			}
			
			if (!inputString.equals(""))
			{
				queueEvent(new InputEvent(inputTime + 1, inputCount,
						getInstanceID(), inputString, playerObject), true);
				
				inputLog.add(getInstanceID() + ", ts=" + (inputTime + 1) + ", count=" + inputCount
						+ ": " + inputString + "@t=" + inputTime);
				
				inputCount++;
			}
				
		}
	}
	
	public void keyReleased()
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
			{
				queueEvent(new InputEvent(inputTime + 1, inputCount,
						getInstanceID(), inputString, playerObject), true);
				
				inputLog.add(getInstanceID() + ", ts=" + (inputTime + 1) + ", count=" + inputCount
						+ ": " + inputString + "@t=" + inputTime);
				
				inputCount++;
			}
				
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

	
	private class ClientLogicThread extends CoreLogicThread
	{
		
		public ClientLogicThread()
		{
			this.setName("Client Logic Thread");
		}
		
		public void run()
		{
			System.out.println("Client's id is: " + instanceID);
			
			super.run();
		}
	}

	class ServerHandler extends NetworkHandler
	{
		String serverHostname;
		
		public ServerHandler(Socket sock)
		{
			super(sock);
		}
	
		@Override
		protected void disconnect()
		{
			super.disconnect();
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
		
		protected void initDataTransactions()
		{
			try
			{
				setGameTitle((String) networkInput.readObject());
				
				initTimeline();
						
				initObjects();
				
				initEvents();
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
	
		private void initTimeline() throws IOException
		{
			//System.out.println("time");
			
			gameTimeline = new Timeline(networkInput.readLong(), 1000000000L
					/ TARGET_FRAMERATE);
		}
		
		private void initObjects() throws ClassNotFoundException, IOException
		{
			//System.out.println("objects");
			
			playerObject = (PlayerObject) networkInput.readObject();
			instanceID = playerObject.getParentInstanceID();
			
			@SuppressWarnings("unchecked")
			HashMap<UUID, GameObject> g = (HashMap<UUID, GameObject>) networkInput.readObject();
			
			for (GameObject o : g.values())
			{
				addToMap(o);
			}
			
			addToMap(playerObject);
		}
		
		private void initEvents() throws IOException, ClassNotFoundException
		{
			//System.out.println("events");
			
			@SuppressWarnings("unchecked")
			ConcurrentHashMap<UUID, EventQueue> queues = 
					(ConcurrentHashMap<UUID, EventQueue>) networkInput.readObject();
			
			long serverGVT = networkInput.readLong();
			
			for (EventQueue q : queues.values())
			{
				eventManager.addQueue(q.getInstanceID());
				
				while (!q.isEmpty())
				{
					eventManager.queueEvent(q.poll());
				}
			}
			
			eventManager.registerHandler(new CoreEventHandler(), new String[ ] {
					"CollisionEvent", "InputEvent", "DeathEvent", "SpawnEvent", "DespawnEvent" });
			
			eventManager.setGVT(serverGVT);
			currentTime = serverGVT;
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
						
						if (incomingEvent instanceof InputEvent)
							System.out.println("got inputEvent from: " + incomingEvent.getInstanceID());
						
						queueEvent(incomingEvent, false);
						
						break;
					}
					case "ClientDisconnectMessage":
					{
						UUID disconnectedClient = ((ClientDisconnectMessage) incomingMessage).getClientID();
						
						eventManager.removeQueue(disconnectedClient);
						
						//removeFromMap(objectMap.getPlayerObject(disconnectedClient));
						eventManager.queueEvent(new DespawnEvent(currentTime, instanceID, 
								objectMap.getPlayerObject(disconnectedClient)));
						
						break;
					}
					case "NewClientMessage":
					{
						PlayerObject newPlayer = ((NewClientMessage) incomingMessage).getPlayer();
						
						eventManager.addQueue(newPlayer.getParentInstanceID());
						
						//addToMap(newPlayer);
						eventManager.queueEvent(new SpawnEvent(currentTime, instanceID, playerObject));
						
						break;
					}
					default:
						break;
				}
			}
			
		}
		
	}
}
