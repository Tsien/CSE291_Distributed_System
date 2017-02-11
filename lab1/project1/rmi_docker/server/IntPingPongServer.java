import rmi.server
import rmi.RMIException

public interface IntPingPongServer(args){
	public String ping(int idNumber) throws RMIException;
}