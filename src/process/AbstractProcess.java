package process;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * Abstract class which creates a primitive model for all the processes.
 * @author Jan Blaha, Radek Bouda
 * @version 1.0.0
 */
public abstract class AbstractProcess {

	private int Pid;
	private Date startTime;
	private int ParentPid;
	private int [] descendantPids;
	private InputStream input;
	private OutputStream output;
	
	
	public int getPid() {
		return Pid;
	}
	public void setPid(int pid) {
		Pid = pid;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public int getParentPid() {
		return ParentPid;
	}
	public void setParentPid(int parentPid) {
		ParentPid = parentPid;
	}
	public int[] getDescendantPids() {
		return descendantPids;
	}
	public void setDescendantPids(int[] descendantPids) {
		this.descendantPids = descendantPids;
	}
	public InputStream getInput() {
		return input;
	}
	public void setInput(InputStream input) {
		this.input = input;
	}
	public OutputStream getOutput() {
		return output;
	}
	public void setOutput(OutputStream output) {
		this.output = output;
	}
}
