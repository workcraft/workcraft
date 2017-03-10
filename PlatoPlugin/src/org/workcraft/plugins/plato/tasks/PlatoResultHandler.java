package org.workcraft.plugins.plato.tasks;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.fst.FstDescriptor;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.plato.commands.PlatoFstConversionCommand;
import org.workcraft.plugins.plato.commands.PlatoStgConversionCommand;
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
import org.workcraft.util.LogUtils;
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
                    if (output.startsWith(".model out") || output.startsWith(".inputs")) {
                        int endOfFile = output.indexOf(".end") + 4;
                        String info = output.substring(endOfFile).trim();
                        output = output.substring(0, endOfFile);

                        String[] invariants = info.split(System.getProperty("line.separator"));
                        if (!info.isEmpty()) {
                            for (String s : invariants) {
                                if (!s.isEmpty()) {
                                    System.out.println(LogUtils.PREFIX_INFO + s);
                                }
                            }
                        }

                        if (sender instanceof PlatoStgConversionCommand) {
                            addStg(output, framework, editor);
                        } else if (sender instanceof PlatoFstConversionCommand) {
                            addFst(output, framework, editor);
                        }
                        return;

                    }
                }
                throw new PlatoException(result);
            } catch (IOException | DeserialisationException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                new PlatoException(result).handleConceptsError();
            } catch (PlatoException e) {
                e.printStackTrace();
                e.handleConceptsError();
            }
        }
    }

    private final String name;
    private final Object sender;
    private WorkspaceEntry we = null;

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
        VisualModel visualModel = editor.getModel();
        visualModel.selectAll();
        if (visualModel.getSelection().isEmpty()) {
            return true;
        }
        visualModel.selectNone();
        return false;
    }

    private void addStg(String output, Framework framework, GraphEditorPanel editor) throws PlatoException, IOException, DeserialisationException {
        ModelEntry me = Import.importFromByteArray(new DotGImporter(), output.getBytes());
        MathModel mathModel = me.getMathModel();
        StgDescriptor stgModel = new StgDescriptor();
        VisualModelDescriptor v = stgModel.getVisualModelDescriptor();
        try {
            VisualStg visualStg = (VisualStg) v.create(mathModel);
            me = new ModelEntry(me.getDescriptor(), visualStg);
            if (!((PlatoStgConversionCommand) sender).getDotLayout()) {
                ConceptsLayout.layout((VisualStg) me.getVisualModel());
            } else {
                visualStg.getBestLayouter().layout(visualStg);
            }
            addWork(framework, we, editor, me);
            framework.getMainWindow().getEditor(we).zoomFit();
        } catch (VisualModelInstantiationException e) {
            e.printStackTrace();
        }
    }

    private void addFst(String output, Framework framework, GraphEditorPanel editor) throws PlatoException, IOException, DeserialisationException {
        ModelEntry me = Import.importFromByteArray(new org.workcraft.plugins.fst.interop.DotGImporter(), output.getBytes());
        MathModel mathModel = me.getMathModel();
        FstDescriptor fstModel = new FstDescriptor();
        VisualModelDescriptor v = fstModel.getVisualModelDescriptor();
        try {
            VisualFst visualFst = (VisualFst) v.create(mathModel);
            me = new ModelEntry(me.getDescriptor(), visualFst);
            visualFst.getBestLayouter().layout(visualFst);
            addWork(framework, we, editor, me);
            framework.getMainWindow().getEditor(we).zoomFit();
        } catch (VisualModelInstantiationException e) {
            e.printStackTrace();
        }
    }


    public WorkspaceEntry getResult() {
        return we;
    }

}
