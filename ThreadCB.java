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
 * Ilan Kleiman
 * 110942711
 * 
 * I pledge my honor that all parts of this project were done by me individually, withoutcollaboration with anyone,
 * and without consulting any external sources that providefull or partial solutions to a similar project.
 * I understand that breaking this pledge will result in an “F” for the entire course.
 */

/**
 * This class is responsible for actions related to threads, including creating,
 * killing, dispatching, resuming, and suspending threads.
 * 
 * @OSPProject Threads
 */
public class ThreadCB extends IflThreadCB {

    // TODO: check if GenericList works instead of ArrayList
    // TODO: use MyOut.print(this, "Resuming " + this), .atWarning(), .atError()...

    /**
     * Enums of the different ready queues Q1, Q2, Q3
     */
    private static enum QueueLevel {
        Q1, Q2, Q3
    }

    /**
     * Static variables
     * 
     * @see #readyQueues - the queue which contains the 3 subqueues Q1, Q2, and Q3.
     * @see #timeSlice - the amount of clock ticks each time slice should be
     */
    private static Map<String, ArrayList<E>> readyQueues;
    private static int timeSlice;

    /**
     * Instance variables
     * 
     * @see #dispatchCount - the number of times the current thread has been
     *      dispatched
     */
    private int dispatchCount;

    /**
     * The thread constructor. Must call
     * 
     * super();
     * 
     * as its first statement.
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
         * Initialize the ready queue
         */
        readyQueues = new HashMap<String, ArrayList<E>>();

        /**
         * Add the 3 sub-queues Q1 -> index 0, Q2 -> index 1, Q3 -> index 2.
         * 
         * TODO: enumerate thru the enums rather than hard coding them like this
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
        // your code goes here

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
        // your code goes here

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
            MyOut.atWarning(this, "Attempted to resume thread which wasn't waiting: " + this);
            return;
        }

        /**
         * Set the thread's new status
         */
        if (getStatus() == ThreadWaiting) {
            MyOut.print(this, "Setting thread status to ThreadReady");
            setStatus(ThreadReady);
        } else if (getStatus() > ThreadWaiting) {
            MyOut.print(this, "Setting thread status to sub ThreadWaiting: " + (getStatus() - 1));
            setStatus(getStatus() - 1);
        }

        /**
         * If the thread is ThreadReady status, then add it to our ready Queue
         */
        if (getStatus() == ThreadReady) {
            move_to_ready_queue(this);
        }

        /**
         * Call dispatch so that the thread can be dispatched onto the CPU for execution
         */
        dispatch();
    }

    /**
     * Selects a thread from the run queue and dispatches it.
     * 
     * If there is just one theread ready to run, reschedule the thread currently on
     * the processor.
     * 
     * In addition to setting the correct thread status it must update the PTBR.
     * 
     * @return SUCCESS or FAILURE
     * 
     * @OSPProject Threads
     */
    public static int do_dispatch() {
        // your code goes here

    }

    /**
     * Called by OSP after printing an error message. The student can insert code
     * here to print various tables and data structures in their state just after
     * the error happened. The body can be left empty, if this feature is not used.
     * 
     * @OSPProject Threads
     */
    public static void atError() {
        // your code goes here

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
        // your code goes here

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
     * @param thread
     */
    private static void move_to_ready_queue(ThreadCB thread) {
        if (thread.dispatchCount == 0) {
            /**
             * Check if the thread exists in any of the other 2 queues before we put it in.
             */
            if ((readyQueues.get(QueueLevel.Q2)).contains(thread)) {
                (readyQueues.get(QueueLevel.Q2)).remove(thread);
            } else if ((readyQueues.get(QueueLevel.Q3)).contains(thread)) {
                (readyQueues.get(QueueLevel.Q3)).remove(thread);
            }

            /**
             * Then finally add the thread to it's destination list
             */
            (readyQueues.get(QueueLevel.Q1)).append(thread);
        }
    }

}

/*
 * Feel free to add local classes to improve the readability of your code
 */
