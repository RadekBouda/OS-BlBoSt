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

	/**
	 * Create new shell with own window.
	 *
	 * @param pid process ID
	 * @param input PipedInputStream
	 * @param commands list of commands
	 */
	public Shell (int pid, PipedInputStream input, List<List<String>> commands){
		super(pid, input, commands);
		this.consoleWindow = new ConsoleWindow(this);
		this.console = consoleWindow.console;
	}

	/**
	 * Execute command, when the line comes from console.
	 *
	 * @param line command
	 */
	public void executeCommand(String line) {
		Parser parser = new Parser(line);   // Parses the line
		commands = parser.getAllCommands();
		this.input = new PipedInputStream(4194304);
		redirectInput(parser.getInputFile());
		callSubProcess();
		redirectOutput(parser.getOutputFile(), getStringFromInput());
	}

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
				console.printNewLine("");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else console.printNewLine(txt);
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
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
