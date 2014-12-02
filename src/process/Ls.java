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
     * @param parentPid process id of parent
     * @param input PipedInput
     * @param commands list of commands
     * @param shell parent shell
     */
    public Ls(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
        this.path = "";
    }

    /**
     * Create new process with path parameter.
     *
     * @param pid process id
     * @param parentPid process id of parent
     * @param input PipedInput
     * @param commands list of commands
     * @param shell parent shell
     * @param path desired path
     */
    public Ls(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell, String path) {
        super(pid, parentPid, input, commands, shell);
        this.path = path;
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        try {
            File directory = new File(shell.getPath(path));
            if(!directory.exists()){
                output.write(("LS Error: Directory "+path+ " does not exist.").getBytes());
                output.close();
                return;
            }
            String files[] = directory.list();
            for(String file : files) output.write((file + "\t").getBytes());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a manual page of a process.
     * @return Manual page
     */
    public static String getMan(){
        return "------------------ LS PROCESS ------------------\n"+
                "- prints a content of current directory\n"+
                "- can be used with a relative or absolute path as a non-compulsory parameter\n\n"+
                "Syntax: ls\n"+
                "- prints content of current directory\n\n"+
                "Syntax: ls <relative/absolute path>\n"+
                "- prints content of directory at selected relative or absolute path\n"+
                "------------------ MANUAL END ------------------";
    }
}
