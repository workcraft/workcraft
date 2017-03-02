package org.workcraft.plugins.dtd.tools;

import java.awt.geom.Point2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
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
                return signal;
            }
        });
    }

}
