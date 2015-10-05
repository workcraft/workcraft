package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.TimeEstimatorSettings;
import org.workcraft.plugins.son.util.Interval;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.plugins.son.util.ScenarioSaveList;

public class TimeEstimatorDialog extends JDialog{

	private static final long serialVersionUID = 1L;
	private SON net;
	private GraphEditor editor;
	private TimeEstimatorSettings settings;
	private Node selection;

	private DefaultDurationPanel defaultDurationPanel;
	private JScrollPane tabelPanel;
	private ScenarioTable scenarioTable;

	private JPanel buttonsPanel;
	private JButton runButton, cancelButton;
	protected Dimension buttonSize = new Dimension(80, 25);
	private int run = 0;

	public TimeEstimatorDialog (GraphEditor editor, TimeEstimatorSettings settings, Node selection){
		super(editor.getMainWindow(), "Estimator Setting", ModalityType.TOOLKIT_MODAL);
		net = (SON)editor.getModel().getMathModel();
		this.editor = editor;
		this.settings = settings;
		this.selection = selection;

		defaultDurationPanel = new DefaultDurationPanel(settings.getDuration());
		createScenarioTable();
		createButtonsPanel();

		setLayout(new BorderLayout(10, 10));
		add(defaultDurationPanel, BorderLayout.NORTH);
		add(tabelPanel, BorderLayout.CENTER);
		add(buttonsPanel, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter()
		{
		  public void windowClosing(WindowEvent e)
		  {
			 setParameters();
		  }
		});

		pack();
	}

	private void createScenarioTable(){

		ScenarioSaveList saveList = saveListFilter(net.importScenarios(editor.getMainWindow()), selection);
		scenarioTable = new ScenarioTable(saveList, editor, new ScenarioListTableModel(), selection);

		tabelPanel = new JScrollPane(scenarioTable);
		tabelPanel.setPreferredSize(new Dimension(1, 100));

		scenarioTable.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int column = scenarioTable.getSelectedColumn();
				int row = scenarioTable.getSelectedRow();

				ScenarioSaveList saveList = scenarioTable.getSaveList();;

				if (column == 0 && row < saveList.size()) {
					saveList.setPosition(row);
					Object obj = scenarioTable.getValueAt(row, column);
					if(obj instanceof ScenarioRef){
						scenarioTable.setScenarioRef((ScenarioRef)obj);
						scenarioTable.setIsCellColor(true);
						scenarioTable.updateTable(editor);
						scenarioTable.updateColor(selection);
					}
				}
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});
	}

	private ScenarioSaveList saveListFilter(ScenarioSaveList saveList, Node selection){
		ScenarioSaveList result = new ScenarioSaveList();
		for(ScenarioRef ref : saveList){
			for(String str : ref){
				if(str.equals(net.getNodeReference(selection))){
					result.add(ref);
					break;
				}
			}
		}
		return result;
	}

	private void createButtonsPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		runButton = new JButton ("Run");
		runButton.setPreferredSize(buttonSize);
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setParameters();
				if(defaultDurationPanel.isValidDuration()){
					run = 1;
					setVisible(false);
				}else{
					defaultDurationPanel.getMin().setForeground(Color.RED);
					defaultDurationPanel.getMax().setForeground(Color.RED);
				}
			}
		});

		cancelButton = new JButton ("Cancel");
		cancelButton.setPreferredSize(buttonSize);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				run = 2;
				setParameters();
				setVisible(false);
			}
		});

		buttonsPanel.add(cancelButton);
		buttonsPanel.add(runButton);
	}

	@SuppressWarnings("serial")
	protected class ScenarioListTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public String getColumnName(int column) {
			return "Scenario List";
		}

		@Override
		public int getRowCount() {
			return scenarioTable.getSaveList().size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (column == 0) {
				if (!scenarioTable.getSaveList().isEmpty() && (row < scenarioTable.getSaveList().size())) {
					return scenarioTable.getSaveList().get(row);
				}
			}
			return "";
		}
	};

	private void setParameters(){
		settings.setPosition(scenarioTable.getSaveList().getPosition());
		settings.setDuration(defaultDurationPanel.getDefaultDuration());

	}

	public Interval getDefaultDuration(){
		return settings.getDuration();
	}

	public ScenarioTable getScenarioTable() {
		return scenarioTable;
	}

	public ScenarioRef getScenarioRef(){
		return scenarioTable.getScenarioRef();
	}

	public int getRun() {
		return run;
	}
}
