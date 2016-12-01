package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.workspace.ModelEntry;

public class FstToFsmConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Finite State Machine";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel().getClass().equals(Fst.class);
    }

    @Override
    public ModelEntry apply(ModelEntry me) {
        final VisualFst src = (VisualFst) me.getVisualModel();
        final VisualFsm dst = new VisualFsm(new Fsm());
        final FstToFsmConverter converter = new FstToFsmConverter(src, dst);
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
