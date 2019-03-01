package org.workcraft.plugins.policy.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNetDescriptor;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.policy.tools.PolicyToPetriConverter;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class PolicyToPetriConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Petri Net";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, PolicyNet.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualPolicyNet visualModel = me.getAs(VisualPolicyNet.class);
        final PolicyToPetriConverter converter = new PolicyToPetriConverter(visualModel);
        return new ModelEntry(new PetriNetDescriptor(), converter.getPetriNet());
    }

}
