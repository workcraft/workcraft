package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.RefinementUtils;
import org.workcraft.plugins.circuit.utils.SelectionUtils;
import org.workcraft.plugins.circuit.utils.SpaceUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.*;

public class ConnectHangingPinsToPortsTransformationCommand
        extends AbstractTransformationCommand
        implements NodeTransformer {

    public static final Section SECTION = new Section("Hanging pin connection", Position.BOTTOM_MIDDLE, 50);

    @Override
    public Section getSection() {
        return SECTION;
    }

    @Override
    public String getDisplayName() {
        return "Connect hanging pins (selected or all) to ports with the same names";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return (node instanceof VisualFunctionContact)
                ? "Connect hanging pin to port with the same name"
                : "Connect hanging pins to ports with the same names";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualCircuit.class);
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualFunctionComponent);
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        VisualModel visualModel = me.getVisualModel();
        if ((node instanceof VisualFunctionContact contact) && isEnabledForContact(contact)) {
            return visualModel.getConnections(node).isEmpty();
        }
        if (node instanceof VisualFunctionComponent component) {
            for (VisualContact contact : component.getVisualContacts()) {
                if (isEnabledForContact(contact) && visualModel.getConnections(contact).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isEnabledForContact(VisualContact contact) {
        return contact.isPin();
    }

    @Override
    public Position getPosition() {
        return Position.BOTTOM_MIDDLE;
    }

    @Override
    public Collection<VisualContact> collectNodes(VisualModel model) {
        Collection<VisualContact> result = new HashSet<>();
        if (model instanceof VisualCircuit circuit) {
            for (VisualContact contact : circuit.getVisualFunctionContacts()) {
                if (isEnabledForContact(contact) && circuit.getConnections(contact).isEmpty()) {
                    result.add(contact);
                }
            }
            SelectionUtils.retainSelectedContacts(circuit, result);
        }
        return result;
    }

    private final Map<VisualContact, Boolean> inputPinInitalState = new HashMap<>();

    @Override
    public void transformNodes(VisualModel model, Collection<? extends VisualNode> nodes) {
        calcInputPinInitialState(nodes);
        super.transformNodes(model, nodes);
    }

    private void calcInputPinInitialState(Collection<? extends VisualNode> nodes) {
        inputPinInitalState.clear();
        Map<VisualFunctionComponent, Set<String>> componentInputSignals = new HashMap<>();
        for (Node node : nodes) {
            if ((node instanceof VisualContact contact)
                    && (contact.getParent() instanceof VisualFunctionComponent component)) {

                componentInputSignals.computeIfAbsent(component, c -> new HashSet<>())
                        .add(contact.getName());
            }
        }
        for (VisualFunctionComponent component : componentInputSignals.keySet()) {
            Set<String> inputSignals = componentInputSignals.getOrDefault(component, Collections.emptySet());
            Map<String, Boolean> inputSignalsInitialState = RefinementUtils.getSignalsInitialState(
                    component.getReferencedComponent(), inputSignals);

            for (VisualContact contact : component.getVisualInputs()) {
                Boolean initialState = inputSignalsInitialState.get(contact.getName());
                if (initialState != null) {
                    inputPinInitalState.put(contact, initialState);
                }
            }
        }
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit circuit) && (node instanceof VisualContact contact)
                && isEnabledForContact(contact) && circuit.getConnections(contact).isEmpty()) {

            String portName = contact.getName();
            VisualContact existingPort = circuit.getVisualComponentByMathReference(portName, VisualContact.class);
            Contact.IOType portType = contact.getReferencedComponent().getIOType();
            VisualFunctionContact port = CircuitUtils.getOrCreatePort(circuit, portName, portType, contact.getDirection());
            if (port != null) {
                if ((portType == Contact.IOType.OUTPUT) && !circuit.getConnections(port).isEmpty()) {
                    LogUtils.logError("Skipping output port " + portName + " as it is already connected");
                } else {
                    if (port != existingPort) {
                        SpaceUtils.alignPortWithPin(port, contact, contact.getDirection().getGradientX());
                    }
                    if (port.isOutput()) {
                        CircuitUtils.connectIfPossible(circuit, contact, port);
                    } else {
                        CircuitUtils.connectIfPossible(circuit, port, contact);
                        Boolean initialState = inputPinInitalState.get(contact);
                        if (initialState != null) {
                            if ((port == existingPort) && (existingPort.getInitToOne() != initialState)) {
                                String pinRef = circuit.getMathReference(contact);
                                LogUtils.logWarning("Changing initial state for port " + portName
                                        + " to match initial state of input pin " + pinRef);
                            }
                            port.setInitToOne(initialState);
                        }
                    }
                }
            }
        }
    }

}
