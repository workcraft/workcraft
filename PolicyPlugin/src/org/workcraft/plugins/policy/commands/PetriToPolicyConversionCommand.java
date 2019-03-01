package org.workcraft.plugins.policy.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.PolicyNetDescriptor;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.policy.tools.PetriToPolicyConverter;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

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
        if (Hierarchy.isHierarchical(me)) {
            DialogUtils.showError("Policy Net cannot be derived from a hierarchical Petri Net.");
            return null;
        }
        final VisualPetriNet src = me.getAs(VisualPetriNet.class);
        final VisualPolicyNet dst = new VisualPolicyNet(new PolicyNet());
        final PetriToPolicyConverter converter = new PetriToPolicyConverter(src, dst);
        return new ModelEntry(new PolicyNetDescriptor(), converter.getDstModel());
    }

}
