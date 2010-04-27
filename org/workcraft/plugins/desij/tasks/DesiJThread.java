/**
 *
 */
package org.workcraft.plugins.desij.tasks;

/**
 * @author Dominic Wist
 *
 */
public class DesiJThread extends Thread {

	private String[] commandLineArguments;
	private int exitCode = 0;

	/*
	 * Constructor
	 */
	public DesiJThread(String[] args) {
		this.commandLineArguments = args;
	}

	/*
	 * Should be called after execution of desiJ
	 */
	public int getExitCode() {
		return exitCode;
	}

	@SuppressWarnings("deprecation")
	public void killThread() {
		this.stop();
		// impossible to do it in any other way without changing the desiJ sources to a large extent
	}

	public void run() {
		exitCode = net.strongdesign.desij.DesiJ.desiJMain(commandLineArguments);
	}
}
