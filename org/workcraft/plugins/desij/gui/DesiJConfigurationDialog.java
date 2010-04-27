package org.workcraft.plugins.desij.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.verification.MpsatMode;
import org.workcraft.plugins.verification.MpsatPreset;
import org.workcraft.plugins.verification.MpsatPresetManager;
import org.workcraft.plugins.verification.MpsatSettings;
import org.workcraft.plugins.verification.MpsatSettings.SolutionMode;
import org.workcraft.plugins.verification.gui.MpsatPresetManagerDialog;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class DesiJConfigurationDialog extends JDialog {

	private JPanel content, presetPanel, reachPanel, buttonsPanel;
	private JLabel numberOfSolutionsLabel;
	private JScrollPane optionsPanel;
	private JComboBox presetCombo, operationCombo, satCombo, verbosityCombo;
	private JButton manageButton, saveAsNewButton, runButton, updatePresetButton, cancelButton;
	private JTextField solutionLimitText;
	private JTextArea reachText;
	private JRadioButton allSolutionsButton, firstSolutionButton, cheapestSolutionButton;
	private MpsatPresetManager presetManager;

	private TableLayout layout;
	private int modalResult = 0;

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
		presetPanel = new JPanel(new SimpleFlowLayout(15, 3));

		presetCombo = new JComboBox();
		for (MpsatPreset p : presetManager.list())
			presetCombo.addItem(p);

		presetCombo.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				applySettingsToControls();
			}
		});


		manageButton = new JButton ("Manage presets...");
		manageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean haveCustomPresets = false;
				for (MpsatPreset p : presetManager.list())
					if (!p.isBuiltIn()) {
						haveCustomPresets = true;
						break;
					}
				if (haveCustomPresets)
					managePresets();
				else
					JOptionPane.showMessageDialog(DesiJConfigurationDialog.this, "There are no custom presets to manage.");

			}
		});

		updatePresetButton = new JButton ("Update preset");
		updatePresetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MpsatPreset selected = (MpsatPreset)presetCombo.getSelectedItem();
				presetManager.update(selected, getSettingsFromControls());
			}
		});

		saveAsNewButton = new JButton ("Save settings as new preset...");
		saveAsNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createPreset();
			}
		});

		presetPanel.add(GUI.createLabeledComponent(presetCombo, "Preset:"));
		presetPanel.add(new SimpleFlowLayout.LineBreak(3));
		presetPanel.add(updatePresetButton);
		presetPanel.add(saveAsNewButton);
		presetPanel.add(manageButton);

	}

	private void managePresets() {
		MpsatPreset selected = (MpsatPreset)presetCombo.getSelectedItem();

		MpsatPresetManagerDialog dlg = new MpsatPresetManagerDialog(this, presetManager);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		GUI.centerFrameToParent(dlg, this);
		dlg.setVisible(true);

		presetCombo.removeAllItems();
		List<MpsatPreset> presets = presetManager.list();

		boolean haveOldSelection = false;

		for (MpsatPreset p : presets) {
			presetCombo.addItem(p);
			if (p == selected)
				haveOldSelection = true;
		}

		if (haveOldSelection)
			presetCombo.setSelectedItem(selected);
		else
			presetCombo.setSelectedIndex(0);

	}

	private void createOptionsPanel() {
		JPanel optionsPanelContent = new JPanel(new SimpleFlowLayout());

		optionsPanel = new JScrollPane(optionsPanelContent);
		optionsPanel.setBorder(BorderFactory.createTitledBorder("DesiJ settings"));
		optionsPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		optionsPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		operationCombo = new JComboBox();
		operationCombo.setEditable(false);

		// DesiJ Operation: Decomposition, redundant place deletion, secure dummy contraction
		for (MpsatMode mode : MpsatMode.modes)
			operationCombo.addItem(mode);

		operationCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MpsatMode selectedMode = (MpsatMode)operationCombo.getSelectedItem();

				if (selectedMode.isReach()) {
					reachPanel.setVisible(true);
					layout.setRow(2, TableLayout.FILL);
				}
				else {
					reachPanel.setVisible(false);
					layout.setRow(2, 0);
				}
			}
		});

		optionsPanelContent.add(new JLabel("Operation:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());
		optionsPanelContent.add(operationCombo);
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(8));

		optionsPanelContent.add(new JLabel("Solution mode:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(-2));

		createSolutionModeButtons();

		optionsPanelContent.add(firstSolutionButton);
		optionsPanelContent.add(cheapestSolutionButton);
		optionsPanelContent.add(allSolutionsButton);

		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());

		solutionLimitText = new JTextField();
		solutionLimitText.setText("WWWW");
		Dimension preferredSize = solutionLimitText.getPreferredSize();
		solutionLimitText.setText("");
		solutionLimitText.setPreferredSize(preferredSize);

		JPanel numberOfSolutionsPanel = new JPanel (new FlowLayout(FlowLayout.LEFT, 3, 0));
		numberOfSolutionsLabel = new JLabel("Maximum number of solutions (leave blank for no limit):");
		numberOfSolutionsPanel.add(numberOfSolutionsLabel);
		numberOfSolutionsPanel.add(solutionLimitText);

		disableNumberOfSolutionControls();

		optionsPanelContent.add(numberOfSolutionsPanel);
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(8));

		satCombo = new JComboBox();
		satCombo.addItem(new IntMode(0, "ZChaff"));
		satCombo.addItem(new IntMode(1, "MiniSat"));

		optionsPanelContent.add(GUI.createLabeledComponent(satCombo, "SAT solver:"));

		verbosityCombo = new JComboBox();
		for (int i=0; i<=9; i++)
			verbosityCombo.addItem(new IntMode(i, Integer.toString(i)));

		optionsPanelContent.add(GUI.createLabeledComponent(verbosityCombo, "Verbosity level:"));
	}

	private void createSolutionModeButtons() {
		firstSolutionButton = new JRadioButton ("Find any solution (default)");
		firstSolutionButton.setSelected(true);
		firstSolutionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableNumberOfSolutionControls();
			}
		});
		allSolutionsButton = new JRadioButton("Find all solutions");
		allSolutionsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableNumberOfSolutionControls();
			}
		});

		cheapestSolutionButton = new JRadioButton("Minimise cost function");
		cheapestSolutionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableNumberOfSolutionControls();
			}
		});


		ButtonGroup bg = new ButtonGroup();
		bg.add(firstSolutionButton); bg.add(allSolutionsButton); bg.add(cheapestSolutionButton);
	}

	private void disableNumberOfSolutionControls() {
		numberOfSolutionsLabel.setEnabled(false);
		solutionLimitText.setEnabled(false);
	}

	private void enableNumberOfSolutionControls() {
		numberOfSolutionsLabel.setEnabled(true);
		solutionLimitText.setEnabled(true);
	}

	private void createReachPanel() {
		reachPanel = new JPanel  (new BorderLayout());
		reachPanel.setBorder(BorderFactory.createTitledBorder("Property specification (Reach)"));

		reachText = new JTextArea();
		reachText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		reachText.setText("test");
		reachPanel.add(reachText);
	}

	private void applySettingsToControls() {
		MpsatPreset p = (MpsatPreset)presetCombo.getSelectedItem();

		if (p == null)
			return;

		if (p.isBuiltIn()) {
			updatePresetButton.setEnabled(false);
			updatePresetButton.setToolTipText("Cannot make changes to a built-in preset");
		}
		else {
			updatePresetButton.setEnabled(true);
			updatePresetButton.setToolTipText("Save these settings to the currently selected preset");
		}

		MpsatSettings settings = p.getSettings();

		operationCombo.setSelectedItem(settings.getMode());
		satCombo.setSelectedIndex(settings.getSatSolver());
		verbosityCombo.setSelectedIndex(settings.getVerbosity());
		switch (settings.getSolutionMode()) {
		case ALL:
			allSolutionsButton.setSelected(true);
			enableNumberOfSolutionControls();
			break;
		case MINIMUM_COST:
			cheapestSolutionButton.setSelected(true);
			disableNumberOfSolutionControls();
			break;
		case FIRST:
			firstSolutionButton.setSelected(true);
			disableNumberOfSolutionControls();
			break;
		}
		int n = settings.getSolutionNumberLimit();

		if (n>0)
			solutionLimitText.setText(Integer.toString(n));
		else
			solutionLimitText.setText("");

		reachText.setText(settings.getReach());
	}

	public DesiJConfigurationDialog(Window owner, MpsatPresetManager presetManager) {
		super(owner, "MPSat configuration", ModalityType.APPLICATION_MODAL);
		this.presetManager = presetManager;

		createPresetPanel();
		createOptionsPanel();
		createReachPanel();
		createButtonsPanel();

		double size[][] = new double[][] {
				{TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.FILL, buttonsPanel.getPreferredSize().height}
		};

		layout = new TableLayout(size);
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

	public MpsatSettings getSettings() {
		return getSettingsFromControls();
	}

	private void createButtonsPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		runButton = new JButton ("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modalResult = 1;
				setVisible(false);
			}
		});

		cancelButton = new JButton ("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				modalResult = 0;
				setVisible(false);
			}
		});

		buttonsPanel.add(runButton);
		buttonsPanel.add(cancelButton);
	}

	private void createPreset() {
		String desc = JOptionPane.showInputDialog(this, "Please enter the description of the new preset:");

		if (! (desc == null || desc.isEmpty())) {
			MpsatSettings settings = getSettingsFromControls();
			MpsatPreset preset = presetManager.save(settings, desc);
			presetCombo.addItem(preset);
			presetCombo.setSelectedItem(preset);
		}
	}

	private MpsatSettings getSettingsFromControls() {
		SolutionMode m;

		if (firstSolutionButton.isSelected())
			m = SolutionMode.FIRST;
		else if (cheapestSolutionButton.isSelected())
			m = SolutionMode.MINIMUM_COST;
		else
			m = SolutionMode.ALL;

		int n;

		try {
			n = Integer.parseInt(solutionLimitText.getText());
		} catch (NumberFormatException e) {
			n = 0;
		}

		if (n<0)
			n=0;

		MpsatSettings settings = new MpsatSettings((MpsatMode) operationCombo
				.getSelectedItem(), verbosityCombo.getSelectedIndex(), satCombo
				.getSelectedIndex(), m, n, reachText.getText());
		return settings;
	}

	public int getModalResult() {
		return modalResult;
	}

}
