package org.workcraft.plugins.cpog.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.gui.PetriToCpogDialog;
import org.workcraft.plugins.cpog.untangling.PetriToCpogConverter;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetriToCpogConversionCommand extends AbstractConversionCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualPetri.class);
    }

    @Override
    public String getDisplayName() {
        return "Conditional Partial Order Graph [Untangling]";
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        if (Hierarchy.isHierarchical(me)) {
            DialogUtils.showError("Conditional Partial Order Graph cannot be derived from a hierarchical Petri Net.");
            return null;
        }
        final MainWindow mainWindow = Framework.getInstance().getMainWindow();
        PetriToCpogParameters parameters = new PetriToCpogParameters();
        PetriToCpogDialog dialog = new PetriToCpogDialog(mainWindow, parameters);
        if (dialog.reveal()) {
            VisualPetri src = me.getAs(VisualPetri.class);
            PetriToCpogConverter converter = new PetriToCpogConverter(src);
            VisualCpog dst = converter.run(parameters);
            return new ModelEntry(new CpogDescriptor(), dst);
        }
        return null;
    }

}