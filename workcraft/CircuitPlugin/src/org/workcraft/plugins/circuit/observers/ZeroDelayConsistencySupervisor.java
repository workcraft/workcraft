package org.workcraft.plugins.circuit.observers;

import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.utils.CircuitUtils;
import org.workcraft.plugins.circuit.utils.StructureUtils;
import org.workcraft.plugins.circuit.utils.ZeroDelayUtils;

public class ZeroDelayConsistencySupervisor extends StateSupervisor {

    private final Circuit circuit;

    public ZeroDelayConsistencySupervisor(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent pce) {
            Object sender = e.getSender();
            String propertyName = pce.getPropertyName();
            if ((sender instanceof FunctionComponent)
                    && propertyName.equals(FunctionComponent.PROPERTY_IS_ZERO_DELAY)) {

                handleZeroDelayChange((FunctionComponent) sender);
            }
            if ((sender instanceof FunctionComponent)
                    && propertyName.equals(FunctionComponent.PROPERTY_AVOID_INIT)) {

                handleAvoidInitChange((FunctionComponent) sender);
            }
        }
    }

    private void handleZeroDelayChange(FunctionComponent component) {
        if (component.getIsZeroDelay()) {
            String componentRef = circuit.getComponentReference(component);
            String messagePrefix = "Component '" + componentRef + "' cannot be zero delay because ";
            if (!component.isInverter() && !component.isBuffer()) {
                component.setIsZeroDelay(false);
                throw new ArgumentException(messagePrefix + "it is neither inverters nor buffers.");
            }
            if (StructureUtils.hasSelfLoop(circuit, component)) {
                component.setIsZeroDelay(false);
                throw new ArgumentException(messagePrefix + "has self-loop.");
            }
            if (ZeroDelayUtils.hasAdjacentOtherZeroDelayComponent(circuit, component)) {
                component.setIsZeroDelay(false);
                throw new ArgumentException(messagePrefix + "it is connected to another zero delay component.");
            }
            if (ZeroDelayUtils.hasDrivenHierarchicalComponent(circuit, component)) {
                component.setIsZeroDelay(false);
                throw new ArgumentException(messagePrefix + "it drives hierarchical component.");
            }
            if (!StructureUtils.getPostsetPorts(circuit, component).isEmpty()) {
                component.setIsZeroDelay(false);
                throw new ArgumentException(messagePrefix + "it drives output port.");
            }
            if (CircuitUtils.findDriven(circuit, component.getGateOutput(), false).size() > 1) {
                component.setIsZeroDelay(false);
                throw new ArgumentException(messagePrefix + "it has fork at its output.");
            }
            for (Contact contact : component.getOutputs()) {
                // Zero delay component cannot be ForceInit or PathBreaker
                contact.setForcedInit(false);
                contact.setPathBreaker(false);
            }
        }
    }

    private void handleAvoidInitChange(FunctionComponent component) {
        if (component.getAvoidInit()) {
            for (Contact contact : component.getOutputs()) {
                contact.setForcedInit(false);
            }
        }
    }

}
