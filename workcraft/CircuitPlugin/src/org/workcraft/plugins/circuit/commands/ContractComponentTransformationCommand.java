package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

public class ContractComponentTransformationCommand extends AbstractTransformationCommand implements NodeTransformer {

    @Override
    public String getDisplayName() {
        return "Contract selected single-input/single-output components";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return "Contract component";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return node instanceof VisualCircuitComponent;
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        boolean result = false;
        if (node instanceof VisualCircuitComponent component) {
            result = component.getReferencedComponent().isSingleInputSingleOutput();
        }
        return result;
    }

    @Override
    public Position getPosition() {
        return Position.MIDDLE;
    }

    @Override
    public Collection<VisualNode> collectNodes(VisualModel model) {
        Collection<VisualNode> components = new HashSet<>(
                Hierarchy.getDescendantsOfType(model.getRoot(), VisualCircuitComponent.class));

        components.retainAll(model.getSelection());
        return components;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit circuit) && (node instanceof VisualCircuitComponent component)) {
            if (isValidContraction(circuit, component)) {
                VisualContact inputContact = component.getFirstVisualInput();
                for (VisualContact outputContact: component.getVisualOutputs()) {
                    CircuitUtils.fuseContacts(circuit, inputContact, outputContact);
                }
                circuit.remove(component);
            }
        }
    }

    private boolean isValidContraction(VisualCircuit circuit, VisualCircuitComponent component) {
        Collection<VisualContact> inputContacts = component.getVisualInputs();
        String componentName = circuit.getMathModel().getComponentReference(component.getReferencedComponent());
        if (inputContacts.size() > 2) {
            LogUtils.logError("Cannot contract component '" + componentName + "' with " + inputContacts.size() + " inputs.");
            return false;
        }
        Collection<VisualContact> outputContacts = component.getVisualOutputs();
        if (outputContacts.size() > 2) {
            LogUtils.logError("Cannot contract component '" + componentName + "' with " + outputContacts.size() + " outputs.");
            return false;
        }
        VisualContact outputContact = component.getFirstVisualOutput();
        VisualContact inputContact = component.getFirstVisualInput();

        // Input and output ports
        Circuit mathCircuit = circuit.getMathModel();
        Contact driver = CircuitUtils.findDriver(mathCircuit, inputContact.getReferencedComponent(), true);
        HashSet<Contact> drivenSet = new HashSet<>();
        drivenSet.addAll(CircuitUtils.findDriven(mathCircuit, driver, true));
        drivenSet.addAll(CircuitUtils.findDriven(mathCircuit, outputContact.getReferencedComponent(), true));
        int outputPortCount = 0;
        for (Contact driven: drivenSet) {
            if (driven.isOutput() && driven.isPort()) {
                outputPortCount++;
                if (outputPortCount > 1) {
                    LogUtils.logError("Cannot contract component '" + componentName + "' as it leads to fork on output ports.");
                    return false;
                }
                if ((driver != null) && driver.isInput() && driver.isPort()) {
                    LogUtils.logError("Cannot contract component '" + componentName + "' as it leads to direct connection from input port to output port.");
                    return false;
                }
            }
        }

        // Handle zero delay components
        Contact directDriver = CircuitUtils.findDriver(mathCircuit, inputContact.getReferencedComponent(), false);
        Node directDriverParent = directDriver == null ? null : directDriver.getParent();
        if (directDriverParent instanceof FunctionComponent directDriverComponent) {
            if (directDriverComponent.getIsZeroDelay()) {
                Collection<Contact> directDrivenSet = CircuitUtils.findDriven(mathCircuit, outputContact.getReferencedComponent(), false);
                for (Contact directDriven: directDrivenSet) {
                    if (directDriven.isOutput() && directDriven.isPort()) {
                        LogUtils.logError("Cannot contract component '" + componentName + "' as it leads to connection of zero delay component to output port.");
                        return false;
                    }
                    Node directDrivenParent = directDriven.getParent();
                    if (directDrivenParent instanceof FunctionComponent directDrivenComponent) {
                        if (directDrivenComponent.getIsZeroDelay()) {
                            LogUtils.logError("Cannot contract component '" + componentName + "' as it leads to connection between zero delay components.");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

}
