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
	
	/**
	 * All registered storage servers
	 */
	private HashSet<StorageStubs> allStbs;
		
	public Replicator(PathInfo pf, Path file, List<StorageStubs> stbs) {
		this.pf = pf;
		this.file = file;
		this.allStbs = new HashSet<StorageStubs>(stbs);;
	}
	
	@Override
	public void run() {
		StorageStubs stb = this.pickStb();
		System.out.println("Replicator : the selected storage is " + stb.getClient_stub());
		// TODO how to get the new stub ???
		try {
			stb.getCMD_stub().copy(file, this.pf.getStbs().get(0).getClient_stub());
		} catch (RMIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// pf.addStbs(stb);
	}
	
	/**
	 * pick a storage server from available ones
	 * @return StorageStubs a storage server
	 */
	private StorageStubs pickStb() {
		allStbs.removeAll(this.pf.getStbs());
		if (allStbs.isEmpty()) {
			System.out.println("Error: there is no available storage servers.");
			return null;
		}
		List<StorageStubs> res = new ArrayList<StorageStubs>(allStbs);
		return res.get(0);
	}
}
