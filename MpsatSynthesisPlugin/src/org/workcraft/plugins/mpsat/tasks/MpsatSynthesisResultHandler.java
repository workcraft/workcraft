package org.workcraft.plugins.mpsat.tasks;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.mpsat.MpsatSynthesisMode;
import org.workcraft.plugins.mpsat.MpsatSynthesisSettings;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

public class MpsatSynthesisResultHandler extends AbstractExtendedResultHandler<MpsatSynthesisChainResult, WorkspaceEntry> {
    private static final String ERROR_CAUSE_PREFIX = "\n\n";
    private final MpsatSynthesisChainTask task;
    private final Collection<Mutex> mutexes;

    public MpsatSynthesisResultHandler(final MpsatSynthesisChainTask task, Collection<Mutex> mutexes) {
        this.task = task;
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry handleResult(final Result<? extends MpsatSynthesisChainResult> result) {
        WorkspaceEntry weResult = null;
        if (result.getOutcome() == Outcome.SUCCESS) {
            weResult = handleSuccess(result);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(result);
        }
        return weResult;
    }

    private WorkspaceEntry handleSuccess(final Result<? extends MpsatSynthesisChainResult> chainResult) {
        MpsatSynthesisChainResult returnValue = chainResult.getPayload();
        MpsatSynthesisMode mpsatMode = returnValue.getMpsatSettings().getMode();
        ExternalProcessOutput mpsatReturnValue = returnValue.getMpsatResult().getPayload();
        WorkspaceEntry synthResult = null;
        switch (mpsatMode) {
        case COMPLEX_GATE_IMPLEMENTATION:
            synthResult = handleSynthesisResult(mpsatReturnValue, false, RenderType.GATE);
            break;
        case GENERALISED_CELEMENT_IMPLEMENTATION:
            synthResult = handleSynthesisResult(mpsatReturnValue, true, RenderType.BOX);
            break;
        case STANDARD_CELEMENT_IMPLEMENTATION:
            synthResult = handleSynthesisResult(mpsatReturnValue, true, RenderType.GATE);
            break;
        case TECH_MAPPING:
            synthResult = handleSynthesisResult(mpsatReturnValue, false, RenderType.GATE);
            break;
        default:
            DialogUtils.showWarning("MPSat synthesis mode \'" + mpsatMode.getArgument() + "\' is not (yet) supported.");
            break;
        }
        return synthResult;
    }

    private WorkspaceEntry handleSynthesisResult(ExternalProcessOutput mpsatResult,
            boolean sequentialAssign, RenderType renderType) {

        final String log = new String(mpsatResult.getStdout());
        if ((log != null) && !log.isEmpty()) {
            System.out.println(log);
            System.out.println();
        }
        handleStgSynthesisResult(mpsatResult);
        return handleVerilogSynthesisResult(mpsatResult, sequentialAssign, renderType);
    }

    private WorkspaceEntry handleStgSynthesisResult(ExternalProcessOutput mpsatResult) {
        WorkspaceEntry dstWe = null;
        if (MpsatSynthesisSettings.getOpenSynthesisStg()) {
            byte[] dstOutput = mpsatResult.getFileData(MpsatSynthesisTask.STG_FILE_NAME);
            if (dstOutput != null) {
                WorkspaceEntry srcWe = task.getWorkspaceEntry();
                Stg srcStg = WorkspaceUtils.getAs(srcWe, Stg.class);
                try {
                    ByteArrayInputStream dstStream = new ByteArrayInputStream(dstOutput);
                    StgModel dstStg = new StgImporter().importStg(dstStream);
                    if (StgUtils.isSameSignals(srcStg, dstStg)) {
                        LogUtils.logInfo("No new signals are inserted in the STG");
                    } else {
                        LogUtils.logInfo("New signals are inserted in the STG");
                        ModelEntry dstMe = new ModelEntry(new StgDescriptor(), dstStg);
                        Path<String> path = srcWe.getWorkspacePath();
                        dstWe = Framework.getInstance().createWork(dstMe, path);
                    }
                } catch (final DeserialisationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return dstWe;
    }

    private WorkspaceEntry handleVerilogSynthesisResult(ExternalProcessOutput mpsatResult,
            boolean sequentialAssign, RenderType renderType) {

        WorkspaceEntry dstWe = null;
        final byte[] verilogOutput = mpsatResult.getFileData(MpsatSynthesisTask.VERILOG_FILE_NAME);
        if ((verilogOutput != null) && (verilogOutput.length > 0)) {
            LogUtils.logInfo("MPSat synthesis result in Verilog format:");
            System.out.println(new String(verilogOutput));
            System.out.println();
        }

        if (MpsatSynthesisSettings.getOpenSynthesisResult() && (verilogOutput != null) && (verilogOutput.length > 0)) {
            try {
                ByteArrayInputStream verilogStream = new ByteArrayInputStream(verilogOutput);
                VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
                Circuit circuit = verilogImporter.importCircuit(verilogStream, mutexes);
                ModelEntry dstMe = new ModelEntry(new CircuitDescriptor(), circuit);

                WorkspaceEntry srcWe = task.getWorkspaceEntry();
                Path<String> path = srcWe.getWorkspacePath();
                dstWe = Framework.getInstance().createWork(dstMe, path);

                final VisualModel visualModel = dstWe.getModelEntry().getVisualModel();
                if (visualModel instanceof VisualCircuit) {
                    VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                    setComponentsRenderStyle(visualCircuit, renderType);
                    String title = srcWe.getModelEntry().getModel().getTitle();
                    visualCircuit.setTitle(title);
                    if (!srcWe.getFile().exists()) {
                        DialogUtils.showError("Unsaved STG cannot be set as the circuit environment.");
                    } else {
                        visualCircuit.setEnvironmentFile(srcWe.getFile());
                        if (srcWe.isChanged()) {
                            DialogUtils.showWarning("The STG with unsaved changes is set as the circuit environment.");
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (Framework.getInstance().isInGuiMode()) {
                                MainWindow mainWindow = Framework.getInstance().getMainWindow();
                                GraphEditorPanel editor = mainWindow.getCurrentEditor();
                                editor.updatePropertyView();
                            }
                        }
                    });
                }
            } catch (final DeserialisationException e) {
                throw new RuntimeException(e);
            }
        }
        return dstWe;
    }

    private void setComponentsRenderStyle(final VisualCircuit visualCircuit, final RenderType renderType) {
        HashSet<String> mutexNames = new HashSet<>();
        for (Mutex me: mutexes) {
            mutexNames.add(me.name);
        }
        for (final VisualFunctionComponent component: visualCircuit.getVisualFunctionComponents()) {
            if (mutexNames.contains(visualCircuit.getNodeMathReference(component))) {
                component.setRenderType(RenderType.BOX);
            } else {
                component.setRenderType(renderType);
            }
        }
        // Redo layout as component shape may have changed.
        AbstractLayoutCommand layoutCommand = visualCircuit.getBestLayouter();
        if (layoutCommand != null) {
            layoutCommand.layout(visualCircuit);
        }
    }

    private void handleFailure(final Result<? extends MpsatSynthesisChainResult> result) {
        String errorMessage = "MPSat synthesis failed.";
        final Throwable genericCause = result.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            final MpsatSynthesisChainResult returnValue = result.getPayload();
            final Result<? extends ExportOutput> exportResult = (returnValue == null) ? null : returnValue.getExportResult();
            final Result<? extends ExternalProcessOutput> punfResult = (returnValue == null) ? null : returnValue.getPunfResult();
            final Result<? extends ExternalProcessOutput> mpsatResult = (returnValue == null) ? null : returnValue.getMpsatResult();
            if ((exportResult != null) && (exportResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nCould not export the model as a .g file.";
                final Throwable exportCause = exportResult.getCause();
                if (exportCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + exportCause.toString();
                }
            } else if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nPunf could not build the unfolding prefix.";
                final Throwable punfCause = punfResult.getCause();
                if (punfCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + punfCause.toString();
                } else {
                    final ExternalProcessOutput punfReturnValue = punfResult.getPayload();
                    if (punfReturnValue != null) {
                        final String punfError = punfReturnValue.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + punfError;
                    }
                }
            } else if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                final Throwable mpsatCause = mpsatResult.getCause();
                if (mpsatCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + mpsatCause.toString();
                } else {
                    final ExternalProcessOutput mpsatReturnValue = mpsatResult.getPayload();
                    if (mpsatReturnValue != null) {
                        final String mpsatError = mpsatReturnValue.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + mpsatError;
                    }
                }
            } else {
                errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
            }
        }
        DialogUtils.showError(errorMessage);
    }

}
