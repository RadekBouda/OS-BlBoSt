package process;

import console.Console;
import console.ConsoleWindow;
import helpers.Parser;
import kernel.Kernel;
import kernel.Run;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

// TODO: Man page
// TODO: Builtin commands man page

/**
 * Shell is a user interface for access to an operating system's services.
 *
 * @Author David Steinberger
 */
public class Shell extends AbstractProcess {

	/** Console window */
	private ConsoleWindow consoleWindow;
	/** Console */
	private Console console;
	/** Console input pipe */
	private PipedInputStream consoleInput;
	/** Current path */
	private String path;
	/** Root path */
	private String root;
	/** Is running or hit the exit command */
	private boolean running;
	/** Basic process or whole console */
	private boolean process;
	/** PID of current running process */
	private int runningProcess;

	/** Virtual filesystem location */
	private static final String PATH_PREFIX = "filesystem" + File.separatorChar;

	/**
	 * Create new shell with own window.
	 *
	 * @param pid process ID
	 * @param parentPid process id of parent
	 * @param input PipedInputStream
	 * @param commands list of commands,
	 * @param shell parent shell
	 */
	public Shell (int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell){
		super(pid, parentPid, input, commands, shell);
		try {
			this.shell = this;                                    	// Shell to be forwarded
			this.running = true;
			this.root = new File(PATH_PREFIX).getCanonicalPath();
			this.path = this.root;
			if (commands.size() > 0) processInit();					// Normal process
			else consoleInit();										// Console
		} catch (IOException e) {}
	}

	/**
	 * Initialization for basic process. Blocking parent shell.
	 */
	private void processInit() {
		this.consoleInput = (PipedInputStream) this.input;			// Read commands from previous process
		this.process = true;										// Set process flag
	}

	/**
	 * Console process initialization.
	 *
	 * @throws IOException
	 */
	private void consoleInit() throws IOException {
		if (this.output != null) this.output.close();        	// Doesn't block parent shell!
		this.consoleWindow = new ConsoleWindow(this);
		this.console = consoleWindow.console;
		console.startConsole(getConsolePrefix());       	    // Everything is ready! Print welcome text.
		this.process = false;
	}

	/**
	 * Own job. Doesn't block previous shell!
	 */
	@Override
	protected void processRun() {
		try {
			int c;
			while(running) {										// Loop until running
				StringBuilder builder = new StringBuilder();
				c = consoleInput.read();
				while (c != -1 && c != '\n') {						// End of pipe or end of line
					builder.append((char) c);
					c = consoleInput.read();
				}
				executeCommand(builder.toString());					// Executes parsed commands
				if(c == -1) {										// In case of process
					output.close();									// Stop blocking parent shell
					return;
				}
			}
		} catch (IOException e) {
			return;
		}
	}

	/**
	 * Execute command, when the line comes from console.
	 *
	 * @param line command
	 */
	public void executeCommand(String line) {
		if(!process) console.setInCommand(true);							// Console inside command
		Parser parser = new Parser(line);   								// Parses the line
		commands = parser.getAllCommands();
		this.input = new PipedInputStream(PIPE_BUFFER_SIZE);
		redirectInput(parser.getInputFile());
		runningProcess = callSubProcess();
		if(!running) return; 												// Self killing check
		printOutput(parser.getOutputFile());
		if(!running) return; 												// Self killing check
		if(!process) console.setInCommand(false);							// Console outside command
	}

	/**
	 * Checks builtin commands. In case of builtin command no further execution is proceeded.
	 *
	 * @param command parsed commands
	 * @return true/false
	 */
	public boolean builtinCommand(List<String> command, PipedInputStream input) {
		if(command.get(0).equals("cd")) {
			if(command.size() < 2) return true;
			cd(command.get(1));
			return true;
		}
		if(command.get(0).equals("pwd")) {
			pwd(input);
			return true;
		}
		if(command.get(0).equals("exit")) {
			exit();
			return true;
		}
		if(command.get(0).equals("echo")) {
			echo(command, input);
			return true;
		}
		return false;
	}

	/**
	 * Print results into pipe.
	 * Separate thread because of the builtin command is called from the same thread as the following command.
	 *
	 * @param text result
	 * @param input input pipe
	 */
	private void printIntoInputPipe(final String text, final PipedInputStream input) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					PipedOutputStream output = new PipedOutputStream(input);
					output.write(text.getBytes());
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// TODO: UTF-8!
	/**
	 * Redirects output.
	 *
	 * @param output file
	 */
	private void printOutput(String output) {
		try {
			if (output != null) fileOutput(output);
			else {
				if (!process) consoleOutput();
				else pipeOutput();
			}
		} catch (IOException e) {
			console.printResults("");
		}
	}

