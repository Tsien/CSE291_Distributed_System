package naming;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
	
	/**
	 * Service Skeleton for client
	 */
	private Skeleton<Service> serviceSklt;
	
	/**
	 * Registration Skeleton for storage servers
	 */
	private Skeleton<Registration> registSklt;
	
	/**
	 * Registration table to avoid duplication
	 */
	private HashSet<StorageStubs> regTable;

	/**
	 * The filesystem directory tree
	 */
	private Map<Path, List<Path>> fileSystem;

	/**
	 * A hashmap mapping from path to the storage servers 
	 */
	private Map<Path, List<StorageStubs>> path2Storage;

	/**
	 * The root node of the filesystem directory tree
	 */
	private Path root;
	
    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
    	serviceSklt = null;
    	registSklt = null;
		regTable = new HashSet<StorageStubs>();
    	fileSystem = new ConcurrentHashMap<Path, List<Path>>();
    	path2Storage = new ConcurrentHashMap<Path, List<StorageStubs>>();
    	root = new Path();
    	
    	fileSystem.put(root, new ArrayList<Path>());
    	path2Storage.put(root, new ArrayList<StorageStubs>());
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

    /** Determines whether a path refers to a directory.

    <p>
    The parent directory should be locked for shared access before this
    operation is performed. This is to prevent the object in question from
    being deleted or re-created while this call is in progress.

    @param path The object to be checked.
    @return <code>true</code> if the object is a directory,
            <code>false</code> if it is a file.
    @throws FileNotFoundException If the object specified by
                                  <code>path</code> cannot be found.
    @throws RMIException If the call cannot be completed due to a network
                         error.
    */    
    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
    	// TODO: The parent directory should be locked for shared access before this
        // operation is performed.
    	
    	// assume path is an absolute path
        return path.getPathType() == Path.PathType.DIRECTORY;
    }

    /** Lists the contents of a directory.

    <p>
    The directory should be locked for shared access before this operation
    is performed, because this operation reads the directory's child list.

    @param directory The directory to be listed.
    @return An array of the directory entries. The entries are not
            guaranteed to be in any particular order.
    @throws FileNotFoundException If the given path does not refer to a
                                  directory.
    @throws RMIException If the call cannot be completed due to a network
                         error.
    */
    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
    	// TODO: lock
    	List<Path> subDir = fileSystem.get(directory);
    	if (subDir == null || directory.getPathType() == Path.PathType.FILE) {
    		throw new FileNotFoundException("Error: the given path does not refer to a directory");
    	}
    	String[] res = new String[subDir.size()];
    	int i = 0;
    	for (Path p : subDir) {
    		res[i++] = p.last();
    	}
    	return res;
    }
    
    /** Creates the given file, if it does not exist.

    <p>
    The parent directory should be locked for exclusive access before this
    operation is performed.

    @param file Path at which the file is to be created.
    @return <code>true</code> if the file is created successfully,
            <code>false</code> otherwise. The file is not created if a file
            or directory with the given name already exists.
    @throws FileNotFoundException If the parent directory does not exist.
    @throws IllegalStateException If no storage servers are connected to the
                                  naming server.
    @throws RMIException If the call cannot be completed due to a network
                         error.
    */
    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
    	if (regTable.isEmpty()) {
    		throw new IllegalStateException("Error: no storage servers are connected to the naming server");
    	}
        return addPath(file);
    }

    /** Creates the given directory, if it does not exist.

    <p>
    The parent directory should be locked for exclusive access before this
    operation is performed.

    @param directory Path at which the directory is to be created.
    @return <code>true</code> if the directory is created successfully,
            <code>false</code> otherwise. The directory is not created if
            a file or directory with the given name already exists.
    @throws FileNotFoundException If the parent directory does not exist.
    @throws RMIException If the call cannot be completed due to a network
                         error.
    */
    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
    	boolean res = false;
    	try {
			res = addPath(directory);
		} catch (RMIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return res;
    }

    /** Deletes a file or directory.

    <p>
    The parent directory should be locked for exclusive access before this
    operation is performed.

    @param path Path to the file or directory to be deleted.
    @return <code>true</code> if the file or directory is deleted;
            <code>false</code> otherwise. The root directory cannot be
            deleted.
    @throws FileNotFoundException If the object or parent directory does not
                                  exist.
    @throws RMIException If the call cannot be completed due to a network
                         error.
    */
    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
    	if (path.isRoot()) {
    		throw new FileNotFoundException("Error: the parent directory does not exist.");    		
    	}    	
    	Path p = path.parent();
    	if (!fileSystem.containsKey(path) || !fileSystem.containsKey(p)) {
			throw new FileNotFoundException("Error: the path does not exist.");	    		
    	}
    	// Delete file on the storage server
    	StorageStubs stb = path2Storage.get(path).get(0);
    	try {
			if (!stb.getCMD_stub().delete(path)) {
				return false;
			}
		} catch (RMIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	fileSystem.get(p).remove(path);
    	path2Storage.remove(path);
        return true;
    }

    /** Returns a stub for the storage server hosting a file.

    <p>
    If the client intends to perform calls only to <code>read</code> or
    <code>size</code> after obtaining the storage server stub, it should
    lock the file for shared access before making this call. If it intends
    to perform calls to <code>write</code>, it should lock the file for
    exclusive access.

    @param file Path to the file.
    @return A stub for communicating with the storage server.
    @throws FileNotFoundException If the file does not exist.
    @throws RMIException If the call cannot be completed due to a network
                         error.
    */
    @Override
    public Storage getStorage(Path file) throws FileNotFoundException
    {
    	if (!fileSystem.containsKey(file)) {
    		throw new FileNotFoundException("Error: the file does not exist.");
    	}
        return path2Storage.get(file).get(0).getClient_stub();
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
        	try {
				if (!addPath(f)) {
					dupFiles.add(f);
				}
			} catch (FileNotFoundException | RMIException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        Path[] res = new Path[dupFiles.size()];
        return dupFiles.toArray(res);
    }
    /**
     * Add a new Path to the filesystem directory  tree
     * @param p Path
     * @return true if adding path successfully, false otherwise
     * @throws FileNotFoundException 
     * @throws RMIException 
     */
    private boolean addPath(Path file) throws FileNotFoundException, RMIException {
    	if (file.isRoot()) {
    		throw new FileNotFoundException("Error: the parent directory does not exist.");    		
    	}
    	Path p = file.parent();
    	if (p == null || !fileSystem.containsKey(p)) {
    		throw new FileNotFoundException("Error: the parent directory does not exist.");
    	}    		
    	// create file on storage server
    	StorageStubs stb = path2Storage.get(p).get(0);
    	if (!stb.getCMD_stub().create(file)) {
    		return false;
    	}
    	// add path to the fileSystem directory tree
    	fileSystem.get(p).add(file);
    	path2Storage.put(file, new ArrayList<>(Arrays.asList(stb)));
        return true;
    }
}
