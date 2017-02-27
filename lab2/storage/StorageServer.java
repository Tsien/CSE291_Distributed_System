package storage;

import java.io.*;
import java.net.*;
import java.util.*;
//import java.lang.IndexOutofBoundsException;

import common.*;
import rmi.*;
import naming.*;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{
    /** Creates a storage server, given a directory on the local filesystem, and
        ports to use for the client and command interfaces.

        <p>
        The ports may have to be specified if the storage server is running
        behind a firewall, and specific ports are open.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @param client_port Port to use for the client interface, or zero if the
                           system should decide the port.
        @param command_port Port to use for the command interface, or zero if
                            the system should decide the port.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */

    Skeleton<Storage> StorageSkt;
    Skeleton<Command> CommandSkt;
    InetSocketAddress clientAddr, cmdAddr;
    int client_port, command_port;
    File rootdir;

    //Registartion register;
    public StorageServer(File root, int client_port, int command_port)
    { 
        client_port  = client_port; 
        command_port = command_port;
        rootdir = root;
    }

    /** Creats a storage server, given a directory on the local filesystem.

        <p>
        This constructor is equivalent to
        <code>StorageServer(root, 0, 0)</code>. The system picks the ports on
        which the interfaces are made available.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
     */
    public StorageServer(File root)
    {
        this(root, 0, 0);
    }

    public void pruneDupeFiles(Path[] dupFilesLists)
    {
        for(Path path: dupFilesLists)
        {
            try
            { 
                if(true == (new File(rootdir + path.toString())).delete())
                {
                    Path parentDir = path.parent();
                    while(!parentDir.isRoot())
                    {
                        File file = new File(rootdir + parentDir.toString());
                        if(file.list().length == 0)
                            file.delete();
                        else
                        {
                            break;
                        }
                        parentDir = parentDir.parent();
                    }
                }
            }
            catch(Exception e)
            {
                System.out.println("could't delete files");
            }
        }
    }
    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    public synchronized void start(String hostname, Registration naming_server)
        throws RMIException, UnknownHostException, FileNotFoundException
    {
        Storage hStorage;
        Command hCommand;

        clientAddr = new InetSocketAddress(hostname, client_port);
        cmdAddr    = new InetSocketAddress(hostname, command_port);
        StorageSkt = new Skeleton<Storage>(Storage.class, this, clientAddr);
        CommandSkt = new Skeleton<Command>(Command.class, this, cmdAddr);

        StorageSkt.start();
        CommandSkt.start();
        hStorage = Stub.create(Storage.class, StorageSkt);
        hCommand = Stub.create(Command.class, CommandSkt);

        try
        {
            Path dupFilesLists[];
            dupFilesLists = naming_server.register(hStorage, hCommand, Path.list(rootdir));
            pruneDupeFiles(dupFilesLists);
        }
        catch(RMIException ex)
        {
            System.out.println("storage Registration failed!!!");
        }
    }

    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
        //throw new UnsupportedOperationException("not implemented");
    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
    }

    protected boolean checkIfFileExists(File file) 
    {
        if(file.exists() && !file.isDirectory())
        {
            return true;
        }
        return false;
    }
    // The following methods are documented in Storage.java.
    @Override
    public synchronized long size(Path path) throws FileNotFoundException
    { 
        File file = new File(rootdir + path.toString()); 
        if(!checkIfFileExists(file))
            throw new FileNotFoundException(path.toString());
        FileInputStream fstream = new FileInputStream(file);
        try
        { 
            return fstream.available();
        }
        catch(IOException ioEx)
        {
            throw new FileNotFoundException();
        }
    }

    @Override
    public synchronized byte[] read(Path path, long offset, int length)
        throws FileNotFoundException, IOException
    {
        File file = new File(rootdir + path.toString());
        if(offset < 0 || length < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        if(!checkIfFileExists(file))
            throw new FileNotFoundException(path.toString()); 
        FileInputStream fstream = new FileInputStream(file);
        if(fstream.available() >= offset + length)
        {
            byte[] readBuff = new byte[length];
            try
            { 
                fstream.read(readBuff, (int) offset, length);
            }
            catch (IOException ioEx)
            {
                throw ioEx;
            }
            return readBuff;
        }
        else
        {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public synchronized void write(Path path, long offset, byte[] data)
        throws FileNotFoundException, IOException
    {
        File file = new File(rootdir + path.toString());
        if(offset < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        if(!checkIfFileExists(file))
            throw new FileNotFoundException(path.toString()); 
        if(null == data)
        {
            throw new NullPointerException();
        }

        FileOutputStream fstream;
        try
        {
            long sz = size(path);
            if(sz < offset)
            { 
                System.out.println("appending");
                System.out.print("data.length: ");
                System.out.println(data.length);
                fstream = new FileOutputStream(file, true);
                while(sz++ < offset)
                { 
                    fstream.write(0);
                }
                fstream.write(data);
            }
            else
            {
                fstream = new FileOutputStream(file);
                fstream.write(data, (int) offset, data.length);
            }
        }
        catch(IOException ioEx)
        {
            System.out.println("IoException");
            throw ioEx;
        }
        fstream.close();
    }

    // The following methods are documented in Command.java.
    @Override
    public synchronized boolean create(Path file)
    {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public synchronized boolean delete(Path path)
    {    
        System.out.println("deleting files");
        return false;
    }

    @Override
    public synchronized boolean copy(Path file, Storage server)
        throws RMIException, FileNotFoundException, IOException
    {
        throw new UnsupportedOperationException("not implemented");
    }
}
