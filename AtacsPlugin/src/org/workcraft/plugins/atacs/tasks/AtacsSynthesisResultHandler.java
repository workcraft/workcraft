package org.workcraft.plugins.atacs.tasks;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.atacs.AtacsSettings;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.stg.Mutex;
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
        return handleVerilogSynthesisResult(atacsOutput);
    }

    private WorkspaceEntry handleVerilogSynthesisResult(AtacsSynthesisOutput atacsResult) {
        WorkspaceEntry dstWe = null;
        String verilogOutput = atacsResult.getVerilog();
        if ((verilogOutput != null) && !verilogOutput.isEmpty()) {
            LogUtils.logInfo("ATACS synthesis result in Verilog format:");
            System.out.println(verilogOutput);
        }

        if (AtacsSettings.getOpenSynthesisResult() && (verilogOutput != null) && !verilogOutput.isEmpty()) {
            try {
                ByteArrayInputStream verilogStream = new ByteArrayInputStream(verilogOutput.getBytes());
                VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
                Circuit circuit = verilogImporter.importCircuit(verilogStream, mutexes);
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
                        visualCircuit.setEnvironmentFile(we.getFile());
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

    private void setComponentsRenderStyle(final VisualCircuit visualCircuit) {
        HashSet<String> mutexNames = new HashSet<>();
        for (Mutex me: mutexes) {
            mutexNames.add(me.name);
        }
        for (final VisualFunctionComponent component: visualCircuit.getVisualFunctionComponents()) {
            String componentRef = visualCircuit.getNodeMathReference(component);
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
        String errorMessage = "Error: ATACS synthesis failed.";
        if (atacsOutput != null) {
            errorMessage += ERROR_CAUSE_PREFIX + atacsOutput.getStderr();
        }
        DialogUtils.showError(errorMessage);
    }

}
