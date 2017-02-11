import rmi.server
import rmi.RMIException

public class PingServerFactory{
	public static PingServer makePingServer(){
		return new PingPongServer();
	}
}