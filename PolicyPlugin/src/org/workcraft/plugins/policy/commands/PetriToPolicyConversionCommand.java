package org.workcraft.plugins.policy.commands;

import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.PolicyNetDescriptor;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.policy.tools.PetriToPolicyConverter;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PetriToPolicyConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Policy Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, PetriNet.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualPetriNet srcModel = me.getAs(VisualPetriNet.class);
        final VisualPolicyNet dstModel = new VisualPolicyNet(new PolicyNet());
        final PetriToPolicyConverter converter = new PetriToPolicyConverter(srcModel, dstModel);
        return new ModelEntry(new PolicyNetDescriptor(), converter.getDstModel());
    }

}
