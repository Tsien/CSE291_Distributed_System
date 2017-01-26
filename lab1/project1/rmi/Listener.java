/**
 * @author Feichao Qian (feqian@ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.net.InetSocketAddress;
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
	private int             serverPort;
	private int             poolSize;
	private boolean         isActive;
	private ServerSocket    serverSocket;
	private ExecutorService threadPool;
	Skeleton<T> localObj;
	
	public Listener(Skeleton<T> Obj, int num) {
		localObj = Obj;
		serverPort = localObj.address.getPort();
		poolSize = num;
		threadPool = Executors.newFixedThreadPool(poolSize);
	}
	
	public void run() {
		try {
			// open server socket
			serverSocket = new ServerSocket(serverPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (isActive()) {
			Socket client = null;
			try {
				// Wait for the Client Request
				client = serverSocket.accept();
			} catch (IOException e) {
				if (!isActive()) {
					System.out.println("The Server is stopped!");
					break;
				}
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// for execution when a thread in the pool becomes idle.
			this.threadPool.execute(new Worker(client, localObj));
		}
		
		this.threadPool.shutdown();
		System.out.println("The Server stopped normally!");
	}
	
    private synchronized boolean isActive() {
        return this.isActive;
    }

    public synchronized void terminate(){
        this.isActive = false;            
        try {
			this.serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Error: fail to stop the server", e);
		}
    }
}