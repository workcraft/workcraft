package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractGraphEditorTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.StructureUtilsKt;
import org.workcraft.util.GUI;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class LoopAnalyserTool extends AbstractGraphEditorTool {

    private HashSet<Node> loopSet;

    @Override
    public String getLabel() {
        return "Loop analyser";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_L;
    }

    @Override
    public Icon getIcon() {
        return GUI.createIconFromSVG("images/circuit-tool-loop_analysis.svg");
    }

    @Override
    public Cursor getCursor(boolean menuKeyDown, boolean shiftKeyDown, boolean altKeyDown) {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click on a driven contact to toggle its break path state.";
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        updateState(circuit);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        super.deactivated(editor);
        loopSet = null;
    }

    @Override
    public void setPermissions(final GraphEditor editor) {
        WorkspaceEntry we = editor.getWorkspaceEntry();
        we.setCanModify(false);
        we.setCanSelect(false);
        we.setCanCopy(false);
    }

    @Override
    public boolean requiresPropertyEditor() {
        return true;
    }

    private void updateState(Circuit circuit) {
        loopSet = new HashSet<>();
        HashMap<MathNode, HashSet<CircuitComponent>> presets = new HashMap<>();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            if (component.getPathBreaker()) continue;
            HashSet<CircuitComponent> componentPreset = new HashSet<>();
            for (Contact contact: component.getInputs()) {
                if (!contact.getPathBreaker()) {
                    HashSet<CircuitComponent> contactPreset = StructureUtilsKt.getPresetComponents(circuit, contact);
                    componentPreset.addAll(contactPreset);
                    presets.put(contact, contactPreset);
                }
            }
            presets.put(component, componentPreset);
        }
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            for (Contact contact: component.getInputs()) {
                HashSet<CircuitComponent> contactPreset = presets.get(contact);
                if (contactPreset == null) continue;
                HashSet<CircuitComponent> visited = new HashSet<>();
                Queue<CircuitComponent> queue = new LinkedList<>(contactPreset);
                while (!queue.isEmpty()) {
                    CircuitComponent predComponent = queue.remove();
                    if (visited.contains(predComponent)) continue;
                    visited.add(predComponent);
                    if (predComponent == component) {
                        loopSet.add(component);
                        loopSet.add(contact);
                        break;
                    }
                    HashSet<CircuitComponent> componentPreset = presets.get(predComponent);
                    if (componentPreset != null) {
                        queue.addAll(componentPreset);
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        boolean processed = false;
        GraphEditor editor = e.getEditor();
        VisualModel model = e.getModel();
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualNode node = (VisualNode) HitMan.hitDeepest(e.getPosition(), editor.getModel());
            if (node instanceof VisualContact) {
                Contact contact = ((VisualContact) node).getReferencedContact();
                if (contact.isDriven()) {
                    editor.getWorkspaceEntry().saveMemento();
                    contact.setPathBreaker(!contact.getPathBreaker());
                    processed = true;
                }
            } else if (node instanceof VisualCircuitComponent) {
                CircuitComponent component = ((VisualCircuitComponent) node).getReferencedComponent();
                component.setPathBreaker(!component.getPathBreaker());
                processed = true;
            }
        }
        if (processed) {
            Circuit circuit = (Circuit) model.getMathModel();
            updateState(circuit);
        } else {
            super.mousePressed(e);
        }
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {
            @Override
            public Decoration getDecoration(Node node) {
                Node mathNode = null;
                if (node instanceof VisualComponent) {
                    mathNode = ((VisualComponent) node).getReferencedComponent();
                } else if (node instanceof VisualConnection) {
                    mathNode = ((VisualConnection) node).getReferencedConnection();
                }

                if (mathNode != null) {
                    if (mathNode instanceof Contact) {
                        return getContactDecoration((Contact) mathNode);
                    }
                    if (mathNode instanceof FunctionComponent) {
                        return getComponentDecoration((FunctionComponent) mathNode);
                    }
                }
                return (mathNode instanceof Contact) ? StateDecoration.Empty.INSTANCE : null;
            }
        };
    }

    private Decoration getContactDecoration(Contact contact) {
        final Color color = contact.getPathBreaker() ? CircuitSettings.getPropagatedInitGateColor()
                : loopSet.contains(contact) ? CircuitSettings.getConflictInitGateColor() : null;

        return new Decoration() {
            @Override
            public Color getColorisation() {
                return color;
            }
            @Override
            public Color getBackground() {
                return color;
            }
        };
    }

    private Decoration getComponentDecoration(FunctionComponent component) {
        final Color color = component.getPathBreaker() ? CircuitSettings.getPropagatedInitGateColor()
                : loopSet.contains(component) ? CircuitSettings.getConflictInitGateColor() : null;

        return new Decoration() {
            @Override
            public Color getColorisation() {
                return color;
            }
            @Override
            public Color getBackground() {
                return color;
            }
        };
    }

}
