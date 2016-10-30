package engine.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public abstract class NetworkHandler
{
	protected Socket connectionSocket;
	protected ObjectOutputStream networkOutput;
	protected ObjectInputStream networkInput;
	public boolean connected;
	
	Semaphore outputLock = new Semaphore(0);
	ConcurrentLinkedQueue<DataPattern> messageQueue = new ConcurrentLinkedQueue<DataPattern>();
	
	protected NetworkOutputThread outputThread;
	protected NetworkInputThread inputThread;
	
	/**
	 * disconnect from this connection
	 */
	protected void disconnect()
	{
		connected = false;
	}
	
	public void queueMessage(DataPattern message)
	{
		messageQueue.add(message);
		outputLock.release();
	}
	
	public abstract class NetworkOutputThread extends Thread
	{
		public DataPattern outgoingPattern = DataPattern.INVALID_PATTERN;
		
		public void run()
		{
			while(connected) 
			{
				outputLoop();
			}
		}
		
		private void waitForMessage()
		{
			try {
				outputLock.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		void outputLoop()
		{
			// Wait for a message to be available
			//waitForMessage();
			
			// Send the data pattern of the next message in the queue
			sendDataPattern();
			
			// If we haven't disconnected, send the rest of the data
			if (connected) {
				sendData();
			}
		}
		
		void sendDataPattern()
		{
			// Get the data pattern
//			try {
//				outgoingPattern = messageQueue.remove();
//			} catch (NoSuchElementException e) {
//				// TODO queue was empty, handle it?
//			}
			
			// Send the data pattern
			try {
				networkOutput.writeObject(outgoingPattern);
			} catch (SocketException e) {
				System.err.println("SocketException occurred when trying to send the"
						+ " data pattern");
				disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		protected abstract void sendData();
		
	}
	
	public abstract class NetworkInputThread extends Thread
	{
		protected DataPattern incomingPattern;
		
		public void run()
		{
			while(connected) {
				inputLoop();
			}
		}
		
		protected void inputLoop()
		{
			// Get the data pattern of incoming data
			receiveDataPattern();
			
			// If the pattern is valid, and we haven't disconnected, receive the data
			if (incomingPattern != DataPattern.INVALID_PATTERN && connected) {
				receiveData();
			}
		}
		
		protected void receiveDataPattern()
		{
			try {
				incomingPattern = DataPattern.INVALID_PATTERN;
				incomingPattern = (DataPattern) networkInput.readObject();
			} catch (SocketException e) {
				System.err.println("SocketException occurred when trying to read the"
						+ " input pattern");
				disconnect();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFoundException occurred when trying to read the"
						+ " input pattern");
				disconnect();
			}
		}
		
		protected abstract void receiveData();
	}
	
	/**
	 * Constructor which sets up I/O streams and performs initial
	 * communications for handling this game client.
	 */
	protected NetworkHandler(Socket sock)
	{
		connected = false;
		
		createThreads();
		
		socketSetup(sock);
		
		if(connected) {
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
