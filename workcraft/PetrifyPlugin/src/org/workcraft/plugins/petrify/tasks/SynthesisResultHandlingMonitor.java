package org.workcraft.plugins.petrify.tasks;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.references.ReferenceHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitDescriptor;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.plugins.stg.utils.StgUtils;
import org.workcraft.tasks.AbstractResultHandlingMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SynthesisResultHandlingMonitor extends AbstractResultHandlingMonitor<SynthesisOutput, WorkspaceEntry> {

    private static final Pattern patternAddingStateSignal = Pattern.compile(
            "Adding state signal: (.*)\\R", Pattern.UNIX_LINES);

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final Collection<Mutex> mutexes;
    private final boolean boxSequentialComponents;
    private final boolean boxCombinationalComponents;
    private final boolean sequentialAssign;
    private final boolean technologyMapping;

    public SynthesisResultHandlingMonitor(WorkspaceEntry we, Collection<Mutex> mutexes,
            boolean boxSequentialComponents, boolean boxCombinationalComponents,
            boolean sequentialAssign, boolean technologyMapping) {

        this.we = we;
        this.mutexes = mutexes;
        this.boxSequentialComponents = boxSequentialComponents;
        this.boxCombinationalComponents = boxCombinationalComponents;
        this.sequentialAssign = sequentialAssign;
        this.technologyMapping = technologyMapping;
    }

    @Override
    public WorkspaceEntry handle(Result<? extends SynthesisOutput> synthResult) {
        WorkspaceEntry weResult = null;
        if (synthResult.getOutcome() == Outcome.SUCCESS) {
            weResult = handleSuccess(synthResult);
        } else if (synthResult.getOutcome() == Outcome.FAILURE) {
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
            LogUtils.logInfo("Petrify synthesis result in EQN format:");
            System.out.println(equations);
        }

        // Open STG if new signals are inserted BEFORE importing the Verilog.
        handleStgSynthesisOutput(synthOutput);

        WorkspaceEntry result = handleVerilogSynthesisOutput(synthOutput);

        // Report inserted CSC signals and unmapped signals AFTER importing the Verilog, so the circuit is visible.
        checkNewSignals(synthOutput);
        if (technologyMapping) {
            CircuitUtils.mapUnmappedBuffers(result);
            CircuitUtils.checkUnmappedSignals(result);
        }

        return result;
    }

    private void checkNewSignals(SynthesisOutput synthOutput) {
        String errorMessage = synthOutput.getStderrString();
        List<String> stateSignals = getMatchedSignals(errorMessage, patternAddingStateSignal);
        if (!stateSignals.isEmpty()) {
            String msg = ReferenceHelper.getTextWithReferences(
                    "CSC conflicts are automatically resolved by inserting signal", stateSignals);
            DialogUtils.showInfo(msg);
        }
    }

    private List<String> getMatchedSignals(String errorMessage, Pattern pattern) {
        List<String> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(errorMessage);
        while (matcher.find()) {
            result.add(matcher.group(1));
        }
        return result;
    }

    private WorkspaceEntry handleStgSynthesisOutput(SynthesisOutput synthOutput) {
        if (PetrifySettings.getOpenSynthesisStg()) {
            return StgUtils.createStgIfNewSignals(we, synthOutput.getStg().getBytes());
        }
        return null;
    }

    private WorkspaceEntry handleVerilogSynthesisOutput(SynthesisOutput synthOutput) {
        WorkspaceEntry result = null;
        String verilogOutput = synthOutput.getVerilog();
        if ((verilogOutput != null) && !verilogOutput.isEmpty()) {
            LogUtils.logInfo("Petrify synthesis result in Verilog format:");
            System.out.println(verilogOutput);
        }

        if ((verilogOutput != null) && !verilogOutput.isEmpty()) {
            try {
                ByteArrayInputStream verilogStream = new ByteArrayInputStream(verilogOutput.getBytes());
                VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
                Circuit circuit = verilogImporter.importTopModule(verilogStream, mutexes);
                Path<String> path = we.getWorkspacePath();
                ModelEntry dstMe = new ModelEntry(new CircuitDescriptor(), circuit);
                Framework framework = Framework.getInstance();
                result = framework.createWork(dstMe, path);

                VisualModel visualModel = result.getModelEntry().getVisualModel();
                if (visualModel instanceof VisualCircuit) {
                    final VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                    setComponentsRenderStyle(visualCircuit);
                    visualCircuit.setTitle(we.getModelTitle());
                    if (!we.getFile().exists()) {
                        DialogUtils.showError("Unsaved STG cannot be set as the circuit environment.");
                    } else {
                        visualCircuit.getMathModel().setEnvironmentFile(we.getFile());
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
        return result;
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

    private void handleFailure(Result<? extends SynthesisOutput> synthResult) {
        String errorMessage = "Petrify synthesis failed.";
        final Throwable genericCause = synthResult.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
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
