package org.workcraft.plugins.mpsat.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.mpsat.MpsatBuiltinPresets;
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatSettings;
import org.workcraft.plugins.mpsat.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.gui.PresetManagerPanel;
import org.workcraft.plugins.shared.presets.Preset;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.plugins.shared.presets.SettingsToControlsMapper;
import org.workcraft.util.GUI;

@SuppressWarnings("serial")
public class MpsatConfigurationDialog extends JDialog {
	private JPanel contentPanel, optionsPanel, predicatePanel, buttonsPanel;
	private PresetManagerPanel<MpsatSettings> presetPanel;
	private JComboBox<MpsatMode> modeCombo;
	private JComboBox<IntMode> verbosityCombo;
	private JButton runButton, cancelButton, helpButton;
	private JLabel numberOfSolutionsLabel;
	private JTextField solutionLimitText, propertyNameText;
	private JTextArea reachText;
	private JRadioButton allSolutionsRadioButton, firstSolutionRadioButton, cheapestSolutionRadioButton;
	private JRadioButton satisfiebleRadioButton, unsatisfiebleRadioButton;
	private PresetManager<MpsatSettings> presetManager;

	private TableLayout layout;
	private int modalResult = 0;

	class IntMode {
		private final int value;
		private final String description;

		public IntMode(int value, String description) {
			this.value = value;
			this.description = description;
		}

		@Override
		public String toString() {
			return description;
		}
	}

	private void createPresetPanel() {
		ArrayList<Preset<MpsatSettings>> builtInPresets = new ArrayList<Preset<MpsatSettings>>();

		builtInPresets.add(MpsatBuiltinPresets.DEADLOCK_CHECKER);
		builtInPresets.add(MpsatBuiltinPresets.DEADLOCK_CHECKER_ALL_TRACES);
		builtInPresets.add(MpsatBuiltinPresets.CONSISTENCY_CHECKER);
		builtInPresets.add(MpsatBuiltinPresets.PERSISTENCY_CHECKER);

		SettingsToControlsMapper<MpsatSettings> guiMapper = new SettingsToControlsMapper<MpsatSettings>() {
			@Override
			public void applySettingsToControls(MpsatSettings settings) {
				MpsatConfigurationDialog.this.applySettingsToControls(settings);
			}

			@Override
			public MpsatSettings getSettingsFromControls() {
				MpsatSettings settings = MpsatConfigurationDialog.this.getSettingsFromControls();
				return settings;
			}
		};

		presetPanel = new PresetManagerPanel<MpsatSettings>(presetManager, builtInPresets, guiMapper, this);
	}

