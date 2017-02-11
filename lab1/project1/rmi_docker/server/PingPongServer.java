import rmi.server
import rmi.RMIException


public class PingPongServer implements IntPingpongServer{
	public String ping(int idNumber) throws RMIException{
		return "Pong "+ idNumber;
	}

}