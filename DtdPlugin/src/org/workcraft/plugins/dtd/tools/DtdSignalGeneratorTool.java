package org.workcraft.plugins.dtd.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.dtd.DtdSettings;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualSignal;

import java.awt.geom.Point2D;
import java.util.LinkedList;

public class DtdSignalGeneratorTool extends NodeGeneratorTool {

    public DtdSignalGeneratorTool() {
        super(new DefaultNodeGenerator(Signal.class) {
            @Override
            public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
                VisualSignal signal = (VisualSignal) super.generate(model, where);
                VisualDtd dtd = (VisualDtd) model;
                dtd.createSignalEntryAndExit(signal);
                spaceVertically(dtd, signal);
                return signal;
            }

            private void spaceVertically(VisualDtd dtd, VisualSignal signal) {
                double yPosition = signal.getY();
                LinkedList<VisualSignal> signalsBelow = new LinkedList<>();
                VisualSignal predecessorSignal = null;
                VisualSignal successorSignal = null;

                Container container = dtd.getCurrentLevel();
                if (container instanceof VisualTransformableNode) {
                    VisualTransformableNode visualNode = (VisualTransformableNode) container;
                    for (VisualComponent visualComp : visualNode.getComponents()) {
                        if (visualComp instanceof VisualSignal) {
                            if (visualComp == signal) continue;
                            VisualSignal visualSignal = (VisualSignal) visualComp;
                            double visualSignalY = visualSignal.getY();
                            if (visualSignalY <= yPosition) {
                                if ((predecessorSignal == null) || (predecessorSignal.getY() < visualSignal.getY())) {
                                    predecessorSignal = visualSignal;
                                }
                            } else {
                                signalsBelow.add(visualSignal);
                                if ((successorSignal == null) || (successorSignal.getY() > visualSignal.getY())) {
                                    successorSignal = visualSignal;
                                }
                            }
                        }
                    }
                }

                double separation = DtdSettings.getVerticalSeparation();

                if (predecessorSignal != null) {
                    Point2D newSignalPosition = new Point2D.Double(signal.getX(), predecessorSignal.getY() + separation);
                    signal.setPosition(newSignalPosition);
                    if (successorSignal != null) {
                        double offset = successorSignal.getY() - (newSignalPosition.getY() + separation);
                        for (VisualSignal visualSignal : signalsBelow) {
                            Point2D position = new Point2D.Double(visualSignal.getX(), visualSignal.getY() - offset);
                            visualSignal.setPosition(position);
                        }
                    }
                } else if (successorSignal != null) {
                    Point2D newSignalPosition = new Point2D.Double(signal.getX(), successorSignal.getY() - separation);
                    signal.setPosition(newSignalPosition);
                }
            }
        });
    }
}
