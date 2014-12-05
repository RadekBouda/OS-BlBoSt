package process;

import helpers.BBPipedInputStream;
import kernel.Kernel;

import java.io.IOException;
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
    public Shutdown(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell) throws IOException {
        super(pid, parentPid, input, commands, shell);
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        Kernel.getInstance().shutdown();
    }

    /**
     * Returns a manual page of a process.
     * @return Manual page
     */
    public static String getMan() {
        return "------------------ SHUTDOWN PROCESS ------------------\n"+
                "- terminates whole OS\n\n"+
                "Syntax: shutdown\n"+
                "- terminates all running processes\n"+
                "------------------ MANUAL END ------------------------";
    }
}
