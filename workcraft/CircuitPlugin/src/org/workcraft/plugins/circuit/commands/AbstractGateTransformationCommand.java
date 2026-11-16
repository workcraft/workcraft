package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.utils.VerificationUtils;
import org.workcraft.workspace.WorkspaceEntry;

public abstract class AbstractGateTransformationCommand extends AbstractComponentTransformationCommand {

    public static final Section SECTION
            = new Section("Gate decomposition", Position.TOP_MIDDLE, 70);

    @Override
    public Section getSection() {
        return SECTION;
    }

    @Override
    public void transform(WorkspaceEntry we) {
        if (VerificationUtils.checkGateLibrary()) {
            super.transform(we);
        }
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualFunctionComponent) && ((VisualFunctionComponent) node).isGate();
    }

    @Override
    public void transformComponent(VisualCircuit circuit, VisualFunctionComponent component) {
        if (component.isGate()) {
            transformGate(circuit, component);
        }
    }

    public abstract void transformGate(VisualCircuit circuit, VisualFunctionComponent gate);

}
