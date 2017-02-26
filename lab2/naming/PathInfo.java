package naming;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import common.Path;

/**
 * A class wrapping all information related to a path
 * @author feichao
 *
 */
public class PathInfo {
	/**
	 * A path's Children
	 */
	private HashSet<Path> children;
	
	/**
	 * Storages that store this path
	 */
	private List<StorageStubs> stbs;

	/**
	 * A lock belongs to this path
	 */
	private ReadWriteLock pLock;
	
	/**
	 * A flag indicating whether this path is a file
	 */
	private boolean isFile;
	
	public PathInfo(boolean tag) {
		this.children = new HashSet<Path>();
		this.stbs = new ArrayList<StorageStubs>();
		this.setpLock(new ReadWriteLock());
		this.setFile(tag);
	}

	/**
	 * @return the children
	 */
	public HashSet<Path> getChildren() {
		return children;
	}

	/**
	 * @param children the children to add
	 */
	public void addChild(Path child) {
		this.children.add(child);
	}

	/**
	 * @param child the child to remove
	 */
	public void rmvChild(Path child) {
		this.children.remove(child);
	}
	/**
	 * @return the stbs
	 */
	public List<StorageStubs> getStbs() {
		return stbs;
	}

	/**
	 * @param stb the stb to add
	 */
	public void addStbs(StorageStubs stb) {
		this.stbs.add(stb);
	}

	/**
	 * @return the pLock
	 */
	public ReadWriteLock getpLock() {
		return pLock;
	}

	/**
	 * @param pLock the pLock to set
	 */
	public void setpLock(ReadWriteLock pLock) {
		this.pLock = pLock;
	}

	/**
	 * @return the isFile
	 */
	public boolean isFile() {
		return isFile;
	}

	/**
	 * @param isFile the isFile to set
	 */
	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}
}
