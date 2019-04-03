package org.workcraft.plugins.policy.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.policy.Policy;
import org.workcraft.plugins.policy.PolicyDescriptor;
import org.workcraft.plugins.policy.VisualPolicy;
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
        return WorkspaceUtils.isApplicableExact(we, Petri.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        if (Hierarchy.isHierarchical(me)) {
            DialogUtils.showError("Policy Net cannot be derived from a hierarchical Petri Net.");
            return null;
        }
        final VisualPetri src = me.getAs(VisualPetri.class);
        final VisualPolicy dst = new VisualPolicy(new Policy());
        final PetriToPolicyConverter converter = new PetriToPolicyConverter(src, dst);
        return new ModelEntry(new PolicyDescriptor(), converter.getDstModel());
    }

}
