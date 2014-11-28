package process;

import console.Console;
import console.ConsoleWindow;
import helpers.Parser;

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
	private final ConsoleWindow consoleWindow;
	/** Console */
	private final Console console;
	/** Console input pipe */
	private PipedInputStream consoleInput;

	/**
	 * Create new shell with own window.
	 *
	 * @param pid process ID
	 * @param input PipedInputStream
	 * @param commands list of commands
	 */
	public Shell (int pid, PipedInputStream input, List<List<String>> commands, Shell shell){
		super(pid, input, commands, shell);
		if(pid == 0) this.shell = this;
		this.consoleWindow = new ConsoleWindow(this);
		this.console = consoleWindow.console;
	}

	/**
	 * Execute command, when the line comes from console.
	 *
	 * @param line command
	 */
	public void executeCommand(String line) {
		console.setInCommand(true);				// Console inside command
		Parser parser = new Parser(line);   	// Parses the line
		commands = parser.getAllCommands();
		this.input = new PipedInputStream(4194304);
		redirectInput(parser.getInputFile());
		callSubProcess();
		redirectOutput(parser.getOutputFile(), getStringFromInput());
		console.setInCommand(false);			// Console outside command
	}

	// TODO: UTF-8!
	/**
	 * Redirects output.
	 *
	 * @param output file
	 * @param txt content
	 */
	private void redirectOutput(String output, String txt) {
		if(output != null) {
			try {
				BufferedWriter bfw = new BufferedWriter(new FileWriter(new File(output)));
				bfw.write(txt);
				bfw.close();
				console.printResults("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else console.printResults(txt);
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
	 * Own job. Doesn't block previous shell!
	 */
	@Override
	protected void processRun() {
		try {
			int c;
			while(true) {										// Infinite loop
				StringBuilder builder = new StringBuilder();
				c = consoleInput.read();
				while (c != -1 && c != '\n') {					// End of pipe or end of line
					builder.append((char) c);
					c = consoleInput.read();
				}
				if(c == '\n') console.printNewLine("");			// Print new line also on console
				if (c == -1) {
					output.close();								// Doesn't block parent shell!
					return;
				}
				executeCommand(builder.toString());				// Executes parsed commands
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			e.printStackTrace();
		}
		return builder.toString();							// Loaded string
	}
}
