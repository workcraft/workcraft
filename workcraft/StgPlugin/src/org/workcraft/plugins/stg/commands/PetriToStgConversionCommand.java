package org.workcraft.plugins.stg.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.converters.PetriToStgConverter;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

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
        final VisualPetri petri = me.getAs(VisualPetri.class);
        final VisualStg stg = new VisualStg(new Stg());
        final PetriToStgConverter converter = new PetriToStgConverter(petri, stg);
        return new ModelEntry(new StgDescriptor(), converter.getDstModel());
    }

}
