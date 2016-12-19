package org.workcraft.plugins.fsm.commands;

import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fsm.converters.FsmToPetriConverter;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

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
        final VisualPetriNet petri = new VisualPetriNet(new PetriNet());
        final FsmToPetriConverter converter = new FsmToPetriConverter(fsm, petri);
        return new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
    }

}
