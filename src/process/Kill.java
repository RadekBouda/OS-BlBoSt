package process;

import java.io.PipedInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kernel.Kernel;

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
        this.pidToKill = commands.get(0).get(1);
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        Set<Map.Entry<Integer, AbstractProcess>> processes = Kernel.getInstance().getProcesses();
        for (Map.Entry<Integer, AbstractProcess> process : processes) {
            if (Integer.parseInt(pidToKill) == process.getKey()) {
                Kernel.getInstance().killProcess(process.getKey());
                if (process.getKey() == Kernel.MAIN_SHELL_PID) {
                    Kernel.getInstance().shutdown();
                }
            }
        }
    }
}