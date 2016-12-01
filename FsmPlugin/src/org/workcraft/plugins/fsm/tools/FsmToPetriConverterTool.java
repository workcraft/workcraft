package org.workcraft.plugins.fsm.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.workspace.ModelEntry;

public class FsmToPetriConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel().getClass().equals(Fsm.class);
    }

    @Override
    public ModelEntry apply(ModelEntry me) {
        final VisualFsm fsm = (VisualFsm) me.getVisualModel();
        final VisualPetriNet petri = new VisualPetriNet(new PetriNet());
        final FsmToPetriConverter converter = new FsmToPetriConverter(fsm, petri);
        return new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
    }

}
