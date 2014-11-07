package console;

import java.util.ArrayList;

/**
 *
 * @author Jan
 */
public class ConsoleHistory {

    private ArrayList<String> commands;
    private int position;
    private final int MAXSIZE = 50;

    /**
     * Constructor. Creates new history arraylist.
     */
    public ConsoleHistory() {
        commands = new ArrayList<String>();
        position = -1;
    }

    /**
     * Increasing position and returning command at that position.
     *
     * @return command
     */
    public ConsoleHistory down() {
        if (position == -1) {
            position = commands.size() - 1;
            return this;
        }
        if (position < commands.size() - 1) {
            position++;
        }
        return this;
    }

    /**
     * Decreasing position and returning command at that position.
     *
     * @return
     */
    public ConsoleHistory up() {
        if (position == -1) {
            position = commands.size() - 1;
            return this;
        }
        if (position > 0) {
            position--;
        }
        return this;
    }

    /**
     * Gets command at actual position.
     *
     * @return command
     */
    public String getCurrentCommand() {
        if (commands.isEmpty() || position == -1) {
            return "";
        }
        return commands.get(position);
    }

    /**
     * Adding command to History list.
     *
     * @param newCommand added command
     */
    public void addCommandToHistory(String newCommand) {
        if (MAXSIZE != -1 && MAXSIZE == commands.size()) {
            commands.remove(0);
        }

        if (commands.contains(newCommand)) {
            commands.remove(newCommand);
        }
        commands.add(newCommand);
    }

    /**
     * Setting starting position to beginning.
     */
    public void resetHistory() {
        position = -1;
    }
}
