package org.workcraft.plugins.policy.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.Petri;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.plugins.policy.PolicyDescriptor;
import org.workcraft.plugins.policy.converters.PetriToPolicyConverter;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

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
        PetriToPolicyConverter converter = new PetriToPolicyConverter(me.getAs(VisualPetri.class));
        return new ModelEntry(new PolicyDescriptor(), converter.getDstModel());
    }

}
