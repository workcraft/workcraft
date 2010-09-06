package org.workcraft.plugins.workspace.handlers;

import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.annotations.DisplayName;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.util.Import;
import org.workcraft.workspace.FileHandler;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

@DisplayName("Open in Workcraft")
public class WorkcraftOpen implements FileHandler {
	private final Framework framework;

	public WorkcraftOpen(Framework framework) {
		this.framework = framework;
	}

	public boolean accept(File f) {
		if (Import.chooseBestImporter(framework.getPluginManager(), f) != null)
			return true;
		else
			return false;
	}

	public void execute(File f) {

		try {
			final Workspace workspace = framework.getWorkspace();
			WorkspaceEntry we = workspace.open(f, false);
			framework.getMainWindow().createEditorWindow(we);
		} catch (DeserialisationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(framework.getMainWindow(), e.getMessage(), "Import error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
