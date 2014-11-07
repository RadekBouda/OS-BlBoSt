package process;

import console.ConsoleWindow;
import helpers.AbstractProcess;

public class shell extends AbstractProcess{
	
	public shell (int Pid){
		new ConsoleWindow().runConsole();
	}
}
