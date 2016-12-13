package org.workcraft.plugins.fst.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.fsm.Fsm;
import org.workcraft.plugins.fsm.FsmDescriptor;
import org.workcraft.plugins.fsm.VisualFsm;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class FstToFsmConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Finite State Machine";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, VisualFst.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualFst src = WorkspaceUtils.getAs(me, VisualFst.class);
        final VisualFsm dst = new VisualFsm(new Fsm());
        final FstToFsmConverter converter = new FstToFsmConverter(src, dst);
        return new ModelEntry(new FsmDescriptor(), converter.getDstModel());
    }

}
