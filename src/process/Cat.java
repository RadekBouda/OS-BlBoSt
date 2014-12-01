package process;

import java.io.*;
import java.util.List;

/**
 * Cat process reads files.
 *
 * @Author David Steinberger
 */
public class Cat extends AbstractProcess {

    /** Path to file */
    private String path;

    /**
     * Create new process.
     *
     * @param pid Process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands List of commands
     * @param shell parent shell
     * @param path Path to file
     */
    public Cat(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell, String path) {
        super(pid, parentPid, input, commands, shell);
        this.path = shell.getPath(path);
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        try {
            int c;
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            while((c = reader.read()) != -1) output.write(c);
            output.close();
        } catch (FileNotFoundException e) {
            try {
                output.write("File not found".getBytes());
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
