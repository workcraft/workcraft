package org.workcraft.plugins.fsm.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class FsmToPetriConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, VisualFsm.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualFsm fsm = WorkspaceUtils.getAs(me, VisualFsm.class);
        final VisualPetriNet petri = new VisualPetriNet(new PetriNet());
        final FsmToPetriConverter converter = new FsmToPetriConverter(fsm, petri);
        return new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
    }

}
