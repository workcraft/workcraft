package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;

import org.workcraft.dom.Node;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.algorithm.SimulationAlg;
import org.workcraft.plugins.son.elements.Event;

public class ParallelSimDialog  extends JDialog{

	private static final long serialVersionUID = 1L;

	private SONModel net;
	protected SimulationAlg alg;

	boolean reverse = false;
	private List<Event> possibleEvents, minimalEvents;
	private Event clickedEvent;

	private JPanel eventPanel, interfacePanel, buttonsPanel, eventInfoPanel;
	private JButton runButton, cancelButton;
	protected JScrollPane infoPanel;
	private JList eventList;

	private HashSet<Event> selectedEvents = new HashSet<Event>();
	private Collection<ArrayList<Node>> sync;
	private Collection<Event> enabledEvents;

	private int run = 0;
	private Window owner;

	class EventItem
	{
		private String label;
		private boolean isSelected = false;
		private Event event;

		public EventItem(String label, Event event, List<Event> postEvents){
			this.label = label;
			this.event = event;
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

		public Event getEvent(){
			return event;
		}

		public void setForegroudColor(Color color){
			event.setForegroundColor(color);
		}

		public void setFillColor(Color color){
			event.setFillColor(color);
		}
	}

	@SuppressWarnings("rawtypes")
	class CheckListRenderer extends JCheckBox implements ListCellRenderer {

		private static final long serialVersionUID = 1L;

		public Component getListCellRendererComponent(
				JList list, Object value, int index,
				boolean isSelected, boolean hasFocus) {

			setEnabled(list.isEnabled());
			setSelected(((EventItem)value).isSelected());
			setFont(list.getFont());

			setBackground(list.getBackground());
			setForeground(list.getForeground());
			setText(value.toString());
			return this;
		}
	}

	@SuppressWarnings("unchecked")
	private void createEventItemsPanel(){

		eventPanel = new JPanel();

		DefaultListModel listModel = new DefaultListModel();

		for(Event event : this.possibleEvents){
			EventItem item = new EventItem(net.getName(event)+"  "+event.getLabel(), event, possibleEvents);
			listModel.addElement(item);
		}


		eventList = new JList (listModel);
		eventList.setCellRenderer(new CheckListRenderer());
		eventList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION);

