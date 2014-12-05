package process;

import helpers.BBPipedInputStream;

import java.io.File;
import java.io.IOException;
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
    public Ls(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell) throws IOException {
        super(pid, parentPid, input, commands, shell);
        this.path = "";
        helpOnly = false;
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
    public Ls(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell, String path) throws IOException {
        super(pid, parentPid, input, commands, shell);
        if(path.equalsIgnoreCase(AbstractProcess.HELP_COMMAND)){
            helpOnly = true;
            this.path = "";
        } else {
            helpOnly = false;
            this.path = path;
        }
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        if(helpOnly) {
            try{
                output.write(getMan().getBytes());
                output.close();
                return;
            } catch (IOException e){
                return;                     // Killed process
            }
        }
        try {
            File directory = new File(shell.getPath(path));
            if(!directory.exists()){
                shell.printError("ls: " + path + ": no such a file or directory");
                output.close();
                return;
            }
            String files[] = directory.list();
            for(String file : files) output.write((file + "\t").getBytes());
            output.close();
        } catch (IOException e) {
            return;                         // Killed process
        }
    }

    /**
     * Returns a manual page of a process.
     * @return Manual page
     */
    public static String getMan() {
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
