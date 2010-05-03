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
import javax.swing.JCheckBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.desij.DesiJOperation;
import org.workcraft.plugins.verification.MpsatMode;
import org.workcraft.plugins.verification.MpsatPreset;
import org.workcraft.plugins.verification.MpsatPresetManager;
import org.workcraft.plugins.verification.MpsatSettings;
import org.workcraft.plugins.verification.MpsatSettings.SolutionMode;
import org.workcraft.plugins.verification.gui.MpsatPresetManagerDialog;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class DesiJConfigurationDialog extends JDialog {

	private JPanel content, presetPanel, buttonsPanel;
	private JScrollPane partitionPanel;
	private JLabel aggregationLabel;
	private JScrollPane optionsPanel;
	private JComboBox presetCombo, operationCombo, satCombo, verbosityCombo;
	private JButton manageButton, saveAsNewButton, runButton, updatePresetButton, cancelButton;
	private JTextField aggregationFactorText;
	private JTextArea partitionText;
	private JRadioButton singleLazyDeco, multiLazyDeco, basicDeco, treeDeco;
	private JRadioButton finestPartition, bestPartition, customPartition;
	private JCheckBox riskyDeco, outdetDeco, safenessContraction;
	private JCheckBox loopDuplicatePlaces, shortcutPlaces, implicitPlaces;
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
		for (DesiJOperation op : DesiJOperation.operations)
			operationCombo.addItem(op);

		operationCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DesiJOperation selectedOp = (DesiJOperation)operationCombo.getSelectedItem();

				if (selectedOp == DesiJOperation.DECOMPOSITION) {
					enableDecoStrategyControls();
					enablePartitionControls();
				}
				else {
					disableDecoStrategyControls();
					disablePartitionControls();
				}

				if (selectedOp.usesContraction()) {
					enableContractionModeControls();
				}
				else {
					disableContractionModeControls();
				}
			}
		});

		optionsPanelContent.add(new JLabel("Operation:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());
		optionsPanelContent.add(operationCombo);
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(8));

		createDecoStrategyControls(optionsPanelContent);
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(8));

		createPartitionControls(optionsPanelContent);
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(8));

		createImplicitPlaceControls(optionsPanelContent);
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(8));

		createContractionModeControls(optionsPanelContent);
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

	private void createImplicitPlaceControls(JPanel optionsPanelContent) {
		optionsPanelContent.add(new JLabel("Deletion of implicit Places:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(-2));

		loopDuplicatePlaces = new JCheckBox("loop-only/duplicate");
		loopDuplicatePlaces.setSelected(true);
		loopDuplicatePlaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (!loopDuplicatePlaces.isSelected()) {
					shortcutPlaces.setSelected(false);
					implicitPlaces.setSelected(false);
				}
			}
		});

		shortcutPlaces = new JCheckBox ("shortcut places");
		shortcutPlaces.setSelected(true);
		shortcutPlaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (shortcutPlaces.isSelected())
					loopDuplicatePlaces.setSelected(true);
				else
					implicitPlaces.setSelected(false);
			}
		});

		implicitPlaces = new JCheckBox("implicit places");
		implicitPlaces.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (implicitPlaces.isSelected()) {
					loopDuplicatePlaces.setSelected(true);
					shortcutPlaces.setSelected(true);
				}
			}
		});

		optionsPanelContent.add(loopDuplicatePlaces);
		optionsPanelContent.add(shortcutPlaces);
		optionsPanelContent.add(implicitPlaces);

		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());
	}

	private void createPartitionControls(JPanel optionsPanelContent) {
		optionsPanelContent.add(new JLabel("Output partition:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(-2));

		createPartitionButtons();

		optionsPanelContent.add(finestPartition);
		optionsPanelContent.add(bestPartition);
		optionsPanelContent.add(customPartition);

		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());

		//finestPartition.doClick(); // default strategy
	}

	private void createPartitionButtons() {
		finestPartition = new JRadioButton("One output per component (default)");
		finestPartition.setSelected(true);
		finestPartition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				partitionPanel.setVisible(false);
				layout.setRow(2, 0);
			}
		});

		bestPartition = new JRadioButton ("Choose best partition");
		bestPartition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				partitionPanel.setVisible(false);
				layout.setRow(2, 0);
			}
		});

		customPartition = new JRadioButton("Use custom partition");
		customPartition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				partitionPanel.setVisible(true);
				layout.setRow(2, TableLayout.FILL);
			}
		});

		// only one can be selected
		ButtonGroup bg = new ButtonGroup();
		bg.add(finestPartition); bg.add(bestPartition); bg.add(customPartition);
	}

	private void enablePartitionControls() {
		finestPartition.setEnabled(true);
		bestPartition.setEnabled(true);
		customPartition.setEnabled(true);

		if (customPartition.isSelected()) {
			partitionPanel.setVisible(true);
			layout.setRow(2, TableLayout.FILL);
		}
	}

	private void disablePartitionControls() {
		if (customPartition.isSelected()) {
			partitionPanel.setVisible(false);
			layout.setRow(2, 0);
		}

		finestPartition.setEnabled(false);
		bestPartition.setEnabled(false);
		customPartition.setEnabled(false);
	}

	private void createDecoStrategyControls(JPanel optionsPanelContent) {

		optionsPanelContent.add(new JLabel("Decomposition strategy:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(-2));

		createDecoStrategyButtons();

		optionsPanelContent.add(basicDeco);
		optionsPanelContent.add(treeDeco);
		optionsPanelContent.add(singleLazyDeco);
		optionsPanelContent.add(multiLazyDeco);

		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());

		aggregationFactorText = new JTextField();
		aggregationFactorText.setText("WW"); // only for adjustment
		Dimension preferredSize = aggregationFactorText.getPreferredSize();
		aggregationFactorText.setText("");
		aggregationFactorText.setPreferredSize(preferredSize);

		JPanel aggregationPanel = new JPanel (new FlowLayout(FlowLayout.LEFT, 3, 0));
		aggregationLabel = new JLabel("Signal aggregation count:");
		aggregationPanel.add(aggregationLabel);
		aggregationPanel.add(aggregationFactorText);

		// treeDeco.doClick(); // default strategy

		optionsPanelContent.add(aggregationPanel);
	}

	private void enableDecoStrategyControls() {
		basicDeco.setEnabled(true);
		treeDeco.setEnabled(true);
		singleLazyDeco.setEnabled(true);
		multiLazyDeco.setEnabled(true);

		if (treeDeco.isSelected())
			enableAggregationControls();

	}

	private void disableDecoStrategyControls() {
		basicDeco.setEnabled(false);
		treeDeco.setEnabled(false);
		singleLazyDeco.setEnabled(false);
		multiLazyDeco.setEnabled(false);

		disableAggregationControls();
	}

	private void createDecoStrategyButtons() {

		treeDeco = new JRadioButton("Tree (default)");
		treeDeco.setSelected(true);
		treeDeco.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableAggregationControls();
			}
		});

		basicDeco = new JRadioButton ("Basic");
		basicDeco.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableAggregationControls();
			}
		});

		singleLazyDeco = new JRadioButton("Lazy single");
		singleLazyDeco.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableAggregationControls();
			}
		});

		multiLazyDeco = new JRadioButton("Lazy multi");
		multiLazyDeco.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableAggregationControls();
			}
		});

		ButtonGroup bg = new ButtonGroup();
		bg.add(treeDeco); bg.add(basicDeco); bg.add(singleLazyDeco); bg.add(multiLazyDeco);
	}

	private void disableAggregationControls() {
		aggregationLabel.setEnabled(false);
		aggregationFactorText.setEnabled(false);
	}

	private void enableAggregationControls() {
		aggregationLabel.setEnabled(true);
		aggregationFactorText.setEnabled(true);
	}

	private void createContractionModeControls(JPanel optionsPanelContent) {
		optionsPanelContent.add(new JLabel("Contraction mode:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(-2));

		riskyDeco = new JCheckBox("Risky");
		outdetDeco = new JCheckBox ("Ouput determinacy");
		safenessContraction = new JCheckBox("Safeness preserving");

		optionsPanelContent.add(safenessContraction);
		optionsPanelContent.add(outdetDeco);
		optionsPanelContent.add(riskyDeco);

		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());

	}

	private void enableContractionModeControls() {
		riskyDeco.setEnabled(true);
		outdetDeco.setEnabled(true);
		safenessContraction.setEnabled(true);
	}

	private void disableContractionModeControls() {
		riskyDeco.setEnabled(false);
		outdetDeco.setEnabled(false);
		safenessContraction.setEnabled(false);
	}

	private void createPartitionPanel() {
		JPanel partitionPanelContent = new JPanel(new BorderLayout());

		partitionPanel = new JScrollPane(partitionPanelContent);
		partitionPanel.setBorder(BorderFactory.createTitledBorder("Partition specification"));
		partitionPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		partitionPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		partitionText = new JTextArea();
		partitionText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		partitionText.setText("test");
		partitionPanelContent.add(partitionText);
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
			singleLazyDeco.setSelected(true);
			enableAggregationControls();
			break;
		case MINIMUM_COST:
			treeDeco.setSelected(true);
			disableAggregationControls();
			break;
		case FIRST:
			basicDeco.setSelected(true);
			disableAggregationControls();
			break;
		}
		int n = settings.getSolutionNumberLimit();

		if (n>0)
			aggregationFactorText.setText(Integer.toString(n));
		else
			aggregationFactorText.setText("");

		partitionText.setText(settings.getReach());
	}

	public DesiJConfigurationDialog(Window owner, MpsatPresetManager presetManager) {
		super(owner, "DesiJ configuration", ModalityType.APPLICATION_MODAL);
		this.presetManager = presetManager;

		createPresetPanel();
		createOptionsPanel();
		createPartitionPanel();
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
		content.add(partitionPanel, "0 2");
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

		if (basicDeco.isSelected())
			m = SolutionMode.FIRST;
		else if (treeDeco.isSelected())
			m = SolutionMode.MINIMUM_COST;
		else
			m = SolutionMode.ALL;

		int n;

		try {
			n = Integer.parseInt(aggregationFactorText.getText());
		} catch (NumberFormatException e) {
			n = 0;
		}

		if (n<0)
			n=0;

		MpsatSettings settings = new MpsatSettings((MpsatMode) operationCombo
				.getSelectedItem(), verbosityCombo.getSelectedIndex(), satCombo
				.getSelectedIndex(), m, n, partitionText.getText());
		return settings;
	}

	public int getModalResult() {
		return modalResult;
	}

}
