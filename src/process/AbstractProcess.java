package process;

import kernel.Kernel;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
	private long startTime;
	/** Parent process id */
	private int parentPid;
	/** PIDs of its children */
	private List<Integer> childPids;
	/** Piped input */
	protected InputStream input;
	/** Piped output */
	protected OutputStream output;
	/** Rest of commands to execute */
	protected List<List<String>> commands;
	/** Parent shell. In case of shell, shell itself. Shell to forward children. */
	protected Shell shell;

	/** Pipe buffer size. 4MB */
	public static final int PIPE_BUFFER_SIZE = 4194304;

	/**
	 * Default constructor with default settings.
	 *
	 * @param pid Process ID
	 * @param parentPid process id of parent
	 * @param input	piped input stream
	 * @param commands list of commands
	 * @param shell parent shell
	 */
	public AbstractProcess(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell) {
		this.childPids = new ArrayList<Integer>();
		this.parentPid = parentPid;
		this.pid = pid;
		this.commands = commands;
		this.shell = shell;
		this.startTime = System.currentTimeMillis();
		this.input = new PipedInputStream(PIPE_BUFFER_SIZE);
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
		callSubProcess();				// Creates subprocesses
		processRun();					// Does own job
		removeFromTable(); 				// Shells does not die
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
		int arguments = commands.get(position).size() + 4;	// Arguments size depends on tokens in specific command. +4 stands for pid, parentPid, input, shell and commands list
		Object args[] = new Object[arguments];				// LEAVE PID EMPTY FOR KERNEL!
		args[1] = pid;
		args[2] = input;
		args[3] = commands.subList(0, position);
		args[4] = shell;
		if (arguments > 5) {								// Optional arguments
			for (int i = 5, j = 1; i < arguments; i++, j++) args[i] = commands.get(position).get(j);
		}
		AbstractProcess process = Kernel.getInstance().newProcess(commands.get(position).get(0), args);	// Get process from kernel

		if(process == null) {
			try {
				output.write((commands.get(position).get(0) + " is not a valid process!\n").getBytes());	// Send error to pipe
				output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

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
	 * Interrupts process.
	 */
	public void kill() {
		this.interrupt();
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
	public long getStartTime() {
		return startTime;
	}
	public int getParentPid() {
		return parentPid;
	}
	public List<Integer> getChildPids() {
		return childPids;
	}
}
