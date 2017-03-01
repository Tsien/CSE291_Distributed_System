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
	 * A counter counting the number of shared access
	 */
	private int readAccess;
	
	/**
	 * A path's Children
	 */
	private HashSet<Path> children;
	
	/**
	 * Storages that store this path
	 */
	private HashSet<StorageStubs> stbs;

	/**
	 * A lock belongs to this path
	 */
	private ReadWriteLock pLock;
	
	/**
	 * A flag indicating whether this path is a file
	 */
	private boolean isFile;
	
	public PathInfo(boolean tag) {
		this.clearReadAccess();
		this.children = new HashSet<Path>();
		this.stbs = new HashSet<StorageStubs>();
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
	public HashSet<StorageStubs> getStbs() {
		return stbs;
	}

	/**
	 * @param stb the stb to add
	 */
	public void addStbs(StorageStubs stb) {
		this.stbs.add(stb);
	}

	/**
	 * @param stb the stb to add
	 */
	public void setStbs(HashSet<StorageStubs> stb) {
		this.stbs = stb;
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

	/**
	 * @return the readAccess
	 */
	public int getReadAccess() {
		return readAccess;
	}

	/**
	 * set the number of shared access to 0
	 */
	public void clearReadAccess() {
		this.readAccess = 0;
	}
	
	/**
	 * Increase the number of shared access by 1
	 */
	public void incReadAccess() {
		this.readAccess++;
	}
}
