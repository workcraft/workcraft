package org.workcraft.plugins.cpog.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.cpog.Cpog;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.converters.CpogToGraphConverter;
import org.workcraft.plugins.graph.Graph;
import org.workcraft.plugins.graph.VisualGraph;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class CpogToGraphConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Directed Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, Cpog.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualCpog cpog = (VisualCpog) me.getVisualModel();
        final VisualGraph graph = new VisualGraph(new Graph());
        final CpogToGraphConverter converter = new CpogToGraphConverter(cpog, graph);
        return new ModelEntry(new CpogDescriptor(), converter.getDstModel());
    }

}
