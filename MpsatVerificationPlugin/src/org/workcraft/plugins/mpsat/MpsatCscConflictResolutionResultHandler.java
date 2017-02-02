package org.workcraft.plugins.mpsat;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.mpsat.tasks.MpsatTask;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatCscConflictResolutionResultHandler implements Runnable {

    private final WorkspaceEntry we;
    private final Result<? extends ExternalProcessResult> result;
    private WorkspaceEntry weResult;

    public MpsatCscConflictResolutionResultHandler(WorkspaceEntry we, Result<? extends ExternalProcessResult> result) {
        this.we = we;
        this.result = result;
        this.weResult = null;
    }

    private StgModel getResolvedStg() {
        final byte[] content = result.getReturnValue().getFileData(MpsatTask.FILE_MPSAT_G);
        if (content == null) {
            return null;
        }
        try {
            return new DotGImporter().importSTG(new ByteArrayInputStream(content));
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        final Framework framework = Framework.getInstance();
        Path<String> path = we.getWorkspacePath();
        String fileName = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));

        StgModel model = getResolvedStg();
        if (model == null) {
            String errorMessage = result.getReturnValue().getErrorsHeadAndTail();
            JOptionPane.showMessageDialog(framework.getMainWindow(),
                    "MPSat output: \n" + errorMessage,
                    "Conflict resolution failed", JOptionPane.WARNING_MESSAGE);
        } else {
            Path<String> directory = path.getParent();
            String name = fileName + "_resolved";
            ModelEntry me = new ModelEntry(new StgDescriptor(), model);
            weResult = framework.createWork(me, directory, name);
        }
    }

    public WorkspaceEntry getResult() {
        return weResult;
    }

}
