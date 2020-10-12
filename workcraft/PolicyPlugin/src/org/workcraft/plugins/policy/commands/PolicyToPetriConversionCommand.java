package org.workcraft.plugins.policy.commands;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriDescriptor;
import org.workcraft.plugins.policy.Policy;
import org.workcraft.plugins.policy.VisualPolicy;
import org.workcraft.plugins.policy.converters.PolicyToPetriConverter;
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
        return WorkspaceUtils.isApplicable(we, Policy.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualPolicy visualModel = me.getAs(VisualPolicy.class);
        final PolicyToPetriConverter converter = new PolicyToPetriConverter(visualModel);
        return new ModelEntry(new PetriDescriptor(), converter.getPetriNet());
    }

}
