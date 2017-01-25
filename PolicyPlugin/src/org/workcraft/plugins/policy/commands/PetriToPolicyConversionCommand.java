package org.workcraft.plugins.policy.commands;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.commands.AbstractConversionCommand;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.policy.PolicyNet;
import org.workcraft.plugins.policy.PolicyNetDescriptor;
import org.workcraft.plugins.policy.VisualPolicyNet;
import org.workcraft.plugins.policy.tools.PetriToPolicyConverter;
import org.workcraft.util.Hierarchy;
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
        if (Hierarchy.isHierarchical(me)) {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            JOptionPane.showMessageDialog(mainWindow,
                    "Policy Net cannot be derived from a hierarchical Petri Net.",
                    "Conversion error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        final VisualPetriNet src = me.getAs(VisualPetriNet.class);
        final VisualPolicyNet dst = new VisualPolicyNet(new PolicyNet());
        final PetriToPolicyConverter converter = new PetriToPolicyConverter(src, dst);
        return new ModelEntry(new PolicyNetDescriptor(), converter.getDstModel());
    }

}
