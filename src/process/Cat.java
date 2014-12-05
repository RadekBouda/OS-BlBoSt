package process;

import helpers.BBInputStream;

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
    public Cat(int pid, int parentPid, BBInputStream input, List<List<String>> commands, Shell shell, String path) {
        super(pid, parentPid, input, commands, shell);

        if(path.equalsIgnoreCase(AbstractProcess.HELP_COMMAND)){
            helpOnly = true;
            this.path = null;
        } else {
            helpOnly = false;
            this.path = shell.getPath(path);
        }

    }

    /**
     * Create new process. Version without parameters. Copy stdin on output.
     *
     * @param pid Process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands List of commands
     * @param shell parent shell
     */
    public Cat(int pid, int parentPid, BBInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
        this.path = null;
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        if(helpOnly){
            try {
                output.write(getMan().getBytes());
                output.close();
                return;
            } catch (IOException e){
                return;
            }
        }
        if(path == null) {
            if(hasPipedInput()) pipedVersion();
            else stdinVersion();
        }
        else argumentVersion();
    }

    /**
     * Version with piped input.
     */
    private void pipedVersion() {
        try {
            String text = getStringFromInput();
            output.write(text.getBytes());
            output.close();
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Version with arguments.
     */
    private void argumentVersion() {
        try {
            int c;
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            while((c = reader.read()) != -1) output.write(c);
            output.close();
        } catch (FileNotFoundException e) {
            try {
                shell.printError("cat: " + path + ": No such a file or directory");
                output.close();
            } catch (IOException e1) {
                return;
            }
        } catch (IOException e) {
            return;
        }
    }

    /**
     * Version without arguments. Stdin.
     */
    private void stdinVersion() {
        try {
            String line;
            while((line = shell.getLine()) != null) output.write((line + "\n").getBytes());
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
    return "------------------ CAT PROCESS ------------------\n"+
            "- concatenate files and print on the standard output\n"+
            "- can be used with a relative or absolute path as a non-compulsory parameter\n\n"+
            "Syntax: cat <file>\n"+
            "- prints file in current directory (if the file exists)\n\n"+
            "Syntax: cat <relative/absolute path><file>\n"+
            "- prints file in directory at selected relative or absolute path (if the file exists)\n"+
            "------------------ MANUAL END ------------------";
    }
}
