package org.workcraft.plugins.plato.tasks;

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
import org.workcraft.plugins.plato.commands.PlatoConversionCommand;
import org.workcraft.plugins.plato.exceptions.PlatoException;
import org.workcraft.plugins.plato.interop.PlatoImporter;
import org.workcraft.plugins.plato.layout.ConceptsLayout;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PlatoResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

    private final class ProcessConceptsResult implements Runnable {
        private final Result<? extends ExternalProcessResult> result;

        private ProcessConceptsResult(Result<? extends ExternalProcessResult> result) {
            this.result = result;
        }

        @Override
        public void run() {
            try {
                String output = new String(result.getReturnValue().getOutput());
                if ((result.getOutcome() == Outcome.FINISHED) && (output.startsWith(".model out"))) {
                    if (!(sender instanceof PlatoImporter)) {
                        final Framework framework = Framework.getInstance();
                        final MainWindow mainWindow = framework.getMainWindow();
                        final GraphEditorPanel editor = mainWindow.getEditor(we);
                        if (output.startsWith(".model out")) {
                            we.captureMemento();
                            ModelEntry me = Import.importFromByteArray(new DotGImporter(), result.getReturnValue().getOutput());
                            String title = "Concepts - ";
                            MathModel mathModel = me.getMathModel();
                            mathModel.setTitle(title + name);
                            if (sender instanceof PlatoConversionCommand && !((PlatoConversionCommand) sender).getDotLayout()) {
                                StgDescriptor stgModel = new StgDescriptor();
                                Path<String> path = we.getWorkspacePath();
                                VisualModelDescriptor v = stgModel.getVisualModelDescriptor();
                                try {
                                    VisualStg visualStg = (VisualStg) v.create(mathModel);
                                    me = new ModelEntry(me.getDescriptor(), visualStg);
                                } catch (VisualModelInstantiationException e) {
                                    System.out.println("Expected");
                                    e.printStackTrace();
                                }
                                final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
                                weResult = framework.createWork(me, Path.<String>empty(), title + name);
                                ConceptsLayout.layout((VisualStg) me.getVisualModel());
                            } else {
                                weResult = framework.createWork(me, Path.<String>empty(), title + name);
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
                    throw new PlatoException(result);
                }
            } catch (IOException | DeserialisationException e) {
                e.printStackTrace();
                we.cancelMemento();
            } catch (NullPointerException e) {
                new PlatoException(result).handleConceptsError();
                we.cancelMemento();
            } catch (PlatoException e) {
                e.handleConceptsError();
                we.cancelMemento();
            }
        }
    }

    private final String name;
    private final Object sender;
    private final WorkspaceEntry we;
    private WorkspaceEntry weResult;

    public PlatoResultHandler(Object sender, String name, WorkspaceEntry we) {
        this.sender = sender;
        this.name = name;
        this.we = we;
    }

    public PlatoResultHandler(Object sender) {
        this(sender, null, null);
    }

    public void finished(final Result<? extends ExternalProcessResult> result, String description) {
        try {
            SwingUtilities.invokeAndWait(new ProcessConceptsResult(result));
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
        visualStg.selectNone();
        return false;
    }

    public WorkspaceEntry getResult() {
        return weResult;
    }

}
