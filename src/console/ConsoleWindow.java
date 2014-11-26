package console;

import process.Shell;

import java.awt.*;

// TODO: Default close operation = close Shell.

/**
 * Instances of this class represent concrete windows with Console.
 * @author Jan Blaha, Radek Bouda, David Steinberger
 * @version 2.0.0
 */
public class ConsoleWindow extends javax.swing.JFrame {

    private static final long serialVersionUID = 1L;
    public Console console;
    private javax.swing.JScrollPane jScrollPane1;


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
    private void initComponents(Shell shell) {

        jScrollPane1 = new javax.swing.JScrollPane();
        console = new Console(shell);


        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);


        this.setTitle("OS Simulator - BlBoSt Team");
        this.setSize(600,500);
        this.setLayout(new BorderLayout());
        this.add(console, BorderLayout.CENTER);

    }

}
