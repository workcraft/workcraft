package org.workcraft.plugins.stg.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.PetriDescriptor;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.converters.StgToPetriConverter;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

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
                final VisualPetri petri = new VisualPetri(new Petri());
                final StgToPetriConverter converter = new StgToPetriConverter(stg, petri);
                return new ModelEntry(new PetriDescriptor(), converter.getDstModel());
            } finally {
                we.cancelMemento();
            }
        }
        return null;
    }

}
