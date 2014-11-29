package process;

import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.util.List;

/**
 * Ls process print directory content.
 *
 * @Author David Steinberger
 */
public class Ls extends AbstractProcess {
    /** Desired folder to print */
    private final String path;

    /**
     * Create new process without parameter.
     *
     * @param pid process id
     * @param input PipedInput
     * @param commands list of commands
     * @param shell parent shell
     */
    public Ls(int pid, PipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, input, commands, shell);
        this.path = "";
    }

    /**
     * Create new process with path parameter.
     *
     * @param pid process id
     * @param input PipedInput
     * @param commands list of commands
     * @param shell parent shell
     * @param path desired path
     */
    public Ls(int pid, PipedInputStream input, List<List<String>> commands, Shell shell, String path) {
        super(pid, input, commands, shell);
        this.path = path;
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        try {
            File directory = new File(shell.getPath(path));
            String text = "";
            String files[] = directory.list();
            for(String file : files) text += file + '\t';
            output.write(text.getBytes());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
