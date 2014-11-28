package console;

import process.Shell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PipedOutputStream;

// TODO: Singnals (CTRL+C etc.)

/**
 * Console - the part of ConsoleWindow where text and IO appears.
 *
 * Created by Radek Bouda, David Steinberger on 9. 11. 2014.
 */
public class Console extends JTextPane implements Runnable {
    /** Own shell reference */
    private final Shell shell;
    /** Commands history */
    private ConsoleHistory history = new ConsoleHistory();
    /** Line prefix */
    private String path = "shell# ";
    /** Stream with shell */
    private PipedOutputStream output;
    /** State of console (inside command or not)  */
    private boolean inCommand;
    /** This constant defines, how many lines are stored in memory. */
    private static final int LINES_CNT_TO_MEMORIZE = 115;

    /**
     * Creates new console instance.
     *
     * @param shell own shell
     */
    public Console(Shell shell){
        this.setBackground(new Color(0, 0, 0));
        this.setForeground(new Color(255, 255, 51));
        this.setCaretColor(new Color(255, 255, 51));
        this.addKeyListener(new CommandsKeyListener());
        this.setText(path);
        this.setCaretPosition(this.getText().length());
        this.output = new PipedOutputStream();
        this.shell = shell;
        this.shell.setConsoleInput(output);
        this.inCommand = false;
    }

    /**
     * Method which returns actually submitted command.
     * @return Submitted command as a string, null otherwise (empty command)
     */
    public String getCommand() {
        String split[] = getText().split("\n");
        if(split[split.length-1].split("# ").length == 1) return null;

        return split[split.length-1].split("# ")[1];
    }

    /**
     * Method which prints data to console, on a new line.
     * @param text Text to be printed on a new line.
     */
    public void printNewLine(String text){
        this.setText(this.getText() + "\n" + text);
        setCaretPosition(getText().length());
    }

    /**
     * Prints results and path prefix on a new line.
     *
     * @param text
     */
    public void printResults(String text) {
        printNewLine(text);
        printNewLine(path);
    }

    /**
     * Method that prints a text to console on a current line.
     * @param text Text to be printed on a current line.
     */
    public void print(String text){
        this.setText(this.getText() + text);
    }

    /**
     * Method which removes last line in a console. It can be handy.
     */
    private void removeLastLineInConsole(){
        String[] data = getText().split("\n");
        setText("");

        int i = data.length - LINES_CNT_TO_MEMORIZE;
        if(i < 0) i = 0;

        while(i < (data.length - 1)){
            setText(getText() + data[i] + "\n");
            i++;
        }
    }

    /**
     * This method checks whether is the caret behind the hashmark of the last line or not.
     * @return True if it is, false otherwise.
     */
    private boolean isCaretWhereItShouldBe() {
        String split[] = getText().split("\n");
        int lastRowLength = split[split.length-1].length();
        int totalLength = getText().length();

        if(getCaretPosition() < totalLength - lastRowLength + path.length()){
            return false;
        }

        return true;

    }

    /**
     * Empty run... Just listening keyboard.
     */
    @Override
    public void run() {}

    /**
     * Private adapter which describes how to manipulate with keys, when they are pressed.
     * Special behavior for Return, Left, Right and BackSpace keys.
     * @author Radek Bouda, David Steinberger
     * @version 1.0.0
     */
    private class CommandsKeyListener extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
                if (!isCaretWhereItShouldBe()) { // if caret is somewhere where it should not be
                    setCaretPosition(getText().length()); // we put it somewhere where it should be
                }

                //Disable deleting with a back space and moving of a caret in front of the hashmark
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_LEFT) {
                    if (getText().toCharArray()[getCaretPosition() - 1] == ' ' && getText().toCharArray()[getCaretPosition() - 2] == '#') {
                        e.consume();
                    }
                    return;
                }
            // Check console state
            if(!inCommand) {
                outsideCommandBehaviour(e);
            } else {
                insideCommandBehaviour(e);
            }
        }
    }

    /**
     * Behaviour inside a command. Just copy stdin into pipe.
     *
     * Signals: Ctrl-D - stop listening
     *
     * @param e key event
     */
    private void insideCommandBehaviour(KeyEvent e) {
        try {
            if(e.isControlDown() && e.getKeyCode() == KeyEvent.VK_D) {
                output.write(0);
            } else {
                output.write(e.getKeyCode());
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Behaviour outside a command.
     *
     * Supported keys:  Enter - consume a command
     *                  Up - Back in history
     *                  Down - Forward in history
     *
     * @param e key event
     */
    private void outsideCommandBehaviour(KeyEvent e) {
        switch (e.getKeyCode()) {
            //Executing command
            case KeyEvent.VK_ENTER:
                e.consume();
                String command = getCommand();
                if (command == null) {
                    printNewLine(path);
                    return;
                }
                history.addCommandToHistory(command);
                history.resetHistory();
                try {
                    output.write((command + '\n').getBytes());  // Shell is waiting for '\n' to execute the command
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                break;
            //Printing previous command
            case KeyEvent.VK_UP:
                e.consume();
                removeLastLineInConsole();
                print(path + history.up().getCurrentCommand());
                break;
            //Printing next command
            case KeyEvent.VK_DOWN:
                e.consume();
                removeLastLineInConsole();
                print(path + history.down().getCurrentCommand());
                break;
            default:
                break;
        }
    }

    /**
     * Set the state of the console.
     *
     * States:  true - inside command
     *          false - outside command
     *
     * @param in true/false
     */
    public void setInCommand(boolean in) {
        inCommand = in;
    }

}
