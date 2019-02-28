package org.workcraft.plugins.cpog.scenco;

import java.awt.Window;

import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class SatBasedScencoCommand extends AbstractScencoCommand {

    @Override
    public String getDisplayName() {
        return "SAT-based optimal encoding";
    }

    @Override
    public AbstractScencoDialog createDialog(Window owner, WorkspaceEntry we) {
        VisualCpog model = WorkspaceUtils.getAs(we, VisualCpog.class);
        EncoderSettings settings = new EncoderSettings(10, GenerationMode.SCENCO, false, false);
        return new SatBasedScencoDialog(owner, "SAT-based optimal encoding", settings, model);
    }

}
