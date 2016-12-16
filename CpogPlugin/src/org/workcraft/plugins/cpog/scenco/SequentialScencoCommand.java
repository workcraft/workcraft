package org.workcraft.plugins.cpog.scenco;

import java.awt.Window;

import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class SequentialScencoCommand extends AbstractScencoCommand {

    @Override
    public String getDisplayName() {
        return "Sequential encoding (trivial)";
    }

    @Override
    public AbstractScencoDialog createDialog(Window owner, WorkspaceEntry we) {
        VisualCpog model = WorkspaceUtils.getAs(we, VisualCpog.class);
        EncoderSettings settings = new EncoderSettings(10, GenerationMode.SEQUENTIAL, false, false);
        return new SingleSequentialScencoDialog(owner, "Sequential encoding", settings, model);
    }

}