	/**
	 * Print output into a file.
	 *
	 * @param output file
	 * @throws IOException
	 */
	private void fileOutput(String output) throws IOException {
		BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(getPath(output))));
		String txt = getStringFromInput();
		if(!running) return;										// Self killing check
		bfw.write(txt);
		bfw.close();
		if (!process) console.printResults("");						// Print new line in console.
	}

	/**
	 * Print output into the console.
	 *
	 * @throws IOException
	 */
	private void consoleOutput() throws IOException {
		while(running) {
			int c = input.read();
			StringBuilder builder = new StringBuilder();
			while (c != -1 && c != '\n') {
				builder.append((char) c);
				c = input.read();
			}
			if (c == '\n') console.printNewLine(builder.toString() + "\n");
			if (c == -1) {
				console.printResults(builder.toString());
				break;
			}
		}
	}

	/**
	 * Print output into output pipe.
	 *
	 * @throws IOException
	 */
	private void pipeOutput() throws IOException {
		String txt = getStringFromInput() + "\n";
		if(!running) return;										// Self killing check
		this.output.write(txt.getBytes());			// Output to parent shell.
	}

	/**
	 * Redirects input using Cat process.
	 *
	 * @param input input file
	 */
	private void redirectInput(String input) {
		if(input == null) return;						// No redirect needed
		int last = commands.size() - 1;					// Last command
		commands = commands.subList(last, last + 1);	// Redirect is possible only on the last command. Previous commands are discarded.
		List<String> cat = new ArrayList<String>();		// Create cat process
		cat.add("cat");
		cat.add(input);
		commands.add(0, cat);							// Adds to first position in commands
	}

	/**
	 * Lets console set its output.
	 *
	 * @param output console output
	 */
	public void setConsoleInput(PipedOutputStream output) {
		try {
			consoleInput = new PipedInputStream(output);
		} catch (IOException e) {
			return;
		}
	}

	/**
	 * Get line from console.
	 *
	 * @return line
	 */
	public String getLine() {
		int c;
		StringBuilder builder = new StringBuilder();
		try {
			c = consoleInput.read();
			while (c != -1 && c != '\n' && c != Console.CONTROL_D_BYTE) {		// 0 - represents Control - D signal
				builder.append((char) c);
				c = consoleInput.read();
			}
			if(c == Console.CONTROL_D_BYTE) return null;							// Finishes stdin
		} catch (IOException e) {
			return null;									// Child is killed
		}
		return builder.toString();							// Loaded string
	}

	/**
	 * Gets absolute path of real filesystem from virtual filesystem path. Supports relative as well as absolute path.
	 * Returns null in case of crossing borders of the virtual filesystem.
	 *
	 * @param path virtual path
	 * @return real path
	 */
	public String getPath(String path) {
		try {
			String fileName = path.length() > 0 && path.charAt(0) == File.separatorChar ? this.root + File.separatorChar + path:this.path + File.separatorChar + path; // Absolute or relative path
			String file = new File(fileName).getCanonicalPath();
			if(!file.matches("^" + root.replaceAll("\\\\", "\\\\\\\\") + ".*")) return null;			// Checking borders - REGEX Windows hack :D
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;			// Out of virtual filesystem.
	}

	/**
	 * Sets current path.
	 *
	 * @param path current path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Change directory command.
	 * Prints current location.
	 *
	 * @param param path
	 */
	private void cd(String param) {
		try {
			if(param.equals("")) setPath(root);					// Cd without params -> go to user folder
			String path = getPath(param);
			if(path == null) return;							// Trying to go out of filesystem
			File folder = new File(path);
			if(!folder.exists()) {
				console.printNewLine("No such a file or directory!");
			} else {
				if(!folder.isDirectory()) {
					console.printNewLine("Not a directory!");
				} else {
					setPath(folder.getCanonicalPath());
				}
			}
		} catch (IOException e) {
			return;
		}
	}

	/**
	 * Clean before killing.
	 */
	public void kill() {
		running = false;
		consoleWindow.closeConsole();
		this.interrupt();
	}

	/**
	 * Prints current path.
	 */
	private void pwd(PipedInputStream input) {
		printIntoInputPipe(getPath(""), input);
	}

	/**
	 * Prints arguments.
	 *
	 * @param arguments arguments
	 * @param input piped input
	 */
	public void echo(List<String> arguments, PipedInputStream input) {
		String text = "";
		if(arguments.size() == 1) text = "\n ";
		else for(int i = 1; i < arguments.size(); i++) text += arguments.get(i) + " ";		// Separates by space
		printIntoInputPipe(text.substring(0, text.length() - 1), input);					// Kills last space
	}

	/**
	 * Exit current shell. If the main shell, call shutdown.
	 */
	public void exit() {
		Kernel.getInstance().killProcess(getPid());
		if(getPid() == Kernel.MAIN_SHELL_PID) Kernel.getInstance().shutdown();
	}

	/**
	 * Get console prefix with Shell name, current location and current user.
	 *
	 * @return BBShell:location user$
	 */
	public String getConsolePrefix() {
		String parts[] = path.split(Run.getPathSeparatorForSplit());
		return "BBShell:" + parts[parts.length-1] + " root$ ";
	}
        
	/**
	 * Returns a manual page of a process.
	 * @return Manual page
	 */
	public static String getMan(){
            return "------------------ BASH PROCESS ------------------\n"+
            "- bash - GNU Bourne-Again SHell\n"+
            "- bash is an sh-compatible command language interpreter that executes\n" +
            "  commands read from the standard input or from a file.\n\n"+
            "Syntax: shell\n"+
            "- starts new shell process\n\n"+
            "Syntax: <command> {<argument>} {´|´ <command> {<argument>}} <input> <output>\n"+
            "- execute given command. Element in {} braces are not mandatory.\n"+
            "  If <input> and <output> elements are not specified, standart input and output will be used.\n"+
            "------------------ MANUAL END ------------------";
    }

	/**
	 * In case of wrong arguments.
	 *
	 * @param process name
	 */
	public void printHelp(String process) {
		try {
			printError((String) Class.forName(Kernel.PACKAGE + "." + process.substring(0, 1).toUpperCase() + process.substring(1).toLowerCase()).getMethod("getMan").invoke(null, null));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			printError("Bad usage! No manual entry for " + process + ".");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints error on console.
	 *
	 * @param error message
	 */
	public void printError(String error) {
		console.printNewLine(error);
	}

	/**
	 * Kills running process.
	 */
	public void killCurrentProcess() {
		Kernel.getInstance().killProcessAndParents(runningProcess);
		runningProcess = -1;
	}
}