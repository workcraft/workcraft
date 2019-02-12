package org.workcraft.plugins.son.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

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

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.algorithm.Path;
import org.workcraft.plugins.son.algorithm.SimulationAlg;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.plugins.son.util.Step;

@SuppressWarnings("rawtypes")
public class ParallelSimDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final SON net;

    boolean isRev = false;
    private static final Color selectedColor = new Color(255, 228, 181);
    private final Step possibleFire, minFire;
    private final TransitionNode clickedEvent;

    private JPanel eventPanel, buttonsPanel, eventInfoPanel;
    protected JScrollPane infoPanel;
    Collection<Path> sync;

    protected Dimension buttonSize = new Dimension(80, 25);

    private final HashSet<TransitionNode> selectedEvents = new HashSet<>();

    private int run = 0;
    private Window owner;

    class EventItem {
        private final String label;
        private boolean isSelected = false;
        private final TransitionNode event;

        EventItem(String label, TransitionNode event, List<TransitionNode> postEvents) {
            this.label = label;
            this.event = event;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        public String toString() {
            return label;
        }

        public TransitionNode getEvent() {
            return event;
        }

        public void setForegroudColor(Color color) {
            event.setForegroundColor(color);
        }

        public void setFillColor(Color color) {
            event.setFillColor(color);
        }
    }

    class CheckListRenderer extends JCheckBox implements ListCellRenderer {

        private static final long serialVersionUID = 1L;

        public Component getListCellRendererComponent(
                JList list, Object value, int index,
                boolean isSelected, boolean hasFocus) {

            setEnabled(list.isEnabled());
            setSelected(((EventItem) value).isSelected());
            setFont(list.getFont());

            setBackground(list.getBackground());
            setForeground(list.getForeground());
            setText(value.toString());
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    private void createEventItemsPanel() {

        eventPanel = new JPanel();

        DefaultListModel listModel = new DefaultListModel();

        for (TransitionNode event : this.possibleFire) {
            EventItem item = new EventItem(net.getNodeReference(event) + "  " + event.getLabel(), event, possibleFire);
            listModel.addElement(item);
        }

        JList eventList = new JList(listModel);
        eventList.setCellRenderer(new CheckListRenderer());
        eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        eventList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                JList list = (JList) event.getSource();

                int index = list.locationToIndex(event.getPoint());
                try {
                    EventItem item = (EventItem) list.getModel().getElementAt(index);
                    item.setSelected(!item.isSelected());

                    ArrayList<EventItem> itemList = new ArrayList<>();
                    for (int i = 0; i < list.getModel().getSize(); i++) {
                        itemList.add((EventItem) list.getModel().getElementAt(i));
                    }

                    if (item instanceof EventItem) {
                        SimulationAlg simuAlg = new SimulationAlg(net);
                        if (item.isSelected()) {
                            selectedEvents.add(item.getEvent());

                            Step minFire = simuAlg.getMinFire(item.getEvent(), sync, possibleFire, isRev);

                            for (TransitionNode e : minFire) {
                                for (EventItem eventItem : itemList) {
                                    if (e == eventItem.getEvent()) {
                                        selectedEvents.add(e);
                                        eventItem.setSelected(true);
                                        eventItem.setFillColor(selectedColor);
                                    }
                                }
                            }
                            item.setFillColor(selectedColor);
                        }

                        if (!item.isSelected()) {
                            selectedEvents.remove(item.getEvent());

                            Step minFire = simuAlg.getMinFire(item.getEvent(), sync, possibleFire, !isRev);

                            //unselected related synchronous events.
                            for (TransitionNode e : minFire) {
                                for (EventItem eventItem : itemList) {
                                    if (e == eventItem.getEvent()) {
                                        selectedEvents.remove(e);
                                        eventItem.setSelected(false);
                                        eventItem.setFillColor(Color.WHITE);
                                    }
                                }
                            }

                            item.setFillColor(Color.WHITE);
                        }

                        for (int i = 0; i < list.getModel().getSize(); i++) {
                            list.repaint(list.getCellBounds(i, i));
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) { }
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
        eventPanel.setBorder(SizeHelper.getTitledBorder("Possible parallel execution:"));
    }

    private void createButtonsPanel() {
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton runButton = new JButton("Run");
        runButton.setPreferredSize(buttonSize);
        runButton.addActionListener(event -> {
            run = 1;
            net.refreshAllColor();
            setVisible(false);
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(buttonSize);
        cancelButton.addActionListener(event -> {
            run = 2;
            net.refreshAllColor();
            setVisible(false);
        });

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(runButton);
    }

    private void createEventInfoPanel() {
        eventInfoPanel = new JPanel();

        String[] colNames = {"Name", "Label"};

        JTable table = new JTable(createData(), colNames);
        table.setEnabled(false);
        TableColumn firsetColumn = table.getColumnModel().getColumn(0);
        firsetColumn.setPreferredWidth(12);

        eventInfoPanel.setLayout(new BoxLayout(eventInfoPanel, BoxLayout.Y_AXIS));
        eventInfoPanel.setBorder(SizeHelper.getTitledBorder("Parallel execution:"));
        eventInfoPanel.add(table);
    }

    private String[][] createData() {
        String[][] dataVal = new String[this.minFire.size() + 1][2];

        dataVal[0][0] = net.getNodeReference(clickedEvent) + "(clicked)";
        dataVal[0][1] = this.clickedEvent.getLabel();

        if (!minFire.isEmpty()) {
            for (int i = 1; i < minFire.size() + 1; i++) {
                dataVal[i][0] = net.getNodeReference(minFire.get(i - 1));
            }
            for (int i = 1; i < minFire.size() + 1; i++) {
                dataVal[i][1] = minFire.get(i - 1).getLabel();
            }
        }

        return dataVal;

    }

    public ParallelSimDialog(Window owner, SON net,
            Step possibleFire, Step minFire,
            TransitionNode event, boolean isRev,
            Collection<Path> sync) {
        super(owner, "Parallel Execution Setting", ModalityType.TOOLKIT_MODAL);

        this.net = net;
        this.isRev = isRev;
        this.possibleFire = possibleFire;
        this.minFire = minFire;
        this.clickedEvent = event;
        this.sync = sync;

        setColor(minFire, event);

        //this.setSize(new Dimension(280, 260));
        createButtonsPanel();
        createEventItemsPanel();
        createEventInfoPanel();

        JPanel interfacePanel = new JPanel(new BorderLayout(10, 10));
        interfacePanel.add(eventInfoPanel, BorderLayout.NORTH);
        interfacePanel.add(eventPanel, BorderLayout.CENTER);
        interfacePanel.add(buttonsPanel, BorderLayout.SOUTH);

        this.add(interfacePanel);
        this.pack();

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                getSONModel().refreshAllColor();
            }
        });

        this.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                if (run == 0) {
                    setVisible(true);
                }
            }
        });

    }

    private void setColor(List<TransitionNode> preEvents, TransitionNode event) {
        event.setFillColor(selectedColor);
        for (TransitionNode e : preEvents) {
            e.setFillColor(selectedColor);
        }
    }

    public SON getSONModel() {
        return this.net;
    }

    public HashSet<TransitionNode> getSelectedEvent() {
        return this.selectedEvents;
    }

    public Window getOwner() {
        return this.owner;
    }

    public int getRun() {
        return run;
    }

}
