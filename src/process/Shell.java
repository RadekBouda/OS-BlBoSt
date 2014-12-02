package process;

import console.Console;
import console.ConsoleWindow;
import helpers.Parser;
import kernel.Kernel;
import kernel.Run;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		this.output = new ByteArrayOutputStream();				// Output for shell errors.
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
		if(!builtinCommand(commands.get(0))) {								// No builtin command
			this.input = new PipedInputStream(PIPE_BUFFER_SIZE);
			redirectInput(parser.getInputFile());
			callSubProcess();
			String output = getStringFromInput();
			if(!running) return;											// Self killing check
			if(output == null) output = readOwnOutput();					// Problem with first command
			redirectOutput(parser.getOutputFile(), output);
		}
		if(!process) console.setInCommand(false);							// Console outside command
	}

	/**
	 * Gets output string from own stream in case of fail in the first command.
	 *
	 * @return output
	 */
	private String readOwnOutput() {
		try {
			String text = ((ByteArrayOutputStream) output).toString("UTF-8");
			((ByteArrayOutputStream) output).reset();							// Clear output for next commands.
			return text;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Checks builtin commands. In case of builtin command no further execution is proceeded.
	 *
	 * @param command parsed commands
	 * @return true/false
	 */
	private boolean builtinCommand(List<String> command) {
		if(command.get(0).equals("cd")) {
			if(command.size() == 1) cd("");
			else cd(command.get(1));
			return true;
		} else if(command.get(0).equals("pwd")) {
			pwd();
			return true;
		} else if(command.get(0).equals("exit")) {
			exit();
			return true;
		}
		return false;
	}

	// TODO: UTF-8!
	/**
	 * Redirects output.
	 *
	 * @param output file
	 * @param txt content
	 */
	private void redirectOutput(String output, String txt) {
		try {
			if (output != null) {											// Output to file
				BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(getPath(output))));
				bfw.write(txt);
				bfw.close();
				if (!process) console.printResults("");						// Print new line in console.
			} else {
				if (!process) console.printResults(txt);					// Print to console
				else this.output.write((txt + "\n").getBytes());			// Output to parent shell.
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			e.printStackTrace();
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
			while (c != -1 && c != '\n' && c != 0) {		// 0 - represents Control - D signal
				builder.append((char) c);
				c = consoleInput.read();
			}
			if(c == 0) return null;							// Finishes stdin
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
			if(!file.matches("^" + root.replaceAll("\\\\", "\\\\\\\\") + ".*")) return null;			// Checking borders
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

	// TODO: Pretty outputs

	/**
	 * Change directory command.
	 * Prints current location.
	 *
	 * @param param path
	 */
	private void cd(String param) {
		try {
			if(param.equals("")) {								// Cd without params -> go to user folder
				setPath(root);
				console.printResults(getPath(""));
				return;
			}
			String path = getPath(param);
			if(path == null) {									// Trying to go out of filesystem
				console.printResults(getPath(""));
				return;
			}
			File folder = new File(path);
			System.out.println(path);
			if(!folder.exists()) {
				console.printResults("No such a file or directory!");
			} else {
				if(!folder.isDirectory()) {
					console.printResults("Not a directory!");
				} else {
					setPath(folder.getCanonicalPath());
					console.printResults("");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
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
	private void pwd() {
		console.printResults(getPath(""));
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
}
