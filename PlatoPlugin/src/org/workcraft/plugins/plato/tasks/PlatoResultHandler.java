package org.workcraft.plugins.plato.tasks;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.plato.commands.PlatoConversionCommand;
import org.workcraft.plugins.plato.exceptions.PlatoException;
import org.workcraft.plugins.plato.layout.ConceptsLayout;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PlatoResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

    private final class ProcessPlatoResult implements Runnable {
        private final Result<? extends ExternalProcessResult> result;

        private ProcessPlatoResult(Result<? extends ExternalProcessResult> result) {
            this.result = result;
        }

        @Override
        public void run() {
            try {
                String output = new String(result.getReturnValue().getOutput());
                if (result.getOutcome() == Outcome.FINISHED) {
                    final Framework framework = Framework.getInstance();
                    final MainWindow mainWindow = framework.getMainWindow();
                    GraphEditorPanel editor = mainWindow.getEditor(we);
                    if (output.startsWith(".model out")) {
                        we.captureMemento();
                        ModelEntry me = Import.importFromByteArray(new DotGImporter(), result.getReturnValue().getOutput());
                        MathModel mathModel = me.getMathModel();

                        if (sender instanceof PlatoConversionCommand && !((PlatoConversionCommand) sender).getDotLayout()) {
                            StgDescriptor stgModel = new StgDescriptor();
                            VisualModelDescriptor v = stgModel.getVisualModelDescriptor();
                            try {
                                VisualStg visualStg = (VisualStg) v.create(mathModel);
                                me = new ModelEntry(me.getDescriptor(), visualStg);
                            } catch (VisualModelInstantiationException e) {
                                e.printStackTrace();
                            }
                            addWork(framework, we, editor, me);
                            ConceptsLayout.layout((VisualStg) me.getVisualModel());
                        } else {
                            we = framework.createWork(me, Path.<String>empty(), name);
                            addWork(framework, we, editor, me);
                        }

                        we.saveMemento();
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
    private WorkspaceEntry we;

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
            SwingUtilities.invokeAndWait(new ProcessPlatoResult(result));
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            we.cancelMemento();
        }
    }

    private void addWork(Framework framework, WorkspaceEntry we, GraphEditorPanel editor, ModelEntry me) {
        if (!isCurrentWorkEmpty(editor)) {
            we = framework.createWork(me, Path.<String>empty(), name);
        } else {
            we.setModelEntry(me);
        }
        framework.getMainWindow().getEditor(we).zoomFit();
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
        return we;
    }

}
