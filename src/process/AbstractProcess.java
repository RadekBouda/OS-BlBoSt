package process;

import kernel.Kernel;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO: Setters to constructor

/**
 * Abstract class which creates a primitive model for all the processes.
 * @author Jan Blaha, Radek Bouda, David Steinberger
 * @version 1.0.0
 */
public abstract class AbstractProcess extends Thread {

	private int pid;
	private Date startTime;
	private int ParentPid;
	private List<Integer> childPids;
	protected InputStream input;
	protected OutputStream output;
	protected List<List<String>> commands;

	/**
	 * Default constructor with default settings.
	 *
	 * @param pid Process ID
	 * @param input	piped input stream
	 * @param commands list of commands
	 */
	public AbstractProcess(int pid, PipedInputStream input, List<List<String>> commands) {
		this.childPids = new ArrayList<Integer>();
		this.pid = pid;
		this.commands = commands;
		this.input = new PipedInputStream(4194304);			// MAGIC NUMBER ONLY FOR TESTING PURPOSES
		try {
			if(input != null) this.output = new PipedOutputStream(input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
		if(getPid() == 0) return;	// Main shell
		callSubProcess();			// Creates subprocesses
		processRun();				// Does own job
		if(!(this instanceof Shell)) removeFromTable(); // Shells does not die
	}

	protected void removeFromTable() {
		Kernel.getInstance().removeReference(pid);
	}

	protected abstract void processRun();

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
		int arguments = commands.get(position).size() + 2;	// Arguments size depends on tokens in specific command. +2 stands for pid, input and commands list
		Object args[] = new Object[arguments];				// LEAVE PID EMPTY FOR KERNEL!
		args[1] = input;
		args[2] = commands.subList(0, position);
		if (arguments > 3) {								// Optional arguments
			for (int i = 3, j = 1; i < arguments; i++, j++) args[i] = commands.get(position).get(j);
		}
		AbstractProcess process = Kernel.getInstance().newProcess(commands.get(position).get(0), args);	// Get process from kernel

		if(process == null){
			return;
		}

		process.setParentPid(pid);							// Set parent pid
		addChildPid(process.getPid());
		process.run();										// Launch process
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
}
