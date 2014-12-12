package org.workcraft.plugins.petrify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.plugins.petrify.tasks.SynthesisResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;


public class SynthesisResultHandler extends DummyProgressMonitor<SynthesisResult> {

	@Override
	public void finished(Result<? extends SynthesisResult> result, String description) {
		if (result.getOutcome() == Outcome.FAILED) {
			String msg = result.getReturnValue().getStderr();
			final Framework framework = Framework.getInstance();
			JOptionPane.showMessageDialog(framework.getMainWindow(), msg, "Error", JOptionPane.ERROR_MESSAGE);
		} else if (result.getOutcome() == Outcome.FINISHED) {

			// output logfile at console
			File logFile = result.getReturnValue().getLogFile();
			if (logFile != null)
				try {
					BufferedReader br = new BufferedReader(new FileReader(logFile));
					String currentLine;
					while ((currentLine = br.readLine()) != null) {
						System.out.println(currentLine);
					}
					br.close();
				} catch (FileNotFoundException e1) {
					throw new RuntimeException(e1);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			File equationsFile = result.getReturnValue().getEquationFile();

			// output equationsFile to console as well, but later insert in workspace for further processing
			if (equationsFile != null)
				try {
					BufferedReader br = new BufferedReader(new FileReader(equationsFile));
					String currentLine;
					while ((currentLine = br.readLine()) != null) {
						System.out.println(currentLine);
					}
					br.close();
				} catch (FileNotFoundException e1) {
					throw new RuntimeException(e1);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}

}
