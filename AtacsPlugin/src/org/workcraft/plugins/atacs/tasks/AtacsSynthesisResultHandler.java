package org.workcraft.plugins.atacs.tasks;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.utils.EnvironmentUtils;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.Signal;
import org.workcraft.plugins.stg.Stg;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.workspace.WorkspaceUtils;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AtacsSynthesisResultHandler extends AbstractExtendedResultHandler<AtacsSynthesisOutput, WorkspaceEntry> {

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final boolean boxSequentialComponents;
    private final boolean boxCombinationalComponents;
    private final boolean sequentialAssign;
    private final Collection<Mutex> mutexes;

    public AtacsSynthesisResultHandler(WorkspaceEntry we,
            boolean boxSequentialComponents, boolean boxCombinationalComponents,
            boolean sequentialAssign, Collection<Mutex> mutexes) {

        this.we = we;
        this.boxSequentialComponents = boxSequentialComponents;
        this.boxCombinationalComponents = boxCombinationalComponents;
        this.sequentialAssign = sequentialAssign;
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry handleResult(Result<? extends AtacsSynthesisOutput> result) {
        WorkspaceEntry weResult = null;
        AtacsSynthesisOutput atacsResult = result.getPayload();
        if (result.getOutcome() == Outcome.SUCCESS) {
            weResult = handleSuccess(atacsResult);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(atacsResult);
        }
        return weResult;
    }

    private WorkspaceEntry handleSuccess(AtacsSynthesisOutput atacsOutput) {
        return handleVerilogSynthesisOutput(atacsOutput);
    }

    private WorkspaceEntry handleVerilogSynthesisOutput(AtacsSynthesisOutput atacsOutput) {
        WorkspaceEntry dstWe = null;
        String verilogOutput = atacsOutput.getVerilog();
        if ((verilogOutput != null) && !verilogOutput.isEmpty()) {
            LogUtils.logInfo("ATACS synthesis result in Verilog format:");
            System.out.println(verilogOutput);
        }

        if ((verilogOutput != null) && !verilogOutput.isEmpty()) {
            try {
                ByteArrayInputStream verilogStream = new ByteArrayInputStream(verilogOutput.getBytes());
                VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
                Circuit circuit = verilogImporter.importCircuit(verilogStream, mutexes);

                removePortsForExposedInternalSignals(circuit);

                Path<String> path = we.getWorkspacePath();
                ModelEntry dstMe = new ModelEntry(new CircuitDescriptor(), circuit);
                Framework framework = Framework.getInstance();
                dstWe = framework.createWork(dstMe, path);

                VisualModel visualModel = dstWe.getModelEntry().getVisualModel();
                if (visualModel instanceof VisualCircuit) {
                    final VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                    setComponentsRenderStyle(visualCircuit);
                    String title = we.getModelEntry().getModel().getTitle();
                    visualCircuit.setTitle(title);
                    if (!we.getFile().exists()) {
                        DialogUtils.showError("Unsaved STG cannot be set as the circuit environment.");
                    } else {
                        EnvironmentUtils.setEnvironmentFile(visualCircuit.getMathModel(), we.getFile());
                        if (we.isChanged()) {
                            DialogUtils.showWarning("The STG with unsaved changes is set as the circuit environment.");
                        }
                    }
                    MainWindow mainWindow = framework.getMainWindow();
                    if (mainWindow != null) {
                        SwingUtilities.invokeLater(() -> mainWindow.getCurrentEditor().updatePropertyView());
                    }
                }
            } catch (final DeserialisationException e) {
                throw new RuntimeException(e);
            }
        }
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
        HashSet<String> mutexNames = new HashSet<>();
        for (Mutex me: mutexes) {
            mutexNames.add(me.name);
        }
        for (final VisualFunctionComponent component: visualCircuit.getVisualFunctionComponents()) {
            String componentRef = visualCircuit.getMathReference(component);
            if (mutexNames.contains(componentRef)) {
                component.setRenderType(RenderType.BOX);
            } else if (component.isSequentialGate()) {
                if (boxSequentialComponents) {
                    component.setRenderType(RenderType.BOX);
                }
            } else {
                if (boxCombinationalComponents) {
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

    private void handleFailure(AtacsSynthesisOutput atacsOutput) {
        String errorMessage = "Error: ATACS synthesis failed.\n"
                + "Please refer to the log in Output window for details.\n"
                + "Note that ATACS requires STG to have Complete State Coding.";
        if (atacsOutput != null) {
            errorMessage += ERROR_CAUSE_PREFIX + atacsOutput.getStderrString();
        }
        DialogUtils.showError(errorMessage);
    }

}
