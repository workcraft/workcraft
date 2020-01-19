package org.workcraft.plugins.son.gui;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.plugins.son.util.ScenarioSaveList;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;

public class ScenarioTable extends JTable {

    private static final long serialVersionUID = 1L;

    private SON net;
    private ScenarioSaveList saveList;
    private ScenarioRef scenarioRef = new ScenarioRef();

    private boolean isCellColorized = true;
    private static final Color greyoutColor = Color.LIGHT_GRAY;

    public ScenarioTable() {
        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setDefaultRenderer(Object.class, new ScenarioTableCellRendererImplementation());
    }

    public SON getNet() {
        return net;
    }

    public void setNet(SON net) {
        this.net = net;
    }

    public ScenarioSaveList getSaveList() {
        return saveList;
    }

    public void setSaveList(ScenarioSaveList saveList) {
        this.saveList = saveList;
    }

    public ScenarioRef getScenarioRef() {
        return scenarioRef;
    }

    public void setScenarioRef(ScenarioRef scenarioRef) {
        this.scenarioRef = scenarioRef;
    }

    @SuppressWarnings("serial")
    protected class ScenarioTableCellRendererImplementation implements TableCellRenderer {

        private final JLabel label = new JLabel() {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
                super.paint(g);
            }
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value instanceof String) {
                label.setText((String) value);
            } else if (value instanceof ScenarioRef) {
                label.setText("Senario " + (row + 1));
            } else {
                return null;
            }

            if (row == saveList.getPosition() && column == 0 && !saveList.isEmpty() && isCellColorized) {
                label.setBackground(Color.PINK);
            } else {
                label.setBackground(Color.WHITE);
            }

            return label;
        }
    }

    public void updateTable() {
        tableChanged(new TableModelEvent(getModel()));
    }

    public void updateColor(Node excludeNodes) {
        net.clearMarking();
        setColors(net.getNodes(), excludeNodes, greyoutColor);
        Collection<Node> nodes = new ArrayList<>();
        nodes.addAll(getScenarioRef().getNodes(net));
        nodes.addAll(getScenarioRef().getConnections(net));
        setColors(nodes, excludeNodes, Color.BLACK);
    }

    public void runtimeUpdateColor() {
        net.clearMarking();
        setColors(net.getNodes(), greyoutColor);
        Collection<Node> nodes = new ArrayList<>();
        nodes.addAll(getScenarioRef().getNodes(net));
        nodes.addAll(getScenarioRef().getRuntimeConnections(net));
        setColors(nodes, Color.BLACK);
    }

    private void setColors(Collection<? extends Node> nodes, Color color) {
        for (Node node : nodes) {
            net.setForegroundColor(node, color);
        }
    }

    private void setColors(Collection<? extends Node> nodes, Node excludeNodes, Color color) {
        for (Node node : nodes) {
            if (node != excludeNodes) {
                net.setForegroundColor(node, color);
            }
        }
    }

    public ArrayList<String> getScenarioNodeRef() {
        ArrayList<String> result = new ArrayList<>();
        for (String str : scenarioRef.getNodeRefs(net)) {
            result.add(str);
        }
        return result;
    }

    public boolean isCellColor() {
        return isCellColorized;
    }

    public void setIsCellColor(boolean setCellColor) {
        this.isCellColorized = setCellColor;
    }
}
