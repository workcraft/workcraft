package org.workcraft.plugins.cpog.scenco;

import java.awt.Window;

import org.workcraft.plugins.cpog.EncoderSettings;
import org.workcraft.plugins.cpog.EncoderSettings.GenerationMode;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class SingleLiteralScencoCommand extends AbstractScencoCommand {

    @Override
    public String getDisplayName() {
        return "Single-literal encoding";
    }

    @Override
    public AbstractScencoDialog createDialog(Window owner, WorkspaceEntry we) {
        VisualCpog model = WorkspaceUtils.getAs(we, VisualCpog.class);
        EncoderSettings settings = new EncoderSettings(10, GenerationMode.OLD_SYNT, false, false);
        return new SingleSequentialScencoDialog(owner, "Single-literal encoding", settings, model);
    }

}
