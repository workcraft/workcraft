package org.workcraft.plugins.petrify;

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
import org.workcraft.plugins.circuit.CircuitSettings;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.interop.VerilogImporter;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.petrify.tasks.SynthesisResult;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class SynthesisResultHandler extends DummyProgressMonitor<SynthesisResult> {
    private static final String TITLE = "Petrify synthesis";
    private static final String ERROR_CAUSE_PREFIX  = "\n\n";
    private final WorkspaceEntry we;
    private final boolean boxSequentialComponents;
    private final boolean boxCombinationalComponents;

    public SynthesisResultHandler(WorkspaceEntry we, boolean boxSequentialComponents, boolean boxCombinationalComponents) {
        this.we = we;
        this.boxSequentialComponents = boxSequentialComponents;
        this.boxCombinationalComponents = boxCombinationalComponents;
    }

    @Override
    public void finished(final Result<? extends SynthesisResult> result, String description) {
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

    private void handleSuccess(final Result<? extends SynthesisResult> result) {
        String log = result.getReturnValue().getLog();
        if ((log != null) && !log.isEmpty()) {
            LogUtils.logInfoLine("Petrify synthesis log:");
            System.out.println(log);
        }

        String equations = result.getReturnValue().getEquation();
        if ((equations != null) && !equations.isEmpty()) {
            LogUtils.logInfoLine("Petrify synthesis result in EQN format:");
            System.out.println(equations);
        }

        String verilog = result.getReturnValue().getVerilog();
        if (CircuitSettings.getOpenSynthesisResult() && (verilog != null) && !verilog.isEmpty()) {
            LogUtils.logInfoLine("Petrify synthesis result in Verilog format:");
            System.out.println(verilog);
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(verilog.getBytes());
                VerilogImporter verilogImporter = new VerilogImporter();
                final Circuit circuit = verilogImporter.importCircuit(in);
                Path<String> path = we.getWorkspacePath();
                final Path<String> directory = path.getParent();
                final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
                final ModelEntry me = new ModelEntry(new CircuitDescriptor(), circuit);
                boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();

                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                final Workspace workspace = framework.getWorkspace();
                final WorkspaceEntry newWorkspaceEntry = workspace.add(directory, name, me, true, openInEditor);
                VisualModel visualModel = newWorkspaceEntry.getModelEntry().getVisualModel();
                if (visualModel instanceof VisualCircuit) {
                    VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                    setComponentsRenderStyle(visualCircuit);
                    String title = we.getModelEntry().getModel().getTitle();
                    visualCircuit.setTitle(title);
                    if (!we.getFile().exists()) {
                        JOptionPane.showMessageDialog(mainWindow,
                                "Error: unsaved STG cannot be set as the circuit environment.",
                                TITLE, JOptionPane.ERROR_MESSAGE);
                    } else {
                        visualCircuit.setEnvironmentFile(we.getFile());
                        if (we.isChanged()) {
                            JOptionPane.showMessageDialog(mainWindow,
                                    "Warning: the STG with unsaved changes is set as the circuit environment.",
                                    TITLE, JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            mainWindow.getCurrentEditor().updatePropertyView();
                        }
                    });
                }
            } catch (DeserialisationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void setComponentsRenderStyle(VisualCircuit visualCircuit) {
        for (VisualFunctionComponent component: visualCircuit.getVisualFunctionComponents()) {
            if (component.isSequentialGate()) {
                if (boxSequentialComponents) {
                    component.setRenderType(RenderType.BOX);
                }
            } else {
                if (boxCombinationalComponents) {
                    component.setRenderType(RenderType.BOX);
                }
            }
        }
    }

    private void handleFailure(final Result<? extends SynthesisResult> result) {
        String errorMessage = "Error: Petrify synthesis failed.";
        SynthesisResult returnValue = result.getReturnValue();
        if (returnValue != null) {
            errorMessage += ERROR_CAUSE_PREFIX + returnValue.getStderr();
        }
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
        JOptionPane.showMessageDialog(mainWindow, errorMessage, TITLE, JOptionPane.ERROR_MESSAGE);
    }

}
