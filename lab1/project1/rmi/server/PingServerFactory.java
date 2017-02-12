package rmi.server;

public class PingServerFactory{
	public static PingPongServer makePingServer(){
		return new PingPongServer();
	}
}