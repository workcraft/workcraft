package org.workcraft.plugins.cpog.gui;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.cpog.CpogProgrammer;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.generationMode;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.workspace.WorkspaceEntry;

public class EncoderConfigurationDialog extends JDialog {

	private JLabel numberOfSolutionsLabel, contLabel,
					verboseModeLabel,exampleLabel,exampleLabel2,exampleLabel3,exampleLabel4,
					exampleLabel5,customEncLabel,bitsLabel,effortLabel,genLabel,
					optimiseLabel, disableLabel;
	private JCheckBox verboseModeCheck, customEncodings, effortCheck,
					disableCheck, contCheck;
	private JComboBox generationModeBox,OptimiseBox;
	private JPanel generationPanel, buttonsPanel, content;
	private JButton saveButton, closeButton;
	private JTextField numberOfSolutionsText, bitsText;
	private PresetManager<EncoderSettings> presetManager;
	private JTable encodingTable;
	JScrollPane scrollPane;
	private TableLayout layout;
	private int m,bits;

	// Core variables
	private CpogProgrammer encoder;
	private EncoderSettings settings;
	private WorkspaceEntry we;
	private static boolean settingsPresent = false;

	Dimension dimensionLabel = new Dimension(270, 22);
	Dimension dimensionBox = new Dimension(270, 22);
	Dimension dimensionText = new Dimension(585,22);
	Dimension dimensionTable = new Dimension(942,100);
	Dimension dimensionWindow = new Dimension(100,400);

	//generationPanel.getPreferredSize().height

	public EncoderSettings getSettings() {
		return settings;
	}

	public EncoderConfigurationDialog(Window owner, PresetManager<EncoderSettings> presetManager, EncoderSettings settings,WorkspaceEntry we) {
		super(owner, "SCENCO configuration", ModalityType.APPLICATION_MODAL);
		this.presetManager = presetManager;
		this.settings = settings;
		this.we = we;

		createGenerationPanel();
		createButtonPanel();

		/*double size[][] = new double[][] {
				{TableLayout.FILL},
				{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, dimensionWindow.height }
		};*/
		double size[][] = new double[][] {
				{946},
				{430,100}
		};

		layout = new TableLayout(size);
		//layout.setHGap(3);
		//layout.setVGap(3);

		content = new JPanel(layout);
		content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		content.add(generationPanel, "0 0");
		content.add(buttonsPanel, "0 1");
		setContentPane(content);



		getRootPane().registerKeyboardAction(new ActionListener() {
	    	@Override
	    	public void actionPerformed(ActionEvent e) {
	    		setVisible(false);
	    	}
	    },
	    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
	    JComponent.WHEN_IN_FOCUSED_WINDOW);

	}

