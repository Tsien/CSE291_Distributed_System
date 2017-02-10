/**
 * @author Feichao Qian (feqian@ucsd.edu)
 */

package rmi;

import java.io.IOException;
import java.net.*;

/** RMI skeleton

    <p>
    A skeleton encapsulates a multithreaded TCP server. The server's clients are
    intended to be RMI stubs created using the <code>Stub</code> class.

    <p>
    The skeleton class is parametrized by a type variable. This type variable
    should be instantiated with an interface. The skeleton will accept from the
    stub requests for calls to the methods of this interface. It will then
    forward those requests to an object. The object is specified when the
    skeleton is constructed, and must implement the remote interface. Each
    method in the interface should be marked as throwing
    <code>RMIException</code>, in addition to any other exceptions that the user
    desires.

    <p>
    Exceptions may occur at the top level in the listening and service threads.
    The skeleton's response to these exceptions can be customized by deriving
    a class from <code>Skeleton</code> and overriding <code>listen_error</code>
    or <code>service_error</code>.
*/
public class Skeleton<T>
{
	/**
	 * A boolean variable to indicate whether the server is running
	 */
	public boolean isRunning;
	
	/**
	 * a Thread Pooled Server 
	 */
	private Listener<T> myServer;
	
	/**
	 * The server's socket
	 */
	public ServerSocket    serverSocket;
	
	/**
	 * an IP Socket Address (IP address + port number)  
	*/
	private InetSocketAddress myAddress;
	
	/**
	 * An object representing the class of the interface for which the 
	 * skeleton server is to handle method call requests.
	 */
	private Class<T> rmtItface;
	
	/**
	 * An object implementing said interface. Requests for method
	 * calls are forwarded by the skeleton to this object.
	 */
	private T rmtObject;
		
	/**
	 * The number of threads in the thread pool
	 */
	private int poolSize = 10;
	
    /** Creates a <code>Skeleton</code> with no initial server address. The
        address will be determined by the system when <code>start</code> is
        called. Equivalent to using <code>Skeleton(null)</code>.

        <p>
        This constructor is for skeletons that will not be used for
        bootstrapping RMI - those that therefore do not require a well-known
        port.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server)
    {
        if (c == null) {
            throw new NullPointerException("Error: An object representing the class of the interface is null!");
        }
        if (server == null) {
            throw new NullPointerException("Error: An object implementing said interface is null");
        }
        if (!IsRemoteInterface.check(c)) {
            throw new Error("Error: " + c.getName() + " is NOT a remote interface!");
        }
        this.myServer = null;
        this.setAddress(null);
        this.setRmtItface(c);
        this.setRmtObject(server);
        this.isRunning = false;
    }

    /** Creates a <code>Skeleton</code> with the given initial server address.

        <p>
        This constructor should be used when the port number is significant.

        @param c An object representing the class of the interface for which the
                 skeleton server is to handle method call requests.
        @param server An object implementing said interface. Requests for method
                      calls are forwarded by the skeleton to this object.
        @param address The address at which the skeleton is to run. If
                       <code>null</code>, the address will be chosen by the
                       system when <code>start</code> is called.
        @throws Error If <code>c</code> does not represent a remote interface -
                      an interface whose methods are all marked as throwing
                      <code>RMIException</code>.
        @throws NullPointerException If either of <code>c</code> or
                                     <code>server</code> is <code>null</code>.
     */
    public Skeleton(Class<T> c, T server, InetSocketAddress address)
    {
        if (c == null) {
            throw new NullPointerException("Error: An object representing the class of the interface is null!");
        }
        if (server == null) {
            throw new NullPointerException("Error: An object implementing said interface is null");
        }
        if (!IsRemoteInterface.check(c)) {
            throw new Error("Error: " + c.getName() + " is NOT a remote interface!");
        }
        this.myServer = null;
        this.setAddress(address);
        this.setRmtItface(c);
        this.setRmtObject(server);
        this.isRunning = false;
    }

