/**
 * Ilan Kleiman
 * 110942711
 * 
 * I pledge my honor that all parts of this project were done by me individually, withoutcollaboration with anyone,
 * and without consulting any external sources that providefull or partial solutions to a similar project.
 * I understand that breaking this pledge will result in an “F” for the entire course.
 */

package osp.Threads;

import java.util.Vector;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

/**
 * This class is responsible for actions related to threads, including creating,
 * killing, dispatching, resuming, and suspending threads.
 * 
 * @OSPProject Threads
 */
public class ThreadCB extends IflThreadCB {
    /**
     * Enums of the different ready queues Q1, Q2, Q3
     */
    private static enum QueueLevel {
        Q1, Q2, Q3;

        private static QueueLevel[] enums = values();

        public QueueLevel next() {
            return enums[(this.ordinal() + 1) % enums.length];
        }
    }

    /**
     * Static variables
     * 
     * @see #cyclesForQueues - The different cycles that are possible and which
     *      queue they should correspond to. This is populated by init() - see there
     *      for configuring.
     * @see #readyQueues - the queue which contains the 3 subqueues Q1, Q2, and Q3.
     * @see #timeSlice - the amount of clock ticks each time slice should be
     * @see #currentCycle - the current cycle number that the system is on.
     */
    private static Map<QueueLevel, ArrayList<Integer>> cyclesForQueues;
    private static Map<QueueLevel, ArrayList<E>> readyQueues;
    private static int timeSlice;
    private static int currentCycle;

    /**
     * Instance variables
     * 
     * @see #dispatchCount - the number of times the current thread has been
     *      dispatched
     */
    public int dispatchCount;

    /**
     * The thread constructor. Must call super() as its first statement.
     * 
     * @OSPProject Threads
     */
    public ThreadCB() {
        super();

        MyOut.print(this, "A new ThreadCB object was created");

        /**
         * Initialize any instance variables that must be set when each thread object is
         * created - b/c init() isn't being called on each thread object
         */
        this.dispatchCount = 0;
    }

    /**
     * This method will be called once at the beginning of the simulation. The
     * student can set up static variables here.
     * 
     * @OSPProject Threads
     */
    public static void init() {
        /**
         * Initialize the map of queue levels and which cycles they correspond to.
         */
        cyclesForQueues = new HashMap<QueueLevel, ArrayList<Integer>>();
        cyclesForQueues.put(QueueLevel.Q1, new ArrayList<Integer>(List.of(1, 2, 3)));
        cyclesForQueues.put(QueueLevel.Q2, new ArrayList<Integer>(List.of(4, 5)));
        cyclesForQueues.put(QueueLevel.Q3, new ArrayList<Integer>(List.of(6)));

        /**
         * Initialize the ready queue
         */
        readyQueues = new HashMap<QueueLevel, ArrayList<E>>();

        /**
         * Add the 3 sub-queues Q1 -> index 0, Q2 -> index 1, Q3 -> index 2.
         */
        readyQueues.put(QueueLevel.Q1, new ArrayList<>());
        readyQueues.put(QueueLevel.Q2, new ArrayList<>());
        readyQueues.put(QueueLevel.Q3, new ArrayList<>());

        /**
         * Set the time slice - instructed by manual (in clock ticks)
         */
        timeSlice = 80;
    }

    /**
     * Sets up a new thread and adds it to the given task. The method must set the
     * ready status and attempt to add thread to task. If the latter fails because
     * there are already too many threads in this task, so does this method,
     * otherwise, the thread is appended to the ready queue and dispatch() is
     * called.
     * 
     * The priority of the thread can be set using the getPriority/setPriority
     * methods. However, OSP itself doesn't care what the actual value of the
     * priority is. These methods are just provided in case priority scheduling is
     * required.
     * 
     * @return thread or null
     * 
     * @OSPProject Threads
     */
    static public ThreadCB do_create(TaskCB task) {
        if (task == null || task.getThreadCount() >= MaxThreadsPerTask) {
            MyOut.print("Attempted to create a thread with a null task.");
            dispatch();
            return null;
        }

        ThreadCB thread = new ThreadCB();
        thread.setTask(task);
        thread.setStatus(ThreadReady);

        if (task.addThread(thread) != SUCCESS) {
            thread.setTask(null);
            thread.setStatus(null);

            dispatch();
            return null;
        }

        /**
         * Move the thread to its appropriate ready queue. It should move it to Q1
         * initially.
         */
        move_to_ready_queue(thread);

        dispatch();
        return thread;
    }

