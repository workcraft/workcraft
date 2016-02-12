package org.workcraft.plugins.xmas;

import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.graph.tools.GraphEditorTool;

public class VisualXmasDescriptor implements VisualModelDescriptor {

    @Override
    public VisualModel create(MathModel mathModel) throws VisualModelInstantiationException {
        return new VisualXmas((Xmas) mathModel);
    }

    @Override
    public Iterable<GraphEditorTool> createTools() {
        throw new org.workcraft.exceptions.NotImplementedException();
    }

}
