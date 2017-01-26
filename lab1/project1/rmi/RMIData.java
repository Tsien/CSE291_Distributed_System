/**
 * @author Feichao Qian (feqian@ucsd.edu)
 */


package rmi;

import java.io.Serializable;

/**
 * This class wraps all kinds of data transformation between server and client
 * @author feichao
 *
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
	 * A list of arguments for the called method
	 */
	private Object[] args;
	
	/**
	 * A list of types of arguments for the called method
	 */
	private String[] argsType;
	
	/**
	 * The return value of the method call
	 */
	private Object result;
	
	/**
	 * Any possible exception while making the method call
	 */
	private Exception exception;
	
	/**
	 * Constructor of {@code RMIData}. 
	 * @param className
	 * @param methodName
	 * @param args
	 * @param argsType
	 * @param res
	 * @param e
	 */
	public RMIData(String className, String methodName, Object[] args, String[] argsType, Object res, Exception e) {
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
			this.argsType = new String[] {};
		}
		else {
			this.argsType = argsType;
		}
	}
	
	/**
	 * Return any possible exception while making the method call
	 * @return exception
	 */
	public Exception getException() {
		return this.exception;
	} 
	
	/**
	 * Return the return value of method call
	 * @return result
	 */
	public Object getResult() {
		return result;
	}
	
	/**
	 * Return a list of types of arguments for the called method
	 * @return types of arguments
	 */
	public String[] getArgsType() {
		return argsType;
	}
	
	/**
	 * Return a list of arguments for the called method
	 * @return arguments
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
}