    /**
     * Kills the specified thread.
     * 
     * The status must be set to ThreadKill, the thread must be removed from the
     * task's list of threads and its pending IORBs must be purged from all device
     * queues.
     * 
     * If some thread was on the ready queue, it must removed, if the thread was
     * running, the processor becomes idle, and dispatch() must be called to resume
     * a waiting thread.
     * 
     * @OSPProject Threads
     */
    public void do_kill() {
        // your code goes here
        return;
    }

    /**
     * Suspends the thread that is currenly on the processor on the specified event.
     * 
     * Note that the thread being suspended doesn't need to be running. It can also
     * be waiting for completion of a pagefault and be suspended on the IORB that is
     * bringing the page in.
     * 
     * Thread's status must be changed to ThreadWaiting or higher, the processor set
     * to idle, the thread must be in the right waiting queue, and dispatch() must
     * be called to give CPU control to some other thread.
     * 
     * @param event - event on which to suspend this thread.
     * 
     * @OSPProject Threads
     */
    public void do_suspend(Event event) {
        if (getStatus() == ThreadRunning) {
            setStatus(ThreadWaiting);

            // attempt to set the current thread the cpu is running to null
            if (MMU.getPTBR() == null) {
                MyOut.atWarning(this, "MMU.getPTBR() returned null when expected a non-null object");
            } else {
                MMU.setPTBR(null);
            }

            // attempt to set the task of the thread to null
            getTask().setCurrentThread(null);
        } else if (getStatus() >= ThreadWaiting) {
            // wait moreso b/c of the suspend
            setStatus(getStatus() + 1);
        } else {
            // in the case where you try to suspend a killed or other unknown status.
            setStatus(ThreadWaiting);
        }

        // TODO: ??
        if (!event.contains(this)) {
            event.addThread(this);
        }

        /**
         * NOTE: says we need to make sure the thread is in the right waiting queue, but
         * we shouldn't have to update that... Since no dispatches directly occur here -
         * then no thread increment of dispatches, which means we shouldn't have to move
         * where the thread exists in w/e queue.
         */

        dispatch();
        return;
    }

    /**
     * Resumes the thread.
     * 
     * Only a thread with the status ThreadWaiting or higher can be resumed. The
     * status must be set to ThreadReady or decremented, respectively. A ready
     * thread should be placed on the ready queue.
     * 
     * @OSPProject Threads
     */
    public void do_resume() {
        /**
         * Check if the thread is in a status that we can resume from
         */
        if (getStatus() < ThreadWaiting) {
            MyOut.print(this, "Attempted to resume thread which wasn't waiting: " + this);
            return;
        }

        /**
         * Set the thread's new status if was waiting, then now make it ready.
         */
        if (getStatus() == ThreadWaiting) {
            MyOut.print(this, "Setting thread status to ThreadReady");
            setStatus(ThreadReady);
            move_to_ready_queue(this);

            dispatch();
            return;
        }

        /**
         * The thread was waiting,.. Now waiting less than before, and if that means
         * it's now thread-ready - also add it to the ready queue.
         */
        if (getStatus() > ThreadWaiting) {
            MyOut.print(this, "Setting thread status to sub ThreadWaiting: " + (getStatus() - 1));
            setStatus(getStatus() - 1);

            /**
             * If the new status of the thread is now ready, then it can be added to the
             * ready queue.
             * 
             * NOTE: not sure if this is correct...
             */
            if (getStatus() == ThreadReady) {
                move_to_ready_queue(this);
            }

            dispatch();
            return;
        }

        /**
         * One of the prior three conditions should've been met... This technically
         * shouldn't be reachable.
         */
        dispatch();
        return;
    }

