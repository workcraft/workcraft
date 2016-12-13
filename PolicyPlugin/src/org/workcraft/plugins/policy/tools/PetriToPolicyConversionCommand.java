package org.workcraft.plugins.policy.tools;

import org.workcraft.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.PolicyNetDescriptor;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetriToPolicyConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Policy Net";
    }

    @Override
    public boolean isApplicableTo(ModelEntry me) {
        return WorkspaceUtils.isApplicableExact(me, PetriNet.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualPetriNet srcModel = (VisualPetriNet) me.getVisualModel();
        final VisualPolicyNet dstModel = new VisualPolicyNet(new PolicyNet());
        final PetriToPolicyConverter converter = new PetriToPolicyConverter(srcModel, dstModel);
        return new ModelEntry(new PolicyNetDescriptor(), converter.getDstModel());
    }

}
