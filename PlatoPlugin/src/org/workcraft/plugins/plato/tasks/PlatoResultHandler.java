package org.workcraft.plugins.plato.tasks;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

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
import org.workcraft.plugins.mpsat.MpsatMode;
import org.workcraft.plugins.mpsat.MpsatParameters;
import org.workcraft.plugins.mpsat.MpsatParameters.SolutionMode;
import org.workcraft.plugins.mpsat.tasks.MpsatChainResultHandler;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.plato.commands.PlatoFstConversionCommand;
import org.workcraft.plugins.plato.commands.PlatoStgConversionCommand;
import org.workcraft.plugins.plato.exceptions.PlatoException;
import org.workcraft.plugins.plato.layout.ConceptsLayout;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.BasicProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.ImportUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class PlatoResultHandler extends BasicProgressMonitor<ExternalProcessOutput> {

    private final class ProcessPlatoResult implements Runnable {
        private final Result<? extends ExternalProcessOutput> result;

        private ProcessPlatoResult(Result<? extends ExternalProcessOutput> result) {
            this.result = result;
        }

        @Override
        public void run() {
            try {
                if (system) {
                    if (result.getOutcome() == Outcome.SUCCESS) {
                        PlatoSystemTask task = new PlatoSystemTask();
                        PlatoResultHandler resultHandler = new PlatoResultHandler(sender, name, we, false);
                        final TaskManager taskManager = Framework.getInstance().getTaskManager();
                        taskManager.queue(task, "Plato - Translating concepts", resultHandler);
                        return;
                    }
                } else {
                    String stdout = result.getPayload().getStdoutString();
                    if (result.getOutcome() == Outcome.SUCCESS) {
                        final Framework framework = Framework.getInstance();
                        final MainWindow mainWindow = framework.getMainWindow();
                        GraphEditorPanel editor = mainWindow.getEditor(we);
                        if (stdout.startsWith(".model out") || stdout.startsWith(".inputs")) {
                            int endOfFile = stdout.indexOf(".end") + 4;
                            String info = stdout.substring(endOfFile).trim();
                            stdout = stdout.substring(0, endOfFile);
                            String[] invariants = info.split(System.getProperty("line.separator"));

                            if (!info.isEmpty()) {
                                for (String s : invariants) {
                                    if (!s.isEmpty()) {
                                        LogUtils.logInfo(s);
                                    }
                                }
                            } else {
                                invariants = new String[0];
                            }

                            if (sender instanceof PlatoStgConversionCommand) {
                                addStg(stdout, framework, editor, invariants);
                            } else if (sender instanceof PlatoFstConversionCommand) {
                                addFst(stdout, framework, editor);
                            }
                            return;

                        }
                    }
                }
                throw new PlatoException(result);
            } catch (IOException | DeserialisationException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                new PlatoException(result).handleConceptsError();
            } catch (PlatoException e) {
                e.handleConceptsError();
            }
        }
    }

    private final String name;
    private final Object sender;
    private WorkspaceEntry we = null;
    private final boolean system;

    public PlatoResultHandler(Object sender, String name, WorkspaceEntry we, boolean system) {
        this.sender = sender;
        this.name = name;
        this.we = we;
        this.system = system;
    }

    public PlatoResultHandler(Object sender) {
        this(sender, null, null, false);
    }

    public void finished(final Result<? extends ExternalProcessOutput> result) {
        super.finished(result);
        try {
            SwingUtilities.invokeAndWait(new ProcessPlatoResult(result));
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private WorkspaceEntry addWork(Framework framework, WorkspaceEntry we, GraphEditorPanel editor, ModelEntry me) {
        WorkspaceEntry newWe = null;
        if (!isCurrentWorkEmpty(editor)) {
            newWe = framework.createWork(me, Path.<String>empty(), name);
        } else {
            we.setModelEntry(me);
            newWe = we;
        }
        framework.getMainWindow().getEditor(we).zoomFit();
        return newWe;
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

    private void addStg(String output, Framework framework, GraphEditorPanel editor, String[] invariants) throws PlatoException, IOException, DeserialisationException {
        ModelEntry me = ImportUtils.importFromByteArray(new StgImporter(), output.getBytes());
        MathModel mathModel = me.getMathModel();
        StgDescriptor stgModel = new StgDescriptor();
        VisualModelDescriptor v = stgModel.getVisualModelDescriptor();
        try {
            me = new ModelEntry(me.getDescriptor(), v.create(mathModel));
            we = addWork(framework, we, editor, me);
            VisualStg visualStg = WorkspaceUtils.getAs(we, VisualStg.class);
            if (!((PlatoStgConversionCommand) sender).getDotLayout()) {
                ConceptsLayout.layout(visualStg);
            } else {
                visualStg.getBestLayouter().layout(visualStg);
            }
            runReachTest(invariants);
        } catch (VisualModelInstantiationException e) {
            e.printStackTrace();
        }
    }

    private void addFst(String output, Framework framework, GraphEditorPanel editor) throws PlatoException, IOException, DeserialisationException {
        ModelEntry me = ImportUtils.importFromByteArray(new org.workcraft.plugins.fst.interop.SgImporter(), output.getBytes());
        MathModel mathModel = me.getMathModel();
        FstDescriptor fstModel = new FstDescriptor();
        VisualModelDescriptor v = fstModel.getVisualModelDescriptor();
        try {
            VisualFst visualFst = (VisualFst) v.create(mathModel);
            me = new ModelEntry(me.getDescriptor(), visualFst);
            visualFst.getBestLayouter().layout(visualFst);
            we = addWork(framework, we, editor, me);
        } catch (VisualModelInstantiationException e) {
            e.printStackTrace();
        }
    }

    public WorkspaceEntry getResult() {
        return we;
    }

    private void runReachTest(String[] invariants) {
        final String prefix = "exists s in SIGNALS \\ DUMMY {\n" + "let Es = ev s {\n";
        final String suffix = "}\n" + "}";

        if (invariants.length != 0) {
            ArrayList<String> expression = new ArrayList<>();

            for (String i : invariants) {
                ArrayList<String> exp = new ArrayList<>();
                if (i.startsWith("invariant = not (")) {
                    i = i.replace("invariant = not (", "");

                    while (!i.startsWith(")")) {
                        int x = i.indexOf(" && ");
                        if (x == -1) {
                            x = i.length() - 1;
                        }

                        String sig = i.substring(0, x);
                        String sigExp = "";
                        if (sig.startsWith("not ")) {
                            sigExp = "~$S\"" + sig.substring(4) + "\"";
                        } else {
                            sigExp = "$S\"" + sig + "\"";
                        }
                        exp.add(sigExp);

                        if (x != i.length() - 1) {
                            x += 4;
                        }
                        i = i.substring(x);
                    }

                    Iterator<String> it = exp.iterator();
                    String fullExp = "";
                    while (it.hasNext()) {
                        fullExp = fullExp + it.next();
                        if (it.hasNext()) {
                            fullExp = fullExp + " & ";
                        }
                    }
                    expression.add(fullExp);
                }
            }
            String fullExpression = "";
            Iterator<String> it = expression.iterator();
            while (it.hasNext()) {
                fullExpression = fullExpression + it.next();
                if (it.hasNext()) {
                    fullExpression = fullExpression + "\n|\n";
                }
            }
            fullExpression = prefix + fullExpression + suffix;

            MpsatParameters param = new MpsatParameters(null, MpsatMode.STG_REACHABILITY, 0, SolutionMode.MINIMUM_COST, 10, fullExpression, true);
            final MpsatChainTask mpsatTask = new MpsatChainTask(we, param);
            final TaskManager taskManager = Framework.getInstance().getTaskManager();
            final MpsatChainResultHandler monitor = new MpsatChainResultHandler(we);
            taskManager.queue(mpsatTask, "Verify invariant of translated concepts", monitor);
        }
    }

}
