package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionUtils;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.gui.tools.editors.AbstractInplaceEditor;
import org.workcraft.gui.tools.editors.NameInplaceEditor;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.utils.Hierarchy;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.*;

public class CircuitSelectionTool extends SelectionTool {

    private HashMap<VisualConnection, VisualConnection.ScaleMode> connectionToScaleModeMap = null;

    @Override
    public JPopupMenu createPopupMenu(VisualNode node, final GraphEditor editor) {
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
                VisualContact contact = component.createContact(IOType.OUTPUT);
                component.setPositionByDirection(contact, VisualContact.Direction.EAST, false);
            });
            popup.add(addOutputMenuItem);

            JMenuItem addInputMenuItem = new JMenuItem("Add input pin");
            addInputMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                VisualContact contact = component.createContact(IOType.INPUT);
                component.setPositionByDirection(contact, VisualContact.Direction.WEST, false);
            });
            popup.add(addInputMenuItem);
            popup.addSeparator();

            JMenuItem removePinsMenuItem = new JMenuItem("Remove all pins");
            removePinsMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                component.remove(component.getVisualFunctionContacts());
            });
            popup.add(removePinsMenuItem);

            JMenuItem defaultContactPositionMenuItem = new JMenuItem("Set contacts in default position");
            defaultContactPositionMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                component.setContactsDefaultPosition();
            });
            popup.add(defaultContactPositionMenuItem);

            JMenuItem repackContactPositionMenuItem = new JMenuItem("Repack component to fit contact names");
            repackContactPositionMenuItem.setEnabled(component.getRenderingResult() == null);
            repackContactPositionMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                component.repackContactsPosition();
            });
            popup.add(repackContactPositionMenuItem);

            JMenuItem centerPivotPointMenuItem = new JMenuItem("Center pivot point");
            centerPivotPointMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                component.centerPivotPoint(true, true);
            });
            popup.add(centerPivotPointMenuItem);

            JMenuItem removeUnusedPinsMenuItem = new JMenuItem("Remove unused pins");
            removeUnusedPinsMenuItem.addActionListener(event -> {
                editor.getWorkspaceEntry().saveMemento();
                VisualCircuit circuit = (VisualCircuit) editor.getModel();
                CircuitUtils.removeUnusedPins(circuit, component);
            });
            popup.add(removeUnusedPinsMenuItem);
            popup.addSeparator();

            JMenuItem openRefinementStgMenuItem = new JMenuItem("Open refinement STG");
            openRefinementStgMenuItem.setEnabled(RefinementUtils.hasRefinementStg(component));
            openRefinementStgMenuItem.addActionListener(event -> RefinementUtils.openRefinementStg(component));
            popup.add(openRefinementStgMenuItem);

            JMenuItem openRefinementCircuitMenuItem = new JMenuItem("Open refinement circuit");
            openRefinementCircuitMenuItem.setEnabled(RefinementUtils.hasRefinementCircuit(component));
            openRefinementCircuitMenuItem.addActionListener(event -> RefinementUtils.openRefinementCircuit(component));
            popup.add(openRefinementCircuitMenuItem);
        }
        return popup;
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() > 1)) {
            GraphEditor editor = e.getEditor();
            final VisualModel model = editor.getModel();
            VisualNode node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (node instanceof VisualContact) {
                final VisualContact contact = (VisualContact) node;
                if (contact.isPort()) {
                    AbstractInplaceEditor textEditor = new NameInplaceEditor(editor, contact, true);
                    textEditor.edit(contact.getName(), contact.getNameFont(),
                            contact.getNameOffset(), Alignment.CENTER, false);

                    processed = true;
                }
            } else if (node instanceof VisualCircuitComponent) {
                VisualCircuitComponent component = (VisualCircuitComponent) node;
                if (e.isCtrlKeyDown()) {
                    RefinementUtils.openRefinementCircuit(component);
                } else {
                    RefinementUtils.openRefinementModel(component);
                }
                processed = true;
            }
        }
        if (!processed) {
            super.mouseClicked(e);
        }
    }

    @Override
    public Collection<VisualNode> getNodeWithAdjacentConnections(VisualModel model, VisualNode node) {
        HashSet<VisualNode> result = new HashSet<>();
        if (node instanceof VisualCircuitComponent) {
            VisualCircuitComponent component = (VisualCircuitComponent) node;
            result.add(component);
            for (VisualContact inputContact : component.getVisualInputs()) {
                for (VisualConnection connection : model.getConnections(inputContact)) {
                    VisualNode firstNode = connection.getFirst();
                    if (firstNode instanceof VisualReplica) {
                        result.add(connection);
                        result.add(firstNode);
                    }
                }
            }
        } else {
            Queue<VisualNode> queue = new LinkedList<>(super.getNodeWithAdjacentConnections(model, node));
            while (!queue.isEmpty()) {
                node = queue.remove();
                if (result.contains(node)) {
                    continue;
                }
                result.add(node);
                if (node instanceof VisualConnection) {
                    VisualConnection connection = (VisualConnection) node;
                    VisualNode firstNode = connection.getFirst();
                    if (firstNode instanceof VisualJoint) {
                        result.add(firstNode);
                        queue.addAll(model.getConnections(firstNode));
                    } else {
                        for (VisualNode masterOrReplicaNode : super.getNodeWithAdjacentConnections(model, firstNode)) {
                            queue.addAll(model.getConnections(masterOrReplicaNode));
                        }
                    }
                    VisualNode secondNode = connection.getSecond();
                    if (secondNode instanceof VisualJoint) {
                        result.add(secondNode);
                        queue.addAll(model.getConnections(secondNode));
                    }
                }
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
        connectionToScaleModeMap = ConnectionUtils.replaceConnectionScaleMode(includedConnections, VisualConnection.ScaleMode.NONE);
    }

    @Override
    public void afterSelectionModification(final GraphEditor editor) {
        ConnectionUtils.restoreConnectionScaleMode(connectionToScaleModeMap);
        super.afterSelectionModification(editor);
    }

}
