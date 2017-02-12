package rmi.server;

import rmi.RMIException;


public interface IntPingPongServer{
	public String ping(int idNumber) throws RMIException;
}