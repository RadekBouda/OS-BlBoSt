package helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Instances of this class can parse user shell commands into separated commands.
 *
 * @author Jan Blaha, Radek Bouda, David Steinberger
 * @version 1.1
 */
public class Parser {

	/** Important characters */
	private final static char PIPE = '|';
	private final static char IN_REROUTE = '<';
	private final static char OUT_REROUTE = '>';

	/** Inner state variables */
	private String parsed_string;
	private int parsed_position;
	private char parsed_char;
	private boolean isFinished;
	private List<String> command = new ArrayList<String>();
	private List<List<String>> allCommands = new ArrayList<List<String>>();

	/** I/O files if available */
	private String inputFile;
	private String outputFile;

	/**
	 * Create new parsed command.
	 *
	 * @param line line to parse
	 */
	public Parser(String line) {
		parse(line);
	}

	/**
	 * Main parsing method.
	 *
	 * @param commandLine line to parse
	 */
	private void parse(String commandLine) {
		if (commandLine.equals("")) return;

		parsed_string = commandLine;
		parsed_position = 0;
		parsed_char = parsed_string.charAt(parsed_position);
		isFinished = false;

		command = getCommand();
		if (command.isEmpty()) return;
		allCommands.add(command);

		consumeWhiteSpaces();

		while (parsed_char == PIPE) {				// Read pipes
			nextChar();
			command = getCommand();
			if (command.isEmpty()) return;
			allCommands.add(command);
			consumeWhiteSpaces();
		}

		while (parsed_char == IN_REROUTE) {			// In redirection
			nextChar();
			inputFile = getReroutes();
			consumeWhiteSpaces();
		}

		while (parsed_char == OUT_REROUTE) {		// Out redirection
			nextChar();
			outputFile = getReroutes();
			consumeWhiteSpaces();
		}
	}

	/**
	 * Get single command.
	 *
	 * @return list of parts of command
	 */
	private ArrayList<String> getCommand() {
		ArrayList<String> result = new ArrayList<String>();
		boolean insideQuotes = false;
		String word;
		do {

			consumeWhiteSpaces();
			word = "";
			while ((!(isFinished || Character.isWhitespace(parsed_char)
					|| parsed_char == PIPE
					|| parsed_char == IN_REROUTE || parsed_char == OUT_REROUTE)) || insideQuotes) {
				if(parsed_char == '"'){
					if (!insideQuotes) insideQuotes = true;
					else insideQuotes = false;
				}
				word += parsed_char;
				nextChar();
				if(isFinished) break; // anti-idiot fail-safe
			}
			if (word.length() > 0) {
				result.add(word.replaceAll("\"",""));
				if(isFinished) break;
			}
		} while (word.length() > 0);
		return result;
	}

	/**
	 * Get I/O redirection.
	 *
	 * @return
	 */
	private String getReroutes() {
		String result = "";
		consumeWhiteSpaces();
		boolean insideQuotes = false;
		if (isFinished || parsed_char == IN_REROUTE
				|| parsed_char == OUT_REROUTE
				|| parsed_char == PIPE) {
			return "";
		}
		do {
			if(parsed_char == '"'){
				if (!insideQuotes) insideQuotes = true;
				else insideQuotes = false;
			}
			result += parsed_char;
			nextChar();
			if(isFinished) break; // anti-idiot failsafe
		} while (!(isFinished || Character.isWhitespace(parsed_char)
				|| parsed_char == IN_REROUTE
				|| parsed_char == OUT_REROUTE || parsed_char == PIPE) || insideQuotes);
		return result.replaceAll("\"", "");
	}

	/**
	 * Consumes whitespaces.
	 */
	private void consumeWhiteSpaces() {
		while (!isFinished && Character.isWhitespace(parsed_char)) {
			nextChar();
		}
	}

	/**
	 * Gets next character.
	 */
	private void nextChar() {
		if (parsed_position + 1 == parsed_string.length()) {
			parsed_char = '\0';
			isFinished = true;
		} else {
			parsed_position++;
			parsed_char = parsed_string.charAt(parsed_position);
		}
	}

	/** Getters and setters */
	public String getInputFile() {
		return inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public List<List<String>> getAllCommands() {
		return allCommands;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}
}