/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kernel;

// TODO: Manage exceptions in whole project!

/**
 * Main class of the simulator.
 * @author Jan Blaha
 */
public class Run {

    /**
     * Main method of the simulator.
     * @param args The command line arguments. Not used.
     */
    public static void main(String[] args) {
        Kernel.getInstance().runShell();
    }
}
