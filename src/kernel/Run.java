/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kernel;

// TODO: Manage exceptions in whole project!

import java.io.File;

/**
 * Main class of the simulator.
 * @author Jan Blaha
 */
public class Run {
    /** Platform independent file separator */
    private static String PATH_SEPARATOR = "/";

    /**
     * Main method of the simulator.
     * @param args The command line arguments. Not used.
     */
    public static void main(String[] args) {

        if(File.separatorChar != '/'){
            PATH_SEPARATOR = "\\\\";        // Windows
        } else {
            PATH_SEPARATOR = "/";           // Unix-like
        }

        Kernel.getInstance().runShell();
    }

    /**
     * This method returns path separator as a string, which can be used as a parameter in SPLIT methods.
     * @return \\ for Windows based OS, / for UNIX-like OS
     */
    public static String getPathSeparatorForSplit() {
        return PATH_SEPARATOR;
    }
}
