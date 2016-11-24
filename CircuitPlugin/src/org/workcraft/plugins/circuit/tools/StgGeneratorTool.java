package org.workcraft.plugins.circuit.tools;

import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.ConversionTool;
import org.workcraft.Framework;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.stg.CircuitStgUtils;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.generator.SignalStg;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class StgGeneratorTool extends ConversionTool {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return we.getModelEntry().getMathModel() instanceof Circuit;
    }

    @Override
    public void run(WorkspaceEntry we) {
        final VisualCircuit circuit = (VisualCircuit) we.getModelEntry().getVisualModel();
        HashSet<String> interfaceSignalNames = new HashSet<>();
        for (VisualFunctionContact contact: circuit.getVisualFunctionContacts()) {
            if (contact.isPort()) {
                interfaceSignalNames.add(contact.getName());
            }
        }
        for (String signalName: interfaceSignalNames) {
            String oneName = SignalStg.getHighName(signalName);
            String zeroName = SignalStg.getLowName(signalName);
            if (interfaceSignalNames.contains(oneName) || interfaceSignalNames.contains(zeroName)) {
                JOptionPane.showMessageDialog(null,
                        "Complimentary STG places cannot be created for the interface signal `"
                                + signalName + "` because of a name clash.\n"
                                + "Either rename the port or change the signal level suffix in the STG plugin settings.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        final CircuitToStgConverter converter = CircuitStgUtils.createCircuitToStgConverter(circuit);
        final Workspace workspace = Framework.getInstance().getWorkspace();
        final Path<String> directory = we.getWorkspacePath().getParent();
        final String name = we.getWorkspacePath().getNode();
        final ModelEntry me = new ModelEntry(new StgDescriptor(), converter.getStg());
        boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
        workspace.add(directory, name, me, false, openInEditor);
    }

}
