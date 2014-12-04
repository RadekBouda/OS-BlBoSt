/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kernel;

import process.AbstractProcess;
import process.Shell;

import java.io.File;
import java.io.PipedInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Instance of this class represents the kernel of an operating system.
 * Class is designed according to SINGLETON design pattern.
 *
 * @author Jan Blaha, Radek Bouda, David Steinberger
 * @version 1.1
 */
public class Kernel {
	/** Package with processes */
	private static final String PACKAGE = "process";
	/** PID counter */
	private int PID;
	/** Table of processes */
	private final Map<Integer, AbstractProcess> processes;
	/** Pid of main shell */
	public static final int MAIN_SHELL_PID = 1;

	/**
	 * Private constructor for singleton.
	 */
	private Kernel() {
		processes = new HashMap<Integer, AbstractProcess>();
		PID = MAIN_SHELL_PID;							// First is created main shell
		checkFolders();
	}

	/**
	 * Single instance of Kernel.
	 */
	private static final Kernel INSTANCE = new Kernel();

	/**
	 * This static method returns the instance of Kernel.
	 * @return One, always the same, instance of Kernel.
	 */
	public static Kernel getInstance(){
		return INSTANCE;
	}

	/**
	 * Checks if the virtual filesystem exists, otherwise creates a new one.
	 */
	private void checkFolders() {
		File mainDir = new File("filesystem");
		if(!mainDir.exists()) mainDir.mkdir();
	}

	/**
	 * Runs main shell.
	 */
	public void runShell() {
		AbstractProcess shell = new Shell(PID, 0, null, new ArrayList<List<String>>(), null);
		increasePID();
		processes.put(shell.getPid(), shell);
		shell.start();
	}

	/**
	 * Increments pid.
	 */
	public void increasePID(){
		PID++;
	}

	/**
	 * Creates a new process with given name and arguments.
	 *
	 * Errors:  -1 - Process not found
	 * 		   	-2 - Wrong arguments of method
	 *			-3 - Other errors
	 * @param process process name
	 * @param arguments constructor parameters
	 * @return pid or error
	 */
	public int newProcess(String process, Object[] arguments) {
		try {
			process = process.substring(0, 1).toUpperCase() +  process.substring(1, process.length()).toLowerCase(); // Makes processes case insensitive!
			Class myClass = Class.forName(PACKAGE + "." + process);		// Finds the class
			Class[] types = new Class[arguments.length];				// Types of arguments
			types[0] = int.class;										// First always pid
			types[1] = int.class;										// Second always father pid
			types[2] = PipedInputStream.class;							// Third always input stream
			types[3] = List.class;										// Fourth always List with rest of commands
			types[4] = Shell.class;										// Fifth always shell
			for(int i = 5; i < arguments.length; i++) types[i] = arguments[i].getClass();	// Fills optional arguments
			Constructor constructor = myClass.getConstructor(types);						// Gets constructor with given types.

			arguments[0] = PID;											// Sets the pid
			Object instance = constructor.newInstance(arguments);		// Initializes the object
			AbstractProcess proc = (AbstractProcess) instance;			// Retypes to AbstractProcess
			increasePID();												// Increases pid
			processes.put(proc.getPid(), proc);							// Adds to the process table
			return proc.getPid();												// Returns the process
		} catch (ClassNotFoundException e) {				// TODO: Wrong arguments etc.
			return -1;
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			return -2;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return -3;
	}

	/**
	 * Start process from process table by pid.
	 *
	 * @param pid process id
	 * @return abstractProcess
	 */
	public void startProcess(int pid) {
		processes.get(pid).start();
	}

	/**
	 * Kills process with given pid. Also kills its children.
	 * In case of no pid in process table, returns false.
	 *
	 * @param pid process id
	 * @return true/false
	 */
	public boolean killProcess(int pid) {
		if(!processes.containsKey(pid)) return false;
        AbstractProcess toKill = processes.get(pid);

		List<Integer> childsToKill = toKill.getChildPids();
		for (int i = 0; i < childsToKill.size(); i++) {
			this.killProcess(childsToKill.get(i));
		}
		
		// remove all children from the child list
		while (!childsToKill.isEmpty()) {
			childsToKill.remove(0);
		}
		
		// delete original process
		removeReference(toKill.getPid());
		toKill.kill();
		return true;
	}

	/**
	 * Kills main shell. Father of all processes.
	 */
	public void shutdown() {
		killProcess(MAIN_SHELL_PID);
	}

	/**
	 * Get processes records.
	 *
	 * @return all processes records
	 */
	public Set<Map.Entry<Integer, AbstractProcess>> getProcesses() {
		return processes.entrySet();
	}

	/**
	 * Removes a process with given pid from table of processes. It should be called only at the end of thread's run method.
	 *
	 * @param pid pid
	 */
	public void removeReference(int pid) {
		if(processes.containsKey(pid)) processes.remove(pid);
	}
}
