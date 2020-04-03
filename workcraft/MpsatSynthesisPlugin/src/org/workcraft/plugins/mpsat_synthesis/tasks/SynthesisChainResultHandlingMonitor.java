package org.workcraft.plugins.mpsat_synthesis.tasks;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.mpsat_synthesis.MpsatSynthesisSettings;
import org.workcraft.plugins.mpsat_synthesis.SynthesisMode;
import org.workcraft.plugins.punf.tasks.PunfOutput;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.StgModel;
import org.workcraft.plugins.stg.interop.StgImporter;
import org.workcraft.plugins.stg.utils.MutexUtils;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.ExportOutput;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashSet;

public class SynthesisChainResultHandlingMonitor extends AbstractResultHandlingMonitor<SynthesisChainOutput, WorkspaceEntry> {

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final Collection<Mutex> mutexes;

    public SynthesisChainResultHandlingMonitor(WorkspaceEntry we, Collection<Mutex> mutexes) {
        this.we = we;
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry handle(final Result<? extends SynthesisChainOutput> chainResult) {
        WorkspaceEntry result = null;
        if (chainResult.getOutcome() == Outcome.SUCCESS) {
            result = handleSuccess(chainResult);
        } else if (chainResult.getOutcome() == Outcome.FAILURE) {
            handleFailure(chainResult);
        }
        return result;
    }

    private WorkspaceEntry handleSuccess(final Result<? extends SynthesisChainOutput> chainResult) {
        SynthesisChainOutput chainOutput = chainResult.getPayload();
        SynthesisMode mpsatMode = chainOutput.getSynthesisMode();
        MpsatOutput mpsatOutput = chainOutput.getMpsatResult().getPayload();
        switch (mpsatMode) {
        case RESOLVE_ENCODING_CONFLICTS:
            return handleConflictResolutionOutput(mpsatOutput);

        case COMPLEX_GATE_IMPLEMENTATION:
            return handleSynthesisOutput(mpsatOutput, false, RenderType.GATE, false);

        case GENERALISED_CELEMENT_IMPLEMENTATION:
            return handleSynthesisOutput(mpsatOutput, true, RenderType.BOX, false);

        case STANDARD_CELEMENT_IMPLEMENTATION:
            return handleSynthesisOutput(mpsatOutput, true, RenderType.GATE, false);

        case TECH_MAPPING:
            return handleSynthesisOutput(mpsatOutput, false, RenderType.GATE, true);

        default:
            DialogUtils.showWarning(mpsatMode.name() + " is not supported by MPSat synthesis.");
            return null;
        }
    }

    public WorkspaceEntry handleConflictResolutionOutput(MpsatOutput mpsatOutput) {
        try {
            ByteArrayInputStream dstStream = new ByteArrayInputStream(mpsatOutput.getStgOutput());
            StgModel model = new StgImporter().importStg(dstStream);
            if (model == null) {
                final String errorMessage = mpsatOutput.getErrorsHeadAndTail();
                DialogUtils.showWarning("Conflict resolution failed. MPSat output: \n" + errorMessage);
                return null;
            }
            model.setTitle(we.getModelTitle());
            MutexUtils.restoreMutexSignals(model, mutexes);
            MutexUtils.restoreMutexPlacesByName(model, mutexes);
            final ModelEntry me = new ModelEntry(new StgDescriptor(), model);
            final Path<String> path = we.getWorkspacePath();
            return Framework.getInstance().createWork(me, path);
        } catch (DeserialisationException e) {
            throw new RuntimeException(e);
        }
    }

    private WorkspaceEntry handleSynthesisOutput(MpsatOutput mpsatOutput,
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

    private WorkspaceEntry handleStgSynthesisOutput(MpsatOutput mpsatOutput) {
        if (MpsatSynthesisSettings.getOpenSynthesisStg()) {
            return StgUtils.createStgIfNewSignals(we, mpsatOutput.getStgOutput());
        }
        return null;
    }

    private WorkspaceEntry handleVerilogSynthesisOutput(MpsatOutput mpsatOutput,
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
                Circuit circuit = verilogImporter.importTopModule(verilogStream, mutexes);
                ModelEntry dstMe = new ModelEntry(new CircuitDescriptor(), circuit);

                Path<String> path = we.getWorkspacePath();
                dstWe = Framework.getInstance().createWork(dstMe, path);

                final VisualModel visualModel = dstWe.getModelEntry().getVisualModel();
                if (visualModel instanceof VisualCircuit) {
                    VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                    setComponentsRenderStyle(visualCircuit, renderType);
                    visualCircuit.setTitle(we.getModelTitle());
                    if (!we.getFile().exists()) {
                        DialogUtils.showError("Unsaved STG cannot be set as the circuit environment.");
                    } else {
                        visualCircuit.getMathModel().setEnvironmentFile(we.getFile());
                        if (we.isChanged()) {
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
            if (mutexNames.contains(visualCircuit.getMathReference(component))) {
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

    private void handleFailure(final Result<? extends SynthesisChainOutput> chainResult) {
        String errorMessage = "MPSat synthesis failed.";
        final Throwable genericCause = chainResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            final SynthesisChainOutput chainOutput = chainResult.getPayload();
            final Result<? extends ExportOutput> exportResult = (chainOutput == null) ? null : chainOutput.getExportResult();
            final Result<? extends PunfOutput> punfResult = (chainOutput == null) ? null : chainOutput.getPunfResult();
            final Result<? extends MpsatOutput> mpsatResult = (chainOutput == null) ? null : chainOutput.getMpsatResult();
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
                    final MpsatOutput mpsatOutput = mpsatResult.getPayload();
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
