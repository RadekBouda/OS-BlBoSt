package process;

import helpers.BBPipedInputStream;
import kernel.Kernel;

import java.util.List;

/**
 * Shutdown process. Close down the system.
 *
 * @Author David Steinberger
 */
public class Shutdown extends AbstractProcess {

    /**
     * Create new process.
     *
     * @param pid Process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands List of commands
     * @param shell parent shell
     */
    public Shutdown(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        Kernel.getInstance().shutdown();
    }
}
