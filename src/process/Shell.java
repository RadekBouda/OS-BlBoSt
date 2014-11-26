package process;

import console.Console;
import console.ConsoleWindow;
import helpers.Parser;

import java.io.IOException;
import java.io.PipedInputStream;
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
		callSubProcess();
		console.printNewLine(getStringFromInput());
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
