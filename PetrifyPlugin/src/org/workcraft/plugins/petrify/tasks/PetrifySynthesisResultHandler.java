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

public class PetrifySynthesisResultHandler extends AbstractExtendedResultHandler<PetrifySynthesisResult, WorkspaceEntry> {

    private static final Pattern patternAddingStateSignal = Pattern.compile(
            "Adding state signal: (.*)\\R", Pattern.UNIX_LINES);

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
        PetrifySynthesisResult petrifyResult = result.getPayload();
        if (result.getOutcome() == Outcome.SUCCESS) {
            weResult = handleSuccess(petrifyResult);
        } else if (result.getOutcome() == Outcome.FAILURE) {
            handleFailure(petrifyResult);
        }
        return weResult;
    }

    private WorkspaceEntry handleSuccess(PetrifySynthesisResult petrifyResult) {
        String log = petrifyResult.getLog();
        if ((log != null) && !log.isEmpty()) {
            LogUtils.logInfo("Petrify synthesis log:");
            System.out.println(log);
        }

        handleStgSynthesisResult(petrifyResult);

        String equations = petrifyResult.getEquation();
        if ((equations != null) && !equations.isEmpty()) {
            LogUtils.logInfo("Petrify synthesis result in EQN format:");
            System.out.println(equations);
        }

        WorkspaceEntry result = handleVerilogSynthesisResult(petrifyResult);

        String errorMessage = new String(petrifyResult.getStderr());
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

        return result;
    }

    private WorkspaceEntry handleStgSynthesisResult(PetrifySynthesisResult petrifyResult) {
        WorkspaceEntry dstWe = null;
        String dstOutput = petrifyResult.getStg();
        if (PetrifySettings.getOpenSynthesisStg() && (dstOutput != null) && !dstOutput.isEmpty()) {
            Stg srcStg = WorkspaceUtils.getAs(we, Stg.class);
            try {
                ByteArrayInputStream dstStream = new ByteArrayInputStream(dstOutput.getBytes());
                StgModel dstStg = new StgImporter().importStg(dstStream);
                if (StgUtils.isSameSignals(srcStg, dstStg)) {
                    LogUtils.logInfo("No new signals are inserted in the STG");
                } else {
                    LogUtils.logInfo("New signals are inserted in the STG");
                    ModelEntry dstMe = new ModelEntry(new StgDescriptor(), dstStg);
                    Path<String> path = we.getWorkspacePath();
                    dstWe = Framework.getInstance().createWork(dstMe, path);
                }
            } catch (final DeserialisationException e) {
                throw new RuntimeException(e);
            }
        }
        return dstWe;
    }

    private WorkspaceEntry handleVerilogSynthesisResult(PetrifySynthesisResult petrifyResult) {
        WorkspaceEntry dstWe = null;
        String verilogOutput = petrifyResult.getVerilog();
        if ((verilogOutput != null) && !verilogOutput.isEmpty()) {
            LogUtils.logInfo("Petrify synthesis result in Verilog format:");
            System.out.println(verilogOutput);
        }

        if (PetrifySettings.getOpenSynthesisResult() && (verilogOutput != null) && !verilogOutput.isEmpty()) {
            try {
                ByteArrayInputStream verilogStream = new ByteArrayInputStream(verilogOutput.getBytes());
                VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
                Circuit circuit = verilogImporter.importCircuit(verilogStream, mutexes);
                Path<String> path = we.getWorkspacePath();
                ModelEntry dstMe = new ModelEntry(new CircuitDescriptor(), circuit);
                dstWe = Framework.getInstance().createWork(dstMe, path);

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
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            MainWindow mainWindow = Framework.getInstance().getMainWindow();
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

    private void handleFailure(PetrifySynthesisResult petrifyResult) {
        String errorMessage = "Error: Petrify synthesis failed.";
        if (petrifyResult != null) {
            errorMessage += ERROR_CAUSE_PREFIX + petrifyResult.getStderr();
        }
        DialogUtils.showError(errorMessage);
    }

}
