package org.workcraft.plugins.circuit.tools;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Queue;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Alignment;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.editors.AbstractInplaceEditor;
import org.workcraft.gui.graph.editors.NameInplaceEditor;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualJoint;
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
            JMenuItem addOutputMenuItem = new JMenuItem("Add output pin");
            addOutputMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                VisualCircuit circuit = (VisualCircuit) editor.getModel();
                circuit.getOrCreateContact(component, null, IOType.OUTPUT);
            });
            popup.add(addOutputMenuItem);
            JMenuItem addInputMenuItem = new JMenuItem("Add input pin");
            addInputMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                VisualCircuit circuit = (VisualCircuit) editor.getModel();
                circuit.getOrCreateContact(component, null, IOType.INPUT);
            });
            popup.add(addInputMenuItem);
            popup.addSeparator();
            JMenuItem defaultContactPositionMenuItem = new JMenuItem("Set contacts in default position");
            defaultContactPositionMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                component.setContactsDefaultPosition();
            });
            popup.add(defaultContactPositionMenuItem);
            JMenuItem centerPivotPointMenuItem = new JMenuItem("Center pivot point");
            centerPivotPointMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                component.centerPivotPoint(true, true);
            });
            popup.add(centerPivotPointMenuItem);
        }
        return popup;
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            GraphEditor editor = e.getEditor();
            final VisualModel model = editor.getModel();
            Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualContact) {
                final VisualContact contact = (VisualContact) node;
                if (contact.isPort()) {
                    AbstractInplaceEditor textEditor = new NameInplaceEditor(editor, contact);
                    textEditor.edit(contact.getName(), contact.getNameFont(), contact.getNameOffset(), Alignment.CENTER, false);
                    processed = true;
                }
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    @Override
    public Collection<Node> getNodeWithAdjacentConnections(VisualModel model, Node node) {
        HashSet<Node> result = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            node = queue.remove();
            if (result.contains(node)) {
                continue;
            }
            result.add(node);
            if (node instanceof VisualConnection) {
                VisualConnection connection = (VisualConnection) node;
                VisualNode first = connection.getFirst();
                if (first instanceof VisualJoint) {
                    queue.add(first);
                } else {
                    queue.addAll(model.getConnections(first));
                }
                VisualNode second = connection.getSecond();
                if (second instanceof VisualJoint) {
                    queue.add(second);
                }
            } else if (node instanceof VisualCircuitComponent) {
                VisualCircuitComponent component = (VisualCircuitComponent) node;
                queue.addAll(component.getContacts());
            } else {
                queue.addAll(model.getConnections(node));
            }
        }
        return result;
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
