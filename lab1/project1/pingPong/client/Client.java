package pingPong.client;

import java.net.*;
import pingPong.server.IntPingPongServer;
import rmi.RMIException;
import rmi.Stub;

public class Client {
	public static void main(String[] args) {
		System.out.println("==============Tests==============");
		int port = Integer.parseInt(args[0]);
		IntPingPongServer server = Stub.create(IntPingPongServer.class,
											   new InetSocketAddress(port));
		int count = 0;
		for(int i=1;i<=4;i++){
			String expectedStr = "Pong " + i;
			String receivedStr = null;
			try{
				receivedStr = server.ping(i);
				if(expectedStr.contentEquals(receivedStr)){
					count++;
				}
			}
			catch (RMIException e) {
				System.out.print("=================Client:Pinging server=======");
				e.printStackTrace();
			}
		}
		System.out.format("%d Tests Completed, %d Tests Failed", count, 4 - count);
	}
}