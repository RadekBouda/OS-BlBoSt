package process;

import kernel.Kernel;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ps process serves as observer of processes in OS.
 *
 * @Author David Steinberger
 */
public class Ps extends AbstractProcess {

    /**
     * Create new ps process.
     *
     * @param pid process id
     * @param parentPid parent process id
     * @param input pipedInput
     * @param commands list of commands
     * @param shell parent shell
     */
    public Ps(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        try {
            Set<Map.Entry<Integer, AbstractProcess>> processes = Kernel.getInstance().getProcesses();
            output.write(getHeaders().getBytes());
            for (Map.Entry<Integer, AbstractProcess> process : processes) {
                if(getPid() == process.getKey()) continue;                      // Doesn't print itself.
                output.write(getRecord(process.getValue()).getBytes());
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets process record with information from process.
     *
     * @param process Abstract process
     * @return record with information
     */
    private String getRecord(AbstractProcess process) {
        return process.getPid() + "\t" + process.getParentPid() + '\t' + process.getStartTime() + '\t' + process.getClass() + '\n';
    }

    /**
     * Gets headers.
     *
     * @return headers.
     */
    private String getHeaders() {
        return "PID\tParent Pid\tStart time\tProcess name\n";
    }
}
