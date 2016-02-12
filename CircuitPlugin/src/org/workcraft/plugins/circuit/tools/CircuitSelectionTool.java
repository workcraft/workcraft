package org.workcraft.plugins.circuit.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.util.Hierarchy;

public class CircuitSelectionTool extends SelectionTool {

    private HashMap<VisualConnection, ScaleMode> connectionToScaleModeMap = null;

    @Override
    public JPopupMenu createPopupMenu(Node node, final GraphEditor editor) {
        JPopupMenu popup = super.createPopupMenu(node, editor);
        if (node instanceof VisualFunctionComponent) {
            final VisualFunctionComponent component = (VisualFunctionComponent) node;
            if (popup != null) {
                popup.addSeparator();
            } else {
                popup = new JPopupMenu();
                popup.setFocusable(false);
            }
            JMenuItem addOutputMenuItem = new JMenuItem("Add output (EAST)");
            addOutputMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.getWorkspaceEntry().saveMemento();
                    VisualCircuit vcircuit = (VisualCircuit) editor.getModel();
                    vcircuit.getOrCreateContact(component, null, IOType.OUTPUT);
                    component.setContactsDefaultPosition();
                }
            });
            popup.add(addOutputMenuItem);
            JMenuItem addInputMenuItem = new JMenuItem("Add input (WEST)");
            addInputMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.getWorkspaceEntry().saveMemento();
                    VisualCircuit vcircuit = (VisualCircuit) editor.getModel();
                    vcircuit.getOrCreateContact(component, null, IOType.INPUT);
                    component.setContactsDefaultPosition();
                }
            });
            popup.add(addInputMenuItem);
            popup.addSeparator();
            JMenuItem defaultContactPositionMenuItem = new JMenuItem("Set contacts in default position");
            defaultContactPositionMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.getWorkspaceEntry().saveMemento();
                    component.setContactsDefaultPosition();
                }
            });
            popup.add(defaultContactPositionMenuItem);
            JMenuItem centerPivotPointMenuItem = new JMenuItem("Center pivot point");
            centerPivotPointMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.getWorkspaceEntry().saveMemento();
                    component.centerPivotPoint();
                }
            });
            popup.add(centerPivotPointMenuItem);
        }
        return popup;
    }

    @Override
    public void beforeSelectionModification(final GraphEditor editor) {
        super.beforeSelectionModification(editor);
        // FIXME: A hack to preserve the shape of selected connections on relocation of their adjacent components (intro).
        // Flipping/rotation of VisualContacts are processed after ControlPoints of VisualConnections.
        // Therefore the shape of connections may change (e.g. if LOCK_RELATIVELY scale mode is selected).
        // To prevent this, the connection scale mode is temporary changed to NONE, and then restored (in afterSelectionModification).
        VisualModel model = editor.getModel();
        Collection<VisualConnection> connections = Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class);
        Collection<VisualConnection> includedConnections = SelectionHelper.getIncludedConnections(model.getSelection(), connections);
        connectionToScaleModeMap = new HashMap<VisualConnection, ScaleMode>();
        for (VisualConnection vc: includedConnections) {
            connectionToScaleModeMap.put(vc, vc.getScaleMode());
            vc.setScaleMode(ScaleMode.NONE);
        }
    }

    @Override
    public void afterSelectionModification(final GraphEditor editor) {
        if (connectionToScaleModeMap != null) {
            for (Entry<VisualConnection, ScaleMode> entry: connectionToScaleModeMap.entrySet()) {
                VisualConnection vc = entry.getKey();
                ScaleMode scaleMode = entry.getValue();
                vc.setScaleMode(scaleMode);
            }
        }
        super.afterSelectionModification(editor);
    }

}
