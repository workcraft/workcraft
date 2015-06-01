package org.workcraft.plugins.cpog.gui;

import info.clearthought.layout.TableLayout;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualScenarioPage;
import org.workcraft.plugins.cpog.tasks.ScencoTask;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class ScencoConfigurationDialog extends JDialog {

	private JLabel numberOfSolutionsLabel, contLabel,
					verboseModeLabel,exampleLabel,exampleLabel2,exampleLabel3,exampleLabel4,
					customEncLabel,bitsLabel,genLabel, guidLabel,
					optimiseLabel, abcLabel;
	private JCheckBox verboseModeCheck, customEncodings,
					 contCheck, abcCheck;
	private JComboBox generationModeBox,OptimiseBox, guidedModeBox;
	private JPanel generationPanel, buttonsPanel, content;
	private JButton saveButton, closeButton;
	private JTextField numberOfSolutionsText, bitsText;
	private PresetManager<EncoderSettings> presetManager;
	private JTable encodingTable;
	JScrollPane scrollPane;
	private TableLayout layout;
	private int m,bits;
	private JRadioButton slow,normal,fast;
	private ButtonGroup group;

	// Core variables
	private ScencoTask encoder;
	private EncoderSettings settings;
	private WorkspaceEntry we;

	// sizes
	Dimension dimensionLabel = new Dimension(270, 22);
	Dimension dimensionLongLabel = new Dimension(800,15);
	Dimension dimensionBox = new Dimension(270, 22);
	Dimension dimensionText = new Dimension(585,22);
	Dimension dimensionTable = new Dimension(800,200);
	Dimension dimensionWindow = new Dimension(100,400);

	//generationPanel.getPreferredSize().height

	public EncoderSettings getSettings() {
		return settings;
	}

	public ScencoConfigurationDialog(Window owner, PresetManager<EncoderSettings> presetManager, EncoderSettings settings,WorkspaceEntry we) {
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
				{800},
				{480,100}
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

		sizeWindow(815,685,610,100);
	}

	private void createButtonPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		saveButton = new JButton ("Run");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);


				// ENCODER EXECUTION

				// abc disabled
				settings.setAbcFlag(abcCheck.isSelected() ? false : true);

				// speed-up mode selection
				settings.setEffort(normal.isSelected() ? true : false);
				settings.setCostFunc(slow.isSelected());

				// number of bits selection
				settings.setBits(Integer.valueOf(bitsText.getText()));

				// optimise for option
				settings.setCpogSize(OptimiseBox.getSelectedIndex() == 0 ? false : true);

				// verbose mode
				settings.setVerboseMode(verboseModeCheck.isSelected());

				// continuous mode or number of solutions
				settings.setContMode(contCheck.isSelected());
				settings.setSolutionNumber(Integer.parseInt(numberOfSolutionsText.getText()));

				// generation mode selection
				switch(generationModeBox.getSelectedIndex()){
					 case 0:
						 settings.setGenerationModeInt(guidedModeBox.getSelectedIndex());
						 break;
					 case 1:
						 settings.setGenerationModeInt(3);
						 break;
					 case 2:
						 settings.setGenerationModeInt(4);
						 break;
					 case 3:
						 settings.setGenerationModeInt(5);
						 break;
					 default:
				}


				// custom encodings
				settings.setNumPO(m);
				if(customEncodings.isSelected()){
					settings.setCustomEncMode(true);
					String encodings[] = new String[m];
					for(int i = 0; i<m; i++){
						encodings[i] = (String) encodingTable.getModel().getValueAt(i, 1);
					}
					settings.setCustomEnc(encodings);
				}else if(generationModeBox.getSelectedIndex() == 3){
					settings.setCustomEncMode(true);
					String encodings[] = new String[m];
					for(int i = 0; i<m; i++)
						encodings[i] = Integer.toBinaryString(i);
					settings.setCustomEnc(encodings);
				}else{
					settings.setBits(bits+1);
					settings.setCustomEncMode(false);
				}

				// Set them on encoder
				encoder = new ScencoTask(settings);

				// Execute scenco
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
		VisualCPOG cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());
		ArrayList<VisualTransformableNode> scenarios = new ArrayList<>();
		CpogParsingTool.getScenarios(cpog, scenarios);
		m = scenarios.size();

		// ABC TOOL DISABLE FLAG
		abcCheck = new JCheckBox("", !settings.isAbcFlag());
		abcLabel = new JLabel("Disable Abc Tool");
		abcLabel.setPreferredSize(new Dimension(150, 15));
		abcCheck.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	if(abcCheck.isSelected()){
            		slow.setVisible(false);
            		normal.setSelected(true);
            		contCheck.setSelected(false);
            		contCheck.setVisible(false);
            		contLabel.setVisible(false);
            	}else{
            		if (generationModeBox.getSelectedIndex() == 0 && guidedModeBox.getSelectedIndex() == 0){
            			slow.setVisible(true);
            			normal.setSelected(true);
            		}
            		if (generationModeBox.getSelectedIndex() == 0 && (guidedModeBox.getSelectedIndex() == 0 ||
            				guidedModeBox.getSelectedIndex() == 2)){
            			contCheck.setVisible(true);
                		contLabel.setVisible(true);
            		}
            	}
            }
        });

		// GENERATION MODE COMBOBOX
		genLabel = new JLabel("Mode:");
		genLabel.setPreferredSize(new Dimension(60, 22));
		generationModeBox = new JComboBox();
		generationModeBox.setEditable(false);
		generationModeBox.setPreferredSize(dimensionBox);
		generationModeBox.addItem("Heuristic-guided search");
		generationModeBox.addItem("SAT-based Optimal Encoding");
		generationModeBox.addItem("Single-literal search");
		generationModeBox.addItem("Sequential encoding");

		guidLabel = new JLabel("        Strategy:");
		guidLabel.setPreferredSize(new Dimension(111, 22));
		guidedModeBox = new JComboBox();
		guidedModeBox.setEditable(false);
		guidedModeBox.setPreferredSize(dimensionBox);
		guidedModeBox.addItem("Simulated annealing");
		guidedModeBox.addItem("Full coverage");
		guidedModeBox.addItem("Random search");

		//generationModeBox.setSelectedIndex(settings.getGenMode().ordinal());
		generationModeBox.setBackground(Color.WHITE);
		guidedModeBox.setBackground(Color.WHITE);
		generationModeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	switch(generationModeBox.getSelectedIndex()){
            		// HEURISTIC-GUIDED SEARCH
            		case 0:
            			guidLabel.setVisible(true);
        				guidedModeBox.setVisible(true);
        				guidedModeBox.setSelectedIndex(0);
	            		break;
                	// OLD SCENCO
            		case 1:
            			// strategy box
            			guidLabel.setVisible(false);
        				guidedModeBox.setVisible(false);

        				// speed-up
                		slow.setVisible(false);
                		normal.setVisible(false);
                		fast.setVisible(false);
                		normal.setSelected(true);

        				// number of solutions
                		numbSolutPanelVisibility(false);

                		// custom encodings
                		customPanelVisibility(false);

                		// set size of window
                		sizeWindow(815,210,130,100);
                		break;
                	// OLD SYNTHESISE
            		case 2:
            			// strategy box
            			guidLabel.setVisible(false);
        				guidedModeBox.setVisible(false);

        				// speed-up
                		slow.setVisible(false);
                		normal.setVisible(false);
                		fast.setVisible(false);
                		normal.setSelected(true);

        				// number of solutions
                		numbSolutPanelVisibility(false);

                		// custom encodings
                		customPanelVisibility(false);

                		// set size of window
                		sizeWindow(815,210,130,100);
                		break;
                		// SEQUENTIAL SYNTHESISE
            		case 3:
            			// strategy box
            			guidLabel.setVisible(false);
        				guidedModeBox.setVisible(false);

        				// speed-up
                		slow.setVisible(false);
                		normal.setVisible(false);
                		fast.setVisible(false);
                		normal.setSelected(true);

        				// number of solutions
                		numbSolutPanelVisibility(false);

                		// custom encodings
                		customPanelVisibility(false);

                		// set size of window
                		sizeWindow(815,210,130,100);
                		break;
                	default:
            	}
            }
        });

		guidedModeBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	switch(guidedModeBox.getSelectedIndex()){
				// SIMULATED ANNEALING
				case 0:
					// number of solutions
					numbSolutPanelVisibility(true);

            		// speed-up
					if(!abcCheck.isSelected())
						slow.setVisible(true);
            		normal.setVisible(true);
            		fast.setVisible(true);
            		normal.setSelected(true);

            		// custom encodings
            		customPanelVisibility(true);


            		// set size of window
            		sizeWindow(815,685,610,100);
					break;
				// FULL COVERAGE
				case 1:
					// number of solutions
					numbSolutPanelVisibility(false);

            		// speed-up
            		slow.setVisible(false);
            		normal.setVisible(true);
            		fast.setVisible(true);
            		normal.setSelected(true);

            		// custom encodings
            		customPanelVisibility(false);

            		// set size of window
            		sizeWindow(815,245,175,100);
					break;
				// RANDOM SEARCH
				case 2:

					// number of solutions
					numbSolutPanelVisibility(true);

            		// speed-up
            		slow.setVisible(false);
            		normal.setVisible(true);
            		fast.setVisible(true);
            		normal.setSelected(true);

            		// custom encodings
            		customPanelVisibility(false);

            		// set size of window
            		sizeWindow(815,300,220,100);
					break;
				default:

			}
            }
		});

		// OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
		optimiseLabel = new JLabel("Generate equations for:");
		optimiseLabel.setPreferredSize(new Dimension(200, 22));
		OptimiseBox = new JComboBox();
		OptimiseBox.setEditable(false);
		OptimiseBox.setPreferredSize(dimensionBox);
		OptimiseBox.addItem("Microcontroller");
		OptimiseBox.addItem("CPOG");
		OptimiseBox.setSelectedIndex(settings.isCpogSize() ? 1 : 0);
		OptimiseBox.setBackground(Color.WHITE);

		// SPEED UP MODE
		slow = new JRadioButton("Slower (Compute exact area at each iteration)");
		normal = new JRadioButton("Normal (Examine solutions around heuristic-optimal point)", true);
		fast = new JRadioButton("Faster (Examine only heuristic-optimal solutions)");
		group = new ButtonGroup();
	    group.add(slow);
	    group.add(normal);
	    group.add(fast);

		// NUMBER OF SOLUTIONS TO GENERATE
		numberOfSolutionsLabel = new JLabel("Number of encodings to generate");
		numberOfSolutionsLabel.setPreferredSize(dimensionLabel);
		numberOfSolutionsText = new JTextField();
		numberOfSolutionsText.setText(String.valueOf(settings.getSolutionNumber()));
		numberOfSolutionsText.setPreferredSize(new Dimension(70, 15));
		numberOfSolutionsText.setBackground(Color.WHITE);

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
		exampleLabel2 = new JLabel("0 1  :   assign a specific bit 0 or 1;");
		exampleLabel3 = new JLabel(" X    :   find the best bit assignment;");
		exampleLabel4 = new JLabel(" -     :    a Don't Care bit.");
		exampleLabel.setPreferredSize(dimensionLongLabel);
		exampleLabel2.setPreferredSize(dimensionLongLabel);
		exampleLabel3.setPreferredSize(dimensionLongLabel);
		exampleLabel4.setPreferredSize(dimensionLongLabel);
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

		bitsLabel = new JLabel("Number of bits to use");
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
            	if(Integer.parseInt(bitsText.getText()) < bits +1){
            		JOptionPane.showMessageDialog(null,
        					"Bits selected are not enough to encode all scenarios.",
        					"Not enough bits",
        					JOptionPane.ERROR_MESSAGE);

            		bitsText.setText(String.valueOf(bits + 1));
            	}
            	for(int i=0;i<m;i++){
            		String data = "";
            		for(int j=0; j < Integer.valueOf(bitsText.getText()); j++) data = data + "X";
            		encodingTable.getModel().setValueAt(data, i, 1);
            	}
            }
        });

		String[] columnNames = {"Graph Name","Encoding"};
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
		verboseModeLabel = new JLabel("Verbose mode");
		verboseModeLabel.setPreferredSize(dimensionLabel);
		verboseModeCheck = new JCheckBox("",false);

		// INSTANTATING PANEL
		generationPanel.add(abcCheck);
		generationPanel.add(abcLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(genLabel);
		generationPanel.add(generationModeBox);
		generationPanel.add(guidLabel);
		generationPanel.add(guidedModeBox);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(optimiseLabel);
		generationPanel.add(OptimiseBox);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(slow);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(normal);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(fast);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(contCheck);
		generationPanel.add(contLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(numberOfSolutionsText);
		generationPanel.add(numberOfSolutionsLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(verboseModeCheck);
		generationPanel.add(verboseModeLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel2);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel3);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(exampleLabel4);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(customEncodings);
		generationPanel.add(customEncLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(bitsText);
		generationPanel.add(bitsLabel);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
		generationPanel.add(scrollPane);
		generationPanel.add(new SimpleFlowLayout.LineBreak());
	}

	private void sizeWindow(int width, int height, int row1, int row2){
		setMaximumSize(new Dimension(width,height));
		setMinimumSize(new Dimension(width,height));
		setPreferredSize(new Dimension(width,height));
		layout.setRow(new double[] {row1, row2});
		pack();

	}

	private void customPanelVisibility(boolean condition){
		exampleLabel.setVisible(condition);
		exampleLabel2.setVisible(condition);
		exampleLabel3.setVisible(condition);
		exampleLabel4.setVisible(condition);
		customEncLabel.setVisible(condition);
		customEncodings.setVisible(condition);
		customEncodings.setSelected(false);
		scrollPane.setVisible(condition);
		bitsLabel.setVisible(condition);
		bitsText.setVisible(condition);
	}

	private void numbSolutPanelVisibility(boolean condition){
		numberOfSolutionsText.setVisible(condition);
		numberOfSolutionsLabel.setVisible(condition);
		if(!abcCheck.isSelected()){
			contLabel.setVisible(condition);
			contCheck.setVisible(condition);
			contCheck.setSelected(false);
		}
	}
}
