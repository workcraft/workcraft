package org.workcraft.plugins.stg.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.converters.StgToPetriConverter;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class StgToPetriConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Stg.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        for (WorkspaceEntry we: workspace.getWorks()) {
            if (we.getModelEntry() != me) continue;
            we.captureMemento();
            try {
                final VisualStg stg = me.getAs(VisualStg.class);
                final VisualPetriNet petri = new VisualPetriNet(new PetriNet());
                final StgToPetriConverter converter = new StgToPetriConverter(stg, petri);
                return new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
            } finally {
                we.cancelMemento();
            }
        }
        return null;
    }

}
