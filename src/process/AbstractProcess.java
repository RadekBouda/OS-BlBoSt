package process;

import kernel.Kernel;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO: Setters to constructor

/**
 * Abstract class which creates a primitive model for all the processes.
 *
 * @author Jan Blaha, Radek Bouda, David Steinberger
 * @version 1.0.0
 */
public abstract class AbstractProcess extends Thread {
	/** Process id */
	private int pid;
	/** Start time */
	private Date startTime;
	/** Parent process id */
	private int ParentPid;
	/** PIDs of its children */
	private List<Integer> childPids;
	/** Piped input */
	protected InputStream input;
	/** Piped output */
	protected OutputStream output;
	/** Rest of commands to execute */
	protected List<List<String>> commands;
	/** Parent shell */
	protected Shell shell;

	/**
	 * Default constructor with default settings.
	 *
	 * @param pid Process ID
	 * @param input	piped input stream
	 * @param commands list of commands
	 */
	public AbstractProcess(int pid, PipedInputStream input, List<List<String>> commands, Shell shell) {
		this.childPids = new ArrayList<Integer>();
		this.pid = pid;
		this.commands = commands;
		this.shell = shell;
		this.input = new PipedInputStream(4194304);			// MAGIC NUMBER ONLY FOR TESTING PURPOSES
		try {
			if(input != null) this.output = new PipedOutputStream(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes equals only if the PIDs are same.
	 *
	 * @param o other object
	 * @return true/false
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof AbstractProcess)) return false;

		AbstractProcess that = (AbstractProcess) o;

		if (pid != that.pid) return false;

		return true;
	}

	/**
	 * Life cycle of process.
	 */
	@Override
	public void run() {
		callSubProcess();			// Creates subprocesses
		processRun();									// Does own job
		if(!(this instanceof Shell)) removeFromTable(); // Shells does not die
	}

	/**
	 * Removes itself from process table.
	 */
	protected void removeFromTable() {
		Kernel.getInstance().removeReference(pid);
	}

	/**
	 * Processes' own job.
	 */
	protected abstract void processRun();

	/**
	 * Overridden hashCode. PID is unique identifier. Cannot be same PIDs.
	 *
	 * @return pid
	 */
	@Override
	public int hashCode() {
		return pid;
	}

	/**
	 * Recursive call of subprocesses.
	 */
	protected void callSubProcess() {
		if(commands == null || commands.size() < 1) return;	// Commands undefined or empty
		int position = commands.size() - 1;					// Last position in commands
		int arguments = commands.get(position).size() + 3;	// Arguments size depends on tokens in specific command. +2 stands for pid, input and commands list
		Object args[] = new Object[arguments];				// LEAVE PID EMPTY FOR KERNEL!
		args[1] = input;
		args[2] = commands.subList(0, position);
		args[3] = shell;
		if (arguments > 4) {								// Optional arguments
			for (int i = 4, j = 1; i < arguments; i++, j++) args[i] = commands.get(position).get(j);
		}
		AbstractProcess process = Kernel.getInstance().newProcess(commands.get(position).get(0), args);	// Get process from kernel

		if(process == null){
			return;
		}

		process.setParentPid(pid);							// Set parent pid
		addChildPid(process.getPid());
		process.start();										// Launch process
	}

	/**
	 * Add child process.
	 *
	 * @param pid
	 */
	private void addChildPid(int pid) {
		childPids.add(pid);
	}

	/**
	 * Reads string from input stream.
	 *
	 * @return string
	 */
	protected String getStringFromInput() {
		try {
			int c;
			StringBuilder builder = new StringBuilder();
			while ((c = input.read()) != -1){
				if(c != '\r'){ builder.append((char) c);}
			}
			return builder.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Checks if the process has piped input.
	 *
	 * @return true/false
	 */
	protected boolean hasPipedInput() {
		return commands.size() == 0 ? false:true;
	}

	// Getters and setters
	public int getPid() {
		return pid;
	}
	public Date getStartTime() {
		return startTime;
	}
	public int getParentPid() {
		return ParentPid;
	}
	public void setParentPid(int parentPid) {
		ParentPid = parentPid;
	}
	public List<Integer> getChildPids() {
		return childPids;
	}
	public void setChildPids(List<Integer> childPids) {
		this.childPids = childPids;
	}
}