    /**
     * Selects a thread from the run queue and dispatches it.
     * 
     * If there is just one thread ready to run, reschedule the thread currently on
     * the processor.
     * 
     * In addition to setting the correct thread status it must update the PTBR.
     * 
     * @return SUCCESS or FAILURE
     * 
     * @OSPProject Threads
     */
    public static int do_dispatch() {
        if (get_total_ready_threads() == 0) {
            MyOut.print("There are no threads ready to dispatch.");
            return FAILURE;
        }

        if (MMU.getPTBR() != null && MMU.getPTBR().getTask() != null
                && MMU.getPTBR().getTask().getCurrentThread() != null) {

            /**
             * There is currently a thread running, so reschedule the currently running
             * thread, and dispatch a new one from the appropriate ready queue.
             */
            ThreadCB currentThread = MMU.getPTBR().getTask().getCurrentThread();
            currentThread.getTask().setCurrentThread(null);
            move_to_ready_queue(currentThread);

            // Now get the next thread up & dispatch it
            ThreadCB nextThread = get_next_thread_at_level(get_current_queue_level());
            if (nextThread == null) {
                MyOut.print("Couldn't find a new thread to dispatch.");
                return FAILURE;
            }

            nextThread.setStatus(ThreadRunning);
            MMU.setPTBR(nextThread.getTask().getPageTable());
            nextThread.getTask().setCurrentThread(nextThread);

            // set hardware interrupt to occur in specified timeslice.
            HTimer.set(timeSlice);

            return SUCCESS;
        } else {
            /**
             * There are no threads currently running, so only get a thread from the ready
             * queue and dispatch it.
             */
            ThreadCB nextThread = get_next_thread_at_level(get_current_queue_level());
            if (nextThread == null) {
                MyOut.print("Couldn't find a new first thread to dispatch.");
                return FAILURE;
            }

            nextThread.setStatus(ThreadRunning);
            MMU.setPTBR(nextThread.getTask().getPageTable());
            nextThread.getTask().setCurrentThread(nextThread);

            // set hardware interrupt to occur in specified timeslice.
            HTimer.set(timeSlice);

            return SUCCESS;
        }
    }

    /**
     * Called by OSP after printing an error message. The student can insert code
     * here to print various tables and data structures in their state just after
     * the error happened. The body can be left empty, if this feature is not used.
     * 
     * @OSPProject Threads
     */
    public static void atError() {
        return;
    }

    /**
     * Called by OSP after printing a warning message. The student can insert code
     * here to print various tables and data structures in their state just after
     * the warning happened. The body can be left empty, if this feature is not
     * used.
     * 
     * @OSPProject Threads
     */
    public static void atWarning() {
        return;
    }

    /**
     * Adds the provided thread to a one of the 3 ready queues: Q1, Q2, Q3. This
     * method will automatically remove the thread from an existing queue (given
     * that it is in one).
     * 
     * -> If thread in Q1 gets dispatched a 4th time then it gets demoted to Q2
     * after it's preempted.
     * 
     * -> If a thread in Q2 gets dispatched an 8th time then it gets demoted to Q3
     * after it's preempted.
     * 
     * -> Threads in Q3 stay there until termination.
     * 
     * @param ThreadCB thread - thread to move to a ready queue
     */
    private static void move_to_ready_queue(ThreadCB thread) {
        if (thread.dispatchCount < 4) {
            thread.setPriority(QueueLevel.Q1);
            (readyQueues.get(QueueLevel.Q1)).add(thread);
        } else if (thread.dispatchCount < 8) {
            thread.setPriority(QueueLevel.Q2);
            (readyQueues.get(QueueLevel.Q2)).add(thread);
        } else {
            thread.setPriority(QueueLevel.Q3);
            (readyQueues.get(QueueLevel.Q3)).add(thread);
        }
    }

    /**
     * Get the total (sum) number of threads in all of the queues.
     * 
     * @return
     */
    private static int get_total_ready_threads() {
        return (readyQueues.get(QueueLevel.Q1)).size() + (readyQueues.get(QueueLevel.Q2)).size()
                + (readyQueues.get(QueueLevel.Q3)).size();
    }

    /**
     * Determine which queue level the current cycle corresponds to.
     * 
     * @return QueueLevel
     */
    private static QueueLevel get_current_queue_level() {
        if ((cyclesForQueues.get(QueueLevel.Q1)).contains(currentCycle)) {
            return QueueLevel.Q1;
        } else if ((cyclesForQueues.get(QueueLevel.Q2)).contains(currentCycle)) {
            return QueueLevel.Q2;
        } else if ((cyclesForQueues.get(QueueLevel.Q3)).contains(currentCycle)) {
            return QueueLevel.Q3;
        } else {
            return null;
        }
    }

    /**
     * Get the next thread to dispatch in the ready queues.
     * 
     * @param QueueLevel level - the queue level to get a thread from
     * @return
     */
    private static ThreadCB get_next_thread_at_level(QueueLevel level) {
        if ((readyQueues.get(level)).size() > 0) {
            return (readyQueues.get(level)).remove(0);
        } else {
            if (level == QueueLevel.Q3) {
                /**
                 * All Queues are empty, and there's nothing to do.
                 */
                return null;
            } else {
                /**
                 * Recursively go to the next queue level and determine if that queue level has
                 * a thread to return.
                 */
                return get_next_thread_at_level(level.next());
            }
        }
    }

}
