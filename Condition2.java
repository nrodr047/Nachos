package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;

/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see	nachos.threads.Condition
 */
public class Condition2 {

	/**
     * Allocate a new condition variable.
     *
     * @param	conditionLock	the lock associated with this condition
     *				variable. The current thread must hold this
     *				lock whenever it uses <tt>sleep()</tt>,
     *				<tt>wake()</tt>, or <tt>wakeAll()</tt>.
     */
    @SuppressWarnings("rawtypes")
	public Condition2(Lock conditionLock) {
	this.conditionLock = conditionLock;
	waitQueue = new LinkedList(); // LinkedList for the queue - NR (task 2)
    }

    /**
     * Atomically release the associated lock and go to sleep on this condition
     * variable until another thread wakes it using <tt>wake()</tt>. The
     * current thread must hold the associated lock. The thread will
     * automatically reacquire the lock before <tt>sleep()</tt> returns.
     */
    public void sleep() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	
	flag = 0; // flag
	//adds value to queue -NR
	waitQueue.add(flag);
	
	//release lock and sleep until other threads wake
	conditionLock.release();
	
	//disable the interrupt for atomicity - NR (task 2)
	boolean status = Machine.interrupt().disable();
	if (flag == 0)
	{
		//current thread waiting for access
		waitThreadQueue.waitForAccess(KThread.currentThread());
		//pause execution for thread.
		KThread.sleep(); 
		
	}
	else
	{
		flag--;
	}
	
	//interrupt
	Machine.interrupt().restore(status);

	conditionLock.acquire();
    }

    /**
     * Wake up at most one thread sleeping on this condition variable. The
     * current thread must hold the associated lock.
     */
    public void wake() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	//disable interrupt 
	boolean status = Machine.interrupt().disable();
	
	//if queue is empty, remove the first waitQueue thread
	//if there is a thread the next thread will execute 	
	if(!waitQueue.isEmpty())
	{
		waitQueue.removeFirst();
		KThread thread = waitThreadQueue.nextThread();
		if(thread != null)
		{
			thread.ready();
		}
		else
		{
			flag++;
		}
	}
	//restore interrupt 
	Machine.interrupt().restore(status);
	
    }

    /**
     * Wake up all threads sleeping on this condition variable. The current
     * thread must hold the associated lock.
     */
    public void wakeAll() {
	Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	while(!waitQueue.isEmpty())
		wake(); //wake all sleep threads --End of task 2 NR
    }

    private Lock conditionLock;
    private LinkedList waitQueue;
    private int flag;
    private ThreadQueue waitThreadQueue = ThreadedKernel.scheduler.newThreadQueue(false);
    
}
