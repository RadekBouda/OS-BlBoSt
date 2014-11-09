package process;

import console.ConsoleWindow;

public class Shell extends AbstractProcess{
	
	public Shell (int Pid){
		new ConsoleWindow().runConsole();
	}
}