		eventList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent event)
			{
				@SuppressWarnings("rawtypes")
				JList list = (JList) event.getSource();

				int index = list.locationToIndex(event.getPoint());
				try{
						EventItem item = (EventItem)list.getModel().getElementAt(index);
						item.setSelected(!item.isSelected());

						ArrayList<EventItem> itemList = new ArrayList<EventItem>();
						for(int i=0; i<list.getModel().getSize(); i++){
							itemList.add((EventItem)list.getModel().getElementAt(i));
						}

						if(item instanceof EventItem){
							if(item.isSelected() ){
								selectedEvents.add(item.getEvent());
								List<Event> set;
								if(!reverse)
									set = alg.getMinimalExeResult(item.getEvent(), sync, enabledEvents);
								else
									set = alg.getMinimalReverseExeResult(item.getEvent(), sync, enabledEvents);

									for(Event e : set){
										for(EventItem eventItem : itemList){
											if(e==eventItem.getEvent()){
												selectedEvents.add(e);
												eventItem.setSelected(true);
												eventItem.setForegroudColor(Color.BLUE);
											}
										}
									}
								item.setForegroudColor(Color.BLUE);
								alg.clearEventSet();
							}

							if(!item.isSelected() ){
								selectedEvents.remove(item.getEvent());
								if(!reverse){
									for(Event e : alg.getPostExeResult(item.getEvent(), sync, enabledEvents)){
										for(EventItem eventItem : itemList){
											if(e==eventItem.getEvent()){
												selectedEvents.remove(e);
												eventItem.setSelected(false);
												eventItem.setForegroudColor(CommonVisualSettings.getEnabledForegroundColor());
											}
										}
									}
								}else{
									for(Event e : alg.getPreExeResult(item.getEvent(), sync, enabledEvents)){
										for(EventItem eventItem : itemList){
											if(e==eventItem.getEvent()){
												selectedEvents.remove(e);
												eventItem.setSelected(false);
												eventItem.setForegroudColor(CommonVisualSettings.getEnabledForegroundColor());
											}
										}
									}
								}
								item.setForegroudColor(CommonVisualSettings.getEnabledForegroundColor());
								alg.clearEventSet();
							}

							for(int i=0; i<list.getModel().getSize(); i++)
								list.repaint(list.getCellBounds(i, i));
						}
				}catch (ArrayIndexOutOfBoundsException e){}
			}
	});

		eventList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		JScrollPane listScroller = new JScrollPane(eventList);
		listScroller.setPreferredSize(new Dimension(250, 150));
		listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		listScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		listScroller.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
		listScroller.getHorizontalScrollBar().setPreferredSize(new Dimension(12, 0));

		eventPanel.setPreferredSize(new Dimension(250, 150));
		eventPanel.setLayout(new BorderLayout());
		eventPanel.add(listScroller, BorderLayout.CENTER);
		eventPanel.setBorder(BorderFactory.createTitledBorder("Possible parallel execution:"));
	}

	private void createButtonsPanel() {
		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		runButton = new JButton ("Run");
		runButton.setSize(30, 20);
		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				run = 1;
				net.refreshColor();
				setVisible(false);
			}
		});

		cancelButton = new JButton ("Cancel");
		cancelButton.setSize(30, 20);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				run = 2;
				net.refreshColor();
				setVisible(false);
			}
		});

		buttonsPanel.add(cancelButton);
		buttonsPanel.add(runButton);
	}

	private void createEventInfoPanel(){
		this.eventInfoPanel = new JPanel();
		eventInfoPanel.setLayout(new BoxLayout(eventInfoPanel, BoxLayout.Y_AXIS));

		String colNames[] = {"Name", "Label"};

		JTable table = new JTable(createData(), colNames);
		table.setEnabled(false);
		TableColumn firsetColumn = table.getColumnModel().getColumn(0);
		firsetColumn.setPreferredWidth(12);

		eventInfoPanel.add(table);
		eventInfoPanel.setBorder(BorderFactory.createTitledBorder("Parallel execution:"));

	}

	private String[][] createData(){
		String dataVal[][] = new String[this.minimalEvents.size()+1][2];

		dataVal[0][0] = net.getName(clickedEvent)+ "(clicked)";
		dataVal[0][1] = this.clickedEvent.getLabel();

		if(!this.minimalEvents.isEmpty()){
			for(int i=1 ; i<this.minimalEvents.size()+1; i++)
				dataVal[i][0]=net.getName(this.minimalEvents.get(i-1));
			for(int i=1 ; i<this.minimalEvents.size()+1; i++)
				dataVal[i][1]=this.minimalEvents.get(i-1).getLabel();
		}

		return dataVal;

	}

	public  ParallelSimDialog (Window owner, SONModel net, List<Event> possibleEvents, List<Event> minimalEvents, Event event, Collection<ArrayList<Node>> sync, Collection<Event> enabledEvents, boolean reverse){
		super(owner, "Parallel Execution Setting", ModalityType.TOOLKIT_MODAL);

		alg = new SimulationAlg(net);

		this.net = net;
		this.reverse = reverse;
		this.possibleEvents = possibleEvents;
		this.minimalEvents = minimalEvents;
		this.clickedEvent = event;
		this.sync = sync;
		this.enabledEvents = enabledEvents;

		setEventsColor(minimalEvents, event);

		//this.setSize(new Dimension(280, 260));
		createButtonsPanel();
		createEventItemsPanel();
		createEventInfoPanel();

		interfacePanel = new JPanel(new BorderLayout(10, 10));
		interfacePanel.add(eventInfoPanel, BorderLayout.NORTH);
		interfacePanel.add(eventPanel,BorderLayout.CENTER);
		interfacePanel.add(buttonsPanel, BorderLayout.SOUTH);

		this.add(interfacePanel);
		this.pack();

		this.addWindowListener(new WindowAdapter()
		{
		  public void windowClosing(WindowEvent e)
		  {
			  getSONModel().refreshColor();
		  }
		});

		this.addWindowFocusListener(new WindowFocusListener()
		{
			@Override
			public void windowGainedFocus(WindowEvent e) {
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				if(run==0)
					setVisible(true);
			}
		});

	}

	private void setEventsColor(List<Event> preEvents, Event event){
		event.setForegroundColor(Color.BLUE);
		for(Event e : preEvents)
			e.setForegroundColor(Color.BLUE);
	}

	public SONModel getSONModel(){
		return this.net;
	}

	public HashSet<Event> getSelectedEvent(){
		return this.selectedEvents;
	}

	public Window getOwner(){
		return this.owner;
	}

	public int getRun(){
		return run;
	}

}
