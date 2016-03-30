package org.workcraft.plugins.circuit.tools;

import java.awt.Color;
import java.util.HashSet;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.circuit.CircuitSettings;

public class InitialisationAnalyserTool extends AbstractTool {

    private HashSet<Node> initHigh;
    private HashSet<Node> initLow;
    private HashSet<Node> initError;

    @Override
    public String getLabel() {
        return "Initialisisation analiser";
    }

    @Override
    public boolean requiresButton() {
        return false;
    }

    public void setState(HashSet<Node> initHigh, HashSet<Node> initLow, HashSet<Node> initError) {
        this.initHigh = initHigh;
        this.initLow = initLow;
        this.initError = initError;
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
                    final boolean b = (initError != null) && initError.contains(mathNode);
                    if ((initHigh != null) && initHigh.contains(mathNode)) {
                        return new Decoration() {
                            @Override
                            public Color getColorisation() {
                                return CircuitSettings.getActiveWireColor();
                            }
                            @Override
                            public Color getBackground() {
                                return b ? CircuitSettings.getInactiveWireColor() : CircuitSettings.getActiveWireColor();
                            }
                        };
                    }
                    if ((initLow != null) && initLow.contains(mathNode)) {
                        return new Decoration() {
                            @Override
                            public Color getColorisation() {
                                return CircuitSettings.getInactiveWireColor();
                            }
                            @Override
                            public Color getBackground() {
                                return b ? CircuitSettings.getActiveWireColor() : CircuitSettings.getInactiveWireColor();
                            }
                        };
                    }
                }

                return null;
            }
        };
    }

}
