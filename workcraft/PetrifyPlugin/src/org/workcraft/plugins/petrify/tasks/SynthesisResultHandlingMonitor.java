package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SynthesisResultHandlingMonitor extends AbstractResultHandlingMonitor<SynthesisOutput, WorkspaceEntry> {

    private static final Pattern ADDING_STATE_SIGNAL_PATTERN = Pattern.compile(
            "Adding state signal: (.*)\\R", Pattern.UNIX_LINES);

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final Collection<Mutex> mutexes;
    private final boolean boxSequentialComponents;
    private final boolean boxCombinationalComponents;
    private final boolean celementAssign;
    private final boolean sequentialAssign;
    private final boolean technologyMapping;

    public SynthesisResultHandlingMonitor(WorkspaceEntry we, Collection<Mutex> mutexes,
            boolean boxSequentialComponents, boolean boxCombinationalComponents,
            boolean celementAssign, boolean sequentialAssign, boolean technologyMapping) {

        this.we = we;
        this.mutexes = mutexes;
        this.boxSequentialComponents = boxSequentialComponents;
        this.boxCombinationalComponents = boxCombinationalComponents;
        this.celementAssign = celementAssign;
        this.sequentialAssign = sequentialAssign;
        this.technologyMapping = technologyMapping;
    }

    @Override
    public WorkspaceEntry handle(Result<? extends SynthesisOutput> synthResult) {
        WorkspaceEntry weResult = null;
        if (synthResult.isSuccess()) {
            weResult = handleSuccess(synthResult);
        } else if (synthResult.isFailure()) {
            handleFailure(synthResult);
        }
        return weResult;
    }

    private WorkspaceEntry handleSuccess(Result<? extends SynthesisOutput> synthResult) {
        SynthesisOutput synthOutput = synthResult.getPayload();
        String log = synthOutput.getLog();
        if ((log != null) && !log.isEmpty()) {
            LogUtils.logInfo("Petrify synthesis log:");
            System.out.println(log);
        }

        String equations = synthOutput.getEquation();
        if ((equations != null) && !equations.isEmpty()) {
            LogUtils.logInfo("Petrify signal equations:");
            System.out.println(equations);
        }

        // Open STG if new signals are inserted BEFORE importing the Verilog.
        if (PetrifySettings.getOpenSynthesisStg()) {
            StgUtils.createStgWorkIfNewSignals(we, synthOutput.getStg());
        }

        WorkspaceEntry result = handleVerilogSynthesisOutput(synthOutput);

        // Report inserted CSC signals and unmapped signals AFTER importing the Verilog, so the circuit is visible.
        checkCscSignals(synthOutput);
        if (technologyMapping) {
            CircuitUtils.mapUnmappedBuffers(result);
            CircuitUtils.checkUnmappedSignals(result);
        }

        return result;
    }

    private void checkCscSignals(SynthesisOutput synthOutput) {
        String errorMessage = synthOutput.getStderrString();
        Matcher matcher = ADDING_STATE_SIGNAL_PATTERN.matcher(errorMessage);
        if (matcher.find()) {
            DialogUtils.showInfo("CSC conflicts are automatically resolved during synthesis");
        }
    }

    private WorkspaceEntry handleVerilogSynthesisOutput(SynthesisOutput synthOutput) {
        VerilogModule verilogModule = synthOutput.getVerilogModule();
        if (verilogModule == null) {
            return null;
        }

        Circuit circuit;
        try {
            VerilogImporter verilogImporter = new VerilogImporter(celementAssign, sequentialAssign);
            circuit = verilogImporter.createCircuit(verilogModule, mutexes);
        } catch (DeserialisationException e) {
            DialogUtils.showError(e.getMessage());
            return null;
        }

        ModelEntry dstMe = new ModelEntry(new CircuitDescriptor(), circuit);
        Framework framework = Framework.getInstance();
        WorkspaceEntry dstWe = framework.createWork(dstMe, we.getFileName());

        VisualCircuit visualCircuit = WorkspaceUtils.getAs(dstWe, VisualCircuit.class);
        setComponentsRenderStyle(visualCircuit);
        CircuitUtils.setTitleAndEnvironment(visualCircuit, we);
        framework.updatePropertyView();
        return dstWe;
    }

    private void setComponentsRenderStyle(final VisualCircuit visualCircuit) {
        for (VisualFunctionComponent component : visualCircuit.getVisualFunctionComponents()) {
            if (!component.getReferencedComponent().getIsArbitrationPrimitive()) {
                boolean isSequential = component.isSequentialGate();
                if ((isSequential && boxSequentialComponents) || (!isSequential && boxCombinationalComponents)) {
                    component.setRenderType(RenderType.BOX);
                }
            }
        }
        // Redo layout as component shape may have changed.
        AbstractLayoutCommand layoutCommand = visualCircuit.getBestLayouter();
        if (layoutCommand != null) {
            layoutCommand.layout(visualCircuit);
        }
    }

    private void handleFailure(Result<? extends SynthesisOutput> synthResult) {
        String errorMessage = "Petrify synthesis failed.";
        final Throwable genericCause = synthResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.getMessage();
        } else {
            final SynthesisOutput synthOutput = synthResult.getPayload();
            if (synthOutput != null) {
                errorMessage += ERROR_CAUSE_PREFIX + synthOutput.getErrorsHeadAndTail();
            } else {
                errorMessage += "\n\nPetrify task returned failure status without further explanation.";
            }
        }
        DialogUtils.showError(errorMessage);
    }

}
