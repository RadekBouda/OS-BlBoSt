/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package kernel;

//import process.Shell;

/**
 * 
 * @author Jan
 */
public class Kernel {
	private int PID;

	protected Kernel() {
		PID = 0;
	}

	public void runShell() {
		//new Shell(PID);
		//incrasePID();
	  }
	
	public void incrasePID(){
		PID++;
	}
}
