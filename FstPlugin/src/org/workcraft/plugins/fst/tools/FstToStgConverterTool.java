package org.workcraft.plugins.fst.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.fst.Fst;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.workspace.ModelEntry;

public class FstToStgConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof Fst;
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final VisualFst fst = (VisualFst) me.getVisualModel();
        final VisualStg stg = new VisualStg(new Stg());
        final FstToStgConverter converter = new FstToStgConverter(fst, stg);
        return new ModelEntry(new StgDescriptor(), converter.getDstModel());
    }

}
