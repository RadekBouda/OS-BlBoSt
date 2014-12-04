package process;

import java.io.IOException;
import java.io.PipedInputStream;
import java.util.List;

/**
 * Created by Radek on 4. 12. 2014.
 */
public class Echo extends AbstractProcess {

    String[] data;

    /**
     * Create new process
     *
     * @param pid process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands list with commands
     * @param shell parent shell
     */
    public Echo(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell) {
        super(pid, parentPid, input, commands, shell);
        this.data = null;
    }

    /**
     * Create new process
     *
     * @param pid process ID
     * @param parentPid process id of parent
     * @param input PipedInputStream
     * @param commands list with commands
     * @param shell parent shell
     * @param data data to print
     */
    public Echo(int pid, int parentPid, PipedInputStream input, List<List<String>> commands, Shell shell, String... data) {
        super(pid, parentPid, input, commands, shell);
        this.data = data;
    }

    @Override
    protected void processRun() {
        String toPrint = "";
        if(data == null) return;
        for(int i = 0; i < data.length; i++){
            if(i != (data.length - 1)){
                toPrint = toPrint + data[i] + " ";
            } else {
                toPrint = toPrint + data[i];
            }
        }
        try {
            output.write(toPrint.getBytes());
            output.close();
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}
