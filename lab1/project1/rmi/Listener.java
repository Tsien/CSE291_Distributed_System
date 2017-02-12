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
	private Skeleton<T> localObj;
	
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
	 * Listen for connections and then create a new thread to deal with the request
	 * 
	 */
	@Override
	public void run() {		
		while (this.localObj.getIsRunning()) {
			Socket client = null;		
			try {
				// Wait for the Client Request
				client = serverSocket.accept();
				// System.out.println("Listener accepted!");
				if (this.localObj.getIsRunning()) {
					// for execution when a thread in the pool becomes idle.
					// System.out.println("Starting service thread!");
					this.threadPool.execute(new Worker<T>(client, localObj));
					// System.out.println("Started service thread!");
				}
			} catch (IOException e) {
				if (!this.localObj.getIsRunning()) {
					// System.out.println("The server is stopped..");
				}
				else {
					this.localObj.stop();
				}
				this.localObj.service_error(new RMIException(e));
				// TODO Auto-generated catch block
				//e.printStackTrace(); 
				// System.out.println("==========Listener: IOException in run()==========");
			}
		}
		
		// shut down thread pool
		this.threadPool.shutdownNow();
	}	
}