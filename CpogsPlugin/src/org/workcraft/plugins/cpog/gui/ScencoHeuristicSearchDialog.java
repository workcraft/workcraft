package org.workcraft.plugins.cpog.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.table.DefaultTableCellRenderer;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.tasks.ScencoTask;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.util.IntDocument;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class ScencoHeuristicSearchDialog extends JDialog {

	private JLabel numberOfSolutionsLabel, verboseModeLabel, exampleLabel,
			exampleLabel2, customEncLabel, bitsLabel, guidLabel, optimiseLabel,
			abcLabel, circuitSizeLabel;
	private JCheckBox verboseModeCheck, customEncodings, abcCheck;
	private JComboBox<String> OptimiseBox, guidedModeBox;
	private JPanel generationPanel, buttonsPanel, content, customPanel,
			standardPanel;
	private JButton saveButton, closeButton;
	private JTextField numberOfSolutionsText, bitsText, circuitSizeText;
	private JTable encodingTable;
	JScrollPane scrollPane;
	private TableLayout layout;
	private int m, bits;
	private JRadioButton normal, fast;
	private ButtonGroup group;

	// Core variables
	private ScencoTask encoder;
	private EncoderSettings settings;
	private WorkspaceEntry we;

	// sizes
	Dimension dimensionLabel = new Dimension(120, 22);
	Dimension dimensionLongLabel = new Dimension(290, 22);
	Dimension dimensionBox = new Dimension(170, 22);
	Dimension dimensionText = new Dimension(585, 22);
	Dimension dimensionTable = new Dimension(400, 180);
	Dimension dimensionWindow = new Dimension(100, 400);

	// generationPanel.getPreferredSize().height

	public EncoderSettings getSettings() {
		return settings;
	}

	public ScencoHeuristicSearchDialog(Window owner,
			PresetManager<EncoderSettings> presetManager,
			EncoderSettings settings, WorkspaceEntry we) {
		super(owner, "Heuristic-Guided Search", ModalityType.APPLICATION_MODAL);
		this.settings = settings;
		this.we = we;

		createStandardPanel();
		createGenerationPanel();
		createCustomPanel();
		createButtonPanel();

		double size[][] = new double[][] { { TableLayout.FILL },
				{ 85, 110, TableLayout.FILL, 35 } };

		layout = new TableLayout(size);
		layout.setHGap(3);
		layout.setVGap(3);

		content = new JPanel(layout);
		content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		content.add(standardPanel, "0, 0");
		content.add(generationPanel, "0 1");
		content.add(customPanel, "0 2");
		content.add(buttonsPanel, "0 3");

		setContentPane(content);

		getRootPane().registerKeyboardAction(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		sizeWindow(425, 550, 200, 100);
	}

	private void createCustomPanel() {
		VisualCPOG cpog = (VisualCPOG) (we.getModelEntry().getVisualModel());
		ArrayList<VisualTransformableNode> scenarios = new ArrayList<>();
		CpogParsingTool.getScenarios(cpog, scenarios);
		m = scenarios.size();

		// TABLE OF ENCODINGS
		exampleLabel = new JLabel(
				"0/1: assign 0 or 1;  X: find best assignment;  -: Don't Care bit");
		exampleLabel.setPreferredSize(new Dimension(400, 22));
		customEncLabel = new JLabel("Customise");
		customEncLabel.setPreferredSize(dimensionLabel);
		customEncodings = new JCheckBox("", false);
		customEncLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				customEncodings.setSelected(customEncodings.isSelected() ? false
						: true);
				if (customEncodings.isSelected()) {
					encodingTable.setEnabled(true);
					encodingTable.setBackground(Color.WHITE);
					bitsText.setBackground(Color.WHITE);
					bitsText.setEnabled(true);
				} else {
					encodingTable.setEnabled(false);
					encodingTable.setBackground(Color.LIGHT_GRAY);
					bitsText.setBackground(Color.LIGHT_GRAY);
					bitsText.setEnabled(false);
				}
			}
		});
		customEncodings.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (customEncodings.isSelected()) {
					encodingTable.setEnabled(true);
					encodingTable.setBackground(Color.WHITE);
					bitsText.setBackground(Color.WHITE);
					bitsText.setEnabled(true);
				} else {
					encodingTable.setEnabled(false);
					encodingTable.setBackground(Color.LIGHT_GRAY);
					bitsText.setBackground(Color.LIGHT_GRAY);
					bitsText.setEnabled(false);
				}
			}
		});

		bitsLabel = new JLabel("Encoding bit-width:");
		bitsLabel.setPreferredSize(dimensionLabel);
		circuitSizeLabel = new JLabel("Circuit size in 2-input gates");
		circuitSizeLabel.setPreferredSize(dimensionLabel);
		int value = 2;
		while (value < m) {
			value *= 2;
			bits++;
		}

		bitsText = new JTextField();
		bitsText.setDocument(new IntDocument(2));
		bitsText.setText(String.valueOf(bits + 1));
		bitsText.setPreferredSize(new Dimension(35, 22));
		bitsText.setBackground(Color.LIGHT_GRAY);
		bitsText.setEnabled(false);
		bitsText.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (Integer.parseInt(bitsText.getText()) < bits + 1) {
					JOptionPane
							.showMessageDialog(
									null,
									"Bits selected are not enough to encode all scenarios.",
									"Not enough bits",
									JOptionPane.ERROR_MESSAGE);

					bitsText.setText(String.valueOf(bits + 1));
				}
				for (int i = 0; i < m; i++) {
					String data = "";
					for (int j = 0; j < Integer.parseInt(bitsText.getText()); j++)
						data = data + "X";
					encodingTable.getModel().setValueAt(data, i, 1);
				}
			}
		});
		circuitSizeText = new JTextField();
		circuitSizeText.setText(String.valueOf(bits + 2));
		circuitSizeText.setPreferredSize(new Dimension(70, 22));
		modifyCircuitSize(false);

		String[] columnNames = { "Name", "Opcode" };
		Object[][] data = new Object[m][3];
		for (int i = 0; i < m; i++) {
			String name;
			if (scenarios.get(i).getLabel().isEmpty()) {
				name = "CPOG " + i;
			} else {
				name = scenarios.get(i).getLabel();
			}
			data[i][0] = name;
			data[i][1] = "";
			for (int j = 0; j < Integer.parseInt(bitsText.getText()); j++) {
				data[i][1] = data[i][1] + "X";
			}
		}
		encodingTable = new JTable(data, columnNames);
		MyTableCellRenderer renderer = new MyTableCellRenderer();
		encodingTable.setDefaultRenderer(Object.class, renderer);
		encodingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		encodingTable.setAutoscrolls(true);
		encodingTable.setFillsViewportHeight(true);
		encodingTable.setEnabled(false);
		encodingTable.setBackground(Color.LIGHT_GRAY);
		DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
		leftRenderer.setHorizontalAlignment(JLabel.LEFT);
		encodingTable.getColumnModel().getColumn(0)
				.setCellRenderer(leftRenderer);
		scrollPane = new JScrollPane(encodingTable);
		scrollPane.setMinimumSize(dimensionTable);
		scrollPane.setPreferredSize(dimensionTable);

		customPanel = new JPanel(new SimpleFlowLayout());
		customPanel.setBorder(BorderFactory
				.createTitledBorder("Custom encoding"));

		customPanel.add(customEncodings);
		customPanel.add(customEncLabel);
		customPanel.add(bitsLabel);
		customPanel.add(bitsText);
		customPanel.add(new SimpleFlowLayout.LineBreak());
		customPanel.add(scrollPane);
		customPanel.add(new SimpleFlowLayout.LineBreak());
		customPanel.add(exampleLabel);
		// customPanel.add(exampleLabel2);

	}

	private void createStandardPanel() {

		standardPanel = new JPanel(new SimpleFlowLayout());

		// OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
		optimiseLabel = new JLabel("Target:");
		optimiseLabel.setPreferredSize(dimensionLabel);
		OptimiseBox = new JComboBox<String>();
		OptimiseBox.setEditable(false);
		OptimiseBox.setPreferredSize(dimensionBox);
		OptimiseBox.addItem("Microcontroller");
		OptimiseBox.addItem("CPOG");
		OptimiseBox.setSelectedIndex(settings.isCpogSize() ? 1 : 0);
		OptimiseBox.setBackground(Color.WHITE);

		// ABC TOOL DISABLE FLAG
		abcCheck = new JCheckBox("", settings.isAbcFlag());
		abcLabel = new JLabel("Use Abc");
		abcLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				abcCheck.setSelected(abcCheck.isSelected() ? false : true);
				if (abcCheck.isSelected()) {
					normal.setSelected(true);
				} else {
					if (guidedModeBox.getSelectedIndex() == 0) {
						normal.setSelected(true);
					}
				}
			}
		});
		abcLabel.setPreferredSize(dimensionLabel);
		abcCheck.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (abcCheck.isSelected()) {
					normal.setSelected(true);
				} else {
					if (guidedModeBox.getSelectedIndex() == 0) {
						normal.setSelected(true);
					}
				}
			}
		});

		// VERBOSE MODE INSTANTIATION
		verboseModeLabel = new JLabel("Verbose mode");
		verboseModeLabel.setPreferredSize(dimensionLabel);
		verboseModeCheck = new JCheckBox("", false);
		verboseModeLabel.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				verboseModeCheck.setSelected(verboseModeCheck.isSelected() ? false
						: true);
			}
		});

		guidLabel = new JLabel("Strategy:");
		guidLabel.setPreferredSize(dimensionLabel);
		guidedModeBox = new JComboBox<String>();
		guidedModeBox.setEditable(false);
		guidedModeBox.setPreferredSize(dimensionBox);
		guidedModeBox.addItem("Simulated annealing");
		guidedModeBox.addItem("Full coverage");
		guidedModeBox.addItem("Random search");

		guidedModeBox.setBackground(Color.WHITE);

		guidedModeBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				switch (guidedModeBox.getSelectedIndex()) {
				// SIMULATED ANNEALING
				case 0:
					// number of solutions
					numbSolutPanelVisibility(true);

					// speed-up
					normal.setVisible(true);
					fast.setVisible(true);
					normal.setSelected(true);

					// custom encodings
					customPanelVisibility(true);
					modifyCircuitSize(false);

					// set size of window
					sizeWindow(425, 570, 200, 100);
					break;
				// FULL COVERAGE
				case 1:
					// number of solutions
					numbSolutPanelVisibility(false);

					// speed-up
					// slow.setVisible(false);
					normal.setVisible(true);
					fast.setVisible(true);
					normal.setSelected(true);

					// custom encodings
					customPanelVisibility(false);
					modifyCircuitSize(false);

					// set size of window
					sizeWindow(425, 280, 200, 100);
					break;
				// RANDOM SEARCH
				case 2:

					// number of solutions
					numbSolutPanelVisibility(true);

					// speed-up
					normal.setVisible(true);
					fast.setVisible(true);
					normal.setSelected(true);

					// custom encodings
					customPanelVisibility(false);
					modifyCircuitSize(false);

					// set size of window
					sizeWindow(425, 280, 200, 100);
					break;
				default:

				}
			}
		});

		// ADD EVERYTHING INTO THE PANEL
		standardPanel.add(optimiseLabel);
		standardPanel.add(OptimiseBox);
		standardPanel.add(new SimpleFlowLayout.LineBreak());
		standardPanel.add(abcCheck);
		standardPanel.add(abcLabel);
		standardPanel.add(verboseModeCheck);
		standardPanel.add(verboseModeLabel);
		standardPanel.add(new SimpleFlowLayout.LineBreak());
		standardPanel.add(guidLabel);
		standardPanel.add(guidedModeBox);
	}

	private void createGenerationPanel() {
		generationPanel = new JPanel(new SimpleFlowLayout());
		generationPanel.setBorder(BorderFactory
				.createTitledBorder("Search range"));

		// SPEED UP MODE
		normal = new JRadioButton("Around optimal solution (slow)", true);
		fast = new JRadioButton("Only the optimal solution (fast)");
		group = new ButtonGroup();
		group.add(normal);
		group.add(fast);

		// NUMBER OF SOLUTIONS TO GENERATE
		numberOfSolutionsLabel = new JLabel(" Number of solutions to explore");
		numberOfSolutionsLabel.setPreferredSize(new Dimension(215, 22));
		numberOfSolutionsText = new JTextField();
		numberOfSolutionsText.setDocument(new IntDocument(3));
		numberOfSolutionsText.setText(String.valueOf(settings
				.getSolutionNumber()));
		numberOfSolutionsText.setPreferredSize(new Dimension(35, 22));
		numberOfSolutionsText.setBackground(Color.WHITE);

		generationPanel.add(normal);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(fast);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(numberOfSolutionsLabel);
		generationPanel.add(numberOfSolutionsText);
		generationPanel.add(new SimpleFlowLayout.LineBreak());

	}

	private void createButtonPanel() {
		buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		saveButton = new JButton("Run");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);

				// ENCODER EXECUTION

				// abc disabled
				settings.setAbcFlag(abcCheck.isSelected() ? true : false);

				// speed-up mode selection
				settings.setEffort(normal.isSelected() ? true : false);
				settings.setCostFunc(false/* slow.isSelected() */);

				// number of bits selection
				settings.setBits(Integer.parseInt(bitsText.getText()));

				// circuit size selection
				// settings.setCircuitSize(Integer.valueOf(circuitSizeText.getText()));

				// optimise for option
				settings.setCpogSize(OptimiseBox.getSelectedIndex() == 0 ? false
						: true);

				// verbose mode
				settings.setVerboseMode(verboseModeCheck.isSelected());

				// continuous mode or number of solutions
				settings.setSolutionNumber(Integer
						.parseInt(numberOfSolutionsText.getText()));

				// generation mode selection
				settings.setGenerationModeInt(guidedModeBox.getSelectedIndex());

				// custom encodings
				settings.setNumPO(m);
				if (customEncodings.isSelected()) {
					settings.setCustomEncMode(true);
					String encodings[] = new String[m];
					for (int i = 0; i < m; i++) {
						encodings[i] = (String) encodingTable.getModel()
								.getValueAt(i, 1);
					}
					settings.setCustomEnc(encodings);
				} else if (guidedModeBox.getSelectedIndex() == 1) {
					settings.setCustomEncMode(false);
				} else {
					settings.setBits(bits + 1);
					settings.setCustomEncMode(false);
				}

				// Set them on encoder
				encoder = new ScencoTask(settings);

				// Execute scenco
				encoder.run(we);
			}
		});

		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		buttonsPanel.add(saveButton);
		buttonsPanel.add(closeButton);

	}

	private void sizeWindow(int width, int height, int row1, int row2) {
		// setMaximumSize(new Dimension(width,height));
		setMinimumSize(new Dimension(width, height));
		setPreferredSize(new Dimension(width, height));
		// layout.setRow(new double[] {row1, row2});
		pack();

	}

	private void customPanelVisibility(boolean condition) {
		customPanel.setVisible(condition);
		exampleLabel.setVisible(condition);
		exampleLabel2.setVisible(condition);
		customEncLabel.setVisible(condition);
		customEncodings.setVisible(condition);
		customEncodings.setSelected(false);
		scrollPane.setVisible(condition);
		bitsLabel.setVisible(condition);
		bitsText.setVisible(condition);

		if (customEncodings.isSelected()) {
			encodingTable.setEnabled(true);
			encodingTable.setBackground(Color.WHITE);
			bitsText.setBackground(Color.WHITE);
			bitsText.setEnabled(true);
		} else {
			encodingTable.setEnabled(false);
			encodingTable.setBackground(Color.LIGHT_GRAY);
			bitsText.setBackground(Color.LIGHT_GRAY);
			bitsText.setEnabled(false);
		}
	}

	private void numbSolutPanelVisibility(boolean condition) {
		numberOfSolutionsText.setVisible(condition);
		numberOfSolutionsLabel.setVisible(condition);
	}

	private void modifyCircuitSize(boolean b) {
		circuitSizeText.setEnabled(b);
		circuitSizeLabel.setVisible(b);
		circuitSizeText.setVisible(b);
	}
}
