package console;

import java.awt.*;

/**
 * Instances of this class represent concrete windows with Console.
 * @author Jan Blaha, Radek Bouda
 * @version 2.0.0
 */
public class ConsoleWindow extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
    private Console console;
    private javax.swing.JScrollPane jScrollPane1;


    /**
     * Creates new form ConsoleWindow.
     */
    public ConsoleWindow() {
        initComponents();
        this.setVisible(true);
    }


    /**
     * Initialization of components.
     */
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        console = new Console();


        this.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);


        this.setTitle("OS Simulator - BlBoSt Team");
        this.setSize(600,500);
        this.setLayout(new BorderLayout());
        this.add(console, BorderLayout.CENTER);

    }

}
