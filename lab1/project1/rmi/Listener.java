/**
 * @author Feichao Qian (feqian@ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class implements a Thread Pooled Server.
 * A thread is created to listen for connection requests.
 * The connection is wrapped in a Runnable and handed off to a thread pool
 * with a fixed number of threads. The Runnable's are kept in a queue in 
 * the thread pool. When a thread in the thread pool is idle it will take 
 * a Runnable from the queue and execute it.
 * @param <T>
 */
public class Listener<T> extends Thread {		
	/**
	 * A sign to indicate whether the server is active
	 */
	private boolean         isActive = false;
	
	/**
	 * The server's socket
	 */
	private ServerSocket    serverSocket;
	
	/**
	 * The thread pool
	 */
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	
	/**
	 * The skeleton created for the local object
	 */
	Skeleton<T> localObj;
	
	/**
	 * Constructor for {@code Listener}
	 * @param Obj The skeleton created for the local object
	 * @param num The max number of threads in thread pool
	 */
	public Listener(Skeleton<T> Obj) {
		this.localObj = Obj;
		this.threadPool = Executors.newFixedThreadPool(Obj.getPoolSize());
		this.serverSocket = Obj.serverSocket;
	}
	
	/**
	 * The tasks for the {@code Listener} thread
	 */
	@Override
	public void run() {		
		this.isActive = true;

		while (isActive() && localObj.isRunning) {

			Socket client = null;		
			try {
				// Wait for the Client Request
				client = serverSocket.accept();
				System.out.println("Listener accepted!");
			} catch (IOException e) {
				/*
				if (!isActive()) {
					System.out.println("******-----*");
					System.out.println("The Server is stopped!");
					System.out.println("************");
					break;
				}
				// TODO Auto-generated catch block
				//e.printStackTrace(); */
			}
			// for execution when a thread in the pool becomes idle.
			System.out.println("Starting service thread!");
			this.threadPool.execute(new Worker<T>(client, localObj));
			System.out.println("Started service thread!");
		}
		
		// shut down thread pool
		//this.threadPool.shutdownNow();
		//System.out.println("************");
		//System.out.println("The Server stopped normally!");
		//System.out.println("************");
		// close socket connection
		//this.terminate();
	}
	
	/**
	 * Checks whether the server is still active
	 * @return A sign to indicate whether the server is active
	 */
    private synchronized boolean isActive() {
        return this.isActive;
    }

    /**
     * Shuts down the server
     */
    public synchronized void terminate(){
        this.isActive = false;
        localObj.stop();
    }
}