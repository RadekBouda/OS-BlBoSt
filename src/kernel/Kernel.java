/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kernel;

import process.Shell;

/**
 * Instance of this class represents the kernel of an operating system.
 * Class is designed according to SINGLETON design pattern.
 * @author Jan Blaha, Radek Bouda
 * @version 1.1
 */
public class Kernel {
	private int PID;

	private Kernel() {
		PID = 0;
	}

	/**
	 * Singel instance of Kernel.
	 */
	private static final Kernel INSTANCE = new Kernel();

	/**
	 * This static method returns the instance of Kernel.
	 * @return One, always the same, instance of Kernel.
	 */
	public static Kernel getInstance(){
		return INSTANCE;
	}

	public void runShell() {
		new Shell(PID);
		increasePID();
	  }
	
	public void increasePID(){
		PID++;
	}
}
