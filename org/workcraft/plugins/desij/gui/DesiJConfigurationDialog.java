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

import javax.swing.Action;
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
import org.workcraft.plugins.desij.DesiJPreset;
import org.workcraft.plugins.desij.DesiJPresetManager;
import org.workcraft.plugins.desij.DesiJSettings;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class DesiJConfigurationDialog extends JDialog {

	// elements of this form
	private JPanel content, presetPanel, buttonsPanel;
	private JScrollPane partitionPanel, optionsPanel;
	private JLabel aggregationLabel, synthesiserLabel;
	private JComboBox presetCombo, operationCombo, synthesiserCombo;
	private JButton manageButton, saveAsNewButton, runButton, updatePresetButton, cancelButton;
	private JTextField aggregationFactorText;
	private JTextArea partitionText;
	private JRadioButton singleLazyDeco, multiLazyDeco, basicDeco, treeDeco;
	private JRadioButton finestPartition, bestPartition, customPartition;
	private JCheckBox riskyDeco, outdetDeco, safenessContraction;
	private JCheckBox loopDuplicatePlaces, shortcutPlaces, implicitPlaces;
	private JCheckBox inclSynthesis, cscAware, intCom;
	private DesiJPresetManager presetManager;

	private TableLayout layout;
	private int modalResult = 0;

	// dedicated for synthesiser Combo box
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

	// ************** Preset panel stuff *****************

	private void createPresetPanel() {
		presetPanel = new JPanel(new SimpleFlowLayout(15, 3));

		presetCombo = new JComboBox();
		for (DesiJPreset p : presetManager.list())
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
				for (DesiJPreset p : presetManager.list())
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
				DesiJPreset selected = (DesiJPreset)presetCombo.getSelectedItem();
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
		DesiJPreset selected = (DesiJPreset)presetCombo.getSelectedItem();

		DesiJPresetManagerDialog dlg = new DesiJPresetManagerDialog(this, presetManager);
		dlg.setModalityType(ModalityType.APPLICATION_MODAL);
		GUI.centerFrameToParent(dlg, this);
		dlg.setVisible(true);

		presetCombo.removeAllItems();
		List<DesiJPreset> presets = presetManager.list();

		boolean haveOldSelection = false;

		for (DesiJPreset p : presets) {
			presetCombo.addItem(p);
			if (p == selected)
				haveOldSelection = true;
		}

		if (haveOldSelection)
			presetCombo.setSelectedItem(selected);
		else
			presetCombo.setSelectedIndex(0);

	}

	private void createPreset() {
		String desc = JOptionPane.showInputDialog(this, "Please enter the description of the new preset:");

		if (! (desc == null || desc.isEmpty())) {
			DesiJSettings settings = getSettingsFromControls();
			DesiJPreset preset = presetManager.save(settings, desc);
			presetCombo.addItem(preset);
			presetCombo.setSelectedItem(preset);
		}
	}

	// apply preset settings to controls
	private void applySettingsToControls() {
		DesiJPreset p = (DesiJPreset)presetCombo.getSelectedItem();

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

		// apply settings to controls
		DesiJSettings settings = p.getSettings();

		// Operation:
		operationCombo.setSelectedItem(settings.getOperation());

		// Decomposition strategy:
		if (settings.getDecoStrategy() != null) {
			switch (settings.getDecoStrategy()) {
			case LAZYSINGLE:
				singleLazyDeco.doClick();
				break;
			case TREE:
				treeDeco.doClick();
				break;
			case BASIC:
				basicDeco.doClick();
				break;
			case LAZYMULTI:
				multiLazyDeco.doClick();
				break;
			}
		}

		int n = settings.getAggregationFactor();
		if (n>0)
			aggregationFactorText.setText(Integer.toString(n));
		else
			aggregationFactorText.setText("");

		// Output partition:
		if (settings.getPartitionMode() != null) {
			switch (settings.getPartitionMode()) {
			case FINEST:
				finestPartition.doClick();
				break;
			case BEST:
				bestPartition.doClick();
				break;
			case CUSTOM:
				customPartition.doClick();
				break;
			}
		}

		partitionText.setText(settings.getPartition());

		// Deletion of implicit Places:
		loopDuplicatePlaces.setSelected(settings.getLoopDuplicatePlaceHandling());
		shortcutPlaces.setSelected(settings.getShortcutPlaceHandling());
		implicitPlaces.setSelected(settings.getImplicitPlaceHandling());

		// Transition contraction mode:
		safenessContraction.setSelected(settings.getSafenessPreservingContractionOption());
		outdetDeco.setSelected(settings.getOutputDeterminacyOption());
		riskyDeco.setSelected(settings.getRiskyOption());

		// Synthesis options:
		synthesiserCombo.setSelectedIndex(settings.getSynthesiser());
		// because the Enabling of inclSynthesis has influence on other controls
		if (inclSynthesis.isEnabled() &&
				(inclSynthesis.isSelected() != settings.getPostSynthesisOption()) )
			inclSynthesis.doClick();
		if (cscAware.isEnabled() &&
				(cscAware.isSelected() != settings.getCSCAwareOption()) )
			cscAware.doClick();
		if (intCom.isEnabled() &&
				(intCom.isSelected() != settings.getInternalCommunicationOption()) )
			intCom.doClick();
	}

	// ************** Main Panel stuff **********************

	// creates the the main panel or options panel resp.
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
					enableSynthesisControls();
				}
				else {
					disableDecoStrategyControls();
					disablePartitionControls();
					disableSynthesisControls();
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

		createSynthesisControls(optionsPanelContent);

	}

	// --------------- Helper routines for creating the main panel --------------

	private void createSynthesisControls(JPanel optionsPanelContent) {

		optionsPanelContent.add(new JLabel("Synthesis options:"));
		optionsPanelContent.add(new SimpleFlowLayout.LineBreak(-2));

		synthesiserCombo = new JComboBox();
		synthesiserCombo.addItem(new IntMode(0, "Petrify"));
		synthesiserCombo.addItem(new IntMode(1, "MpSat"));

		JPanel synthesiserPanel = new JPanel (new FlowLayout(FlowLayout.LEFT, 3, 0));
		synthesiserLabel = new JLabel("Logic Synthesiser:");
		synthesiserPanel.add(synthesiserLabel);
		synthesiserPanel.add(synthesiserCombo);

		disableSynthesiserCombo(); // initially

		inclSynthesis = new JCheckBox("Component Synthesis");
		inclSynthesis.setSelected(false);
		inclSynthesis.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (inclSynthesis.isSelected())
					enableSynthesiserCombo();
				else
					disableSynthesiserCombo();

			}
		});

		optionsPanelContent.add(synthesiserPanel);
		//optionsPanelContent.add(GUI.createLabeledComponent(synthesiserCombo, "Logic Synthesiser:"));
		optionsPanelContent.add(inclSynthesis);

		optionsPanelContent.add(new SimpleFlowLayout.LineBreak());

		cscAware = new JCheckBox("CSC-Aware (Tree-Deco)");
		cscAware.setSelected(false);
		cscAware.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cscAware.isSelected()) {
					disableDecoStrategyRadioButtons();
					enableAggregationControls();
				}
				else {
					enableDecoStrategyControls();
				}
			}
		});
		intCom = new JCheckBox ("Internal Communication (Self-Triggers)");

		optionsPanelContent.add(cscAware);
		optionsPanelContent.add(intCom);

	}

	private void enableSynthesiserCombo() {
		synthesiserLabel.setEnabled(true);
		synthesiserCombo.setEnabled(true);
	}

	private void disableSynthesiserCombo() {
		synthesiserLabel.setEnabled(false);
		synthesiserCombo.setEnabled(false);
	}

	private void enableSynthesisControls() {
		inclSynthesis.setEnabled(true);
		if (inclSynthesis.isSelected())
			enableSynthesiserCombo();
		cscAware.setEnabled(true);
		intCom.setEnabled(true);
	}

	private void disableSynthesisControls() {
		inclSynthesis.setEnabled(false);
		disableSynthesiserCombo();
		cscAware.setEnabled(false);
		intCom.setEnabled(false);
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
				// update GUI
				content.doLayout();
			}
		});

		bestPartition = new JRadioButton ("Choose best partition");
		bestPartition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				partitionPanel.setVisible(false);
				layout.setRow(2, 0);
				// update GUI
				content.doLayout();
			}
		});

		customPartition = new JRadioButton("Use custom partition");
		customPartition.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				partitionPanel.setVisible(true);
				layout.setRow(2, TableLayout.FILL);
				// update GUI
				content.doLayout();
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
			content.doLayout(); // update the GUI
		}
	}

	private void disablePartitionControls() {
		if (customPartition.isSelected()) {
			partitionPanel.setVisible(false);
			layout.setRow(2, 0);
			content.doLayout(); // update the GUI
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
		aggregationLabel = new JLabel("Aggregation - Signal count:");
		aggregationPanel.add(aggregationLabel);
		aggregationPanel.add(aggregationFactorText);

		// treeDeco.doClick(); // default strategy

		optionsPanelContent.add(aggregationPanel);
	}


	private void enableDecoStrategyRadioButtons() {
		basicDeco.setEnabled(true);
		treeDeco.setEnabled(true);
		singleLazyDeco.setEnabled(true);
		multiLazyDeco.setEnabled(true);
	}

	private void disableDecoStrategyRadioButtons() {
		basicDeco.setEnabled(false);
		treeDeco.setEnabled(false);
		singleLazyDeco.setEnabled(false);
		multiLazyDeco.setEnabled(false);
	}

	private void enableDecoStrategyControls() {
		if (!cscAware.isSelected())
			enableDecoStrategyRadioButtons();
		if (treeDeco.isSelected() || cscAware.isSelected())
			enableAggregationControls();
		else
			disableAggregationControls();
	}

	private void disableDecoStrategyControls() {
		disableDecoStrategyRadioButtons();
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

	// ----------------- End of Helper routines for creating options panel -----------------

	// ***************** Buttons Panel stuff *****************************

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



	/**
	 * Constructor of the Configuration Dialog
	 *
	 * @param owner
	 * @param presetManager - ... of DesiJ settings
	 */
	public DesiJConfigurationDialog(Window owner, DesiJPresetManager presetManager) {
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


	// **************** for external use **********************

	public DesiJSettings getSettings() {
		return getSettingsFromControls();
	}

	private DesiJSettings getSettingsFromControls() {
//		SolutionMode m;
//
//		if (basicDeco.isSelected())
//			m = SolutionMode.FIRST;
//		else if (treeDeco.isSelected())
//			m = SolutionMode.MINIMUM_COST;
//		else
//			m = SolutionMode.ALL;
//
//		int n;
//
//		try {
//			n = Integer.parseInt(aggregationFactorText.getText());
//		} catch (NumberFormatException e) {
//			n = 0;
//		}
//
//		if (n<0)
//			n=0;
//
//		MpsatSettings settings = new MpsatSettings((MpsatMode) operationCombo
//				.getSelectedItem(), verbosityCombo.getSelectedIndex(), synthesiserCombo
//				.getSelectedIndex(), m, n, partitionText.getText());
//		return settings;
		return null;
	}

	public int getModalResult() {
		return modalResult;
	}

}
