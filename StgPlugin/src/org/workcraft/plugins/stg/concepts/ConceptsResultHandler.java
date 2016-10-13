package org.workcraft.plugins.stg.concepts;

import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.DockableWindowContentPanel;
import org.workcraft.gui.DockableWindowContentPanel.ViewAction;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.ConceptsImporter;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
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
                                    we.captureMemento();
                                    ModelEntry me = Import.importFromByteArray(new DotGImporter(), result.getReturnValue().getOutput());
                                    String title = "Concepts - ";
                                    me.getModel().setTitle(title + name);
                                    if (sender instanceof ConceptsWritingTool && !((ConceptsWritingTool) sender).getDotLayout()) {
                                        StgDescriptor stgModel = new StgDescriptor();
                                        MathModel mathModel = me.getMathModel();
                                        Path<String> path = we.getWorkspacePath();
                                        VisualModelDescriptor v = stgModel.getVisualModelDescriptor();
                                        try {
                                            VisualStg visualStg = (VisualStg) v.create(mathModel);
                                            me.setModel(visualStg);
                                        } catch (VisualModelInstantiationException e) {
                                            System.out.println("Expected");
                                            e.printStackTrace();
                                        }
                                        final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
                                        framework.getWorkspace().add(Path.<String>empty(), title + name, me, false, true);
                                        ConceptsLayout.layout((VisualStg) me.getVisualModel());
                                    } else {
                                        framework.getWorkspace().add(Path.<String>empty(), title + name, me, false, true);
                                        VisualStg newVisualStg = (VisualStg) me.getVisualModel();
                                        newVisualStg.selectAll();
                                        editor.zoomFit();
                                    }

                                    if (isCurrentWorkEmpty(editor)) {
                                        closeWork(editor);
                                    }
                                    we.saveMemento();
                                }
                            }
                        } else {
                            throw new ConceptsToolException(result);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        we.cancelMemento();
                    } catch (DeserialisationException e) {
                        e.printStackTrace();
                        we.cancelMemento();
                    } catch (NullPointerException e) {
                        new ConceptsToolException(result).handleConceptsError();
                        we.cancelMemento();
                    } catch (ConceptsToolException e) {
                        e.handleConceptsError();
                        we.cancelMemento();
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            we.cancelMemento();
        }
    }

    private void closeWork(GraphEditorPanel editor) {
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
