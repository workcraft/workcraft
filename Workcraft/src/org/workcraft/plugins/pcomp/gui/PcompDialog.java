package org.workcraft.plugins.pcomp.gui;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.workcraft.Framework;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.plugins.shared.util.STGWorkspaceFilter;

@SuppressWarnings("serial")
public class PcompDialog extends JDialog {
	private final Framework framework;
	protected boolean result;
	private Set<Path<String>> sourcePaths;
	private JCheckBox showInEditor;

	JRadioButton leaveOutputs;
	JRadioButton internalize;
	JRadioButton dummify;
	private JCheckBox improvedPcomp;

	public PcompDialog(Window owner, Framework framework) {
		super(owner, "Parallel composition", ModalityType.DOCUMENT_MODAL);
		this.framework = framework;

		final JPanel content = createContents();
		this.setContentPane(content);
	}

	public Set<Path<String>> getSourcePaths() {
		return sourcePaths;
	}

	public boolean showInEditor() {
		return showInEditor.isSelected();
	}

	public boolean isImprovedPcompChecked() {
		return improvedPcomp.isSelected();
	}

	public PCompOutputMode getMode()
	{
		if(leaveOutputs.isSelected())
			return PCompOutputMode.OUTPUT;
		if(internalize.isSelected())
			return PCompOutputMode.INTERNAL;
		if(dummify.isSelected())
			return PCompOutputMode.DUMMY;
		throw new NotSupportedException("No button is selected. Cannot proceed.");
	}

	public boolean run() {
		setVisible(true);
		return result;
	}

	private JPanel createContents() {
		double sizes[][] = {
				{ TableLayout.FILL, TableLayout.PREFERRED },
				{ TableLayout.FILL, TableLayout.PREFERRED }
		};

		final JPanel content = new JPanel(new TableLayout(sizes));

		final WorkspaceChooser chooser = new WorkspaceChooser(framework.getWorkspace(), new STGWorkspaceFilter(framework));
		chooser.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Source STGs"), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
		chooser.setCheckBoxMode(TreeWindow.CheckBoxMode.LEAF);

		content.add(chooser, "0 0 0 1");

		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));

		options.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Options"), BorderFactory.createEmptyBorder(2, 2, 2, 2)));

		JPanel outputOptions = new JPanel();
		outputOptions.setLayout(new BoxLayout(outputOptions, BoxLayout.Y_AXIS));

		outputOptions.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Outputs"), BorderFactory.createEmptyBorder(2, 2, 2, 2)));


		showInEditor = new JCheckBox();
		showInEditor.setText("Show result in editor");

		ButtonGroup dummifyGroup = new ButtonGroup();
		leaveOutputs = new JRadioButton("Leave as outputs");
		internalize = new JRadioButton("Make internal");
		dummify = new JRadioButton("Make dummy");
		leaveOutputs.setSelected(true);

		improvedPcomp = new JCheckBox("Improved parallel composition");

		dummifyGroup.add(leaveOutputs);
		dummifyGroup.add(dummify);
		dummifyGroup.add(internalize);

		options.add(showInEditor, 0);
		outputOptions.add(leaveOutputs);
		outputOptions.add(internalize);
		outputOptions.add(dummify);

		options.add(outputOptions, 1);
		options.add(improvedPcomp, 2);

		content.add(options, "1 0");

		JPanel buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		JButton runButton = new JButton ("Run");

		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				result = true;
				sourcePaths = chooser.getCheckedNodes();
				setVisible(false);
			}
		});

		JButton cancelButton = new JButton ("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				result = false;
				setVisible(false);
			}
		});

		buttonsPanel.add(runButton);

		buttonsPanel.add(cancelButton);

		content.add(buttonsPanel, "1 1");

		getRootPane().setDefaultButton(runButton);

		return content;
	}
}
