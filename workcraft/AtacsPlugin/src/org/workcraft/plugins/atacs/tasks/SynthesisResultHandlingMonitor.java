package org.workcraft.plugins.atacs.tasks;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.math.PageNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.verilog.VerilogModule;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.Set;

public class SynthesisResultHandlingMonitor extends AbstractResultHandlingMonitor<AtacsOutput, WorkspaceEntry> {

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final boolean boxSequentialComponents;
    private final boolean boxCombinationalComponents;
    private final boolean celementAssign;
    private final boolean sequentialAssign;
    private final Collection<Mutex> mutexes;

    public SynthesisResultHandlingMonitor(WorkspaceEntry we,
            boolean boxSequentialComponents, boolean boxCombinationalComponents,
            boolean celementAssign, boolean sequentialAssign, Collection<Mutex> mutexes) {

        this.we = we;
        this.boxSequentialComponents = boxSequentialComponents;
        this.boxCombinationalComponents = boxCombinationalComponents;
        this.celementAssign = celementAssign;
        this.sequentialAssign = sequentialAssign;
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry handle(Result<? extends AtacsOutput> result) {
        WorkspaceEntry weResult = null;
        AtacsOutput atacsResult = result.getPayload();
        if (result.isSuccess()) {
            weResult = handleSuccess(atacsResult);
        } else if (result.isFailure()) {
            handleFailure(atacsResult);
        }
        return weResult;
    }

    private WorkspaceEntry handleSuccess(AtacsOutput atacsOutput) {
        return handleVerilogSynthesisOutput(atacsOutput);
    }

    private WorkspaceEntry handleVerilogSynthesisOutput(AtacsOutput atacsOutput) {
        VerilogModule verilogModule = atacsOutput.getVerilogModule();
        if (verilogModule == null) {
            return null;
        }

        Circuit circuit;
        try {
            VerilogImporter verilogImporter = new VerilogImporter(celementAssign, sequentialAssign);
            circuit = verilogImporter.createCircuit(verilogModule, mutexes);
            removePortsForExposedInternalSignals(circuit);
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

    private void removePortsForExposedInternalSignals(Circuit circuit) {
        Stg stg = WorkspaceUtils.getAs(we, Stg.class);
        Set<String> outputSignals = stg.getSignalReferences(Signal.Type.OUTPUT);
        // Add intentionally exposed mutex grants the list of output signals.
        for (Mutex mutex: mutexes) {
            outputSignals.add(mutex.g1.name);
            outputSignals.add(mutex.g2.name);
        }
        // Restore internal signals (except for MUTEX outputs).
        for (Contact port: circuit.getPorts()) {
            String signal = circuit.getNodeReference(port);
            if (port.isOutput() && !outputSignals.contains(signal)) {
                LogUtils.logInfo("Internal signal '" + signal + "' was exposed by ATACS as an output and is restored as internal now.");
                circuit.remove(port);
            }
        }
        // Clean up empty pages
        for (PageNode page: Hierarchy.getDescendantsOfType(circuit.getRoot(), PageNode.class)) {
            if (page.getChildren().isEmpty()) {
                circuit.remove(page);
            }
        }
    }

    private void setComponentsRenderStyle(final VisualCircuit visualCircuit) {
        for (VisualFunctionComponent component : visualCircuit.getVisualFunctionComponents()) {
            if (component.getReferencedComponent().getIsArbitrationPrimitive()) {
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

    private void handleFailure(AtacsOutput atacsOutput) {
        String errorMessage = "Error: ATACS synthesis failed.\n"
                + "Please refer to the log in Output window for details.\n"
                + "Note that ATACS requires STG to have Complete State Coding.";
        if (atacsOutput != null) {
            errorMessage += ERROR_CAUSE_PREFIX + atacsOutput.getStderrString();
        }
        DialogUtils.showError(errorMessage);
    }

}
