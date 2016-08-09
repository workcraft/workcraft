/**
 *
 */
package org.workcraft.plugins.mpsat.tasks;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
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
import org.workcraft.plugins.mpsat.MpsatSynthesisMode;
import org.workcraft.plugins.mpsat.MpsatSynthesisUtilitySettings;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class MpsatSynthesisResultHandler extends DummyProgressMonitor<MpsatSynthesisChainResult> {
    private static final String TITLE = "MPSat synthesis";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";
    private final MpsatSynthesisChainTask task;

    public MpsatSynthesisResultHandler(MpsatSynthesisChainTask task) {
        this.task = task;
    }

    @Override
    public void finished(final Result<? extends MpsatSynthesisChainResult> result, String description) {
        switch (result.getOutcome()) {
        case FINISHED:
            handleSuccess(result);
            break;
        case FAILED:
            handleFailure(result);
            break;
        default:
            break;
        }
    }

    private void handleSuccess(final Result<? extends MpsatSynthesisChainResult> result) {
        MpsatSynthesisChainResult returnValue = result.getReturnValue();
        final MpsatSynthesisMode mpsatMode = returnValue.getMpsatSettings().getMode();
        ExternalProcessResult mpsatReturnValue = returnValue.getMpsatResult().getReturnValue();
        switch (mpsatMode) {
        case COMPLEX_GATE_IMPLEMENTATION:
            handleSynthesisResult(mpsatReturnValue, false, RenderType.GATE);
            break;
        case GENERALISED_CELEMENT_IMPLEMENTATION:
            handleSynthesisResult(mpsatReturnValue, true, RenderType.BOX);
            break;
        case STANDARD_CELEMENT_IMPLEMENTATION:
            handleSynthesisResult(mpsatReturnValue, true, RenderType.GATE);
            break;
        case TECH_MAPPING:
            handleSynthesisResult(mpsatReturnValue, false, RenderType.GATE);
            break;
        default:
            MainWindow mainWindow = Framework.getInstance().getMainWindow();
            JOptionPane.showMessageDialog(mainWindow,
                    "Warning: MPSat synthesis mode \'" + mpsatMode.getArgument() + "\' is not (yet) supported.",
                    TITLE, JOptionPane.WARNING_MESSAGE);
            break;
        }
    }

    private void handleSynthesisResult(ExternalProcessResult mpsatResult, boolean sequentialAssign, RenderType renderType) {
        final String log = new String(mpsatResult.getOutput());
        if ((log != null) && !log.isEmpty()) {
            System.out.println(log);
            System.out.println();
        }
        byte[] eqnOutput = mpsatResult.getFileContent(MpsatSynthesisTask.EQN_FILE_NAME);
        if (eqnOutput != null) {
            LogUtils.logInfoLine("MPSat synthesis result in EQN format:");
            System.out.println(new String(eqnOutput));
            System.out.println();
        }
        byte[] verilogOutput = mpsatResult.getFileContent(MpsatSynthesisTask.VERILOG_FILE_NAME);
        if (verilogOutput != null) {
            LogUtils.logInfoLine("MPSat synthesis result in Verilog format:");
            System.out.println(new String(verilogOutput));
            System.out.println();
        }
        if (MpsatSynthesisUtilitySettings.getOpenSynthesisResult() && (verilogOutput != null)) {
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(verilogOutput);
                VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
                final Circuit circuit = verilogImporter.importCircuit(in);
                final WorkspaceEntry we = task.getWorkspaceEntry();
                Path<String> path = we.getWorkspacePath();
                final Path<String> directory = path.getParent();
                final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
                final ModelEntry me = new ModelEntry(new CircuitDescriptor(), circuit);
                boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();

                final Framework framework = Framework.getInstance();
                final Workspace workspace = framework.getWorkspace();
                WorkspaceEntry newWorkspaceEntry = workspace.add(directory, name, me, true, openInEditor);
                VisualModel visualModel = newWorkspaceEntry.getModelEntry().getVisualModel();
                if (visualModel instanceof VisualCircuit) {
                    VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                    for (VisualFunctionComponent component: visualCircuit.getVisualFunctionComponents()) {
                        component.setRenderType(renderType);
                    }
                    String title = we.getModelEntry().getModel().getTitle();
                    visualCircuit.setTitle(title);
                    if (!we.getFile().exists()) {
                        JOptionPane.showMessageDialog(null,
                                "Error: Unsaved STG cannot be set as the circuit environment.",
                                TITLE, JOptionPane.ERROR_MESSAGE);
                    } else {
                        visualCircuit.setEnvironmentFile(we.getFile());
                        if (we.isChanged()) {
                            JOptionPane.showMessageDialog(null,
                                    "Warning: The STG with unsaved changes is set as the circuit environment.",
                                    TITLE, JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            framework.getMainWindow().getCurrentEditor().updatePropertyView();
                        }
                    });
                }
            } catch (DeserialisationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void handleFailure(final Result<? extends MpsatSynthesisChainResult> result) {
        String errorMessage = "Error: MPSat synthesis failed.";
        Throwable genericCause = result.getCause();
        if (genericCause != null) {
            // Exception was thrown somewhere in the chain task run() method (not in any of the subtasks)
            errorMessage += ERROR_CAUSE_PREFIX + genericCause.toString();
        } else {
            MpsatSynthesisChainResult returnValue = result.getReturnValue();
            Result<? extends Object> exportResult = (returnValue == null) ? null : returnValue.getExportResult();
            Result<? extends ExternalProcessResult> punfResult = (returnValue == null) ? null : returnValue.getPunfResult();
            Result<? extends ExternalProcessResult> mpsatResult = (returnValue == null) ? null : returnValue.getMpsatResult();
            if ((exportResult != null) && (exportResult.getOutcome() == Outcome.FAILED)) {
                errorMessage += "\n\nCould not export the model as a .g file.";
                Throwable exportCause = exportResult.getCause();
                if (exportCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + exportCause.toString();
                }
            } else if ((punfResult != null) && (punfResult.getOutcome() == Outcome.FAILED)) {
                errorMessage += "\n\nPunf could not build the unfolding prefix.";
                Throwable punfCause = punfResult.getCause();
                if (punfCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + punfCause.toString();
                } else {
                    ExternalProcessResult punfReturnValue = punfResult.getReturnValue();
                    if (punfReturnValue != null) {
                        errorMessage += ERROR_CAUSE_PREFIX + new String(punfReturnValue.getErrors());
                    }
                }
            } else if ((mpsatResult != null) && (mpsatResult.getOutcome() == Outcome.FAILED)) {
                errorMessage += "\n\nMPSat did not execute as expected.";
                Throwable mpsatCause = mpsatResult.getCause();
                if (mpsatCause != null) {
                    errorMessage += ERROR_CAUSE_PREFIX + mpsatCause.toString();
                } else {
                    ExternalProcessResult mpsatReturnValue = mpsatResult.getReturnValue();
                    if (mpsatReturnValue != null) {
                        String mpsatError = new String(mpsatReturnValue.getErrors());
                        errorMessage += ERROR_CAUSE_PREFIX + mpsatError;
                    }
                }
            } else {
                errorMessage += "\n\nMPSat chain task returned failure status without further explanation.";
            }
        }
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        JOptionPane.showMessageDialog(mainWindow, errorMessage, TITLE, JOptionPane.ERROR_MESSAGE);
    }

}
