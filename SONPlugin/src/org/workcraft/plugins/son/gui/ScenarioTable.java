package org.workcraft.plugins.son.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.workcraft.dom.Node;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.util.ScenarioRef;
import org.workcraft.plugins.son.util.ScenarioSaveList;

public class ScenarioTable extends JTable{

    private static final long serialVersionUID = 1L;

    protected GraphEditor editor;
    protected ScenarioSaveList saveList;

    protected SON net;
    protected ScenarioRef scenarioRef = new ScenarioRef();
    protected ScenarioRef scenarioNodeRef = new ScenarioRef();

    private boolean isCellColorized = true;
    private Color greyoutColor = Color.LIGHT_GRAY;

    public ScenarioTable(ScenarioSaveList saveList, GraphEditor editor) {
        this(saveList, editor, null, null);
    }

    public ScenarioTable(ScenarioSaveList saveList, GraphEditor editor, TableModel model) {
        this(saveList, editor, model, null);
    }

    public ScenarioTable(ScenarioSaveList saveList, GraphEditor editor, TableModel model, Node selection) {
        this.editor = editor;
        this.saveList = saveList;
        net = (SON) editor.getModel().getMathModel();

        if(!saveList.isEmpty()){
            //get scenario node refs without connection refs
            scenarioRef.addAll(saveList.get(0));
            updateColor(selection);
        }

        if(model == null)
            this.setModel(new ScenarioTableModel());
        else
            this.setModel(model);

        this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.setDefaultRenderer(Object.class, new ScenarioTableCellRendererImplementation());
    }

    @SuppressWarnings("serial")
    protected class ScenarioTableCellRendererImplementation implements TableCellRenderer {

        JLabel label = new JLabel() {
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
            if(value instanceof String)
                label.setText((String) value);
            else if(value instanceof ScenarioRef){
                label.setText("Senario "+(row+1));
            } else
                return null;

            if (row == saveList.getPosition() && column == 0 && !saveList.isEmpty() && isCellColorized) {
                label.setBackground(Color.PINK);
            }else {
                label.setBackground(Color.WHITE);
            }

            return label;
        }
    }

    @SuppressWarnings("serial")
    protected class ScenarioTableModel extends AbstractTableModel {
        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0) return "Save List";
            return "Scenario";
        }

        @Override
        public int getRowCount() {
            return Math.max(saveList.size(), getScenarioNodeRef().size());
        }

        @Override
        public Object getValueAt(int row, int column) {
            if (column == 0) {
                if (!saveList.isEmpty() && (row < saveList.size())) {
                    return saveList.get(row);
                }
            } else {
                ArrayList<String> nodes = getScenarioNodeRef();
                if (!nodes.isEmpty() && (row < nodes.size())) {
                    return nodes.get(row);
                }
            }
            return "";
        }
    }

    public void updateTable(final GraphEditor editor) {
        tableChanged(new TableModelEvent(getModel()));
    }

    public void updateColor(){
        net.clearMarking();
        setColors(net.getNodes(), greyoutColor);
        Collection<Node> nodes = new ArrayList<Node>();
        nodes.addAll(getScenarioRef().getNodes(net));
        nodes.addAll(getScenarioRef().getConnections(net));
        setColors(nodes, Color.BLACK);
    }

    public void updateColor(Node exclude){
        net.clearMarking();
        setColors(net.getNodes(), exclude, greyoutColor);
        Collection<Node> nodes = new ArrayList<Node>();
        nodes.addAll(getScenarioRef().getNodes(net));
        nodes.addAll(getScenarioRef().getConnections(net));
        setColors(nodes, exclude, Color.BLACK);
    }

    public void runtimeUpdateColor(){
        net.clearMarking();
        setColors(net.getNodes(), greyoutColor);
        Collection<Node> nodes = new ArrayList<Node>();
        nodes.addAll(getScenarioRef().getNodes(net));
        nodes.addAll(getScenarioRef().getRuntimeConnections(net));
        setColors(nodes, Color.BLACK);
    }

    private void setColors(Collection<? extends Node> nodes, Color color){
        for(Node node : nodes){
            net.setForegroundColor(node, color);
        }
    }

    private void setColors(Collection<? extends Node> nodes, Node exclude, Color color){
        for(Node node : nodes){
            if(node != exclude)
                net.setForegroundColor(node, color);
        }
    }

    public ScenarioRef getScenarioRef() {
        return scenarioRef;
    }

    public void setScenarioRef(ScenarioRef scenarioRef) {
        this.scenarioRef = scenarioRef;
    }

    public ArrayList<String> getScenarioNodeRef() {
        ArrayList<String> result = new ArrayList<String>();
        for(String str : scenarioRef.getNodeRefs(net)){
            result.add(str);
        }
        return result;
    }

    public ScenarioSaveList getSaveList() {
        return saveList;
    }

    public void setSaveList(ScenarioSaveList saveList) {
        this.saveList = saveList;
    }

    public boolean isCellColor() {
        return isCellColorized;
    }

    public void setIsCellColor(boolean setCellColor) {
        this.isCellColorized = setCellColor;
    }
}
