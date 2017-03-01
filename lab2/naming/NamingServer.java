package naming;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

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
	private Map<Storage, Command> regTable;

	/**
	 * A list of registered storages
	 */
	private List<StorageStubs> storages;
	
	/**
	 * The filesystem directory tree.
	 */
	private Map<Path, PathInfo> fileSystem;
	
    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
    	this.serviceSklt = null;
    	this.registSklt = null;
    	this.regTable = new ConcurrentHashMap<Storage, Command>();
    	this.storages = new ArrayList<StorageStubs>();
    	this.fileSystem = new ConcurrentHashMap<Path, PathInfo>();
    	this.fileSystem.put(new Path(), new PathInfo(false));
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
    /**
	*	@param path The file or directory to be locked.
    *    @param exclusive If <code>true</code>, the object is to be locked for
    *                     exclusive access. Otherwise, it is to be locked for
    *                     shared access.
    *    @throws FileNotFoundException If the object specified by
    *                                  <code>path</code> cannot be found.
    *    @throws IllegalStateException If the object is a file, the file is
    *                                  being locked for write access, and a stale
    *                                  copy cannot be deleted from a storage
    *                                  server for any reason, or if the naming
    *                                  server has shut down and the lock attempt
    *                                  has been interrupted.
    *    @throws RMIException If the call cannot be completed due to a network
    *                         error. This includes server shutdown while a client
    *                         is waiting to obtain the lock.
    */
    @Override
    public void lock(Path path, boolean exclusive) throws FileNotFoundException
    {    	
    	// sanity check
    	if (!this.fileSystem.containsKey(path)) {
    		throw new FileNotFoundException("Error: the object specified by " +
    										path + " cannot be found.");
    	}
    	
    	// Request lock
    	// When any object is locked for either kind of access, all objects along
        // the path up to, but not including, the object itself, are locked for
        // shared access to prevent their modification or deletion by other users.
    	try {
			this.lockParent(path, true);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			// e1.printStackTrace();
			throw new IllegalStateException();
		}
    	ReadWriteLock pLock = this.fileSystem.get(path).getpLock();
    	if (exclusive) {
    		try {
				pLock.lockWrite();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				throw new IllegalStateException();
			}
        	// invalidation
    		// exclusive access = write request -> causes all copies of the file 
    		// but one to be deleted
    		HashSet<StorageStubs> stbs = this.fileSystem.get(path).getStbs();
    		Iterator<StorageStubs> it = stbs.iterator();
    		while (stbs.size() > 1) {
    			try {
					it.next().getCMD_stub().delete(path);
				} catch (RMIException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			// deleted stale copies
    			it.remove();
    		}
    		this.fileSystem.get(path).setStbs(stbs);
    	}
    	else {
    		try {
				pLock.lockRead();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				throw new IllegalStateException();
			}
    		PathInfo pf = this.fileSystem.get(path);
    		pf.incReadAccess();
        	// duplication
    		// the file is replicated once for every 20 read requests
    		if (pf.getReadAccess() >= 20) {
    			pf.clearReadAccess();
    			// make a copy
    			Replicator copier = new Replicator(pf, path, this.storages);
    			new Thread(copier).start();
    			// register storage server
    			this.createFile(path, copier.getStub());
    		}
    	}    
    }

    /** Unlocks a file or directory.

    @param path The file or directory to be unlocked.
    @param exclusive Must be <code>true</code> if the object was locked for
                     exclusive access, and <code>false</code> if it was
                     locked for shared access.
    @throws IllegalArgumentException If the object specified by
                                     <code>path</code> cannot be found. This
                                     is a client programming error, as the
                                     path must have previously been locked,
                                     and cannot be removed while it is
                                     locked.
    @throws RMIException If the call cannot be completed due to a network
                         error.
    */
    @Override
    public void unlock(Path path, boolean exclusive)
    {    	
        if (!this.fileSystem.containsKey(path)) {
        	throw new IllegalArgumentException();
        }
        try {
			this.lockParent(path, false);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("Error: fail to unlock parent path.");
		}
        if (exclusive) {
        	this.fileSystem.get(path).getpLock().unlockWrite();
        }
        else {
        	this.fileSystem.get(path).getpLock().unlockRead();
        }
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
    	if (!this.fileSystem.containsKey(path)) {
    		throw new FileNotFoundException("Error: the path <" + path 
    				+ "> cannot be found.");
    	}
        return !this.fileSystem.get(path).isFile();
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
    	if (!this.fileSystem.containsKey(directory) || this.fileSystem.get(directory).isFile()) {
    		throw new FileNotFoundException("Error: the given path does not refer to a directory");
    	}
    	HashSet<Path> subDir = this.fileSystem.get(directory).getChildren();
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
    	// Sanity check
    	if (regTable.isEmpty()) {
    		throw new IllegalStateException("Error: no storage servers are connected to the naming server");
    	}
    	// If the file already exists
    	if (this.fileSystem.containsKey(file)) {
    		return false;
    	}

    	Path p = file.parent();
    	if (!this.fileSystem.containsKey(p) || this.fileSystem.get(p).isFile()) {
    		throw new FileNotFoundException("Error: the parent directory does not exist.");
    	}    		

    	// create file on storage server
    	int idx = ThreadLocalRandom.current().nextInt(this.storages.size());
    	Storage client = this.storages.get(idx).getClient_stub();
    	Command cmd = this.regTable.get(client);
    	if (!cmd.create(file)) {
    		return false;
    	}
    	// add file path to the fileSystem directory tree
    	this.fileSystem.put(file, new PathInfo(true));
    	StorageStubs stb = new StorageStubs(client, cmd);
    	this.fileSystem.get(file).addStbs(stb);
    	// add parent path to the fileSystem directory tree
    	this.createFile(file, stb);
        return true;
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
    	// Sanity check
    	
    	// If the directory already exists
    	if (this.fileSystem.containsKey(directory)) {
    		return false;
    	}
    	
    	Path p = directory.parent();
    	if (!this.fileSystem.containsKey(p) || this.fileSystem.get(p).isFile()) {
    		throw new FileNotFoundException("Error: the parent directory does not exist.");
    	}    	

    	// add a directory path to the fileSystem directory tree
    	this.fileSystem.put(directory, new PathInfo(false));
    	// add parent path to the fileSystem directory tree
    	this.createDirect(directory);
    	return true;
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
    	Path p = path.parent();
    	if (!this.fileSystem.containsKey(p) || !this.fileSystem.containsKey(path)) {
    		throw new FileNotFoundException("Error: the object or parent directory does not exist.");
    	}
    	PathInfo pt = this.fileSystem.get(path);
    	HashSet<StorageStubs> stbs = pt.getStbs();
    	try {
    		for (StorageStubs stb : stbs) {
    			if (!stb.getCMD_stub().delete(path)) {
    				return false;
    			}
    		}
		} catch (RMIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	this.fileSystem.remove(path);
		this.fileSystem.get(p).rmvChild(path);
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
    	if (!this.fileSystem.containsKey(file) || !this.fileSystem.get(file).isFile()) {
    		throw new FileNotFoundException("Error: the file does not exist.");
    	}
    	HashSet<StorageStubs> stbs = this.fileSystem.get(file).getStbs();
    	Iterator<StorageStubs> it = stbs.iterator();
    	int idx = ThreadLocalRandom.current().nextInt(stbs.size());
    	while (idx > 0) {
    		--idx;
    		it.next();
    	}
        return it.next().getClient_stub();
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
    	// Sanity Check
        if (client_stub == null || command_stub == null || files == null) {
        	throw new NullPointerException("Error: NULL arguments.");
        }
        if (this.regTable.containsKey(client_stub)) {
        	throw new IllegalStateException("Error: the storage server is already registered.");
        }
        
        // Add storage server to the register table
		StorageStubs stb = new StorageStubs(client_stub, command_stub);
        this.regTable.put(client_stub, command_stub);
        this.storages.add(stb);
        
        // Register new files 
        List<Path> dupFiles = new ArrayList<>();
        for (Path f : files) {
        	if (f.isRoot()) {
        		continue;
        	}
        	if (this.fileSystem.containsKey(f)) {
        		dupFiles.add(f);
        	}
        	else {
        		this.fileSystem.put(f, new PathInfo(true));
        		this.fileSystem.get(f).addStbs(stb);
        		this.createFile(f, stb);
        	}
        }
        Path[] res = new Path[dupFiles.size()];
        return dupFiles.toArray(res);
    }
 
    /**
     * add parent path to the filesystem
     * @param file a path to a file
     * @param stb the storage server that stores the file
     */
    private void createFile(Path file, StorageStubs stb) {
    	Path p = null;
    	do {
    		p = file.parent();
    		if (!this.fileSystem.containsKey(p)) {
    			this.fileSystem.put(p, new PathInfo(false));
    		}
			this.fileSystem.get(p).addChild(file);
    		this.fileSystem.get(p).addStbs(stb);
    		file = p;
    	} while(!p.isRoot());
    }
    
    /**
     * add parent path to the filesystem
     * @param dir A path to the target directory
     */
    private void createDirect(Path dir) {
    	Path p = null;
    	do {
    		p = dir.parent();
    		if (this.fileSystem.containsKey(p)) {
    			this.fileSystem.get(p).addChild(dir);
    			break;
    		}
    		else {
    			this.fileSystem.put(p, new PathInfo(false));
    		}
    		this.fileSystem.get(p).addChild(dir);
    		dir = p;
    	} while (!p.isRoot());
    }

    /**
     * When any object is locked for either kind of access, all objects along
     * the path up to, but not including, the object itself, are locked for
     * shared access to prevent their modification or deletion by other users.
     * @param file
     * @param dolock indicate whether it's requesting lock or unlock
     * @throws InterruptedException 
     */
    private void lockParent(Path file, boolean dolock) throws InterruptedException {
    	if (!file.isRoot()) {
    		Path p = file.parent();
    		if (dolock) {
    			this.fileSystem.get(p).getpLock().lockRead();
    		}
    		else {
        		this.fileSystem.get(p).getpLock().unlockRead();    			
    		}
    		this.lockParent(p, dolock);
    	}
    }    
}
