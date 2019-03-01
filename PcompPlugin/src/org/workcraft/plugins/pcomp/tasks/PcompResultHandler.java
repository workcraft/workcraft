package org.workcraft.plugins.pcomp.tasks;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.AbstractResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class PcompResultHandler extends AbstractResultHandler<PcompOutput> {
    private final boolean showInEditor;
    private final File outputFile;
    private final Collection<Mutex> mutexes;

    public PcompResultHandler(boolean showInEditor, File outputFile, Collection<Mutex> mutexes) {
        this.showInEditor = showInEditor;
        this.outputFile = outputFile;
        this.mutexes = mutexes;
    }

    @Override
    public void handleResult(final Result<? extends PcompOutput> result) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                final Framework framework = Framework.getInstance();
                final Workspace workspace = framework.getWorkspace();
                if (result.getOutcome() == Outcome.FAILURE) {
                    String message;
                    if (result.getCause() != null) {
                        message = result.getCause().getMessage();
                        result.getCause().printStackTrace();
                    } else {
                        message = "Pcomp errors:\n" + result.getPayload().getErrorsHeadAndTail();
                    }
                    DialogUtils.showError(message);
                } else if (result.getOutcome() == Outcome.SUCCESS) {
                    try {
                        if (showInEditor) {
                            WorkspaceEntry we = framework.loadWork(outputFile);
                            StgModel model = WorkspaceUtils.getAs(we, StgModel.class);
                            MutexUtils.restoreMutexPlacesByName(model, mutexes);
                        } else {
                            Path<String> path = Path.fromString(outputFile.getName());
                            workspace.addMount(path, outputFile, true);
                        }
                    } catch (DeserialisationException e) {
                        DialogUtils.showError(e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
