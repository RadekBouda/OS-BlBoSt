package src.helpers;

import java.util.ArrayList;
//import java.util.Scanner;

public class Parser {

//	private static String regexp;
	private final static char PIPE = '|';
	private final static char IN_REROUTE = '<';
	private final static char OUT_REROUTE = '>';

	private static String parsed_string;
	private static int parsed_position;
	private static char parsed_char;
	private static boolean isFinished;
	private static ArrayList<String> command = new ArrayList<String>();
	private static ArrayList<ArrayList<String>> allCommands = new ArrayList<ArrayList<String>>();
	private static String inputFile;
	private static String outputFile;

	/**
	 * @param args
	 */
/*	public static void main(String[] args) {
		if (args.length == 1) {
			regexp = args[0];
		} else {
			Scanner sc = new Scanner(System.in);
			System.out.print("Zadejte regularni vyraz: ");
			regexp = sc.nextLine();
		}

		System.out.println(regexp);
		parse(regexp);
	}
*/
	public static void parse(String commandLine) {
		if (commandLine.equals("")) {
			System.out.println("Parser received an empty command.");
			return;
		}

		parsed_string = commandLine;
		parsed_position = 0;
		parsed_char = parsed_string.charAt(parsed_position);
		isFinished = false;

		command = getCommand();
		if (command.isEmpty()) {
			System.out.println("Parser couldn't parse the command.");
			return;
		}
		allCommands.add(command);

		consumeWhiteSpaces();

		while (parsed_char == IN_REROUTE) {
			nextChar();
			inputFile = getReroutes();
			if (inputFile.length() == 0) {
				System.out.println("Parser couldn't parse the standard input file reroute.");
			}
			consumeWhiteSpaces();
		}

		while (parsed_char == PIPE) {
			nextChar();
			command = getCommand();
			if (command.isEmpty()) {
				System.out.println("Parser couldn't parse the command defined after pipe character.");
				return;
			}
			allCommands.add(command);
			consumeWhiteSpaces();
		}
		
		while (parsed_char == OUT_REROUTE) {
			nextChar();
			outputFile = getReroutes();
			if (outputFile.length() == 0) {
				System.out.println("Parser couldn't parse the standard output file reroute.");
			}
			consumeWhiteSpaces();
		}	
	}

	private static ArrayList<String> getCommand() {
		ArrayList<String> result = new ArrayList<String>();
		String word;
		do {
			consumeWhiteSpaces();
			word = "";
			while (!(isFinished || Character.isWhitespace(parsed_char)
					|| parsed_char == PIPE
					|| parsed_char == IN_REROUTE || parsed_char == OUT_REROUTE)) {
				word += parsed_char;
				nextChar();
			}
			if (word.length() > 0) {
				result.add(word);
				System.out.println(word);
			}
		} while (word.length() > 0);
		return result;
	}

	private static String getReroutes() {
		String result = "";
		consumeWhiteSpaces();
		if (isFinished || parsed_char == IN_REROUTE
				|| parsed_char == OUT_REROUTE
				|| parsed_char == PIPE) {
			return "";
		}
		do {
			result += parsed_char;
			nextChar();
		} while (!(isFinished || Character.isWhitespace(parsed_char)
				|| parsed_char == IN_REROUTE
				|| parsed_char == OUT_REROUTE || parsed_char == PIPE));
		return result;
	}

	private static void consumeWhiteSpaces() {
		while (!isFinished && Character.isWhitespace(parsed_char)) {
			nextChar();
		}
	}

	private static void nextChar() {
		if (parsed_position + 1 == parsed_string.length()) {
			parsed_char = '\0';
			isFinished = true;
		} else {
			parsed_position++;
			parsed_char = parsed_string.charAt(parsed_position);
		}
	}

	public static String getInputFile() {
		return inputFile;
	}

	public static String getOutputFile() {
		return outputFile;
	}

	public static ArrayList<ArrayList<String>> getAllCommands() {
		return allCommands;
	}

	public static void setInputFile(String inputFile) {
		Parser.inputFile = inputFile;
	}

	public static void setOutputFile(String outputFile) {
		Parser.outputFile = outputFile;
	}
}