/**
 * @author Feichao Qian (feqian@ucsd.edu)
 */

package rmi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * The worker processes each request from the client.
 * @author feichao
 * @param <T>
 *
 */
public class Worker<T> extends Thread {
	private Socket client;
	public T localObj;
	
	public Worker(Socket ct, Skeleton<T> obj) {
		client = ct;
		localObj = obj.rmtObject;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Create I/O streams for communicating to the client
		ObjectOutputStream oStream = null;
		ObjectInputStream iStream = null;
		RMIData rmiData = null;

		try {
			oStream = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			iStream = new ObjectInputStream(client.getInputStream());
			// Read object from stream
			rmiData = (RMIData)iStream.readObject();
			// TODO: what if rmiData.className != T
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}	
	
	public Object runMethod(String methodName, Object[] args, String[] argsType) {
		Object res = null;
		
		return res;
	}
}
