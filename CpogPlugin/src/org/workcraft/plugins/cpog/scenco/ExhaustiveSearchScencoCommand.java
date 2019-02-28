package org.workcraft.plugins.cpog.scenco;

import java.awt.Window;

import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class ExhaustiveSearchScencoCommand extends AbstractScencoCommand {

    @Override
    public String getDisplayName() {
        return "Exhaustive search (supports constraints)";
    }

    @Override
    public AbstractScencoDialog createDialog(Window owner, WorkspaceEntry we) {
        VisualCpog model = WorkspaceUtils.getAs(we, VisualCpog.class);
        EncoderSettings settings = new EncoderSettings(10, GenerationMode.OPTIMAL_ENCODING, false, false);
        return new ConstrainedSearchScencoDialog(owner, "Exhaustive search", settings, model, 1);
    }

}
