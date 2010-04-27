package org.workcraft.plugins.workspace.handlers;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.PluginConsumer;
import org.workcraft.PluginProvider;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.interop.Importer;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Import;
import org.workcraft.workspace.FileHandler;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

@DisplayName("Open in Workcraft")
public class WorkcraftOpen implements FileHandler, PluginConsumer {
	private PluginProvider pluginProvider;

	@Override
	public void processPlugins(PluginProvider pluginManager) {
		this.pluginProvider = pluginManager;
	}

	public boolean accept(File f) {
		if (Import.chooseBestImporter(pluginProvider, f) != null)
			return true;
		else
			return false;
	}

	public void execute(File f, Framework framework) {

		try {
			final Workspace workspace = framework.getWorkspace();
			final Importer importer = Import.chooseBestImporter(pluginProvider, f);

			Model model = Import.importFromFile(importer, f);

			Path<String> path = workspace.getWorkspacePath(f.getParentFile());

			WorkspaceEntry we = workspace.add(path, FileUtils.getFileNameWithoutExtension(f), model, true);
			framework.getMainWindow().createEditorWindow(we);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(framework.getMainWindow(), e.getMessage(), "I/O error", JOptionPane.ERROR_MESSAGE);
		} catch (DeserialisationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(framework.getMainWindow(), e.getMessage(), "Import error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
