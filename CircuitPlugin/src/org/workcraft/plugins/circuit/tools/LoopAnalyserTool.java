package org.workcraft.plugins.circuit.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.Icon;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.CircuitUtils;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.util.GUI;

public class LoopAnalyserTool extends AbstractTool {

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
        return GUI.createIconFromSVG("images/tool-loop_analysis.svg");
    }

    @Override
    public Cursor getCursor() {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    }

    @Override
    public String getHintMessage() {
        return "Click on a driven contact to toggle its break path state.";
    }

    @Override
    public void activated(final GraphEditor editor) {
        Circuit circuit = (Circuit) editor.getModel().getMathModel();
        updateState(circuit);
        super.activated(editor);
    }

    @Override
    public void setup(final GraphEditor editor) {
        super.setup(editor);
        editor.getWorkspaceEntry().setCanModify(false);
        editor.getWorkspaceEntry().setCanSelect(false);
        editor.getWorkspaceEntry().setCanCopy(false);
    }

    @Override
    public void deactivated(final GraphEditor editor) {
        loopSet = null;
    }

    private void updateState(Circuit circuit) {
        loopSet = new HashSet<>();
        HashMap<MathNode, HashSet<CircuitComponent>> presets = new HashMap<>();
        for (FunctionComponent component: circuit.getFunctionComponents()) {
            if (component.getPathBreaker()) continue;
            HashSet<CircuitComponent> componentPreset = new HashSet<>();
            for (Contact contact: component.getInputs()) {
                if (!contact.getPathBreaker()) {
                    HashSet<CircuitComponent> contactPreset = CircuitUtils.getComponentPreset(circuit, contact);
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
    public void mouseClicked(GraphEditorMouseEvent e) {
        boolean processed = false;
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        if (e.getButton() == MouseEvent.BUTTON1) {
            VisualNode node = (VisualNode) HitMan.hitTestForSelection(e.getPosition(), model);
            if (node instanceof VisualContact) {
                Contact contact = ((VisualContact) node).getReferencedContact();
                if (contact.isDriven()) {
                    editor.getWorkspaceEntry().saveMemento();
                    contact.setPathBreaker(!contact.getPathBreaker());
                    processed = true;
                }
            } else if (node instanceof VisualCircuitComponent) {
                CircuitComponent component = ((VisualCircuitComponent) node).getReferencedCircuitComponent();
                component.setPathBreaker(!component.getPathBreaker());
                processed = true;
            }
        }
        if (processed) {
            Circuit circuit = (Circuit) editor.getModel().getMathModel();
            updateState(circuit);
        } else {
            super.mouseClicked(e);
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
        final Color color = contact.getPathBreaker() ? CircuitSettings.getInitialisedGateColor()
                : loopSet.contains(contact) ? CircuitSettings.getConflictGateColor() : null;

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
        final Color color = component.getPathBreaker() ? CircuitSettings.getInitialisedGateColor()
                : loopSet.contains(component) ? CircuitSettings.getConflictGateColor() : null;

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
