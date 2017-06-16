package org.workcraft.plugins.cpog.tasks;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCpog;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.MessageUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PGMinerResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

    private VisualCpog visualCpog;
    private final WorkspaceEntry we;
    private final boolean createNewWindow;
    private WorkspaceEntry weResult;

    public PGMinerResultHandler(final VisualCpog visualCpog, final WorkspaceEntry we, final boolean createNewWindow) {
        this.visualCpog = visualCpog;
        this.we = we;
        this.createNewWindow = createNewWindow;
        this.weResult = null;
    }

    public void finished(final Result<? extends ExternalProcessResult> result, final String description) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    final Framework framework = Framework.getInstance();
                    final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
                    final Toolbox toolbox = editor.getToolBox();
                    final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);
                    if (result.getOutcome() == Outcome.FAILED) {
                        MessageUtils.showError("PGMiner could not run, concurrency extraction failed.");
                    } else {
                        if (createNewWindow) {
                            final CpogDescriptor cpogModel = new CpogDescriptor();
                            final MathModel mathModel = cpogModel.createMathModel();
                            final VisualModelDescriptor v = cpogModel.getVisualModelDescriptor();
                            try {
                                if (v == null) {
                                    throw new VisualModelInstantiationException(
                                            "visual model is not defined for '" + cpogModel.getDisplayName() + "'.");
                                }
                                visualCpog = (VisualCpog) v.create(mathModel);
                                final ModelEntry me = new ModelEntry(cpogModel, visualCpog);
                                final Path<String> path = we.getWorkspacePath();
                                weResult = framework.createWork(me, path);
                            } catch (final VisualModelInstantiationException e) {
                                e.printStackTrace();
                            }
                        }
                        final String[] output = new String(result.getReturnValue().getOutput()).split("\n");

                        we.captureMemento();
                        try {
                            for (int i = 0; i < output.length; i++) {
                                final String exp = output[i];
                                tool.insertExpression(exp, visualCpog, false, false, true, false);
                            }

                            we.saveMemento();
                        } catch (final Exception e) {
                            we.cancelMemento();
                        }
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public WorkspaceEntry getResult() {
        return weResult;
    }

}

