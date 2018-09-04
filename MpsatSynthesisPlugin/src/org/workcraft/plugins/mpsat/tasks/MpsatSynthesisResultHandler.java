package org.workcraft.plugins.mpsat.tasks;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.mpsat.MpsatSynthesisMode;
import org.workcraft.plugins.mpsat.MpsatSynthesisSettings;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.shared.tasks.ExportOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgUtils;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashSet;

public class MpsatSynthesisResultHandler extends AbstractExtendedResultHandler<MpsatSynthesisChainOutput, WorkspaceEntry> {
    private static final String ERROR_CAUSE_PREFIX = "\n\n";
    private final MpsatSynthesisChainTask task;
    private final Collection<Mutex> mutexes;

    public MpsatSynthesisResultHandler(final MpsatSynthesisChainTask task, Collection<Mutex> mutexes) {
        this.task = task;
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry handleResult(final Result<? extends MpsatSynthesisChainOutput> result) {
        WorkspaceEntry weResult = null;
        if (result.getOutcome() == Outcome.SUCCESS) {
            weResult = handleSuccess(result);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(result);
        }
        return weResult;
    }

    private WorkspaceEntry handleSuccess(final Result<? extends MpsatSynthesisChainOutput> chainResult) {
        MpsatSynthesisChainOutput chainOutput = chainResult.getPayload();
        MpsatSynthesisMode mpsatMode = chainOutput.getMpsatSettings().getMode();
        MpsatSynthesisOutput mpsatOutput = chainOutput.getMpsatResult().getPayload();
        WorkspaceEntry synthResult = null;
        switch (mpsatMode) {
        case COMPLEX_GATE_IMPLEMENTATION:
            synthResult = handleSynthesisOutput(mpsatOutput, false, RenderType.GATE, false);
            break;
        case GENERALISED_CELEMENT_IMPLEMENTATION:
            synthResult = handleSynthesisOutput(mpsatOutput, true, RenderType.BOX, false);
            break;
        case STANDARD_CELEMENT_IMPLEMENTATION:
            synthResult = handleSynthesisOutput(mpsatOutput, true, RenderType.GATE, false);
            break;
        case TECH_MAPPING:
            synthResult = handleSynthesisOutput(mpsatOutput, false, RenderType.GATE, true);
            break;
        default:
            DialogUtils.showWarning("MPSat synthesis mode \'" + mpsatMode.getArgument() + "\' is not (yet) supported.");
            break;
        }
        return synthResult;
    }

    private WorkspaceEntry handleSynthesisOutput(MpsatSynthesisOutput mpsatOutput,
            boolean sequentialAssign, RenderType renderType, boolean technologyMapping) {

        final String log = mpsatOutput.getStdoutString();
        if (!log.isEmpty()) {
            System.out.println(log);
            System.out.println();
        }

        // Open STG if new signals are inserted BEFORE importing the Verilog.
        handleStgSynthesisOutput(mpsatOutput);

        WorkspaceEntry result = handleVerilogSynthesisOutput(mpsatOutput, sequentialAssign, renderType);

        // Report unmapped signals AFTER importing the Verilog, so the circuit is visible.
        if (technologyMapping) {
            CircuitUtils.checkUnmappedSignals(result);
        }

        return result;
    }

    private WorkspaceEntry handleStgSynthesisOutput(MpsatSynthesisOutput mpsatOutput) {
        if (MpsatSynthesisSettings.getOpenSynthesisStg()) {
            return StgUtils.createStgIfNewSignals(task.getWorkspaceEntry(), mpsatOutput.getStgOutput());
        }
        return null;
    }

    private WorkspaceEntry handleVerilogSynthesisOutput(MpsatSynthesisOutput mpsatOutput,
            boolean sequentialAssign, RenderType renderType) {

        WorkspaceEntry dstWe = null;
        final byte[] verilogOutput = mpsatOutput.getVerilogOutput();
        if ((verilogOutput != null) && (verilogOutput.length > 0)) {
            LogUtils.logInfo("MPSat synthesis result in Verilog format:");
            System.out.println(new String(verilogOutput));
            System.out.println();
        }

        if ((verilogOutput != null) && (verilogOutput.length > 0)) {
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
                    if (Framework.getInstance().isInGuiMode()) {
                        GraphEditorPanel editor = Framework.getInstance().getMainWindow().getCurrentEditor();
                        SwingUtilities.invokeLater(() -> editor.updatePropertyView());
                    }
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

    private void handleFailure(final Result<? extends MpsatSynthesisChainOutput> chainResult) {
        String errorMessage = "MPSat synthesis failed.";
        final Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            final MpsatSynthesisChainOutput chainOutput = chainResult.getPayload();
            final Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            final Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            final Result<? extends MpsatSynthesisOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
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
                    final PunfOutput punfOutput = punfResult.getPayload();
                    if (punfOutput != null) {
                        final String punfErrorMessage = punfOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + punfErrorMessage;
                    }
                }
            } else if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILURE)) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                final Throwable mpsatCause = mpsatResult.getCause();
                if (mpsatCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + mpsatCause.toString();
                } else {
                    final MpsatSynthesisOutput mpsatOutput = mpsatResult.getPayload();
                    if (mpsatOutput != null) {
                        final String mpsatErrorMessage = mpsatOutput.getErrorsHeadAndTail();
                        errorMessage += ERROR_CAUSE_PREFIX + mpsatErrorMessage;
                    }
                }
            } else {
                errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
            }
        }
        DialogUtils.showError(errorMessage);
    }

}
