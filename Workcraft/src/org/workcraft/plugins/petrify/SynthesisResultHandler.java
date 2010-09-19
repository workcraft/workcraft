package org.workcraft.plugins.petrify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.plugins.petrify.tasks.SynthesisResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;


public class SynthesisResultHandler extends DummyProgressMonitor<SynthesisResult> {
	private final Framework framework;

	public SynthesisResultHandler(Framework framework) {
		this.framework = framework;
	}

	@Override
	public void finished(Result<? extends SynthesisResult> result, String description) {
		if (result.getOutcome() == Outcome.FAILED) {
			JOptionPane.showMessageDialog(framework.getMainWindow(), "Petrify execution failed :-(", "Error", JOptionPane.ERROR_MESSAGE);
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
				} catch (FileNotFoundException e1) {
					throw new RuntimeException(e1);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}


/*			// pop up MessageBox
			final String successMessage = "Petrify synthesis succeeded.";
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, successMessage);
				}
			});*/
		}
	}

}
