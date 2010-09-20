package org.workcraft.plugins.workspace.handlers;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.tasks.TaskFailureNotifier;
import org.workcraft.plugins.shared.tasks.PunfTask;
import org.workcraft.workspace.FileHandler;

public class PunfUnfolding implements FileHandler {
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

		if (getLastUnfoldingPath() != null)
			fc.setCurrentDirectory(new File(getLastUnfoldingPath()));

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

		setLastUnfoldingPath(path);

		PunfTask task = new PunfTask(f.getPath(), path);

		framework.getTaskManager().queue(task, "Unfolding " + f.getName(), new TaskFailureNotifier());
	}

	@Override
	public String getDisplayName() {
		return "Unfold using punf";
	}

	public void setLastUnfoldingPath(String path) {
		framework.getConfig().set("Verification.punf.lastUnfoldingPath", path);
	}

	public String getLastUnfoldingPath() {
		return framework.getConfig().getString("Verification.punf.lastUnfoldingPath", null);
	}
}
