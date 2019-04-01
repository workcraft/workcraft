package org.workcraft.plugins.cpog.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.gui.PetriToCpogDialog;
import org.workcraft.plugins.cpog.untangling.PetriToCpogConverter;
import org.workcraft.plugins.petri.VisualPetri;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class PetriToCpogConversionCommand extends AbstractConversionCommand {

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualPetri.class);
    }

    @Override
    public String getDisplayName() {
        return "Conditional Partial Order Graph [Untanglingusing bots]";
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (Hierarchy.isHierarchical(me)) {
            DialogUtils.showError("Conditional Partial Order Graph cannot be derived from a hierarchical Petri Net.");
            return null;
        }
        PetriToCpogParameters settings = new PetriToCpogParameters();
        PetriToCpogDialog dialog = new PetriToCpogDialog(mainWindow, settings);
        GuiUtils.centerToParent(dialog, mainWindow);
        dialog.setVisible(true);
        if (dialog.getModalResult() != 1) {
            return null;
        } else {
            VisualPetri src = me.getAs(VisualPetri.class);
            PetriToCpogConverter converter = new PetriToCpogConverter(src);
            VisualCpog dst = converter.run(settings);
            return new ModelEntry(new CpogDescriptor(), dst);
        }
    }

}