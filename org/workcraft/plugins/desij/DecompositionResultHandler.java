package org.workcraft.plugins.desij;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.desij.tasks.DesiJResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.workspace.Workspace;

public class DecompositionResultHandler extends DummyProgressMonitor<DesiJResult> {

	private Framework framework;
	private boolean logFileOutput;

	public DecompositionResultHandler(Framework framework, boolean logFileOutput) {
		this.framework = framework;
		this.logFileOutput = logFileOutput;
	}

	@Override
	public void finished(Result<? extends DesiJResult> result, String description) {

		if (result.getOutcome() == Outcome.FINISHED) {
			final Workspace workspace = framework.getWorkspace();

			DesiJResult desijResult = result.getReturnValue();

			// logfile output at console
			if (this.logFileOutput)
				try {
					BufferedReader br = new BufferedReader(new FileReader(desijResult.getLogFile()));
					String currentLine;
					while ((currentLine = br.readLine()) != null) {
						System.out.println(currentLine);
					}
				} catch (FileNotFoundException e1) {
					throw new RuntimeException(e1);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			// components output in workspace
			if (desijResult.getComponentFiles() != null)  // independent from the compiler
				if (desijResult.getComponentFiles().length > 0) {

					Path<String> componentsDirectoryPath = Path.fromString(
							desijResult.getSpecificationModel().getTitle() + "-components");

					try {
						workspace.delete(componentsDirectoryPath);
					} catch (OperationCancelledException e) {
						return;
					}

					File componentsDir = workspace.getFile(componentsDirectoryPath);
					componentsDir.mkdirs();

					for (File file : desijResult.getComponentFiles()) {
						File target = new File(componentsDir, getComponentSuffix(file) + ".g");
						file.renameTo(target);
					}

					// output of the petrify- or mpsat-equations
					if (desijResult.getEquationFile() != null) { // synthesis successful
						try {
							BufferedReader br = new BufferedReader(new FileReader(desijResult.getEquationFile()));
							String currentLine;
							while ((currentLine = br.readLine()) != null) {
								System.out.println(currentLine);
							}
						} catch (FileNotFoundException e1) {
							throw new RuntimeException(e1);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}

					workspace.fireWorkspaceChanged();

					// pop up MessageBox
					final String successMessage = "Decomposition succeeded!";
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(null, successMessage);
						}
					});
				}
			if (desijResult.getModifiedSpecResult() != null) {
				String resultPath = desijResult.getSpecificationModel().getTitle() + "_modifiedResult.g";

				try {
					workspace.delete(Path.fromString(resultPath));
				} catch (OperationCancelledException e) {
					return;
				}

				File modifiedSpecification = workspace.getFile(Path.fromString(resultPath));
				desijResult.getModifiedSpecResult().renameTo(modifiedSpecification);

				workspace.fireWorkspaceChanged(); // update of workspace window
			}
		}
		else if (result.getOutcome() != Outcome.CANCELLED) {
			final String errorMessage = "DesiJ execution failed :-(";

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
				}
			});
		}
	}

	private String getComponentSuffix(File componentFile) {

		String fileName = componentFile.getName(); // stg.g__final_suffix.g

		// determine the suffix
		String suffix = fileName.substring(
				fileName.lastIndexOf("__final_") + 8,
				fileName.lastIndexOf(".g"));

		return suffix;
	}

}
