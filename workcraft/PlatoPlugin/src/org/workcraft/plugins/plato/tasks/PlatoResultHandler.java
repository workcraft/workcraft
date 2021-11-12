package org.workcraft.plugins.plato.tasks;

import org.workcraft.Framework;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.plugins.fst.FstDescriptor;
import org.workcraft.plugins.fst.VisualFst;
import org.workcraft.plugins.fst.VisualFstDescriptor;
import org.workcraft.plugins.mpsat_verification.presets.VerificationMode;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters;
import org.workcraft.plugins.mpsat_verification.presets.VerificationParameters.SolutionMode;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainResultHandlingMonitor;
import org.workcraft.plugins.mpsat_verification.tasks.VerificationChainTask;
import org.workcraft.plugins.plato.commands.FstConversionCommand;
import org.workcraft.plugins.plato.commands.StgConversionCommand;
import org.workcraft.plugins.plato.exceptions.PlatoException;
import org.workcraft.plugins.plato.layout.ConceptsLayout;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.BasicProgressMonitor;
import org.workcraft.tasks.ExternalProcessOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.TaskManager;
import org.workcraft.utils.ImportUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

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
                    if (result.isSuccess()) {
                        PlatoSystemTask task = new PlatoSystemTask();
                        PlatoResultHandler resultHandler = new PlatoResultHandler(sender, name, we, false);
                        final TaskManager taskManager = Framework.getInstance().getTaskManager();
                        taskManager.queue(task, "Plato - Translating concepts", resultHandler);
                        return;
                    }
                } else {
                    String stdout = result.getPayload().getStdoutString();
                    if (result.isSuccess()) {
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

                            if (sender instanceof StgConversionCommand) {
                                addStg(stdout, framework, editor, invariants);
                            } else if (sender instanceof FstConversionCommand) {
                                addFst(stdout, framework, editor);
                            }
                            return;

                        }
                    }
                }
                throw new PlatoException(result);
            } catch (OperationCancelledException e) {
                // Operation cancelled by the user
            } catch (PlatoException e) {
                e.handleConceptsError();
            } catch (IOException | DeserialisationException e) {
                e.printStackTrace();
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

    @Override
    public void isFinished(final Result<? extends ExternalProcessOutput> result) {
        super.isFinished(result);
        try {
            SwingUtilities.invokeAndWait(new ProcessPlatoResult(result));
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private WorkspaceEntry addWork(Framework framework, WorkspaceEntry we, GraphEditorPanel editor, ModelEntry me) {
        WorkspaceEntry newWe = null;
        if (!isCurrentWorkEmpty(editor)) {
            newWe = framework.createWork(me, name);
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

    private void addStg(String output, Framework framework, GraphEditorPanel editor, String[] invariants)
            throws IOException, DeserialisationException, OperationCancelledException {
        ModelEntry me = ImportUtils.importFromByteArray(new StgImporter(), output.getBytes());
        MathModel mathModel = me.getMathModel();
        StgDescriptor stgModel = new StgDescriptor();
        VisualModelDescriptor vmd = stgModel.getVisualModelDescriptor();
        try {
            me = new ModelEntry(me.getDescriptor(), vmd.create(mathModel));
            we = addWork(framework, we, editor, me);
            VisualStg visualStg = WorkspaceUtils.getAs(we, VisualStg.class);
            if (!((StgConversionCommand) sender).getDotLayout()) {
                ConceptsLayout.layout(visualStg);
            } else {
                visualStg.getBestLayouter().layout(visualStg);
            }
            runReachTest(invariants);
        } catch (VisualModelInstantiationException e) {
            e.printStackTrace();
        }
    }

    private void addFst(String output, Framework framework, GraphEditorPanel editor)
            throws IOException, DeserialisationException, OperationCancelledException {
        ModelEntry me = ImportUtils.importFromByteArray(new org.workcraft.plugins.fst.interop.SgImporter(), output.getBytes());
        MathModel mathModel = me.getMathModel();
        FstDescriptor fstModel = new FstDescriptor();
        VisualFstDescriptor vmd = fstModel.getVisualModelDescriptor();
        try {
            VisualFst visualFst = vmd.create(mathModel);
            me = new ModelEntry(me.getDescriptor(), visualFst);
            visualFst.getBestLayouter().layout(visualFst);
            we = addWork(framework, we, editor, me);
        } catch (VisualModelInstantiationException e) {
            e.printStackTrace();
        }
    }

    private void runReachTest(String[] invariants) {
        final String prefix = "exists s in SIGNALS \\ DUMMY {\n" + "let Es = ev s {\n";
        final String suffix = "}\n" + "}";

        if (invariants.length != 0) {
            ArrayList<String> expression = new ArrayList<>();

            for (String invariant : invariants) {
                ArrayList<String> exp = new ArrayList<>();
                if (invariant.startsWith("invariant = not (")) {
                    String s = invariant.replace("invariant = not (", "");
                    while (!s.startsWith(")")) {
                        int x = s.indexOf(" && ");
                        if (x == -1) {
                            x = s.length() - 1;
                        }

                        String sig = s.substring(0, x);
                        String sigExp = "";
                        if (sig.startsWith("not ")) {
                            sigExp = "~$S\"" + sig.substring(4) + "\"";
                        } else {
                            sigExp = "$S\"" + sig + "\"";
                        }
                        exp.add(sigExp);

                        if (x != s.length() - 1) {
                            x += 4;
                        }
                        s = s.substring(x);
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

            VerificationParameters param = new VerificationParameters(null, VerificationMode.STG_REACHABILITY, 0, SolutionMode.MINIMUM_COST, 10, fullExpression, true);
            final VerificationChainTask mpsatTask = new VerificationChainTask(we, param);
            final TaskManager taskManager = Framework.getInstance().getTaskManager();
            final VerificationChainResultHandlingMonitor monitor = new VerificationChainResultHandlingMonitor(we, true);
            taskManager.queue(mpsatTask, "Verify invariant of translated concepts", monitor);
        }
    }

}
