/**
 * @author Feichao Qian (feqian@ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The worker processes each request from the client.
 * @author feichao
 * @param <T>
 *
 */
public class Worker<T> extends Thread {
	/**
	 * The client socket
	 */
	private Socket client;
	
	/**
	 * The real local object on the server
	 */
	public T localObj;
	
	/**
	 * Constructor for {@code Worker}
	 * @param ct The client socket
	 * @param obj The real local object on the server
	 */
	public Worker(Socket ct, Skeleton<T> obj) {
		client = ct;
		localObj = obj.getRmtObject();
	}

	/**
	 * The tasks that {@code Worker} thread needs to handle
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Create I/O streams for communicating to the client
		ObjectOutputStream oStream = null;
		ObjectInputStream iStream = null;
		RMIData request = null;

		try {
			oStream = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			iStream = new ObjectInputStream(client.getInputStream());
			// Read object from stream
			request = (RMIData)iStream.readObject();
			// TODO what if rmiData.className != T
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (request != null) {
			Object res = runMethod(request.getMethodName(), request.getArgs());
			RMIData response = new RMIData(res, null);			
			try {
				oStream.writeObject(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		try {
			oStream.close();
			iStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	
	/**
	 * Executes the method call
	 * @param methodName the name of called method
	 * @param args An array of objects containing the values of the arguments passed in the method 
	 * @return An object, the return value of the method call
	 */
	public Object runMethod(String methodName, Object[] args) {
		// TODO how to find the right method? 
		Object res = null;		
		Class<?> objClass = localObj.getClass();
		Method targetMethod = null;
		Class<?>[] argsType = getArgsType(args);
		try {
			targetMethod = objClass.getDeclaredMethod(methodName, argsType);
		} catch (NoSuchMethodException | SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (targetMethod != null) {
			try {
				res = targetMethod.invoke(localObj, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return res;
	}
	
	/**
	 * Gets the class of objects, given an array of objects  
	 * @param args, an array of objects
	 * @return an array of class
	 */
	private Class<?>[] getArgsType(Object[] args) {
		ArrayList<Class<?>> argsType = new ArrayList<>();
		for (Object obj : args) {
			argsType.add(obj.getClass());
		}
		Class<?>[] res = new Class<?>[argsType.size()];
		return (Class<?>[])argsType.toArray(res);
	}
}
