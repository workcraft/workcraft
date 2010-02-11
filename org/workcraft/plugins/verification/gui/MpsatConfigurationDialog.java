package org.workcraft.plugins.verification.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.workcraft.plugins.verification.MpsatMode;
import org.workcraft.plugins.verification.MpsatPreset;
import org.workcraft.plugins.verification.MpsatPresetManager;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class MpsatConfigurationDialog extends JDialog {

	private JPanel content, presetPanel, optionsPanel, reachPanel, buttonsPanel;
	private JComboBox presetCombo, modeCombo, satCombo, verbosityCombo;
	private JButton manageButton, saveButton, runButton, updateButton, cancelButton;
	private JTextArea reachText;
	private JCheckBox minimiseCostCheck;
	private MpsatPresetManager presetManager;

	class IntMode {
		public int value;
		public String description;

		public IntMode(int value, String description) {
			this.value = value;
			this.description = description;
		}

		public String toString() {
			return description;
		}
	}

	private void createPresetPanel() {
		presetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 3));

		presetCombo = new JComboBox();
		for (MpsatPreset p : presetManager.getPresets())
			presetCombo.addItem(p);

		presetCombo.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applySelectedPreset();
			}
		});


		manageButton = new JButton ("Manage presets...");
		updateButton = new JButton ("Save settings");

		saveButton = new JButton ("Save settings as new preset...");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createPreset();
			}
		});

		presetPanel.add(GUI.createLabeledComponent(presetCombo, "Preset:"));
		presetPanel.add(updateButton);
		presetPanel.add(saveButton);
		presetPanel.add(manageButton);

	}

	private void createOptionsPanel() {
		optionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 3));
		optionsPanel.setBorder(BorderFactory.createTitledBorder("MPSat options"));

		modeCombo = new JComboBox();
		modeCombo.setEditable(false);

		for (MpsatMode mode : MpsatMode.modes)
			modeCombo.addItem(mode);

		modeCombo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MpsatMode selectedMode = (MpsatMode)modeCombo.getSelectedItem();

				if (selectedMode.isReach())
					reachPanel.setVisible(true);
				else
					reachPanel.setVisible(false);
			}
		});

		optionsPanel.add(GUI.createLabeledComponent(modeCombo, "Mode:"));

		satCombo = new JComboBox();
		satCombo.addItem(new IntMode(0, "ZChaff"));
		satCombo.addItem(new IntMode(1, "MiniSat"));

		optionsPanel.add(GUI.createLabeledComponent(satCombo, "SAT solver:"));

		minimiseCostCheck = new JCheckBox("Minimise cost function (may be slow)");
		optionsPanel.add(minimiseCostCheck);

		verbosityCombo = new JComboBox();
		for (int i=0; i<=9; i++)
			verbosityCombo.addItem(new IntMode(i, Integer.toString(i)));

		optionsPanel.add(GUI.createLabeledComponent(verbosityCombo, "Verbosity level:"));
	}

	private void createReachPanel() {
		reachPanel = new JPanel  (new BorderLayout());
		reachPanel.setBorder(BorderFactory.createTitledBorder("Property specification (Reach)"));

		reachText = new JTextArea();
		reachText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		reachText.setText("test");
		reachPanel.add(reachText);
	}

	private void applySelectedPreset() {
		MpsatPreset p = (MpsatPreset)presetCombo.getSelectedItem();

		modeCombo.setSelectedItem(p.getMode());
		satCombo.setSelectedIndex(p.getSatSolver());
		verbosityCombo.setSelectedIndex(p.getVerbosity());
		minimiseCostCheck.setSelected(p.isMinimiseCost());
		reachText.setText(p.getReach());
	}



	public MpsatConfigurationDialog(Window owner, MpsatPresetManager presetManager) {
		super(owner, "MPSat configuration", ModalityType.APPLICATION_MODAL);
		this.presetManager = presetManager;

		createPresetPanel();
		createOptionsPanel();
		createReachPanel();
		createButtonsPanel();

		double size[][] = new double[][] {
				{TableLayout.FILL},
				{presetPanel.getPreferredSize().height, 100, TableLayout.FILL, buttonsPanel.getPreferredSize().height}
		};

		TableLayout layout = new TableLayout(size);
		layout.setHGap(3);
		layout.setVGap(3);

		content = new JPanel(layout);
		content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		content.add(presetPanel, "0 0");
		content.add(optionsPanel, "0 1");
		content.add(reachPanel, "0 2");
		content.add(buttonsPanel, "0 3");

		setContentPane(content);

		presetCombo.setSelectedIndex(0);
	}

	private void createButtonsPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		runButton = new JButton ("Run");
		cancelButton = new JButton ("Cancel");

		buttonsPanel.add(cancelButton);
		buttonsPanel.add(runButton);
	}

	private void createPreset() {
		String desc = JOptionPane.showInputDialog(this, "Please enter the description of the new preset:");

		if (! (desc == null || desc.isEmpty())) {
			MpsatPreset preset = new MpsatPreset((MpsatMode)modeCombo.getSelectedItem(),
					verbosityCombo.getSelectedIndex(), satCombo.getSelectedIndex(),
					minimiseCostCheck.isSelected(), reachText.getText(), desc, false );
			presetManager.createPreset(preset);
			presetCombo.addItem(preset);
		}
	}


}
