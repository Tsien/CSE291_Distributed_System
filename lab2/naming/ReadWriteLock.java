package naming;

import java.util.HashMap;
import java.util.Map;

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
	 * The writing thread
	 */
	private Thread writer = null;
	
	/**
	 * The number of exclusive access requests
	 */
	private int writeRequests = 0;
	
	/**
	 * Record readers for reentrance
	 */
	private Map<Thread, Integer> readingThreads = new HashMap<Thread, Integer>();
	
	/**
	 * Request for shared access
	 * @throws InterruptedException 
	 */
	public synchronized void lockRead() throws InterruptedException {
		Thread cur = Thread.currentThread();
		while (!canGrantRead(cur)) {
			wait();
		}
		readingThreads.put(cur, readingThreads.getOrDefault(cur, 0) + 1);
	}
	
	/**
	 * <p>
	 * Release reading lock
	 */
	public synchronized void unlockRead() {
		Thread cur = Thread.currentThread();
		int val = readingThreads.get(cur);
		if (1 == val) {
			readingThreads.remove(cur);
		}
		else {
			readingThreads.put(cur, val - 1);
		}
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
		Thread cur = Thread.currentThread();
		while (this.canGrantWrite(cur)) {
			wait();
		}
		--writeRequests;
		++writeAccess;
		writer = Thread.currentThread();
	}
	
	/**
	 * Release writing lock
	 */
	public synchronized void unlockWrite() {
		--writeAccess;
		if (writeAccess == 0) {
			writer = null;			
		}
		notifyAll();
	}
	
	private boolean isOnlyReader(Thread cur) {
		return readingThreads.size() == 1 && readingThreads.get(cur) != null;
	}

	/**
	 * Check conditions to decide whether we should grant read access
	 * @param cur The current thread
	 * @return
	 */
	private boolean canGrantRead(Thread cur) {
		// Reader reentrance
		if (readingThreads.containsKey(cur)) return true;
		// Write-to-read reentrance
		if (writer == cur) return true;
		// if there is a writer
		if (writer != null) return false;
		// if there is someone who is waiting for writing access
		if (writeRequests > 0) return false; 
		return true;
	}
	
	/**
	 * Check conditions to decide whether we should grant write access
	 * @param cur The current thread
	 * @return
	 */
	private boolean canGrantWrite(Thread cur) {
		// Writer reentrance
		if (writer == cur) return true;
		// Read-to-write reentrance
		if (isOnlyReader(cur)) return true;
		// If someone is writing
		if (writer != null) return false;
		// if no one is reading
		if (!readingThreads.isEmpty()) return false; 
		return true;
	}
}
