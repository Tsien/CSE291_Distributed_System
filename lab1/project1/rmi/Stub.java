package rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/** RMI stub factory.

    <p>
    RMI stubs hide network communication with the remote server and provide a
    simple object-like interface to their users. This class provides methods for
    creating stub objects dynamically, when given pre-defined interfaces.

    <p>
    The network address of the remote server is set when a stub is created, and
    may not be modified afterwards. Two stubs are equal if they implement the
    same interface and carry the same remote server address - and would
    therefore connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub
{
	/**
	 * This class implements <code>InvocationHandler</code>. 
	 * Each proxy instance has an associated invocation handler object, 
	 * which implements the interface InvocationHandler. 
	 * @author feichao
	 *
	 */
    private static class MyInvocationHandler implements InvocationHandler, Serializable {
    	
 		private static final long serialVersionUID = 8636975228194099266L;
 		
 		/**
 		 * The server's socket address
 		 */
 		private InetSocketAddress serverAddress;
 		
 		/**
 		 * A <code>Class</code> object representing the interface
 		 * implemented by the remote object.
 		 */
 		private Class<?> myClass;

 		/**
 		 * Constructor
 		 * @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
 		 * @param address The server's socket address
 		 */
 		public MyInvocationHandler(Class<?> c, InetSocketAddress address) {
 			this.myClass = c;
 			this.serverAddress = address;
 		}
 		/**
 		 * The invocation handler processes the encoded method invocation as appropriate 
 		 * and the result that it returns will be returned as the result of the method 
 		 * invocation on the proxy instance.
 		 * @param proxy the proxy that is associated to the remote interface
 		 * @param method the invoked method on the proxy
 		 * @param args the parameters for the method call
 		 * @return the return value of the method call
 		 */
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// TODO Auto-generated method stub
			Socket client = new Socket();
			ObjectInputStream iStream = null;
			ObjectOutputStream oStream = null;
			RMIData request = null;
			RMIData response = null;
			// TODO try/catch exceptions
			try {
				client.connect(serverAddress);
				oStream = new ObjectOutputStream(client.getOutputStream());
				oStream.flush();			
				iStream = new ObjectInputStream(client.getInputStream());
				System.out.println("==============OPEN I_STREAM============");
				request = new RMIData(myClass.getName(), method.getName(), args, method.getParameterTypes(), null, null);
				oStream.writeObject(request);
				response = (RMIData) iStream.readObject();
				client.close();				
			} catch (IOException e){
				System.out.println("==============IO_EXCEPTION============");				
				//e.printStackTrace();
				throw (Throwable) (new RMIException(e));
			}
						
			System.out.println("==============CLIENT CLOSE============");
			
			if (response != null) {
				Object res = response.getResult();
				Exception e = response.getException();
				System.out.println("Remote call result: " + res);
				System.out.println("Remote call exception: " + e);
				if (e != null) {
					throw e;
				}
				return res;
			}
			return null;
		}
    } 

    /** Creates a stub, given a skeleton with an assigned adress.

        <p>
        The stub is assigned the address of the skeleton. The skeleton must
        either have been created with a fixed address, or else it must have
        already been started.

        <p>
        This method should be used when the stub is created together with the
        skeleton. The stub may then be transmitted over the network to enable
        communication with the skeleton.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose network address is to be used.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned an
                                      address by the user and has not yet been
                                      started.
        @throws UnknownHostException When the skeleton address is a wildcard and
                                     a port is assigned, but no address can be
                                     found for the local host.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton)
        throws UnknownHostException
    {
        if (c == null || skeleton == null) {
        	throw new NullPointerException("Error : argument is null!");
        }
        // check remote interface
        if (!IsRemoteInterface.check(c)) {
            throw new Error("Error: " + c.getName() + " is NOT a remote interface!");
        }
        InetSocketAddress address = skeleton.getAddress();
        if (address == null) {
        	throw new IllegalStateException("The skeleton has not been assigned an address by the user and has not yet been started.");
        }
		return create(c, address);
    }

    /** Creates a stub, given a skeleton with an assigned address and a hostname
        which overrides the skeleton's hostname.

        <p>
        The stub is assigned the port of the skeleton and the given hostname.
        The skeleton must either have been started with a fixed port, or else
        it must have been started to receive a system-assigned port, for this
        method to succeed.

        <p>
        This method should be used when the stub is created together with the
        skeleton, but firewalls or private networks prevent the system from
        automatically assigning a valid externally-routable address to the
        skeleton. In this case, the creator of the stub has the option of
        obtaining an externally-routable address by other means, and specifying
        this hostname to this method.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose port is to be used.
        @param hostname The hostname with which the stub will be created.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned a
                                      port.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton,
                               String hostname)
    {
        if (c == null || skeleton == null || hostname == null) {
        	throw new NullPointerException("Error : argument is null!");
        }
        // check remote interface
        if (!IsRemoteInterface.check(c)) {
            throw new Error("Error: " + c.getName() + " is NOT a remote interface!");
        }
        InetSocketAddress address = skeleton.getAddress();
        if (address == null) {
        	throw new IllegalStateException("The skeleton has not been assigned an address" + 
        									" by the user and has not yet been started.");
        }
		return create(c, new InetSocketAddress(hostname, address.getPort()));
    }

    /** Creates a stub, given the address of a remote server.

        <p>
        This method should be used primarily when bootstrapping RMI. In this
        case, the server is already running on a remote host but there is
        not necessarily a direct way to obtain an associated stub.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param address The network address of the remote skeleton.
        @return The stub created.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, InetSocketAddress address)
    {
        if (c == null || address == null) {
        	throw new NullPointerException("Error : argument is null!");
        }
        // check remote interface
        if (!IsRemoteInterface.check(c)) {
            throw new Error("Error: " + c.getName() + " is NOT a remote interface!");
        }
		T obj = (T)Proxy.newProxyInstance(c.getClassLoader(), 
    			new Class<?>[] {c}, 
    			new MyInvocationHandler(c, address));
    	return obj;
    }
    
    // TODO : implements equals, hashCode, toString
}
