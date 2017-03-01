package naming;

/**
 * <p>
 * A custome Lock
 * 
 * <p>
 * Read Access   	If no threads are writing, and no threads have requested write access.
 * Write Access   	If no threads are reading or writing.
 * 
 * @author feichao
 *
 */
public class ReadWriteLock {	
	/**
	 * The number of exclusive access
	 */
	private int writeAccess = 0;
		
	/**
	 * The number of exclusive access requests
	 */
	private int writeRequests = 0;
	
	/**
	 * The number of shared access 
	 */
	private int readAccess = 0;
	
	/**
	 * Request for shared access
	 * @throws InterruptedException 
	 */
	public synchronized void lockRead() throws InterruptedException {
		while (this.writeAccess > 0 || this.writeRequests > 0) {
			wait();
		}
		this.readAccess++;
	}
	
	/**
	 * <p>
	 * Release reading lock
	 */
	public synchronized void unlockRead() {
		this.readAccess--;
		notifyAll();
	}
	
	/**
	 * <p>
	 * Request for exclusive access
	 * 
	 * <p>
	 * Write reentrance is granted only if the thread has already write access. 
	 * 
	 * @throws InterruptedException 
	 */
	public synchronized void lockWrite() throws InterruptedException {
		++writeRequests;
		while (this.readAccess > 0 || this.writeAccess > 0) {
			wait();
		}
		--writeRequests;
		++writeAccess;
	}
	
	/**
	 * Release writing lock
	 */
	public synchronized void unlockWrite() {
		--writeAccess;
		notifyAll();
	}
}
