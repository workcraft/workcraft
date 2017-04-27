package org.workcraft.plugins.petrify.tasks;

import java.io.ByteArrayInputStream;

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
import org.workcraft.plugins.petrify.PetrifySettings;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class PetrifySynthesisResultHandler extends DummyProgressMonitor<PetrifySynthesisResult> {
    private static final String TITLE = "Petrify synthesis";
    private static final String ERROR_CAUSE_PREFIX = "\n\n";
    private final WorkspaceEntry we;
    private final boolean boxSequentialComponents;
    private final boolean boxCombinationalComponents;
    private final boolean sequentialAssign;
    private WorkspaceEntry result;

    public PetrifySynthesisResultHandler(final WorkspaceEntry we, final boolean boxSequentialComponents,
            final boolean boxCombinationalComponents, final boolean sequentialAssign) {
        this.we = we;
        this.boxSequentialComponents = boxSequentialComponents;
        this.boxCombinationalComponents = boxCombinationalComponents;
        this.sequentialAssign = sequentialAssign;
        this.result = null;
    }

    @Override
    public void finished(final Result<? extends PetrifySynthesisResult> result, final String description) {
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

    private void handleSuccess(final Result<? extends PetrifySynthesisResult> result) {
        final String log = result.getReturnValue().getLog();
        if ((log != null) && !log.isEmpty()) {
            LogUtils.logInfoLine("Petrify synthesis log:");
            System.out.println(log);
        }

        final String equations = result.getReturnValue().getEquation();
        if ((equations != null) && !equations.isEmpty()) {
            LogUtils.logInfoLine("Petrify synthesis result in EQN format:");
            System.out.println(equations);
        }

        final String verilog = result.getReturnValue().getVerilog();
        if (PetrifySettings.getOpenSynthesisResult() && (verilog != null) && !verilog.isEmpty()) {
            LogUtils.logInfoLine("Petrify synthesis result in Verilog format:");
            System.out.println(verilog);
            try {
                final ByteArrayInputStream in = new ByteArrayInputStream(verilog.getBytes());
                final VerilogImporter verilogImporter = new VerilogImporter(sequentialAssign);
                final Circuit circuit = verilogImporter.importCircuit(in);
                final Path<String> path = we.getWorkspacePath();
                final ModelEntry me = new ModelEntry(new CircuitDescriptor(), circuit);
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                this.result = framework.createWork(me, path);
                final VisualModel visualModel = this.result.getModelEntry().getVisualModel();
                if (visualModel instanceof VisualCircuit) {
                    final VisualCircuit visualCircuit = (VisualCircuit) visualModel;
                    setComponentsRenderStyle(visualCircuit);
                    final String title = we.getModelEntry().getModel().getTitle();
                    visualCircuit.setTitle(title);
                    if (!we.getFile().exists()) {
                        JOptionPane.showMessageDialog(mainWindow,
                                "Error: Unsaved STG cannot be set as the circuit environment.",
                                TITLE, JOptionPane.ERROR_MESSAGE);
                    } else {
                        visualCircuit.setEnvironmentFile(we.getFile());
                        if (we.isChanged()) {
                            JOptionPane.showMessageDialog(mainWindow,
                                    "Warning: The STG with unsaved changes is set as the circuit environment.",
                                    TITLE, JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
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
    }

    private void setComponentsRenderStyle(final VisualCircuit visualCircuit) {
        for (final VisualFunctionComponent component: visualCircuit.getVisualFunctionComponents()) {
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

    private void handleFailure(final Result<? extends PetrifySynthesisResult> result) {
        String errorMessage = "Error: Petrify synthesis failed.";
        final PetrifySynthesisResult returnValue = result.getReturnValue();
        if (returnValue != null) {
            errorMessage += ERROR_CAUSE_PREFIX + returnValue.getStderr();
        }
        final MainWindow mainWindow = Framework.getInstance().getMainWindow();
        JOptionPane.showMessageDialog(mainWindow, errorMessage, TITLE, JOptionPane.ERROR_MESSAGE);
    }

    public WorkspaceEntry getResult() {
        return result;
    }

}
