package process;

import helpers.BBPipedInputStream;

import java.io.IOException;
import java.util.ArrayList;
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
    public Grep(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell, String grep) throws IOException {
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
        try {
            if (helpOnly) helpOnly();
            else {
                if (hasPipedInput()) pipeVersion();
                else stdinVersion();
            }
        } catch (IOException e) {
            return;                                 // Killed process
        }
    }

    /**
     * Prints help.
     *
     * @throws IOException
     */
    private void helpOnly() throws IOException {
        output.write(getMan().getBytes());
        output.close();
        return;
    }

    /**
     * Piped version.
     *
     * @throws IOException
     */
    private void pipeVersion() throws IOException {
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
    }

    /**
     * Stdin version.
     *
     * @throws IOException
     */
    private void stdinVersion() throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        String currentLine = "";

        while ((currentLine = shell.getLine()) != null) {
            lines.add(currentLine);
        }

        String[] linesField = new String[lines.size()];
        lines.toArray(linesField);
        for (String line : linesField) {
            if (line.contains(stringToGrep)) {
                output.write(line.getBytes());
                output.write("\n".getBytes());
            }
        }
        output.close();
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