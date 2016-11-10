package engine.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public abstract class NetworkHandler
{
	protected Socket connectionSocket;
	protected ObjectOutputStream networkOutput;
	protected ObjectInputStream networkInput;
	public boolean connected;
	
	Semaphore outputLock = new Semaphore(0);
	ConcurrentLinkedQueue<NetworkMessage> messageQueue = new ConcurrentLinkedQueue<NetworkMessage>();
	
	protected NetworkOutputThread outputThread;
	protected NetworkInputThread inputThread;
	
	/**
	 * disconnect from this connection
	 */
	protected void disconnect()
	{
		connected = false;
	}
	
	public void queueMessage(NetworkMessage message)
	{
		messageQueue.add(message);
		outputLock.release();
	}
	
	public class NetworkOutputThread extends Thread
	{
		NetworkMessage outgoingMessage;
		
		public void run()
		{
			while(connected)
			{
				outputLoop();
			}
		}
		
		private void outputLoop()
		{
			outgoingMessage = null;
			
			// Wait for a message to be available
			waitForMessage();
			
			// Send the message
			sendMessage();
		}
		
		private void waitForMessage()
		{
			try
			{
				outputLock.acquire();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
		private void sendMessage()
		{
			outgoingMessage = messageQueue.poll();
			
			if (outgoingMessage != null)
			{
				try
				{
					networkOutput.writeObject(outgoingMessage);
				}
				catch (SocketException e)
				{
					System.err.println("SocketException occurred when trying to send the"
							+ " message: " + outgoingMessage);
					disconnect();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public abstract class NetworkInputThread extends Thread
	{
		protected NetworkMessage incomingMessage;
		
		public void run()
		{
			while(connected)
			{
				inputLoop();
			}
		}
		
		private void inputLoop()
		{
			incomingMessage = null;
			
			receiveMessage();
			
			if (connected)
			{
				respondToMessage();
			}
		}
		
		private void receiveMessage()
		{
			try
			{
				incomingMessage = (NetworkMessage) networkInput.readObject();
			}
			catch (ClassNotFoundException e)
			{
				System.err.println("ClassNotFoundException occurred when trying to read the"
						+ " message");
				disconnect();
			}
			catch (SocketException e)
			{
				System.err.println("SocketException occurred when trying to read the"
						+ " message");
				disconnect();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		protected abstract void respondToMessage();
	}
	
	/**
	 * Constructor which sets up I/O streams and performs initial communications
	 * for handling this game client.
	 */
	protected NetworkHandler(Socket sock)
	{
		connected = false;
		
		createThreads();
		
		socketSetup(sock);
		
		if (connected)
		{
			initDataTransactions();
		}
	}
	
	protected abstract void createThreads();
	
	protected abstract void socketSetup(Socket sock);
	
	protected abstract void initDataTransactions();
	
	public void startThreads()
	{
		inputThread.start();
		outputThread.start();
	}
}
