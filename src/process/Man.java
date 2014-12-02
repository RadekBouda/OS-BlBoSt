package process;

import java.io.IOException;
import java.io.PipedInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * This class represents a process which is generating manual pages.
 * @author Radek Bouda
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
    public Man(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell, String manPage) {
        super(pid, parentPid, input, commands, shell);
        this.manPage = manPage;
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
    public Man(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
        this.manPage = null;
    }

    @Override
    protected void processRun() {



      try {
          if (manPage == null || manPage.equals("")) {
              output.write("Default shell man".getBytes());
              output.close();
              return;
          }
          Method man = Class.forName("process." + manPage.substring(0, 1).toUpperCase() + manPage.substring(1, manPage.length()).toLowerCase()).getDeclaredMethod("getMan");
          output.write(man.invoke(null, null).toString().getBytes());
          output.close();
      } catch (IOException e){
          // error during IO with console, can't be printed to the console because the problem is communicating with a console
          e.printStackTrace();
      } catch (ClassNotFoundException e){
          //unknown command
          try{
              output.write(("Man error: "+this.manPage+" is an unknown process.").getBytes());
              output.close();
          }catch (IOException ex){
              // error during IO with console, can't be printed to the console because the problem is communicating with a console
              e.printStackTrace();
          }

      } catch (NoSuchMethodException e){
          // manual page for a process is not defined (process class doesn't contain public static String getMan() method)
          try{
              output.write(("Man error: Manual page for process "+this.manPage+" not ready yet.").getBytes());
              output.close();
          }catch (IOException ex){
              // error during IO with console, can't be printed to the console because the problem is communicating with a console
              e.printStackTrace();
          }
      } catch (IllegalAccessException e){
          e.printStackTrace(); // i don't know when this exception can occur
      } catch (InvocationTargetException e){
          e.printStackTrace(); // it will be very big surprise when this exception occur
      }

    }

    public static String getMan(){
        return "------------------ MAN PROCESS ------------------\n"+
                "- shows a manual page for selected process\n\n"+
                "Syntax: man <process_name>\n"+
                "------------------ MANUAL END ------------------";
    }
}