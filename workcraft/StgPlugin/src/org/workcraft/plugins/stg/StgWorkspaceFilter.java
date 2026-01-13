package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.types.Func;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class StgWorkspaceFilter implements Func<Path<String>, Boolean> {

    @Override
    public Boolean eval(Path<String> arg) {
        final Workspace workspace = Framework.getInstance().getWorkspace();
        WorkspaceEntry we = workspace.getWork(arg);
        return WorkspaceUtils.isApplicable(we, StgModel.class) || arg.getNode().endsWith(".g");
    }
}
