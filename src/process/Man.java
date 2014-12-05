package process;

import helpers.BBPipedInputStream;
import kernel.Kernel;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This class represents a process which is generating manual pages.
 *
 * @author Radek Bouda, David Steinberger
 * @version 1.0.0
 */
public class Man extends AbstractProcess{
    private String manPage;

    /**
     * Create new process
     *
     * @param pid process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands list with commands
     * @param shell parent shell
     * @param manPage manual page to show
     */
    public Man(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell, String manPage) {
        super(pid, parentPid, input, commands, shell);
        if(manPage.equalsIgnoreCase(AbstractProcess.HELP_COMMAND)){
            helpOnly = true;
        } else {
            helpOnly = false;
            this.manPage = manPage;
        }
    }

    /**
     * Create new process
     *
     * @param pid process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands list with commands
     * @param shell parent shell
     */
    public Man(int pid, int parentPid, BBPipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
        this.manPage = null;
        this.helpOnly = false;
    }

    /**
     * Own job.
     */
    @Override
    protected void processRun() {
        try {
            if(helpOnly){
                printOwnHelp();
            } else if(manPage == null || manPage.equals("")) {
                noArgsVersion();
            } else if(!builtInVersion()) {
                classicProcessVersion();
            }
        } catch (IOException e) {
            // error during IO with console, can't be printed to the console because the problem is communicating with a console
            return;
        } catch (ClassNotFoundException e) {
            //unknown command
            try {
                shell.printError("man: " + this.manPage + " is an unknown process.");
                output.close();
            } catch (IOException ex) {
                // error during IO with console, can't be printed to the console because the problem is communicating with a console
                return;
            }

        } catch (NoSuchMethodException e) {
            // manual page for a process is not defined (process class doesn't contain public static String getMan() method)
            try {
                shell.printError("man: Manual page for process " + this.manPage + " not ready yet.");
                output.close();
            } catch (IOException ex) {
                // error during IO with console, can't be printed to the console because the problem is communicating with a console
                return;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace(); // i don't know when this exception can occur
        } catch (InvocationTargetException e) {
            e.printStackTrace(); // it will be very big surprise when this exception occur
        }
    }

    /**
     * Classic version for basic process.
     *
     * @throws ClassNotFoundException
     * @throws IOException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    private void classicProcessVersion() throws ClassNotFoundException, IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method man = Class.forName(Kernel.PACKAGE + "." + manPage.substring(0, 1).toUpperCase() + manPage.substring(1, manPage.length()).toLowerCase()).getDeclaredMethod("getMan");
        output.write(man.invoke(null, null).toString().getBytes());
        output.close();
    }

    /**
     * Version for builtin commands.
     *
     * @return true/false
     * @throws IOException
     */
    private boolean builtInVersion() throws IOException {
        if(manPage.equals("cd") || manPage.equals("echo") || manPage.equals("exit") || manPage.equals("pwd") || manPage.equals("builtin") || manPage.equals("builtins")) {
            output.write(builtinMan().getBytes());
            output.close();
            return true;
        }
        return false;
    }

    /**
     * Version for no arguments.
     *
     * @throws IOException
     */
    private void noArgsVersion() throws IOException {
        output.write("Default shell man\n".getBytes());
        output.write(listOfProcesses().getBytes());
        output.close();
    }

    /**
     * Version for its own help.
     *
     * @throws IOException
     */
    private void printOwnHelp() throws IOException {
        output.write(getMan().getBytes());
        output.close();
    }

    /**
     * Returns a manual page of a process.
     *
     * @return Manual page
     */
    public static String getMan(){
        return "----------------- MAN PROCESS ------------------\n"+
                "- shows a manual page for selected process\n\n"+
                "Syntax: man <process_name>\n"+
                "------------------ MANUAL END ------------------";
    }

    /**
     * Gets a manual page of builtin commands.
     *
     * @return Manual page
     */
    private String builtinMan() {
        return "---------------- MAN BUILTINS ------------------\n" +
                "cd - change directory\n usage: cd <relative/absolute path>\n\n" +
                "echo - write arguments to the standard output\n usage: echo <args>\n\n" +
                "exit - close current shell\n usage: exit\n\n" +
                "pwd - print working directory\n usage: pwd \n\n" +
                "---------------- MANUAL END -------------------\n";
    }

    /**
     * Gets list of implemented methods.
     *
     * @return list
     */
    private String listOfProcesses() {
        return "---------- LIST OF IMPLEMENTED COMMANDS---------\n" +
                "cat - print files\n" +
                "grep - print lines containing a pattern\n" +
                "kill - terminate a process\n" +
                "ls - list directory contents\n" +
                "man - display manual pages\n" +
                "ps - list processes\n" +
                "shell - recursive shell\n" +
                "shutdown - close down the system\n" +
                "sort - sort lines of text files\n" +
                "wc - count lines\n" +
                "------------------ END OF LIST -------------------\n" +
                "---------- LIST OF BUILTIN SHELL COMMANDS --------\n" +
                "cd - change directory\n" +
                "echo - write arguments to the standard output\n" +
                "exit - close current shell\n" +
                "pwd - print working directory\n" +
                "------------------ END OF LIST -------------------\n";
    }
}
