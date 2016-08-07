package org.workcraft.plugins.stg.tasks;

import java.awt.Container;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DockableWindowContentPanel;
import org.workcraft.gui.DockableWindowContentPanel.ViewAction;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.ConceptsImporter;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.plugins.stg.tools.ConceptsTool;
import org.workcraft.plugins.stg.tools.ConceptsToolException;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ConceptsResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

    private final String name;
    private final Object sender;
    private final WorkspaceEntry we;

    public ConceptsResultHandler(Object sender, String inputName, WorkspaceEntry we) {
        this.sender = sender;
        name = inputName;
        this.we = we;
    }

    public ConceptsResultHandler(Object sender) {
        this.sender = sender;
        name = null;
        we = null;
    }

    public void finished(final Result<? extends ExternalProcessResult> result, String description) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {

                    try {
                        String output = new String(result.getReturnValue().getOutput());
                        if ((result.getOutcome() == Outcome.FINISHED) && (output.startsWith(".model out"))) {
                            if (!(sender instanceof ConceptsImporter)) {
                                final Framework framework = Framework.getInstance();
                                MainWindow mainWindow = framework.getMainWindow();
                                GraphEditorPanel editor = mainWindow.getEditor(we);
                                if (output.startsWith(".model out")) {
                                    ModelEntry me = Import.importFromByteArray(new DotGImporter(), result.getReturnValue().getOutput());
                                    if (sender instanceof ConceptsTool) {
                                        if (isCurrentWorkEmpty(editor)) {
                                            closeEmptyWork(editor);
                                        }
                                    }
                                    String title = "Concepts - ";
                                    me.getModel().setTitle(title + name);
                                    boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
                                    framework.getWorkspace().add(Path.<String>empty(), title + name, me, false, openInEditor);
                                }
                            }
                        } else {
                            throw new ConceptsToolException(result);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (DeserialisationException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        new ConceptsToolException(result).handleConceptsError();
                    } catch (ConceptsToolException e) {
                        e.handleConceptsError();
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void closeEmptyWork(GraphEditorPanel editor) {
        Container p = editor.getParent();

        while (!(p instanceof DockableWindowContentPanel) && (p != null)) {
            p = p.getParent();
        }

        if (p instanceof DockableWindowContentPanel) {
            DockableWindowContentPanel d = (DockableWindowContentPanel) p;
            new ViewAction(d.getID(), ViewAction.CLOSE_ACTION).run();
        }
    }

    private boolean isCurrentWorkEmpty(GraphEditorPanel editor) {
        VisualStg visualStg = (VisualStg) editor.getModel();
        visualStg.selectAll();
        if (visualStg.getSelection().isEmpty()) {
            return true;
        }
        return false;
    }

}
