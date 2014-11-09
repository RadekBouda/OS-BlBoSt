package console;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Console - the part of ConsoleWindow where text and IO appears.
 * Created by Radek Bouda on 9. 11. 2014.
 */
public class Console extends JTextPane{
    private ConsoleHistory history = new ConsoleHistory();
    private String path = "shell# ";


    public Console(){
        this.setForeground(new Color(0,0,0));
        this.addKeyListener(new CommandsKeyListener());
        this.setText(path);
        this.setCaretPosition(this.getText().length());
    }

    /**
     * Method which returns actually submited command.
     * @return Submited command as a string, null otherwise (empty command)
     */
    public String getCommand() {
        if(getText().split("\n")[getText().split("\n").length-1].split("# ").length == 1) return null;

        return getText().split("\n")[getText().split("\n").length-1].split("# ")[1];
    }

    /**
     * Method which prints data to console, on a new line.
     * @param text Text to be printed on a new line.
     */
    public void printNewLine(String text){
        this.setText(this.getText() + "\n" + text);
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
     * Private adapter which describes how to manipulate with keys, when they are pressed.
     * Special behavior for Return, Left, Right and BackSpace keys.
     * @author Radek Bouda
     * @version 1.0.0
     */
    private class CommandsKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
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

                    if(getCommand() == null){
                        printNewLine(path);
                        return;
                    }

                    history.addCommandToHistory(getCommand());
                    history.resetHistory();
                    printNewLine(path);
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
