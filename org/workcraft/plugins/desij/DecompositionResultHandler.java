package org.workcraft.plugins.desij;

import java.io.File;

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

	public DecompositionResultHandler(Framework framework) {
		this.framework = framework;
	}

	@Override
	public void finished(Result<? extends DesiJResult> result, String description) {

		if (result.getOutcome() == Outcome.FINISHED) {
			final Workspace workspace = framework.getWorkspace();

			DesiJResult desijResult = result.getReturnValue();
			File[] componentSTGFiles = desijResult.getComponentFiles();

			if (componentSTGFiles != null) {

				Path<String> componentsDirectoryPath = Path.fromString(
						desijResult.getSpecificationModel().getTitle() + "-components");

				try {
					workspace.delete(componentsDirectoryPath);
				} catch (OperationCancelledException e) {
					return;
				}

				File componentsDir = workspace.getFile(componentsDirectoryPath);
				componentsDir.mkdirs();

				for (File file : componentSTGFiles) {
					File target = new File(componentsDir, getComponentSuffix(file) + ".g");
					file.renameTo(target);
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

	private boolean deleteDirectory(File directory) {

		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file: files) {
				if (file.isDirectory())
					deleteDirectory(file);
				else
					file.delete();
			}
		}

		return directory.delete();
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
