package org.workcraft.plugins.pcomp.tasks;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.MutexUtils;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.MessageUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PcompResultHandler extends DummyProgressMonitor<ExternalProcessResult> {
    private final boolean showInEditor;
    private final File outputFile;
    private final Collection<Mutex> mutexes;

    public PcompResultHandler(boolean showInEditor, File outputFile, Collection<Mutex> mutexes) {
        this.showInEditor = showInEditor;
        this.outputFile = outputFile;
        this.mutexes = mutexes;
    }

    @Override
    public void finished(final Result<? extends ExternalProcessResult> result, String description) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    final Framework framework = Framework.getInstance();
                    final Workspace workspace = framework.getWorkspace();
                    if (result.getOutcome() == Outcome.FAILED) {
                        String message;
                        if (result.getCause() != null) {
                            message = result.getCause().getMessage();
                            result.getCause().printStackTrace();
                        } else {
                            message = "Pcomp errors:\n" + result.getReturnValue().getErrorsHeadAndTail();
                        }
                        MessageUtils.showError(message);
                    } else if (result.getOutcome() == Outcome.FINISHED) {
                        try {
                            if (showInEditor) {
                                WorkspaceEntry we = framework.loadWork(outputFile);
                                StgModel model = WorkspaceUtils.getAs(we, StgModel.class);
                                MutexUtils.restoreMutexPlacesByName(model, mutexes);
                                MainWindow mainWindow = framework.getMainWindow();
                                mainWindow.createEditorWindow(we);
                            } else {
                                Path<String> path = Path.fromString(outputFile.getName());
                                workspace.addMount(path, outputFile, true);
                            }
                        } catch (DeserialisationException e) {
                            MessageUtils.showError(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
