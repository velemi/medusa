package engine.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class ConnectionHandler
{
	protected Socket connectionSocket;
	protected ObjectOutputStream networkOutput;
	protected ObjectInputStream networkInput;
	boolean connected;
	
//	ConcurrentHashMap<UUID,GameObject> objectMap;
//	PlayerObject playerObject;
//	
//	ConcurrentLinkedQueue<GameObject> updatedObjects = 
//			new ConcurrentLinkedQueue<GameObject>();
	
//	ConcurrentLinkedQueue<DATA_PATTERN> messageQueue = 
//			new ConcurrentLinkedQueue<DATA_PATTERN>();
	
	/**
	 * A semaphore used to control when the output thread runs. Should have
	 * a value equal to the number of messages in the outMessageQueue
	 */
	//Semaphore outputLock = new Semaphore(0);
	
//	protected OutputThread outputThread = new OutputThread();
//	protected InputThread inputThread = new InputThread();
	
	/**
	 * disconnect from this client, and remove any gameObjects belonging to
	 * it from the game state
	 */
	abstract void disconnect();
	
//	abstract void triggerUpdate(GameObject updated);
	
//	public void queueMessage(DATA_PATTERN message)
//	{
//		messageQueue.add(message);
//	}
	
//	class OutputThread extends Thread
//	{
//		DATA_PATTERN currentMessage;
//		
//		void outputLoop()
//		{
//			try {
//				outputLock.acquire();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			currentMessage = messageQueue.poll();
//			if (currentMessage == null) {
//				// something bad happened, had more locks than messages
//			}
//			
//			try {
//				networkOutput.reset();
//				networkOutput.writeObject(currentMessage);
//			} catch (EOFException e) {
//				disconnect();
//			} catch (SocketException e) {
//				disconnect();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			switch (currentMessage) {
//				case GAME_OBJECTS_UPDATE:
//					sendUpdatedObjects();
//					break;
//				default:
//					break;
//			}
//		}
//		
//		public void run()
//		{
//			while(connected) {
//				outputLoop();
//			}
//		}
//		
//		private void sendUpdatedObjects()
//		{
//			// send message/objects
//			try {
//				networkOutput.reset();
//				networkOutput.writeObject(updatedObjects);
//			} catch (SocketException e) {
//				disconnect();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
//	class InputThread extends Thread
//	{
//		DATA_PATTERN currentMessage;
//		
//		PlayerObject p = new PlayerObject();
//		
//		private void inputLoop()
//		{
//			try {
//				currentMessage = (DATA_PATTERN) networkInput.readObject();
//			} catch (EOFException e) {
//				disconnect();
//			} catch (SocketException e) {
//				disconnect();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			}
//			
//			if (currentMessage != null) {
//				switch (currentMessage) {
//					case GAME_OBJECTS_UPDATE:
//						receiveObjects();
//						break;
//					default:
//						break;
//				}
//			}
//		}
//		
//		public void run()
//		{
//			while(connected) {
//				inputLoop();
//				
//			}
//		}
//		
//		@SuppressWarnings("unchecked")
//		private void receiveObjects()
//		{
//			ConcurrentLinkedQueue<GameObject> updatedItems = 
//					new ConcurrentLinkedQueue<GameObject>();
//			
//			try {
//				updatedItems = (ConcurrentLinkedQueue<GameObject>) networkInput.readObject();
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			} catch (SocketException e) {
//				disconnect();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			
//			//Apply/store the updated information
//			while (updatedItems != null && !updatedItems.isEmpty()){
//				GameObject currentItem = updatedItems.poll();
//				
//				if(currentItem != null)
//				{
//					KeySetView<UUID,GameObject> keySet = objectMap.keySet();
//					UUID key = currentItem.getID();
//					
//					if(keySet.contains(key)) 
//					{
//						GameObject old = objectMap.get(key);
//						old.becomeCopyOf(currentItem);
//					}
//					else
//					{
//						objectMap.put(key, currentItem);
//					}
//					
//					// If server, send it back out to all the clients
////					if (mode == 2 && currentMessage != ClientServerMessage.GAME_OBJECTS_NO_PROP)
////						triggerUpdate(currentItem);
//				}
//			}
//		}
//	}
	
	/**
	 * Constructor which sets up I/O streams and performs initial
	 * communications for handling this game client.
	 */
	protected ConnectionHandler()
	{
		connected = false;
	}
	
	abstract void socketSetup();

	abstract void initDataTransactions();

	public void startThreads()
	{
//		inputThread.start();
//		outputThread.start();
	}
}
