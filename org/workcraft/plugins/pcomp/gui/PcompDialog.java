package org.workcraft.plugins.pcomp.gui;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooserDialog;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class PcompDialog extends JDialog {

	private final Framework framework;

	public PcompDialog(Window owner, Framework framework) {
		super(owner, "Parallel composition", ModalityType.DOCUMENT_MODAL);
		this.framework = framework;

		final JPanel content = createContents();
		this.setContentPane(content);
	}

	private JPanel createContents() {
		final JPanel content = new JPanel(new BorderLayout());

		final Func<Path<String>, Boolean> stgFilter = new Func<Path<String>, Boolean>() {
			@Override
			public Boolean eval(Path<String> arg) {
				WorkspaceEntry entry = framework.getWorkspace().getOpenFile(arg);

				if (entry != null && (entry.getObject() instanceof STGModel  || entry.getObject() instanceof VisualSTG))
					return true;
				if (arg.getNode().endsWith(".g"))
					return true;
				return false;
			}
		};

		final JButton chooseButton = new JButton("Choose...");
		chooseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WorkspaceChooserDialog chooser = new WorkspaceChooserDialog(PcompDialog.this, "Choose STG", framework.getWorkspace(), stgFilter);
				GUI.centerFrameToParent(chooser, PcompDialog.this);
				chooser.choose();
				setVisible(false);
			}
		});

		content.add(chooseButton);

		return content;
	}
}
