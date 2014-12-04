package process;

import kernel.Kernel;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.List;

/**
 *
 * @author Jan
 */
public class Kill extends AbstractProcess {

    String pidToKill;

    /**
     * Create new kill process.
     *
     * @param pid process id
     * @param parentPid parent process id
     * @param input pipedInput
     * @param commands list of commands
     * @param shell parent shell
     */
    public Kill(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell, String pidToKill) {
        super(pid, parentPid, input, commands, shell);
        if(pidToKill.equalsIgnoreCase(AbstractProcess.HELP_COMMAND)){
            helpOnly = true;
        } else {
            helpOnly = false;
            this.pidToKill = pidToKill;
        }

    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        if(helpOnly){
            try{
                output.write(getMan().getBytes());
                output.close();
                return;
            } catch (IOException e){
                return;
            }
        }
        try {
            int pid;
            try {
                pid = Integer.valueOf(pidToKill);
                if(pid == Kernel.MAIN_SHELL_PID) {
                    Kernel.getInstance().killProcess(pid);
                    output.close();
                } else {
                    output.write((Kernel.getInstance().killProcess(pid) ? "Process successfully killed" : "No pid " + pidToKill + " found!").getBytes());
                    output.close();
                }
            } catch (NumberFormatException e) {
                shell.printError("kill: " + pidToKill + ": Wrong format of pid!");
                output.close();
            }
        } catch (IOException e) {
            return;
        }
    }
    
    /**
     * Returns a manual page of a process.
     * @return Manual page
     */
    public static String getMan() {
    return "------------------ KILL PROCESS ------------------\n"+
            "- terminate a process\n\n"+
            "Syntax: kill <pid>\n"+
            "- kills running process with given pid\n\n"+
            "------------------ MANUAL END ------------------";
    }
}