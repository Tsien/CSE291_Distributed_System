package rmi.server;

import rmi.RMIException;

public class PingPongServer implements IntPingPongServer{
	public String ping(int idNumber) throws RMIException{
		return ("Pong "+ idNumber);
	}

}