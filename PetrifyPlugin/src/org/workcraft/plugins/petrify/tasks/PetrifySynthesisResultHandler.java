package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
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
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.plugins.stg.Mutex;
import org.workcraft.tasks.AbstractExtendedResultHandler;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifySynthesisResultHandler extends AbstractExtendedResultHandler<PetrifySynthesisResult, WorkspaceEntry> {

    private static final Pattern patternAddingStateSignal =
            Pattern.compile("Adding state signal: (.*)\n", Pattern.UNIX_LINES);

    private static final String ERROR_CAUSE_PREFIX = "\n\n";

    private final WorkspaceEntry we;
    private final boolean boxSequentialComponents;
    private final boolean boxCombinationalComponents;
    private final boolean sequentialAssign;
    private final Collection<Mutex> mutexes;

    public PetrifySynthesisResultHandler(final WorkspaceEntry we, final boolean boxSequentialComponents,
            final boolean boxCombinationalComponents, final boolean sequentialAssign, Collection<Mutex> mutexes) {
        this.we = we;
        this.boxSequentialComponents = boxSequentialComponents;
        this.boxCombinationalComponents = boxCombinationalComponents;
        this.sequentialAssign = sequentialAssign;
        this.mutexes = mutexes;
    }

    @Override
    public WorkspaceEntry handleResult(final Result<? extends PetrifySynthesisResult> result) {
        WorkspaceEntry weResult = null;
        if (result.getOutcome() == Outcome.SUCCESS) {
            weResult = handleSuccess(result);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(result);
        }
        return weResult;
    }

    private WorkspaceEntry handleSuccess(final Result<? extends PetrifySynthesisResult> result) {
        WorkspaceEntry synthResult = null;

        final String log = result.getReturnValue().getLog();
        if ((log != null) && !log.isEmpty()) {
            LogUtils.logInfo("Petrify synthesis log:");
            System.out.println(log);
        }

        final String equations = result.getReturnValue().getEquation();
        if ((equations != null) && !equations.isEmpty()) {
            LogUtils.logInfo("Petrify synthesis result in EQN format:");
            System.out.println(equations);
        }

        final String verilog = result.getReturnValue().getVerilog();
        if ((verilog != null) && !verilog.isEmpty()) {
            LogUtils.logInfo("Petrify synthesis result in Verilog format:");
            System.out.println(verilog);
        }

        String errorMessage = new String(result.getReturnValue().getStderr());
        String signalNames = "";
        Matcher matcher = patternAddingStateSignal.matcher(errorMessage);
        while (matcher.find()) {
            if (!signalNames.isEmpty()) {
                signalNames += ", ";
            }
            signalNames += matcher.group(1);
        }
        if (!signalNames.isEmpty()) {
            LogUtils.logWarning("Petrify automatically resolved CSC conflicts by inserting new signals: " + signalNames);
        }

        if (PetrifySettings.getOpenSynthesisResult() && (verilog != null) && !verilog.isEmpty()) {
            try {
                final ByteArrayInputStream in = new ByteArrayInputStream(verilog.getBytes());
                final VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
                final Circuit circuit = verilogImporter.importCircuit(in, mutexes);
                final Path<String> path = we.getWorkspacePath();
                final ModelEntry me = new ModelEntry(new CircuitDescriptor(), circuit);
                final Framework framework = Framework.getInstance();
                synthResult = framework.createWork(me, path);
                final VisualModel visualModel = synthResult.getModelEntry().getVisualModel();
                if (visualModel instanceof VisualCircuit) {
                    final VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                    setComponentsRenderStyle(visualCircuit);
                    final String title = we.getModelEntry().getModel().getTitle();
                    visualCircuit.setTitle(title);
                    if (!we.getFile().exists()) {
                        DialogUtils.showError("Unsaved STG cannot be set as the circuit environment.");
                    } else {
                        visualCircuit.setEnvironmentFile(we.getFile());
                        if (we.isChanged()) {
                            DialogUtils.showWarning("The STG with unsaved changes is set as the circuit environment.");
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            final MainWindow mainWindow = framework.getMainWindow();
                            if (mainWindow != null) {
                                mainWindow.getCurrentEditor().updatePropertyView();
                            }
                        }
                    });
                }
            } catch (final DeserialisationException e) {
                throw new RuntimeException(e);
            }
        }
        return synthResult;
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

    private void handleFailure(final Result<? extends PetrifySynthesisResult> result) {
        String errorMessage = "Error: Petrify synthesis failed.";
        final PetrifySynthesisResult returnValue = result.getReturnValue();
        if (returnValue != null) {
            errorMessage += ERROR_CAUSE_PREFIX + returnValue.getStderr();
        }
        DialogUtils.showError(errorMessage);
    }

}
