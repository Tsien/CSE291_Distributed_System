/**
 * @author Feichao Qian (feqian@ucsd.edu)
 */


package rmi;

import java.io.Serializable;

/**
 * This class encapsulates all kinds of data transformation between server and client
 * @author feichao
 */
public class RMIData implements Serializable {

	/**
	 * used during deserialization to verify that the sender and receiver
	 * of a serialized object have loaded classes for that object that are 
	 * compatible with respect to serialization.
	 */
	private static final long serialVersionUID = 6690597745848705263L;
	
	/**
	 * The name of the class implementing the remote interface 
	 */
	private String className;
	
	/**
	 * The name of method called by the client
	 */
	private String methodName;
	
	/**
	 * An array of objects containing the values of the arguments passed in the method 
	 */
	private Object[] args;
	
	/**
	 * An array of types of arguments
	 */
	private Class<?>[] argsType;
	
	/**
	 * The return value of the method call
	 */
	private Object result;
	
	/**
	 * Any possible exception while making the method call
	 */
	private Exception exception;
	
	/**
	 * Constructor of {@code RMIData}, for request from client. 
	 * @param className The name of the class implementing the remote interface 
	 * @param methodName The name of method called by the client
	 * @param args An array of objects containing the values of the arguments passed in the method 
	 * @param res The return value of the method call
	 * @param e Any possible exception while making the method call
	 */
	public RMIData(String className, String methodName, Object[] args, Class<?>[] argsType, Object res, Exception e) {
		this.className = className;
		this.methodName = methodName;
		this.result = res;
		this.exception = e;
		if (args == null) {
			this.args = new Object[] {};
		}
		else {
			this.args = args;
		}
		if (argsType == null) {
			this.setArgsType(new Class<?>[] {});
		}
		else {
			this.setArgsType(argsType);
		}
		this.result = null;
		this.exception = null;
	}
	
	/**
	 * Constructor of {@code RMIData}, for response from server.
	 * @param res The return value of the method call
	 * @param e Any possible exception while making the method call
	 */
	public RMIData(Object res, Exception e) {
		this.className = null;
		this.methodName = null;
		this.args = null;
		this.result = res;
		this.exception = e;
	}
	
	
	/**
	 * Return any possible exception while making the method call
	 * @return Any possible exception while making the method call
	 */
	public Exception getException() {
		return this.exception;
	} 
	
	/**
	 * Return the return value of method call
	 * @return The return value of the method call
	 */
	public Object getResult() {
		return result;
	}
	
	/**
	 * Return a array of arguments for the called method
	 * @return An array of objects containing the values of the arguments passed in the method 
	 */
	public Object[] getArgs() {
		return args;
	}
	
	/**
	 * Return the name of method called by the client
	 * @return a string, method's name
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * Return the name of the class implementing the remote interface 
	 * @return a string, class's name
	 */
	public String getClassName() {
		return className;
	}

	public Class<?>[] getArgsType() {
		return argsType;
	}

	public void setArgsType(Class<?>[] argsType) {
		this.argsType = argsType;
	}
}
