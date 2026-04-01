package org.workcraft.plugins.circuit.commands;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualContact;
import org.workcraft.plugins.circuit.VisualFunctionComponent;
import org.workcraft.plugins.circuit.VisualFunctionContact;
import org.workcraft.workspace.ModelEntry;

public class ConnectHangingInputPinsToPortsTransformationCommand
        extends ConnectHangingPinsToPortsTransformationCommand {

    @Override
    public String getDisplayName() {
        return "Connect hanging input pins (selected or all) to ports with the same names";
    }

    @Override
    public String getPopupName(ModelEntry me, VisualNode node) {
        return (node instanceof VisualFunctionContact)
                ? "Connect hanging input pin to port with the same name"
                : "Connect hanging input pins to ports with the same names";
    }

    @Override
    public boolean isApplicableTo(VisualNode node) {
        return (node instanceof VisualFunctionComponent)
                || ((node instanceof VisualFunctionContact contact) && contact.isPin() && contact.isInput());
    }

    @Override
    public boolean isEnabledForContact(VisualContact contact) {
        return contact.isPin() && contact.isInput();
    }

}
