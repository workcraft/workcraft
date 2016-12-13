package org.workcraft.plugins.policy.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.workspace.ModelEntry;

public class PolicyToPetriConversionCommand extends AbstractConversionCommand {

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
        final PolicyToPetriConverter converter = new PolicyToPetriConverter(visualModel);
        return new ModelEntry(new PetriNetDescriptor(), converter.getPetriNet());
    }

}
