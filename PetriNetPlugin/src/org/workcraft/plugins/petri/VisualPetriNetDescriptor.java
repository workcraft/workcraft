package org.workcraft.plugins.petri;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class VisualPetriNetDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) {
        return new VisualPetriNet((PetriNet)mathModel);
    }

    @Override
    public Iterable<GraphEditorTool> createTools() {
        throw new org.workcraft.exceptions.NotImplementedException();
    }

}
