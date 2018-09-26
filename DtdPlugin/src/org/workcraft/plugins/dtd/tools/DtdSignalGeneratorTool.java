package org.workcraft.plugins.dtd.tools;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.*;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.dtd.Signal;
import org.workcraft.plugins.dtd.VisualDtd;
import org.workcraft.plugins.dtd.VisualSignal;

public class DtdSignalGeneratorTool extends NodeGeneratorTool {

    public DtdSignalGeneratorTool() {
        super(new DefaultNodeGenerator(Signal.class) {
            @Override
            public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
                VisualSignal signal = (VisualSignal) super.generate(model, where);
                VisualDtd dtd = (VisualDtd) model;
                dtd.createSignalEntryAndExit(signal);

                spaceVertically(signal, dtd);

                return signal;
            }

            private void spaceVertically(VisualSignal signal, VisualDtd dtd) {
                double yPosition = signal.getY();
                LinkedList<VisualSignal> signalsBelow = new LinkedList<>();
                VisualSignal predecessorSignal = null;
                VisualSignal successorSignal = null;

                Container container = dtd.getCurrentLevel();
                if (container instanceof VisualPage) { //should be visualWaveform
                    VisualPage visualWaveform = (VisualPage) container;
                    for (VisualComponent visualComp : visualWaveform.getComponents()) {
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

                double separation = 1.0; //this should be a constant

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
