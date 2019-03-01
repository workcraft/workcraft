package org.workcraft.plugins.circuit.commands;

import java.util.HashSet;

import org.workcraft.commands.AbstractConversionCommand;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.plugins.circuit.stg.CircuitToStgConverter;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.stg.converters.SignalStg;
import org.workcraft.utils.DialogUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class CircuitToStgConversionCommand extends AbstractConversionCommand {

    @Override
    public String getDisplayName() {
        return "Signal Transition Graph";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicableExact(we, VisualCircuit.class);
    }

    @Override
    public ModelEntry convert(ModelEntry me) {
        final VisualCircuit circuit = (VisualCircuit) me.getVisualModel();
        HashSet<String> interfaceSignalNames = new HashSet<>();
        for (VisualFunctionContact contact: circuit.getVisualFunctionContacts()) {
            if (contact.isPort()) {
                interfaceSignalNames.add(contact.getName());
            }
        }
        for (String signalName: interfaceSignalNames) {
            String oneName = SignalStg.appendHighSuffix(signalName);
            String zeroName = SignalStg.appendLowSuffix(signalName);
            if (interfaceSignalNames.contains(oneName) || interfaceSignalNames.contains(zeroName)) {
                DialogUtils.showError("Complimentary STG places cannot be created for the interface signal '"
                                + signalName + "' because of a name clash.\n"
                                + "Either rename the port or change the signal level suffix in the STG plugin settings.");
                return null;
            }
        }
        final CircuitToStgConverter converter = getCircuitToStgConverter(circuit);
        return new ModelEntry(new StgDescriptor(), converter.getStg());
    }

    public CircuitToStgConverter getCircuitToStgConverter(VisualCircuit circuit) {
        return new CircuitToStgConverter(circuit);
    }

}
