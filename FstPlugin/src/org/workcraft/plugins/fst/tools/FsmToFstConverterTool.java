package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.workspace.ModelEntry;

public class FsmToFstConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Finite State Transducer";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel().getClass().equals(Fsm.class);
    }

    @Override
    public ModelEntry apply(ModelEntry me) {
        final VisualFsm src = (VisualFsm) me.getVisualModel();
        final VisualFst dst = new VisualFst(new Fst());
        final FsmToFstConverter converter = new FsmToFstConverter(src, dst);
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