	private void createOptionsPanel() {
		optionsPanel = new JPanel(new SimpleFlowLayout());
		optionsPanel.setBorder(BorderFactory.createTitledBorder("MPSat settings"));

		modeCombo = new JComboBox<MpsatMode>();
		modeCombo.setEditable(false);
		modeCombo.addItem(MpsatMode.DEADLOCK);
		modeCombo.addItem(MpsatMode.REACHABILITY);
		modeCombo.addItem(MpsatMode.STG_REACHABILITY);

		modeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				MpsatMode selectedMode = (MpsatMode)modeCombo.getSelectedItem();
				if (selectedMode.hasReach()) {
					predicatePanel.setVisible(true);
					layout.setRow(2, TableLayout.FILL);
					Dimension dimension = new Dimension(530, 550);
					MpsatConfigurationDialog.this.setMinimumSize(dimension);
					MpsatConfigurationDialog.this.setSize(dimension);
				} else {
					predicatePanel.setVisible(false);
					layout.setRow(2, 0);
					Dimension dimension = new Dimension(530, 295);
					MpsatConfigurationDialog.this.setMinimumSize(dimension);
					MpsatConfigurationDialog.this.setSize(dimension);
				}
			}
		});

		verbosityCombo = new JComboBox<IntMode>();
		for (int i=0; i<=9; i++) {
			verbosityCombo.addItem(new IntMode(i, "level " + i));
		}

		optionsPanel.add(GUI.createLabeledComponent(modeCombo, "Mode:"));
		optionsPanel.add(GUI.createLabeledComponent(verbosityCombo, "Verbosity:"));
		optionsPanel.add(new SimpleFlowLayout.LineBreak(8));
		optionsPanel.add(new JLabel("Solution:"));
		optionsPanel.add(new SimpleFlowLayout.LineBreak(-2));
		createSolutionModeButtons();
		optionsPanel.add(firstSolutionRadioButton);
		optionsPanel.add(cheapestSolutionRadioButton);
		optionsPanel.add(allSolutionsRadioButton);
		optionsPanel.add(new SimpleFlowLayout.LineBreak());

		solutionLimitText = new JTextField();
		solutionLimitText.setText("WWWWWW");
		Dimension soludioLimitDimention = solutionLimitText.getPreferredSize();
		solutionLimitText.setText("");
		solutionLimitText.setPreferredSize(soludioLimitDimention);

		JPanel numberOfSolutionsPanel = new JPanel (new FlowLayout(FlowLayout.LEFT, 3, 0));
		numberOfSolutionsLabel = new JLabel("Maximum number of solutions (leave blank for no limit):");
		numberOfSolutionsPanel.add(numberOfSolutionsLabel);
		numberOfSolutionsPanel.add(solutionLimitText);
		disableNumberOfSolutionControls();
		optionsPanel.add(numberOfSolutionsPanel);
		optionsPanel.add(new SimpleFlowLayout.LineBreak(5));
	}

	private void createSolutionModeButtons() {
		firstSolutionRadioButton = new JRadioButton ("Find any solution");
		firstSolutionRadioButton.setSelected(true);
		firstSolutionRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableNumberOfSolutionControls();
			}
		});
		allSolutionsRadioButton = new JRadioButton("Find all solutions");
		allSolutionsRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableNumberOfSolutionControls();
			}
		});

		cheapestSolutionRadioButton = new JRadioButton("Minimise cost function");
		cheapestSolutionRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				disableNumberOfSolutionControls();
			}
		});

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(firstSolutionRadioButton);
		buttonGroup.add(allSolutionsRadioButton);
		buttonGroup.add(cheapestSolutionRadioButton);
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
		predicatePanel = new JPanel(new BorderLayout());
		predicatePanel.setBorder(BorderFactory.createTitledBorder("Reach predicate (use '" + NamespaceHelper.flatNameSeparator + "' as hierarchy separator)"));

		reachText = new JTextArea();
		reachText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		reachText.setText("");
		JScrollPane reachScrollPane = new JScrollPane(reachText);

		propertyNameText = new JTextField();
		propertyNameText.setText("WWWWWWWWWWWWWWWWWWWWWWWWWWWWWWWW");
		Dimension propertNameDimention = propertyNameText.getPreferredSize();
		propertyNameText.setText("");
		propertyNameText.setPreferredSize(propertNameDimention);

		satisfiebleRadioButton = new JRadioButton("satisfieble");
		unsatisfiebleRadioButton = new JRadioButton("unsatisfieble");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(satisfiebleRadioButton);
		buttonGroup.add(unsatisfiebleRadioButton);
		unsatisfiebleRadioButton.setSelected(true);

		JPanel propertyPanel = new JPanel(new SimpleFlowLayout());
		propertyPanel.add(new SimpleFlowLayout.LineBreak(5));
		propertyPanel.add(GUI.createLabeledComponent(propertyNameText, "Property name:"));
		propertyPanel.add(new SimpleFlowLayout.LineBreak(5));
		propertyPanel.add(new JLabel("Property holds when predicate is:"));
		propertyPanel.add(satisfiebleRadioButton);
		propertyPanel.add(unsatisfiebleRadioButton);

		predicatePanel.add(reachScrollPane, BorderLayout.CENTER);
		predicatePanel.add(propertyPanel, BorderLayout.SOUTH);
	}

	private void applySettingsToControls(MpsatSettings settings) {
		modeCombo.setSelectedItem(settings.getMode());
		verbosityCombo.setSelectedIndex(settings.getVerbosity());

		switch (settings.getSolutionMode()) {
		case ALL:
			allSolutionsRadioButton.setSelected(true);
			enableNumberOfSolutionControls();
			break;
		case MINIMUM_COST:
			cheapestSolutionRadioButton.setSelected(true);
			disableNumberOfSolutionControls();
			break;
		case FIRST:
			firstSolutionRadioButton.setSelected(true);
			disableNumberOfSolutionControls();
			break;
		}

		int n = settings.getSolutionNumberLimit();
		if (n>0) {
			solutionLimitText.setText(Integer.toString(n));
		} else {
			solutionLimitText.setText("");
		}

		reachText.setText(settings.getReach());
		String propertyName = settings.getName();
		if (propertyName == null) {
			propertyName = "";
		}
		propertyNameText.setText(propertyName);
		unsatisfiebleRadioButton.setSelected(settings.getInversePredicate());
	}

	public MpsatConfigurationDialog(Window owner, PresetManager<MpsatSettings> presetManager) {
		super(owner, "Custom property definition", ModalityType.APPLICATION_MODAL);
		this.presetManager = presetManager;

		createOptionsPanel();
		createReachPanel();
		createButtonsPanel();
		createPresetPanel();

		double size[][] = new double[][] {
				{TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, buttonsPanel.getPreferredSize().height}
		};

		layout = new TableLayout(size);
		layout.setHGap(3);
		layout.setVGap(3);

		contentPanel = new JPanel(layout);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		contentPanel.add(presetPanel, "0 0");
		contentPanel.add(optionsPanel, "0 1");
		contentPanel.add(predicatePanel, "0 2");
		contentPanel.add(buttonsPanel, "0 3");

		setContentPane(contentPanel);

		presetPanel.selectFirst();

	    getRootPane().registerKeyboardAction(new ActionListener() {
	    	@Override
	    	public void actionPerformed(ActionEvent e) {
	    		modalResult = 0;
	    		setVisible(false);
	    	}
	    },
	    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
	    JComponent.WHEN_IN_FOCUSED_WINDOW);
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

		helpButton = new JButton ("Help");
		helpButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DesktopApi.open(new File("help/reach.html"));
			}
		});

		buttonsPanel.add(runButton);
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(helpButton);
	}

	private MpsatSettings getSettingsFromControls() {
		SolutionMode solutionMode;
		if (firstSolutionRadioButton.isSelected()) {
			solutionMode = SolutionMode.FIRST;
		} else if (cheapestSolutionRadioButton.isSelected()) {
			solutionMode = SolutionMode.MINIMUM_COST;
		} else {
			solutionMode = SolutionMode.ALL;
		}

		int solutionLimin;
		try {
			solutionLimin = Integer.parseInt(solutionLimitText.getText());
		} catch (NumberFormatException e) {
			solutionLimin = 0;
		}
		if (solutionLimin < 0) {
			solutionLimin = 0;
		}

		MpsatSettings settings = new MpsatSettings(propertyNameText.getText(),
				(MpsatMode)modeCombo.getSelectedItem(),	verbosityCombo.getSelectedIndex(),
				solutionMode, solutionLimin, reachText.getText(),
				unsatisfiebleRadioButton.isSelected());

		return settings;
	}

	public int getModalResult() {
		return modalResult;
	}
}
