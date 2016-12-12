package org.workcraft.plugins.policy.tools;

import org.workcraft.ConversionTool;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.workspace.ModelEntry;

public class PetriNetGeneratorTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return me.getMathModel() instanceof PolicyNet;
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualPolicyNet visualModel = (VisualPolicyNet) me.getVisualModel();
        final PetriNetGenerator generator = new PetriNetGenerator(visualModel);
        return new ModelEntry(new PetriNetDescriptor(), generator.getPetriNet());
    }

}
