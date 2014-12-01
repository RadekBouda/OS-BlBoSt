package process;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.List;

/**
 * Wc process serves as line counter.
 *
 * @Author David Steinberger
 */
public class Wc extends AbstractProcess {

    /**
     * Create new process
     *
     * @param pid process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands list with commands
     * @param shell parent shell
     */
    public Wc(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        if(hasPipedInput()) {
            pipedInput();
        } else {
            stdInput();
        }
    }

    /**
     * Piped input version. Reads from pipe.
     */
    private void pipedInput() {
        try {
            String text = getStringFromInput();
            output.write((text.split("\n").length + "").getBytes());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stdin version. Reads from stdin.
     */
    private void stdInput() {
        try {
            int count = 0;
            while(shell.getLine() != null) count++;
            output.write(("" + count).getBytes());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
