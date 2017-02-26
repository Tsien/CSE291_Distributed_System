package storage;

import java.io.*;
import java.net.*;

import common.*;
import rmi.*;
import naming.*;

public class CommandServerImplementation implements Command
{
    /** Creates a file on the storage server.

        @param file Path to the file to be created. The parent directory will be
                    created if it does not exist. This path may not be the root
                    directory.
        @return <code>true</code> if the file is created; <code>false</code>
                if it cannot be created.
        @throws RMIException If the call cannot be completed due to a network
                             error.
     */
    public boolean create(Path file) throws RMIException
    {
        return true;
    }

    /** Deletes a file or directory on the storage server.

        <p>
        If the file is a directory and cannot be deleted, some, all, or none of
        its contents may be deleted by this operation.

        @param path Path to the file or directory to be deleted. The root
                    directory cannot be deleted.
        @return <code>true</code> if the file or directory is deleted;
                <code>false</code> otherwise.
        @throws RMIException If the call cannot be completed due to a network
                             error.
     */
    public boolean delete(Path path) throws RMIException
    {
        return true;
    }

    /** Copies a file from another storage server.

        @param file Path to the file to be copied.
        @param server Storage server from which the file is to be downloaded.
        @return <code>true</code> if the file is successfully copied;
                <code>false</code> otherwise.
        @throws FileNotFoundException If the file is not present on the remote
                                      storage server, or the path refers to a
                                      directory.
        @throws IOException If an I/O exception occurs either on the remote or
                            on this storage server.
        @throws RMIException If the call cannot be completed due to a network
                             error, whether between the caller and this storage
                             server, or between the two storage servers.
     */
    public boolean copy(Path file, Storage server)
        throws RMIException, FileNotFoundException, IOException
        {
            return true;
        }
}
