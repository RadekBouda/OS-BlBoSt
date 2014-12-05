package process;

import helpers.BBPipedInputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents the Sort process.
 * Process is used to sort data from STDIN.
 * @author Radek Bouda
 */
public class Sort extends AbstractProcess {

    /**
     * Creates new process
     * @param pid process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands list with commands
     * @param shell parent shell
     */
    public Sort(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
    }

    /**
     * Own job of the process.
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
            if(text == null || text.equals("")){
                output.write("\n".getBytes());
            }
            else {
                String[] lines = text.split("\n");
                Arrays.sort(lines);
                for(String line : lines){
                    output.write(line.getBytes());
                    output.write("\n".getBytes());
                }
            }
            output.close();
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Stdin version. Reads from stdin.
     */
    private void stdInput() {
        try {
            ArrayList<String> lines = new ArrayList<String>();
            String currentLine = "";

            while((currentLine = shell.getLine()) != null){
                lines.add(currentLine);
            }

            String[] linesField = new String[lines.size()];
            lines.toArray(linesField);
            Arrays.sort(linesField);
            for(String line : linesField){
                output.write(line.getBytes());
                output.write("\n".getBytes());
            }
            output.close();
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Returns a manual page of a process.
     * @return Manual page
     */
    public static String getMan() {
        return "------------------ SORT PROCESS ------------------\n"+
                "- sorts the lines of standard input\n\n"+
                "Syntax: sort\n"+
                "- executes standard input, reading lines\n"+
                "- you can use CTRL+D to finish STDIN and print sorted lines\n\n"+
                "Example: cat \"file\" | sort\n"+
                "- prints the content of the selected file (rel/abs path) to the standard output, but lines are sorted\n\n"+
                "Example: cat \"file\" | sort > \"output_file\"\n"+
                "- saves the content of the selected file (rel/abs path) to the output file, but lines are sorted\n"+
                "------------------ MANUAL END -------------------";
    }
}
