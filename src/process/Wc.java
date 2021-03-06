package process;

import helpers.BBPipedInputStream;

import java.io.IOException;
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
    public Wc(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell) throws IOException {
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
            if(text == null || text.equals("")) output.write("0".getBytes());
            else output.write((text.split("\n").length + "").getBytes());
            output.close();
        } catch (IOException e) {
            return;                 // Killed process
        }
    }

    /**
     * Stdin version. Reads from stdin.
     */
    private void stdInput() {
        try {
            int count = 0;
            while(shell.getLine() != null) count++;
            output.write(("" + (count + 1)).getBytes());
            output.close();
        } catch (IOException e) {
            return;                     // Killed process
        }
    }
    
   /**
    * Returns a manual page of a process.
    * @return Manual page
    */
    public static String getMan() {
    return "------------------ WC PROCESS ------------------\n"+
            "- print the number of newlines in files\n\n"+
            "Syntax: wc <file>\n"+
            "- prints  the number of lines in file in directory at selected relative or absolute path (if the file exists)\n"+
            "------------------ MANUAL END ------------------";
    }
}
