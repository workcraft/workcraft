package org.workcraft.plugins.cpog.tasks;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;

public class PGMinerResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

	private VisualCPOG visualCpog;

	public PGMinerResultHandler(VisualCPOG visualCpog) {
		this.visualCpog = visualCpog;
	}

	public void finished(final Result<? extends ExternalProcessResult> result, String description) {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {


				@Override
				public void run() {
					byte[] output = result.getReturnValue().getOutputFile("output.1.cpog");
					String line = "";
					System.out.println("\nResulting Parameterised Graph Equations");
					for (byte b : output) {
						if ((char)b != '\n') {
							line = line + (char)b;
						} else {
							line = line.replaceAll("\r", "");
							System.out.println(line);
							visualCpog.getSelectionTool().insertExpression(line, false, false, true);
							line = "";
						}
					}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}

