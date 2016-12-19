package org.workcraft.plugins.stg;

import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.util.Func;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class StgWorkspaceFilter implements Func<Path<String>, Boolean> {

    @Override
    public Boolean eval(Path<String> arg) {
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        WorkspaceEntry we = workspace.getWork(arg);
        return WorkspaceUtils.isApplicable(we, StgModel.class) || arg.getNode().endsWith(".g");
    }
}
