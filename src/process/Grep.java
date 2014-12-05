package process;

import helpers.BBPipedInputStream;

import java.io.IOException;
import java.util.List;

/**
 * This class represents the Grep process.
 * Grep process is used to find user-specific lines in file.
 * @author Radek Bouda
 */
public class Grep extends AbstractProcess {
    /** String to be found in lines. */
    private String stringToGrep;

    /**
     * Creates new process
     *
     * @param pid       process ID
     * @param parentPid process id of parent
     * @param input     PipedInputStream
     * @param commands  list with commands
     * @param shell     parent shell
     */
    public Grep(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell, String grep) {
        super(pid, parentPid, input, commands, shell);
        if (grep.equalsIgnoreCase(AbstractProcess.HELP_COMMAND)) {
            helpOnly = true;
        } else {
            helpOnly = false;
            this.stringToGrep = grep;
        }
    }

    /**
     * Own job of the process.
     */
    @Override
    protected void processRun() {
        if (helpOnly || !hasPipedInput()) {
            try {
                output.write(getMan().getBytes());
                output.close();
                return;
            } catch (IOException e) {
                return;
            }
        }


        try {
            String text = getStringFromInput();
            if (text == null || text.equals("")) {
                output.write("\n".getBytes());
            } else {
                String[] lines = text.split("\n");
                for (String line : lines) {
                    if (line.contains(stringToGrep)) {
                        output.write(line.getBytes());
                        output.write("\n".getBytes());
                    }
                }
            }
            output.close();
        } catch (IOException e) {
            return;
        }
    }


    /**
     * Returns a manual page of a process.
     *
     * @return Manual page
     */
    public static String getMan() {
        return "------------------ GREP PROCESS ------------------\n" +
                "- gets lines from pipe and prints those which contain string\n\n" +
                "Example: cat \"file\" | grep <str>\n" +
                "- prints lines containing <str> from content of the selected file (rel/abs path) to the standard output\n\n" +
                "Example: cat \"file\" | grep <str> > \"output_file\"\n" +
                "- saves lines containing <str> from content of the selected file (rel/abs path) to the output file\n" +
                "------------------ MANUAL END -------------------";
    }
}