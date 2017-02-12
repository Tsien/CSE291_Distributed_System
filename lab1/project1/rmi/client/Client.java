package rmi.client;
import java.net.*;

import rmi.RMIException;
import rmi.Stub;
import rmi.server.IntPingPongServer;

public class Client {
	public static void main(String[] args){

		String address = args[0];
		int port = Integer.parseInt(args[1]);
		IntPingPongServer server = Stub.create(IntPingPongServer.class,
											   new InetSocketAddress(address, port));
		int i;
		int count = 0;
		for(i=1;i<=4;i++){
			String expectedStr = "Pong "+i;
			String receivedStr = null;
			try{
				receivedStr=server.ping(i);
				if(expectedStr.contentEquals(receivedStr)){
				count++;
				}
			}
			
			catch (RMIException e) {
				System.out.print("Pinging server");
				e.printStackTrace();
			}
		}
		System.out.format("%d Tests Completed, %d Tests Failed",count,4-count);
	}
}