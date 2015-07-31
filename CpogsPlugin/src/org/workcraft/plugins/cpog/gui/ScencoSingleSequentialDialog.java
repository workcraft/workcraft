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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.tasks.SatBasedSolver;
import org.workcraft.plugins.cpog.tools.CpogParsingTool;
import org.workcraft.plugins.shared.presets.PresetManager;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class ScencoSingleSequentialDialog extends JDialog {

	private JLabel verboseModeLabel,optimiseLabel, abcLabel;
	private JCheckBox verboseModeCheck, abcCheck;
	private JComboBox<String> OptimiseBox, guidedModeBox;
	private JPanel buttonsPanel, content, standardPanel;
	private JButton saveButton, closeButton;
	JScrollPane scrollPane;
	private TableLayout layout;
	private JRadioButton normal;
	private int m,bits;
	// Core variables
	private SatBasedSolver encoder;
	private EncoderSettings settings;
	private WorkspaceEntry we;
	private int modalResult;

	public ScencoSingleSequentialDialog(Window owner, PresetManager<EncoderSettings> presetManager, EncoderSettings settings,WorkspaceEntry we, String string) {
		super(owner, string, ModalityType.APPLICATION_MODAL);
		this.settings = settings;
		this.we = we;
		modalResult = 0;

		createStandardPanel();
		createButtonPanel(string);

		double size[][] = new double[][] {
				{TableLayout.FILL},
				{60, TableLayout.FILL}
		};

		layout = new TableLayout(size);
		layout.setHGap(3);
		layout.setVGap(3);

		content = new JPanel(layout);
		content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		content.add(standardPanel, "0, 0");
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

		sizeWindow(365,155,200,100);
	}

	private void createStandardPanel() {

				standardPanel = new JPanel(new SimpleFlowLayout());

				// OPTIMISE FOR MICROCONTROLLER/CPOG SIZE
				optimiseLabel = new JLabel(ScencoDialogSupport.textOptimiseForLabel);
				//optimiseLabel.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForLabel);
				OptimiseBox = new JComboBox<String>();
				OptimiseBox.setEditable(false);
				OptimiseBox.setPreferredSize(ScencoDialogSupport.dimensionOptimiseForBox);
				OptimiseBox.addItem(ScencoDialogSupport.textOptimiseForFirstElement);
				OptimiseBox.addItem(ScencoDialogSupport.textOptimiseForSecondElement);
				OptimiseBox.setSelectedIndex(settings.isCpogSize() ? 1 : 0);
				OptimiseBox.setBackground(Color.WHITE);

				// ABC TOOL DISABLE FLAG
				abcCheck = new JCheckBox("", settings.isAbcFlag());
				abcLabel = new JLabel(ScencoDialogSupport.textAbcLabel);
				//abcLabel.setPreferredSize(ScencoDialogSupport.dimensionShortLabel);
				abcLabel.addMouseListener(new MouseAdapter()
				{
				    public void mouseClicked(MouseEvent e)
				    {
				    	abcCheck.setSelected(abcCheck.isSelected() ? false : true);
				    	if(abcCheck.isSelected()){
		            		normal.setSelected(true);
		            	}else{
		            		if (guidedModeBox.getSelectedIndex() == 0){
		            			normal.setSelected(true);
		            		}
		            	}
				    }
				});

				// VERBOSE MODE INSTANTIATION
				verboseModeLabel = new JLabel(ScencoDialogSupport.textVerboseMode);
				//verboseModeLabel.setPreferredSize(ScencoDialogSupport.dimensionVerboseLabel);
				verboseModeCheck = new JCheckBox("",false);
				verboseModeLabel.addMouseListener(new MouseAdapter()
				{
				    public void mouseClicked(MouseEvent e)
				    {
				    	verboseModeCheck.setSelected(verboseModeCheck.isSelected() ? false : true);
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
	}



	private void createButtonPanel(final String string) {
		VisualCPOG cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());
		ArrayList<VisualTransformableNode> scenarios = CpogParsingTool.getScenarios(cpog);
		m = scenarios.size();

		int value = 2;
		while(value < m){
			value *=2;
			bits++;
		}

		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		saveButton = new JButton ("Run");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);


				// ENCODER EXECUTION

				// abc disabled
				settings.setAbcFlag(abcCheck.isSelected() ? true : false);

				// speed-up mode selection
				//settings.setEffort(normal.isSelected() ? true : false);
				//settings.setCostFunc(/*slow.isSelected()*/false);

				// number of bits selection
				settings.setBits(bits + 1);

				// circuit size selection
				//settings.setCircuitSize(Integer.valueOf(circuitSizeText.getText()));

				// optimise for option
				settings.setCpogSize(OptimiseBox.getSelectedIndex() == 0 ? false : true);

				// verbose mode
				settings.setVerboseMode(verboseModeCheck.isSelected());

				// continuous mode or number of solutions
				//settings.setSolutionNumber(Integer.parseInt(numberOfSolutionsText.getText()));

				// generation mode selection
				settings.setGenerationModeInt(string.matches("Single-literal Search") ? 4 : 5);


				// custom encodings
				settings.setNumPO(m);
				if(settings.getGenMode() == GenerationMode.SEQUENTIAL){
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
				encoder = new SatBasedSolver(settings);

				// Execute scenco
				//encoder.run(we);
				modalResult = 1;
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

	private void sizeWindow(int width, int height, int row1, int row2){
		setMinimumSize(new Dimension(width,height));
		//setPreferredSize(new Dimension(width,height));
		pack();

	}

	public int getModalResult() {
		return modalResult;
	}

	public void setModalResult(int modalResult) {
		this.modalResult = modalResult;
	}

	public EncoderSettings getSettings() {
		return settings;
	}
}
