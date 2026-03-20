package org.workcraft.plugins.circuit.commands;

import org.workcraft.commands.AbstractTransformationCommand;
import org.workcraft.commands.NodeTransformer;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.SelectionUtils;
import org.workcraft.plugins.circuit.utils.SpaceUtils;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

import java.util.Collection;
import java.util.HashSet;

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
        return (node instanceof VisualFunctionComponent)
                || ((node instanceof VisualContact contact) && isApplicableToContact(contact));
    }

    public boolean isApplicableToContact(VisualContact contact) {
        return contact.isPin();
    }

    @Override
    public boolean isEnabled(ModelEntry me, VisualNode node) {
        VisualModel visualModel = me.getVisualModel();
        if ((node instanceof VisualFunctionContact contact) && isApplicableToContact(contact)) {
            return visualModel.getConnections(node).isEmpty();
        }
        if (node instanceof VisualFunctionComponent component) {
            for (VisualContact contact : component.getVisualContacts()) {
                if (isApplicableToContact(contact) && visualModel.getConnections(contact).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
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
                if (isApplicableToContact(contact) && circuit.getConnections(contact).isEmpty()) {
                    result.add(contact);
                }
            }
            SelectionUtils.retainSelectedContacts(circuit, result);
        }
        return result;
    }

    @Override
    public void transformNode(VisualModel model, VisualNode node) {
        if ((model instanceof VisualCircuit circuit) && (node instanceof VisualContact contact)
                && isApplicableToContact(contact) && circuit.getConnections(contact).isEmpty()) {

            String portName = contact.getName();
            Contact.IOType portType = contact.getReferencedComponent().getIOType();
            VisualFunctionContact port = CircuitUtils.getOrCreatePort(circuit, portName, portType, null);
            if (port != null) {
                if ((portType == Contact.IOType.OUTPUT) && !circuit.getConnections(port).isEmpty()) {
                    LogUtils.logError("Skipping output port '" + portName + "' as it is already connected");
                } else {
                    if (port.isInput()) {
                        CircuitUtils.connectIfPossible(circuit, port, contact);
                        SpaceUtils.alignPortWithPin(circuit, port, -1.0);
                    } else {
                        CircuitUtils.connectIfPossible(circuit, contact, port);
                        SpaceUtils.alignPortWithPin(port, contact, 1.0);
                    }
                }
            }
        }
    }

}