    /** Called when the listening thread exits.

        <p>
        The listening thread may exit due to a top-level exception, or due to a
        call to <code>stop</code>.

        <p>
        When this method is called, the calling thread owns the lock on the
        <code>Skeleton</code> object. Care must be taken to avoid deadlocks when
        calling <code>start</code> or <code>stop</code> from different threads
        during this call.

        <p>
        The default implementation does nothing.

        @param cause The exception that stopped the skeleton, or
                     <code>null</code> if the skeleton stopped normally.
     */
    protected void stopped(Throwable cause)
    {
    
    }

    /** Called when an exception occurs at the top level in the listening
        thread.

        <p>
        The intent of this method is to allow the user to report exceptions in
        the listening thread to another thread, by a mechanism of the user's
        choosing. The user may also ignore the exceptions. The default
        implementation simply stops the server. The user should not use this
        method to stop the skeleton. The exception will again be provided as the
        argument to <code>stopped</code>, which will be called later.

        @param exception The exception that occurred.
        @return <code>true</code> if the server is to resume accepting
                connections, <code>false</code> if the server is to shut down.
     */
    protected boolean listen_error(Exception exception)
    {
    	// TODO
        return false;
    }

    /** Called when an exception occurs at the top level in a service thread.

        <p>
        The default implementation does nothing.

        @param exception The exception that occurred.
     */
    protected void service_error(RMIException exception)
    {
    	// TODO
		System.out.println("************");
    	System.out.println("Exception on the server end:");
		System.out.println("************");
    	exception.printStackTrace();
    }

    /** Starts the skeleton server.

        <p>
        A thread is created to listen for connection requests, and the method
        returns immediately. Additional threads are created when connections are
        accepted. The network address used for the server is determined by which
        constructor was used to create the <code>Skeleton</code> object.

        @throws RMIException When the listening socket cannot be created or
                             bound, when the listening thread cannot be created,
                             or when the server has already been started and has
                             not since stopped.
     */
    public synchronized void start() throws RMIException
    {
    	if (!this.isRunning) {
	    	this.isRunning = true;
    		try {
    			// open server socket
    			if (this.myAddress == null) {
    				//use port 0 to choose a random port number from 1024
    				serverSocket = new ServerSocket(0, poolSize);
    				myAddress = (InetSocketAddress)serverSocket.getLocalSocketAddress();

    			}
    			else {
    				serverSocket = new ServerSocket(myAddress.getPort(), poolSize, myAddress.getAddress());
    			}
    			
    			System.out.println("************");
    			System.out.println("IP:" + serverSocket.getLocalSocketAddress() + ", Port:" + serverSocket.getLocalPort());
    			System.out.println("************");
        		myServer = new Listener<T>(this); 
    	    	myServer.start();
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			System.out.println("************");
    			System.out.println("Fail to open a server socket!");
    			System.out.println("************");
    			//e.printStackTrace();
    		}    		

    	}
    	else {
    		throw new RMIException("Error: The server is already running!");
    	}
    }

    /** Stops the skeleton server, if it is already running.

        <p>
        The listening thread terminates. Threads created to service connections
        may continue running until their invocations of the <code>service</code>
        method return. The server stops at some later time; the method
        <code>stopped</code> is called at that point. The server may then be
        restarted.
     */
    public synchronized void stop()
    {
    	myServer = null;
    	this.isRunning = false;

    	try { 
            if(!serverSocket.isClosed()) {
                System.out.println("-------not closed.. going to close---------");
			serverSocket.close();
        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
            System.out.println("-------STOP---------");
		//	e.printStackTrace();
		} catch(NullPointerException e){
            System.out.println("-------NullPointerException---------");
        }

        stopped(null);
 System.out.println("-------going to exit!--------");
        
    	
    }
    
	public InetSocketAddress getAddress() {
		return myAddress;
	}

	public void setAddress(InetSocketAddress address) {
		this.myAddress = address;
	}

	public T getRmtObject() {
		return rmtObject;
	}

	public void setRmtObject(T rmtObject) {
		this.rmtObject = rmtObject;
	}

	public Class<T> getRmtItface() {
		return rmtItface;
	}

	public void setRmtItface(Class<T> rmtItface) {
		this.rmtItface = rmtItface;
	}

	public int getPoolSize() {
		return poolSize;
	}
}
