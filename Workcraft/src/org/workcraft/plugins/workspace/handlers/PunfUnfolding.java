package org.workcraft.plugins.workspace.handlers;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.workcraft.Config;
import org.workcraft.ConfigurablePlugin;
import org.workcraft.Framework;
import org.workcraft.annotations.DisplayName;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.tasks.TaskFailureNotifier;
import org.workcraft.plugins.shared.tasks.PunfTask;
import org.workcraft.workspace.FileHandler;

@DisplayName("Unfold using punf")
public class PunfUnfolding implements FileHandler, ConfigurablePlugin {
	private String lastUnfoldingPath = null;
	private final Framework framework;

	public PunfUnfolding(Framework framework) {
		this.framework = framework;
	}

	public boolean accept(File f) {
		if (f.getName().endsWith(".g"))
			return true;
		else
			return false;
	}

	public void execute(File f) {
		JFileChooser fc = new JFileChooser();
		fc.setDialogType(JFileChooser.SAVE_DIALOG);

		if (lastUnfoldingPath != null)
			fc.setCurrentDirectory(new File(lastUnfoldingPath));

		fc.setFileFilter(new FileFilters.GenericFileFilter(".mci", "Petri Net Unfolding Prefix (.mci)"));
		fc.setMultiSelectionEnabled(false);
		fc.setDialogTitle("Save unfolding as...");

		String path;

		while (true) {
			if(fc.showSaveDialog(framework.getMainWindow())==JFileChooser.APPROVE_OPTION) {
				path = fc.getSelectedFile().getPath();

				if (!path.endsWith(".mci"))
					path += ".mci";

				File out = new File(path);

				if (!out.exists())
					break;
				else
					if (JOptionPane.showConfirmDialog(framework.getMainWindow(), "The file \"" + out.getName()+"\" already exists. Do you want to overwrite it?", "Confirm",
							JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
						break;
			} else
				return;
		}

		framework.getConfig().set("Verification.punf.lastUnfoldingPath", path);

		PunfTask task = new PunfTask(f.getPath(), path);

		framework.getTaskManager().queue(task, "Unfolding " + f.getName(), new TaskFailureNotifier());
	}

	@Override
	public void readConfig(Config config) {
		lastUnfoldingPath = config.getString("Verification.punf.lastUnfoldingPath", null);
	}
}
