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
     * @param input PipedInputStream
     * @param commands list with commands
     */
    public Wc(int pid, PipedInputStream input, List<List<String>> commands) {
        super(pid, input, commands);
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        try {
            String text = getStringFromInput();
            output.write((text.split("\n").length + "").getBytes());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
