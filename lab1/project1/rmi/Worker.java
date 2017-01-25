/**
 * @author Feichao Qian (feqian@ucsd.edu)
 */

package rmi;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * The worker processes each request from the client.
 * @author feichao
 *
 */
public class Worker extends Thread {
	private Socket client;
	
	public Worker(Socket ct) {
		client = ct;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Create I/O streams for communicating to the client
		try {
			DataInputStream is = new DataInputStream(client.getInputStream());
			DataOutputStream os = new DataOutputStream(client.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
