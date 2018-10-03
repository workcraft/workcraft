package org.workcraft.plugins.dtd.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
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
                double y = signal.getY();
                VisualSignal aboveSignal = null;
                VisualSignal belowSignal = null;
                LinkedList<VisualSignal> belowSignals = new LinkedList<>();
                Container container = (Container) signal.getParent();
                for (VisualSignal otherSignal : dtd.getVisualSignals(container)) {
                    if (signal == otherSignal) continue;
                    if (otherSignal.getY() <= y) {
                        if ((aboveSignal == null) || (aboveSignal.getY() < otherSignal.getY())) {
                            aboveSignal = otherSignal;
                        }
                    } else {
                        belowSignals.add(otherSignal);
                        if ((belowSignal == null) || (belowSignal.getY() > otherSignal.getY())) {
                            belowSignal = otherSignal;
                        }
                    }
                }

                Double dy = DtdSettings.getVerticalSeparation();
                if (aboveSignal != null) {
                    signal.setY(aboveSignal.getY() + dy);
                    if (belowSignal != null) {
                        double offset = belowSignal.getY() - signal.getY() - dy;
                        System.out.println(offset);
                        for (VisualSignal otherSignal : belowSignals) {
                            otherSignal.setY(otherSignal.getY() - offset);
                        }
                    }
                } else if (belowSignal != null) {
                    signal.setY(belowSignal.getY() - dy);
                }
            }
        });
    }

}
