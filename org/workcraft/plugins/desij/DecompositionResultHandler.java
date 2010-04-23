package org.workcraft.plugins.desij;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Import;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.desij.tasks.DesiJResult;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;

public class DecompositionResultHandler extends DummyProgressMonitor<DesiJResult> {

	private Framework framework;

	public DecompositionResultHandler(Framework framework) {
		this.framework = framework;
	}

	@Override
	public void finished(Result<? extends DesiJResult> result, String description) {

		if (result.getOutcome() == Outcome.FINISHED) {

			DesiJResult desijResult = result.getReturnValue();
			File[] componentSTGFiles = desijResult.getComponentFiles();

			if (componentSTGFiles != null) {

				try {

					Path<String> partsDirectoryPath = Path.fromString("Components");
					File partsDir = framework.getWorkspace().getFile(partsDirectoryPath);
					partsDir.mkdirs();

					Model[] componentModels = new Model[componentSTGFiles.length];

					for (int i = 0; i < componentSTGFiles.length; i++) {
						componentModels[i] =
							Import.importFromFile(new DotGImporter(), componentSTGFiles[i]);

						framework.getWorkspace().add(partsDirectoryPath, "component" + i , componentModels[i], true);
					}

					framework.getMainWindow().getWorkspaceWindow().repaint();

				} catch (IOException e) {
					throw new RuntimeException(e);
				} catch (DeserialisationException e) {
					throw new RuntimeException(e);
				}

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

}
