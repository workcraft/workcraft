package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.AbstractTableModel;

import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.gui.TimeConsistencyDialog.Granularity;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.plugins.son.util.ScenarioSaveList;

public class TimeValueEstimatorPanel extends JPanel{

	private static final long serialVersionUID = 1L;
	private SON net;
	private GraphEditor editor;

	private DefaultDurationPanel defaultDurationPanel;
	private JScrollPane tabelPanel;
	private ScenarioTable scenarioTable;
	private GranularityPanel granularityPanel;

	public TimeValueEstimatorPanel (SON net, GraphEditor editor){
		this.net = net;
		this.editor = editor;

		createScenarioTable();
		granularityPanel = new GranularityPanel(null);
		defaultDurationPanel = new DefaultDurationPanel();
		//defaultDurationPanel.setBorder(BorderFactory.createTitledBorder("Default Duration"));

		setLayout(new BorderLayout());
		add(granularityPanel, BorderLayout.NORTH);
		add(defaultDurationPanel, BorderLayout.CENTER);
		add(tabelPanel, BorderLayout.SOUTH);

	}

	private void createScenarioTable(){

		ScenarioSaveList saveList = net.importScenarios(editor.getMainWindow());
		scenarioTable = new ScenarioTable(saveList, editor, new ScenarioListTableModel());
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
						scenarioTable.setIsCellColor(true);
						scenarioTable.updateTable(editor);
						scenarioTable.updateGrayoutColor();
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

	public Granularity getGranularity(){
		return granularityPanel.getSelection();
	}

	public ScenarioTable getScenarioTable() {
		return scenarioTable;
	}

	public ScenarioRef getSelection(){
		return scenarioTable.getScenarioRef();
	}
}
