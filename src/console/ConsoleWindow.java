package console;

import process.Shell;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

// TODO: Default close operation = close Shell.

/**
 * Instances of this class represent concrete windows with Console.
 * @author Jan Blaha, Radek Bouda, David Steinberger
 * @version 2.0.0
 */
public class ConsoleWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    /** Own console */
    public Console console;


    /**
     * Creates new form ConsoleWindow.
     */
    public ConsoleWindow(Shell shell) {
        initComponents(shell);
        this.setVisible(true);
    }


    /**
     * Initialization of components.
     */
    private void initComponents(final Shell shell) {
        console = new Console(shell);

        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setLocationRelativeTo(null);

        this.setTitle("OS Simulator - BlBoSt Team");
        this.setSize(600,500);
        this.setLayout(new BorderLayout());

        JScrollPane jsp = new JScrollPane(console);

        this.add(jsp, BorderLayout.CENTER);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shell.exit();
            }
        });
    }

    public void closeConsole() {
        this.setVisible(false);
        final JFrame that = this;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                that.dispose();
            }
        });
        t.start();
    }

}
