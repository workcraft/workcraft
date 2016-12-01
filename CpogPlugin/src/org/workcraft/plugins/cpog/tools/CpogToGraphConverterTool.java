package org.workcraft.plugins.cpog.tools;

import org.workcraft.ConversionTool;
import org.workcraft.dom.math.MathModel;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.workspace.ModelEntry;

public class CpogToGraphConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Directed Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        MathModel mathModel = me.getMathModel();
        return mathModel.getClass().equals(Cpog.class);
    }

    @Override
    public ModelEntry apply(ModelEntry me) {
        final VisualCpog cpog = (VisualCpog) me.getVisualModel();
        final VisualGraph graph = new VisualGraph(new Graph());
        final CpogToGraphConverter converter = new CpogToGraphConverter(cpog, graph);
        return new ModelEntry(new CpogDescriptor(), converter.getDstModel());
    }

}
