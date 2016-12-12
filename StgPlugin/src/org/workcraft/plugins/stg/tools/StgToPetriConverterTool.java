package org.workcraft.plugins.stg.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;

public class StgToPetriConverterTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public Position getPosition() {
        return null;
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicable(me, Stg.class);
    }

    @Override
    public ModelEntry run(ModelEntry me) {
        final VisualStg stg = (VisualStg) me.getVisualModel();
        final VisualPetriNet petri = new VisualPetriNet(new PetriNet());
        final StgToPetriConverter converter = new StgToPetriConverter(stg, petri);
        return new ModelEntry(new PetriNetDescriptor(), converter.getDstModel());
    }

}
