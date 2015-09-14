package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.Scenario;
import org.workcraft.plugins.son.gui.StructureVerifyDialog.CheckListItem;
import org.workcraft.plugins.son.gui.StructureVerifyDialog.CheckListRenderer;

public class TimeConsistencyDialog extends StructureVerifyDialog{

	private static final long serialVersionUID = 1L;

	protected JPanel infoPanel, scenarioPanel;
	protected JTabbedPane selectionPane;
	protected JList scenarioList;

	private Color greyoutColor = Color.LIGHT_GRAY;
	protected Scenario seletedScenario = new Scenario();

	class ScenarioListItem
	{
		private String label;
		private boolean isSelected = false;
		private Scenario scenario;

		public ScenarioListItem(String label, Scenario scenario){
			this.label = label;
			this.scenario = scenario;
		}

		public boolean isSelected(){
			return isSelected;
		}

		public void setSelected(boolean isSelected){
			this.isSelected = isSelected;
		}

		public String toString(){
			return label;
		}

		public Scenario getScenario(){
			return scenario;
		}
	}

	class CheckListRenderer extends JRadioButton implements ListCellRenderer {

		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean isSelected, boolean hasFocus) {

			setSelected(isSelected);
			setFont(list.getFont());

			setBackground(list.getBackground());
			setForeground(list.getForeground());
			setText(value.toString());
			return this;
		}
	}

	@Override
	protected void createSettingPanel(){
		settingPanel = new JPanel(new BorderLayout());
		JPanel leftColumn = new JPanel();
		leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

		settingPanel.setBorder(BorderFactory.createTitledBorder("Setting"));
		highLight = new JCheckBox("Highlight erroneous nodes");
		highLight.setFont(font);
		highLight.setSelected(true);

		leftColumn.add(highLight);

		settingPanel.add(leftColumn, BorderLayout.WEST);
	}
	protected void createInfoPanel(){
		infoPanel = new JPanel(new BorderLayout());
		infoPanel.setBorder(BorderFactory.createTitledBorder("Time Granularity"));
	}

	protected void createSelectionPane(){
		selectionPane = new JTabbedPane();
		selectionPane.setSize(new Dimension(490, 250));
		selectionPane.addTab("Group", groupPanel);
		selectionPane.addTab("Scenario", scenarioPanel);
		selectionPane.addTab("Node", null);

	}

	@SuppressWarnings("unchecked")
	protected void createScenarioPanel(){
		scenarioPanel = new JPanel();

		JLabel label = new JLabel("Scenario items:");
		label.setFont(this.getFont());

		DefaultListModel listModel = new DefaultListModel();

		for(int i=0; i<net.getScenarioList().size(); i++){
			listModel.addElement(new ScenarioListItem("Scenario "+(i+1), net.getScenarioList().get(i)));
		}

		scenarioList = new JList (listModel);
		scenarioList.setCellRenderer(new CheckListRenderer());
		scenarioList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scenarioList.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed (MouseEvent event){
				JList list = (JList) event.getSource();

				int index = list.locationToIndex(event.getPoint());
				try{
					for(int i=0; i<list.getModel().getSize();i++){
						ScenarioListItem item;
						item = (ScenarioListItem)list.getModel().getElementAt(i);
							if(item != null)item.setSelected(false);
					}

					ScenarioListItem item = (ScenarioListItem)list.getModel().getElementAt(index);
					item.setSelected(true);
					seletedScenario = item.getScenario();
					updateColor();
					owner.repaint();
					list.repaint(list.getCellBounds(index, index));
				}catch (ArrayIndexOutOfBoundsException e){}
			}
		});

		JScrollPane listScroller = new JScrollPane(scenarioList);
		listScroller.setPreferredSize(new Dimension(250, 150));
		listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		listScroller.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		listScroller.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 0));

		scenarioPanel.setLayout(new BorderLayout());
		scenarioPanel.add(label, BorderLayout.NORTH);
		scenarioPanel.add(listScroller, BorderLayout.CENTER);
	}

	@Override
	protected void createInterface(){
		createGroupPanel();
		createScenarioPanel();
		createButtonsPanel();
		createSettingPanel();
		createSelectionPane();

		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

		content.add(Box.createRigidArea(new Dimension(500, 15)));
		content.add(selectionPane);
		content.add(Box.createRigidArea(new Dimension(0, 15)));
		content.add(settingPanel);
		content.add(confirmButtonsPanel);

		this.setSize(new Dimension(520, 600));
		this.add(content);
		this.setResizable(false);
		this.pack();
	}

	public TimeConsistencyDialog (Window owner, SON net){
		super(owner, "Time Consistency Setting",  ModalityType.APPLICATION_MODAL, net);
	}

	@Override
	protected String groupPanelTitle(){
		return "";
	}

	protected void updateColor(){
		net.clearMarking();
		setGrayout(net.getNodes(), greyoutColor);
		Collection<Node> nodes = new ArrayList<Node>();
		nodes.addAll(seletedScenario.getNodes(net));
		nodes.addAll(seletedScenario.getConnections(net));
		setGrayout(nodes, Color.BLACK);
	}

	protected void setGrayout(Collection<? extends Node> nodes, Color color){
		for(Node node : nodes){
			net.setForegroundColor(node, color);
		}
	}
}
