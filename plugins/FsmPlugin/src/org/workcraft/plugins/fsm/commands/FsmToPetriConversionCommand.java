package org.workcraft.plugins.fsm.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.converters.FsmToPetriConverter;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.PetriDescriptor;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class FsmToPetriConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualFsm.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualFsm fsm = me.getAs(VisualFsm.class);
        final VisualPetri petri = new VisualPetri(new Petri());
        final FsmToPetriConverter converter = new FsmToPetriConverter(fsm, petri);
        return new ModelEntry(new PetriDescriptor(), converter.getDstModel());
    }

}
