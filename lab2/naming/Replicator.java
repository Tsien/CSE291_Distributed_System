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
	
	/**
	 * The selected stub
	 */
	private StorageStubs pickedStb;
	
	public Replicator(PathInfo pf, Path file, List<StorageStubs> stbs) {
		this.pf = pf;
		this.file = file;
		this.allStbs = new HashSet<StorageStubs>(stbs);;
	}
	
	@Override
	public void run() {
		this.allStbs.removeAll(this.pf.getStbs());
		if (this.allStbs.isEmpty()) {
			System.out.println("Error: there is no available storage servers.");
			return ;
		}
		List<StorageStubs> res = new ArrayList<StorageStubs>(allStbs);

		this.pickedStb = res.get(0);
		
		try {
			this.pickedStb.getCMD_stub().copy(file, this.pf.getStbs().iterator().next().getClient_stub());
		} catch (RMIException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pf.addStbs(this.pickedStb);
	}
	
	public StorageStubs getStub() {
		return this.pickedStb;
	}
}
