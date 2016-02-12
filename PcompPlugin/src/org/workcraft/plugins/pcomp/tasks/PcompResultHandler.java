package org.workcraft.plugins.pcomp.tasks;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class PcompResultHandler extends DummyProgressMonitor<ExternalProcessResult> {
    private final boolean showInEditor;

    public PcompResultHandler(boolean showInEditor) {
        this.showInEditor = showInEditor;
    }

    @Override
    public void finished(final Result<? extends ExternalProcessResult> result, String description) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {

                    final Framework framework = Framework.getInstance();
                    MainWindow mainWindow = framework.getMainWindow();
                    if (result.getOutcome() == Outcome.FAILED) {
                        String message;
                        if (result.getCause() != null) {
                            message = result.getCause().getMessage();
                            result.getCause().printStackTrace();
                        } else {
                            message = "Pcomp errors: \n" + new String(result.getReturnValue().getErrors());
                        }
                        JOptionPane.showMessageDialog(mainWindow, message, "Parallel composition failed", JOptionPane.ERROR_MESSAGE);
                    } else if (result.getOutcome() == Outcome.FINISHED) {
                        try {
                            File pcompResult = File.createTempFile("pcompresult", ".g");
                            FileUtils.writeAllText(pcompResult, new String(result.getReturnValue().getOutput()));

                            if (showInEditor) {
                                WorkspaceEntry we = framework.getWorkspace().open(pcompResult, false);
                                mainWindow.createEditorWindow(we);
                            } else {
                                framework.getWorkspace().add(pcompResult.getName(), pcompResult, true);
                            }
                        } catch (IOException e) {
                            JOptionPane.showMessageDialog(mainWindow, e.getMessage(), "Parallel composition failed", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        } catch (DeserialisationException e) {
                            JOptionPane.showMessageDialog(mainWindow, e.getMessage(), "Parallel composition failed", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
