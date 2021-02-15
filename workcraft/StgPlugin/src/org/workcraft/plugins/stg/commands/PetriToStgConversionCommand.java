package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.converters.PetriToStgConverter;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, Petri.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        PetriToStgConverter converter = new PetriToStgConverter(me.getAs(VisualPetri.class));
        return new ModelEntry(new StgDescriptor(), converter.getDstModel());
    }

}
