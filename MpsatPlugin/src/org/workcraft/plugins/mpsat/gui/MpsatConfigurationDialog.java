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
import org.workcraft.util.IntDocument;

@SuppressWarnings("serial")
public class MpsatConfigurationDialog extends JDialog {
	private JPanel contentPanel, optionsPanel, predicatePanel, buttonsPanel;
	private PresetManagerPanel<MpsatSettings> presetPanel;
	private JComboBox<MpsatMode> modeCombo;
	private JButton runButton, cancelButton, helpButton;
	private JTextField solutionLimitText;
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

	public MpsatConfigurationDialog(Window owner, PresetManager<MpsatSettings> presetManager) {
		super(owner, "Custom property definition", ModalityType.APPLICATION_MODAL);
		this.presetManager = presetManager;

		createPresetPanel();
		createOptionsPanel();
		createReachPanel();
		createButtonsPanel();

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
		Dimension modeComboDimention = modeCombo.getPreferredSize();
		modeComboDimention.width = 318;
		modeCombo.setPreferredSize(modeComboDimention);
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
					Dimension dimension = new Dimension(465, 550);
					Dimension minDimension = new Dimension(dimension);
					minDimension.height = 350;
					MpsatConfigurationDialog.this.setMinimumSize(minDimension);
					MpsatConfigurationDialog.this.setSize(dimension);
				} else {
					predicatePanel.setVisible(false);
					layout.setRow(2, 0);
					Dimension dimension = new Dimension(465, 240);
					MpsatConfigurationDialog.this.setMinimumSize(dimension);
					MpsatConfigurationDialog.this.setSize(dimension);
				}
			}
		});
		optionsPanel.add(GUI.createLabeledComponent(modeCombo, "Mode:      "));
		optionsPanel.add(new SimpleFlowLayout.LineBreak());

		JPanel solutionModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		solutionModePanel.add(new JLabel("Solution:"));
		cheapestSolutionRadioButton = new JRadioButton("minimise cost function");
		cheapestSolutionRadioButton.setSelected(true);
		cheapestSolutionRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solutionLimitText.setEnabled(false);
			}
		});
		firstSolutionRadioButton = new JRadioButton ("any");
		firstSolutionRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solutionLimitText.setEnabled(false);
			}
		});
		allSolutionsRadioButton = new JRadioButton("all");
		allSolutionsRadioButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				solutionLimitText.setEnabled(true);
			}
		});
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(cheapestSolutionRadioButton);
		buttonGroup.add(firstSolutionRadioButton);
		buttonGroup.add(allSolutionsRadioButton);
		solutionModePanel.add(cheapestSolutionRadioButton);
		solutionModePanel.add(firstSolutionRadioButton);
		solutionModePanel.add(allSolutionsRadioButton);

		solutionLimitText = new JTextField();
		Dimension dimension = solutionLimitText.getPreferredSize();
		dimension.width = 38;
		solutionLimitText.setPreferredSize(dimension);
		solutionLimitText.setToolTipText("Maximum number of solutions. Leave blank for no limit.");
		solutionLimitText.setDocument(new IntDocument(3));
		solutionLimitText.setEnabled(false);
		solutionModePanel.add(solutionLimitText);
		optionsPanel.add(solutionModePanel);
		optionsPanel.add(new SimpleFlowLayout.LineBreak());
	}

	private void createReachPanel() {
		predicatePanel = new JPanel(new BorderLayout());
		String title = "Reach predicate (use '" + NamespaceHelper.flatNameSeparator + "' as hierarchy separator)";
		predicatePanel.setBorder(BorderFactory.createTitledBorder(title));

		reachText = new JTextArea();
		reachText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		reachText.setText("");
		JScrollPane reachScrollPane = new JScrollPane(reachText);

		satisfiebleRadioButton = new JRadioButton("satisfiable");
		unsatisfiebleRadioButton = new JRadioButton("unsatisfiable");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(satisfiebleRadioButton);
		buttonGroup.add(unsatisfiebleRadioButton);
		unsatisfiebleRadioButton.setSelected(true);

		JPanel propertyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		propertyPanel.add(new JLabel("Property holds if predicate is:"));
		propertyPanel.add(satisfiebleRadioButton);
		propertyPanel.add(unsatisfiebleRadioButton);

		predicatePanel.add(reachScrollPane, BorderLayout.CENTER);
		predicatePanel.add(propertyPanel, BorderLayout.SOUTH);
	}

	private void applySettingsToControls(MpsatSettings settings) {
		modeCombo.setSelectedItem(settings.getMode());

		switch (settings.getSolutionMode()) {
		case MINIMUM_COST:
			cheapestSolutionRadioButton.setSelected(true);
			solutionLimitText.setEnabled(false);
			break;
		case FIRST:
			firstSolutionRadioButton.setSelected(true);
			solutionLimitText.setEnabled(false);
			break;
		case ALL:
			allSolutionsRadioButton.setSelected(true);
			solutionLimitText.setEnabled(true);
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
		unsatisfiebleRadioButton.setSelected(settings.getInversePredicate());
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

		MpsatSettings settings = new MpsatSettings(null, (MpsatMode)modeCombo.getSelectedItem(),
				0, solutionMode, solutionLimin, reachText.getText(), unsatisfiebleRadioButton.isSelected());

		return settings;
	}

	public int getModalResult() {
		return modalResult;
	}
}
