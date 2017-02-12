package pingPong.server;

import java.net.*;
import rmi.RMIException;
import rmi.Skeleton;

public class Server {
	public static void main(String[] args){
		int port = Integer.parseInt(args[0]);
		IntPingPongServer server = PingServerFactory.makePingServer();
		Skeleton<IntPingPongServer> skeleton = new Skeleton<IntPingPongServer>(IntPingPongServer.class, server, new InetSocketAddress(port));

		try{
			skeleton.start();
		}catch(RMIException e){
			System.out.println("Trying to start skeleton");
			e.printStackTrace();
		}

	}
}