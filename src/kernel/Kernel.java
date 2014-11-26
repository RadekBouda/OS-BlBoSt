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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instance of this class represents the kernel of an operating system.
 * Class is designed according to SINGLETON design pattern.
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

	/**
	 * Private constructor for singleton.
	 */
	private Kernel() {
		processes = new HashMap<Integer, AbstractProcess>();
		PID = 0;
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
	 * Checks if the filesystem exists, otherwise creates a new one.
	 */
	private void checkFolders() {
		File mainDir = new File("filesystem");
		if(!mainDir.exists()) mainDir.mkdir();
	}

	/**
	 * Runs main shell.
	 */
	public void runShell() {
		AbstractProcess shell = new Shell(PID, null, null);
		processes.put(shell.getPid(), shell);
		shell.run();
		increasePID();
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
	 * @param process
	 * @param arguments
	 * @return
	 */
	public AbstractProcess newProcess(String process, Object[] arguments) {
		try {
			process = process.substring(0, 1).toUpperCase() +  process.substring(1, process.length()).toLowerCase(); // Makes processes case insensitive!
			Class myClass = Class.forName(PACKAGE + "." + process);		// Finds the class
			Class[] types = new Class[arguments.length];				// Types of arguments
			types[0] = int.class;										// First always pid
			types[1] = PipedInputStream.class;							// Second always input stream
			types[2] = List.class;										// Third always List with rest of commands
			for(int i = 3; i < arguments.length; i++) types[i] = arguments[i].getClass();	// Fills optional arguments
			Constructor constructor = myClass.getConstructor(types);				// Gets constructor with given types.

			arguments[0] = PID;			// Sets the pid
			Object instance = constructor.newInstance(arguments);		// Initializes the object
			AbstractProcess proc = (AbstractProcess) instance;			// Retypes to AbstractProcess
			increasePID();												// Increases pid
			processes.put(proc.getPid(), proc);							// Adds to the process table
			return proc;												// Returns the process
		} catch (ClassNotFoundException e) {				// TODO: Wrong arguments etc.
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	// TODO: Killing
	public int killProcess(int pid) {
		if(!processes.containsKey(pid)) return 1;
		return 0;
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
