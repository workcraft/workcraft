package org.workcraft.plugins.stg.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetriToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, PetriNet.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualPetriNet petri = (VisualPetriNet) me.getVisualModel();
        final VisualStg stg = new VisualStg(new Stg());
        final PetriToStgConverter converter = new PetriToStgConverter(petri, stg);
        return new ModelEntry(new StgDescriptor(), converter.getDstModel());
    }

}
