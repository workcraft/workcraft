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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.plugins.cpog.PnToCpogSettings;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class PnToCpogDialog extends JDialog {

	private JLabel reduceLabel, isomorphismLabel, significanceLabel;
	private JCheckBox reduceCheck, isomorphismCheck;
	private JComboBox<String> significanceBox;
	private JButton closeButton, runButton;
	private TableLayout layout;
	private JPanel buttonPanel, content, settingPanel;
	protected int modalResult;

	public PnToCpogDialog(Window owner, PnToCpogSettings settings,WorkspaceEntry we) {
		super(owner, "Petri net to Cpog converter", ModalityType.APPLICATION_MODAL);
		modalResult = 0;

		createSettingPanel();
		createButtonPanel(settings);

		double size[][] = new double[][] {
				{TableLayout.FILL},
				{80, TableLayout.FILL}
		};

		layout = new TableLayout(size);
		layout.setHGap(3);
		layout.setVGap(3);

		content = new JPanel(layout);
		content.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		content.add(settingPanel, "0, 0");
		content.add(buttonPanel, "0 1");

		setContentPane(content);

		getRootPane().registerKeyboardAction(new ActionListener() {
	    	@Override
	    	public void actionPerformed(ActionEvent e) {
	    		setVisible(false);
	    	}
	    },
	    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
	    JComponent.WHEN_IN_FOCUSED_WINDOW);

		sizeWindow(517, 159, 200, 100);
	}

	/** creates the panel containing the settings of the converter **/
	private void createSettingPanel() {

		settingPanel = new JPanel(new SimpleFlowLayout());

		// reduction of maximal significant runs, check box
		reduceCheck = new JCheckBox("", false);
		reduceLabel = new JLabel(PnToCpogDialogSupport.textReduceLabel);
		reduceLabel.addMouseListener(new MouseAdapter()
		{
		    public void mouseClicked(MouseEvent e)
		    {
		    	reduceCheck.setSelected(reduceCheck.isSelected() ? false : true);
		    }
		});

		// reduce isomorphic processes, check box
		isomorphismCheck = new JCheckBox("", false);
		isomorphismLabel = new JLabel(PnToCpogDialogSupport.textIsomorphismLabel);
		isomorphismLabel.addMouseListener(new MouseAdapter()
		{
		    public void mouseClicked(MouseEvent e)
		    {
		    	isomorphismCheck.setSelected(isomorphismCheck.isSelected() ? false : true);
		    }
		});

		// Algorithm for checking significance property of a run, combo box
		significanceLabel = new JLabel(PnToCpogDialogSupport.significanceLabel);
		significanceBox = new JComboBox<String>();
		significanceBox.setEditable(false);
		significanceBox.setPreferredSize(PnToCpogDialogSupport.significanceSize);
		significanceBox.addItem(PnToCpogDialogSupport.significanceItems[0]);
		significanceBox.addItem(PnToCpogDialogSupport.significanceItems[1]);
		significanceBox.addItem(PnToCpogDialogSupport.significanceItems[2]);
		significanceBox.setSelectedIndex(0);
		significanceBox.setBackground(Color.WHITE);

		// adding everything into the panel
		settingPanel.add(significanceLabel);
		settingPanel.add(significanceBox);
		settingPanel.add(new SimpleFlowLayout.LineBreak());
		settingPanel.add(reduceCheck);
		settingPanel.add(reduceLabel);
		settingPanel.add(new SimpleFlowLayout.LineBreak());
		settingPanel.add(isomorphismCheck);
		settingPanel.add(isomorphismLabel);

	}

	/** creates panel containing the buttons for running or closing the converter **/
	private void createButtonPanel(final PnToCpogSettings settings) {

		buttonPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		// run the converter
		runButton = new JButton ("Run");
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);

				settings.setReduce(reduceCheck.isSelected() ? true : false);
				settings.setIsomorphism(isomorphismCheck.isSelected() ? true : false);
				settings.setSignificance(significanceBox.getSelectedIndex());

				modalResult = 1;
			}
		});

		// close the converter
		closeButton = new JButton ("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});

		buttonPanel.add(runButton);
		buttonPanel.add(closeButton);

	}

	private void sizeWindow(int width, int height, int row1, int row2){
		setMinimumSize(new Dimension(width,height));
		//setPreferredSize(new Dimension(width,height));
		pack();
	}

	public int getModalResult(){
		return modalResult;
	}
}
