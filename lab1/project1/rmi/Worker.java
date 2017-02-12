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

/**
 * The worker processes each request from the client.
 * @author feichao
 * @param <T>
 *
 */
public class Worker<T> extends Thread {
	/**
	 * The skeleton
	 */
	private Skeleton<T> skt;
	
	/**
	 * The client socket
	 */
	private Socket client;
	
	/**
	 * The real local object on the server
	 */
	private T localObj;

	/**
	 * Constructor for {@code Worker}
	 * @param ct The client socket
	 * @param obj The real local object on the server
	 */
	public Worker(Socket ct, Skeleton<T> obj) {
		client = ct;
		localObj = obj.getRmtObject();
		skt = obj;
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
			// System.out.println("INSIDE WORKER TRY!");
			oStream = new ObjectOutputStream(client.getOutputStream());
			oStream.flush();
			iStream = new ObjectInputStream(client.getInputStream());
			// Read object from stream
			request = (RMIData)iStream.readObject();
			// System.out.println("INSIDE WORKER: REMOTE METHOD NAME is : " + request.getMethodName());
			// TODO what if rmiData.className != T
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			// System.out.println("INSIDE WORKER: IOException!");
			//e.printStackTrace();
			skt.service_error(new RMIException(e1));
		} catch (ClassNotFoundException e) {
			// System.out.println("INSIDE WORKER: ClassNotFoundException!");
			// TODO Auto-generated catch block
			//e.printStackTrace();
			skt.service_error(new RMIException(e));
		}
		
		if (request != null) {
			RMIData response = runMethod(request);
			try {
				oStream.writeObject(response);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				// System.out.println("==========IOException in Worker:run()==========");
				skt.service_error(new RMIException(e));
			}
		}
		
		try {
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			// System.out.println("==========IOException in Worker:run()CLOSE CLIENT==========");
			skt.service_error(new RMIException(e));
		} 
	}	
	
	/**
	 * Executes the method call
	 * @param methodName the name of called method
	 * @param args An array of objects containing the values of the arguments passed in the method 
	 * @return An object, the return value of the method call
	 */
	private RMIData runMethod(RMIData request) {
		// TODO how to find the right method?
		String methodName = request.getMethodName();
		String className = request.getClassName();
		String itfName = skt.getRmtItface().getName();
		Object[] args = request.getArgs(); 
		Class<?>[] argsType = request.getArgsType();
		Object val = null;
		Class<?> objClass = localObj.getClass();
		Method targetMethod = null;

		try {
			// System.out.println("********" + methodName + " in " + objClass.getName() + "*****");
			targetMethod = objClass.getMethod(methodName, argsType);
		} catch (NoSuchMethodException | SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			// System.out.println("==========Worker: Exp when runMethod1==========");
			skt.service_error(new RMIException(e1));
			return new RMIData(null, e1);
		}
		if (targetMethod != null) {
			if (!className.equals(itfName)) {
				skt.service_error(new RMIException(new NoSuchMethodException("calling method not declared in the interface for which the skeleton was created")));
				return new RMIData(null, new RMIException("calling method not declared in the interface for which the skeleton was created"));
			}
			try {
				// System.out.println("==========Worker: class:" + localObj.getClass().getName());
				// System.out.println("==========Worker: class:" + itfName);
				// System.out.println("==========Worker: method: " + targetMethod.getName());
				// System.out.println("==========Worker: method: " + className);
				targetMethod.setAccessible(true);
				val = targetMethod.invoke(localObj, args);
			} catch (InvocationTargetException e1) {
				return new RMIData(null, (Exception)e1.getTargetException());				
			} catch (IllegalAccessException | IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				// System.out.println("==========Worker: Exp when runMethod2==========");
				skt.service_error(new RMIException(e));
				return new RMIData(null, new RMIException(e));
			}
		}
		return new RMIData(val, null);
	}
}
