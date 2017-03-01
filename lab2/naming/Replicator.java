package naming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import common.Path;
import rmi.RMIException;

/**
 * Make a copy of a file 
 * @author feichao
 *
 */
public class Replicator implements Runnable {
	
	/**
	 * A file to be copied
	 */
	private Path file;

	/**
	 * Information related to this path
	 */
	private PathInfo pf;
		
	public Replicator(PathInfo pf, Path file) {
		this.pf = pf;
		this.file = file;
	}
	
	@Override
	public void run() {
		StorageStubs stb = this.pf.getStbs().get(0);
		System.out.println("Replicator : the selected storage is " + stb.getClient_stub());
		// TODO Auto-generated method stub
		try {
			stb.getCMD_stub().copy(file, stb.getClient_stub());
		} catch (RMIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pf.addStbs(stb);
	}
}
