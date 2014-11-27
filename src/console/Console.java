package console;

import process.Shell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

// TODO: Singnals (CTRL+C)

/**
 * Console - the part of ConsoleWindow where text and IO appears.
 * Created by Radek Bouda, David Steinberger on 9. 11. 2014.
 */
public class Console extends JTextPane{
    private final Shell shell;          // Shell reference
    private ConsoleHistory history = new ConsoleHistory();
    private String path = "shell# ";


    public Console(Shell shell){
        this.setBackground(new Color(0, 0, 0));
        this.setForeground(new Color(255, 255, 51));
        this.setCaretColor(new Color(255, 255, 51));
        this.addKeyListener(new CommandsKeyListener());
        this.setText(path);
        this.setCaretPosition(this.getText().length());
        this.shell = shell;
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
        this.setText(this.getText() + "\n" + path);
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
        for (int i = 0; i < data.length - 1; i++){
            setText(getText() + data[i] + "\n");
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
     * Private adapter which describes how to manipulate with keys, when they are pressed.
     * Special behavior for Return, Left, Right and BackSpace keys.
     * @author Radek Bouda
     * @version 1.0.0
     */
    private class CommandsKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {

            if(!isCaretWhereItShouldBe()){ // if caret is somewhere where it should not be
                setCaretPosition(getText().length()); // we put it somewhere where it should be
            }

            //Disable deleting with a back space and moving of a caret in front of the hashmark
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_LEFT) {
                if(getText().toCharArray()[getCaretPosition() - 1] == ' ' && getText().toCharArray()[getCaretPosition() - 2] == '#'){
                    e.consume();
                }
                return;
            }

            switch (e.getKeyCode() ) {
                //Executing command
                case KeyEvent.VK_ENTER:
                    e.consume();
                    String command = getCommand();
                    if(command == null){
                        printNewLine(path);
                        return;
                    }
                    history.addCommandToHistory(command);
                    history.resetHistory();

                    shell.executeCommand(command);  // Shell executes command
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
    }


}
