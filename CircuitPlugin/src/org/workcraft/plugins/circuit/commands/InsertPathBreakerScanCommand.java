package org.workcraft.plugins.circuit.commands;

import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.utils.ScanUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;

public class InsertPathBreakerScanCommand extends CircuitAbstractPathbreakerCommand {

    @Override
    public String getDisplayName() {
        return "Insert scan for path breaker components";
    }

    @Override
    public Void execute(WorkspaceEntry we) {
        VisualCircuit circuit = WorkspaceUtils.getAs(we, VisualCircuit.class);

        Collection<VisualFunctionComponent> components = Hierarchy.getDescendantsOfType(circuit.getRoot(),
                VisualFunctionComponent.class, component -> component.getReferencedComponent().getPathBreaker());

        if (!components.isEmpty()) {
            we.saveMemento();
            ScanUtils.insertScan(circuit, components);
        }

        return null;
    }

}
