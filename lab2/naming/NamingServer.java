package naming;

import java.io.*;
import java.net.*;
import java.util.*;

import rmi.*;
import common.*;
import storage.*;

/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{
	private HashSet<Storage> regTable;
	private Skeleton<Service> serviceSklt;
	private Skeleton<Registration> registSklt;
	
	/**
	 * The root node of the filesystem directory tree
	 */
	private Node root;
	
    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
    	root = new Node(null, false, "");
    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
    	InetSocketAddress service_address = new InetSocketAddress(NamingStubs.SERVICE_PORT);
    	InetSocketAddress register_address = new InetSocketAddress(NamingStubs.REGISTRATION_PORT);
    	serviceSklt = new Skeleton<Service>(Service.class, this, service_address);
    	registSklt = new Skeleton<Registration>(Registration.class, this, register_address);
    	serviceSklt.start();
    	registSklt.start();
    }

    /** Stops the naming server.

        <p>
        This method commands both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
    	serviceSklt.stop();
    	registSklt.stop();
        stopped(null);
    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
    }

    // The following public methods are documented in Service.java.
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void unlock(Path path, boolean exclusive)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
    	// TODO: The parent directory should be locked for shared access before this
        // operation is performed.
    	
    	// assume path is an absolute path
    	Node cur = root;
    	for (String s : path) {
    		Node next = cur.getChild(s);
    		if (next == null) {
    			throw new FileNotFoundException("Error: " + path + " cannot be found...");
    		}
    		cur = next;
    	}
        return cur.getIsFile();
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
    	// TODO: lock
    	
    	Node cur = root;
    	for (String s : directory) {
    		Node next = cur.getChild(s);
    		if (next == null || next.getIsFile()) {
    			throw new FileNotFoundException("Error: " + directory + " cannot be found...");
    		}
    		cur = next;
    	}
    	Node[] children = cur.getChildren();
    	List<String> names = null;
    	for (Node nd : children) {
    		names.add(nd.getName());
    	}
    	String[] res = new String[names.size()];
    	return names.toArray(res);
    }

    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
    	if (regTable.isEmpty()) {
    		throw new IllegalStateException("Error: no storage servers are connected to the naming server");
    	}
    	Path p = null;
    	try {
    		p = toAbsolute(file);
    	}
    	catch (FileNotFoundException e) {
    		System.out.println(e);
    	}
        return addPath(p, true);
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
    	if (regTable.isEmpty()) {
    		throw new IllegalStateException("Error: no storage servers are connected to the naming server");
    	}
    	Path p = null;
    	try {
    		p = toAbsolute(directory);
    	}
    	catch (FileNotFoundException e) {
    		System.out.println(e);
    	}
        return addPath(p, false);    	
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
    	Path p = null;
    	try {
    		p = toAbsolute(path);
    	}
    	catch (FileNotFoundException e) {
    		System.out.println(e);
    		return false;
    	}
    	Node cur = root;
    	for (String s : p) {
    		Node next = cur.getChild(s);
    		if (next == null) {
    			throw new FileNotFoundException("Error: the path does not exist.");
    		} 
    		cur = next;
    	}
    	// TODO: delete
        return true;
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
        throw new UnsupportedOperationException("not implemented");
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
        if (client_stub == null || command_stub == null || files == null) {
        	throw new NullPointerException("Error: NULL arguments.");
        }
        if (regTable.contains(client_stub)) {
        	throw new IllegalStateException("Error: the storage server is already registered.");
        }
        
        List<Path> dupFiles = new ArrayList<>();
        
        for (Path f : files) {
        	if (!addPath(f, true)) {
        		dupFiles.add(f);
        	}
        }
        Path[] res = new Path[dupFiles.size()];
        return dupFiles.toArray(res);
    }
    /**
     * Add a new Path to the filesystem directory  tree
     * @param p Path
     * @return true if adding path successfully, false otherwise
     */
    private boolean addPath(Path p, boolean isFile) {
    	boolean tag = false;
    	Node cur = root;
    	for (String s : p) {
    		Node next = cur.getChild(s);
    		if (next == null) {
    			next = new Node(cur, false, s);
    			cur.addChild(next);
    			tag = true;
    		} 
    		cur = next;
    	}
    	if (tag) {
    		cur.setIsFile(isFile);
    	}
    	return tag;
    }
    
    /**
     * Transform relative path to absolute path
     * @param p
     * @return path
     */
    public Path toAbsolute(Path p) throws FileNotFoundException {
    	Path parent = p.parent();
    	if (parent == null) {
    		throw new FileNotFoundException("Error: the parent directory does not exist.");
    	}
    	return new Path();
    }
}