	private void createButtonPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		saveButton = new JButton ("Run");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);


				// ENCODER EXECUTION

				// Read parameters
				if (effortCheck.isSelected())
					settings.setEffort(false);
				else
					settings.setEffort(true);
				settings.setBits(Integer.valueOf(bitsText.getText()));
				settings.setCpogSize(OptimiseBox.getSelectedIndex() == 0 ? false : true);
				settings.setCostFunc(disableCheck.isSelected());
				settings.setVerboseMode(verboseModeCheck.isSelected());
				settings.setContMode(contCheck.isSelected());
				settings.setGenerationModeInt(generationModeBox.getSelectedIndex());
				settings.setSolutionNumber(Integer.parseInt(numberOfSolutionsText.getText()));
				settings.setNumPO(m);
				if(customEncodings.isSelected()){
					settings.setCustomEncMode(true);
					String encodings[] = new String[m];
					for(int i = 0; i<m; i++){
						encodings[i] = (String) encodingTable.getModel().getValueAt(i, 1);
					}
					settings.setCustomEnc(encodings);
				}else{
					settings.setBits(bits+1);
					settings.setCustomEncMode(false);
				}

				// Set them on encoder
				if(settingsPresent == false){
					settingsPresent = true;
					encoder = new CpogProgrammer(settings);
				}else{
					encoder.setSettings(settings);
				}

				// Execute programmer.x
				encoder.run(we);
			}
		});

		closeButton = new JButton ("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		buttonsPanel.add(saveButton);
		buttonsPanel.add(closeButton);

	}

	private void createGenerationPanel() {
		setMinimumSize(new Dimension(600, 400));
		generationPanel = new JPanel(new SimpleFlowLayout());
		JPanel numberOfSolutionsPanel = new JPanel (new FlowLayout(FlowLayout.LEFT, 3, 0));
		VisualCPOG cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());
		ArrayList<VisualScenario> scenarios = new ArrayList<VisualScenario>(cpog.getGroups());
		m = scenarios.size();

		// GENERATION MODE COMBOBOX
		genLabel = new JLabel("Mode:");
		genLabel.setPreferredSize(dimensionLabel);
		generationModeBox = new JComboBox();
		generationModeBox.setEditable(false);
		generationModeBox.setPreferredSize(dimensionBox);
		for (generationMode mode : generationMode.modes) {
			generationModeBox.addItem(mode.name);
		}
		generationModeBox.setSelectedIndex(settings.getGenMode().ordinal());
		generationModeBox.setBackground(Color.WHITE);
		generationModeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	switch(generationModeBox.getSelectedIndex()){
            		// SIMULATED ANNEALING
            		case 0:
	            		numberOfSolutionsText.setBackground(Color.WHITE);
	            		numberOfSolutionsText.setEnabled(true);
	            		contCheck.setEnabled(true);
	            		customEncodings.setEnabled(true);
	            		disableCheck.setSelected(false);
                		disableCheck.setEnabled(true);
	            		break;
	            	// EXHAUSTIVE SEARCH
            		case 1:
            			numberOfSolutionsText.setBackground(Color.LIGHT_GRAY);
                		numberOfSolutionsText.setEnabled(false);
                		contCheck.setEnabled(false);
                		contCheck.setSelected(false);
                		customEncodings.setEnabled(false);
                		customEncodings.setSelected(false);
                		encodingTable.setEnabled(false);
                		encodingTable.setBackground(Color.LIGHT_GRAY);
                		bitsText.setBackground(Color.LIGHT_GRAY);
                		bitsText.setEnabled(false);
                		disableCheck.setSelected(false);
                		disableCheck.setEnabled(false);
                		break;
                	// RANDOM SEARCH
            		case 2:
            			numberOfSolutionsText.setBackground(Color.WHITE);
                		numberOfSolutionsText.setEnabled(true);
                		contCheck.setEnabled(true);
                		customEncodings.setEnabled(false);
                		customEncodings.setSelected(false);
                		encodingTable.setEnabled(false);
                		encodingTable.setBackground(Color.LIGHT_GRAY);
                		bitsText.setBackground(Color.LIGHT_GRAY);
                		bitsText.setEnabled(false);
                		disableCheck.setSelected(false);
                		disableCheck.setEnabled(false);
                		break;
                	// OLD SCENCO
            		case 3:
            			numberOfSolutionsText.setBackground(Color.LIGHT_GRAY);
                		numberOfSolutionsText.setEnabled(false);
                		contCheck.setEnabled(false);
                		contCheck.setSelected(false);
                		customEncodings.setSelected(false);
                		customEncodings.setEnabled(false);
                		encodingTable.setEnabled(false);
                		encodingTable.setBackground(Color.LIGHT_GRAY);
                		bitsText.setBackground(Color.LIGHT_GRAY);
                		bitsText.setEnabled(false);
                		disableCheck.setSelected(false);
                		disableCheck.setEnabled(false);
                		break;
                	// OLD SYNTHESISE
            		case 4:
            			numberOfSolutionsText.setBackground(Color.LIGHT_GRAY);
                		numberOfSolutionsText.setEnabled(false);
                		contCheck.setEnabled(false);
                		contCheck.setSelected(false);
                		customEncodings.setSelected(false);
                		customEncodings.setEnabled(false);
                		encodingTable.setEnabled(false);
                		encodingTable.setBackground(Color.LIGHT_GRAY);
                		bitsText.setBackground(Color.LIGHT_GRAY);
                		bitsText.setEnabled(false);
                		disableCheck.setSelected(false);
                		disableCheck.setEnabled(false);
                		break;
                	default:
            	}
            }
        });

		// OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
		optimiseLabel = new JLabel("Optimise for:");
		optimiseLabel.setPreferredSize(dimensionLabel);
		OptimiseBox = new JComboBox();
		OptimiseBox.setEditable(false);
		OptimiseBox.setPreferredSize(dimensionBox);
		OptimiseBox.addItem("microcontroller");
		OptimiseBox.addItem("CPOG size");
		OptimiseBox.setSelectedIndex(settings.isCpogSize() ? 1 : 0);
		OptimiseBox.setBackground(Color.WHITE);

		// DISABLE COST FUNCTION
		disableLabel = new JLabel("Disable cost function approximation: ");
		disableLabel.setPreferredSize(dimensionLabel);
		disableCheck = new JCheckBox("",settings.isCostFunc());
		disableCheck.setToolTipText("Requires a big amount of time");


		// Speed-up
		effortLabel = new JLabel("Using heuristic for speed-up:");
		effortLabel.setPreferredSize(dimensionLabel);
		effortCheck = new JCheckBox("",settings.isEffort());

		// NUMBER OF SOLUTIONS TO GENERATE
		numberOfSolutionsLabel = new JLabel("Number of encodings to generate: ");
		numberOfSolutionsLabel.setPreferredSize(dimensionLabel);
		numberOfSolutionsText = new JTextField();
		numberOfSolutionsText.setText(String.valueOf(settings.getSolutionNumber()));
		numberOfSolutionsText.setPreferredSize(new Dimension(70, 15));

		// CONTINUOUS MODE
		contCheck = new JCheckBox("",settings.isContMode());
		contLabel = new JLabel("Continuous mode");
		contLabel.setPreferredSize(new Dimension(150, 15));
		contCheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	if(contCheck.isSelected()){
            		numberOfSolutionsText.setBackground(Color.LIGHT_GRAY);
            		numberOfSolutionsText.setEnabled(false);
            	}else{
            		numberOfSolutionsText.setBackground(Color.WHITE);
            		numberOfSolutionsText.setEnabled(true);
            	}
            }
        });

		// TABLE OF ENCODINGS
		exampleLabel = new JLabel("Fill in the table below if you want to set a custom op-code to a whichever Partial Order Graph. Below symbols allowed:");
		exampleLabel2 = new JLabel("- 0 1    assign a specific bit 0 or 1;");
		exampleLabel3 = new JLabel("-  X      find the best bit assignment;");
		exampleLabel4 = new JLabel("-  -       a Don't Care bit.");
		exampleLabel5 = new JLabel("You have to take care of selecting an enough number of bits for encoding all Partial Order.");
		exampleLabel.setPreferredSize(new Dimension(942,15));
		exampleLabel2.setPreferredSize(new Dimension(800,15));
		exampleLabel3.setPreferredSize(new Dimension(800,15));
		exampleLabel4.setPreferredSize(new Dimension(800,15));
		exampleLabel5.setPreferredSize(new Dimension(800,15));
		customEncLabel = new JLabel("Custom encodings: ");
		customEncLabel.setPreferredSize(dimensionLabel);
		customEncodings = new JCheckBox("", false);
		customEncodings.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	if(customEncodings.isSelected()){
            		encodingTable.setEnabled(true);
            		encodingTable.setBackground(Color.WHITE);
            		bitsText.setBackground(Color.WHITE);
            		bitsText.setEnabled(true);
            	}
            	else{
            		encodingTable.setEnabled(false);
            		encodingTable.setBackground(Color.LIGHT_GRAY);
            		bitsText.setBackground(Color.LIGHT_GRAY);
            		bitsText.setEnabled(false);
            	}
            }
        });

		bitsLabel = new JLabel("Number of bits to use: ");
		bitsLabel.setPreferredSize(dimensionLabel);
		int value = 2;
		while(value < m){
			value *=2;
			bits++;
		}

		bitsText = new JTextField();
		bitsText.setText(String.valueOf(bits + 1));
		bitsText.setPreferredSize(new Dimension(70, 15));
		bitsText.setBackground(Color.LIGHT_GRAY);
		bitsText.setEnabled(false);
		bitsText.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	if(Integer.parseInt(bitsText.getText()) < bits +1)
            		bitsText.setText(String.valueOf(bits + 1));
            	for(int i=0;i<m;i++){
            		String data = "";
            		for(int j=0; j < Integer.valueOf(bitsText.getText()); j++) data = data + "X";
            		encodingTable.getModel().setValueAt(data, i, 1);
            	}
            }
        });

		String[] columnNames = {"Partial Order Name","Encoding"};
		Object[][] data = new Object[m][3];
		for(int i=0; i<m; i++){
			String name;
			if(scenarios.get(i).getLabel().equals("")){
				name = "CPOG " + i;
			}
			else{
				name = scenarios.get(i).getLabel();
			}
			data[i][0] = name;
			data[i][1] = "";
			for(int j=0; j < Integer.valueOf(bitsText.getText()); j++) data[i][1] = data[i][1] + "X";
		}
		encodingTable = new JTable(data, columnNames);
		MyTableCellRenderer renderer = new MyTableCellRenderer();
		encodingTable.setDefaultRenderer(Object.class, renderer);
		scrollPane = new JScrollPane(encodingTable);
		scrollPane.setPreferredSize(dimensionTable);
		encodingTable.setFillsViewportHeight(false);
		encodingTable.setEnabled(false);
		encodingTable.setBackground(Color.LIGHT_GRAY);


		// VERBOSE MODE INSTANTIATION
		verboseModeLabel = new JLabel("Activate verbose mode:");
		verboseModeLabel.setPreferredSize(dimensionLabel);
		verboseModeCheck = new JCheckBox("",false);

		// INSTANTATING PANEL
		generationPanel.add(genLabel);
		generationPanel.add(generationModeBox);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(optimiseLabel);
		generationPanel.add(OptimiseBox);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(disableLabel);
		generationPanel.add(disableCheck);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(effortLabel);
		generationPanel.add(effortCheck);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(numberOfSolutionsLabel);
		generationPanel.add(numberOfSolutionsText);
		generationPanel.add(contCheck);
		generationPanel.add(contLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(verboseModeLabel);
		generationPanel.add(verboseModeCheck);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel2);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel3);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel4);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel5);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(customEncLabel);
		generationPanel.add(customEncodings);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(bitsLabel);
		generationPanel.add(bitsText);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(scrollPane);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
	}
}
