package org.workcraft.plugins.circuit.observers;

import org.workcraft.dom.Node;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.utils.StructureUtilsKt;

import java.util.HashSet;

public class ZeroDelayConsistencySupervisor extends StateSupervisor {

    private final Circuit circuit;

    public ZeroDelayConsistencySupervisor(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            String propertyName = pce.getPropertyName();
            if ((sender instanceof FunctionContact)
                    && (propertyName.equals(FunctionContact.PROPERTY_FUNCTION))) {

                handleFunctionChange((FunctionContact) sender);
            }
            if ((sender instanceof FunctionComponent)
                    && propertyName.equals(FunctionComponent.PROPERTY_IS_ZERO_DELAY)) {

                handleZeroDelayChange((FunctionComponent) sender);
            }
        }
    }

    private void handleFunctionChange(FunctionContact contact) {
        Node parent = contact.getParent();
        if (parent instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) parent;
            component.setIsZeroDelay(false);
        }
    }

    private void handleZeroDelayChange(FunctionComponent component) {
        if (component.getIsZeroDelay()) {
            for (Contact contact : component.getOutputs()) {
                contact.setForcedInit(false);
                contact.setPathBreaker(false);
            }
            if (!component.isInverter() && !component.isBuffer()) {
                component.setIsZeroDelay(false);
                throw new ArgumentException("Only inverters and buffers can be zero delay.");
            }
            HashSet<CircuitComponent> componentPreset = StructureUtilsKt.getPresetComponents(circuit, component);
            for (CircuitComponent predComponent: componentPreset) {
                if (predComponent instanceof FunctionComponent) {
                    FunctionComponent predFunctionComponent = (FunctionComponent) predComponent;
                    if (predFunctionComponent.getIsZeroDelay()) {
                        component.setIsZeroDelay(false);
                        throw new ArgumentException("Zero delay components cannot be connected to each other.");
                    }
                }
            }
            HashSet<CircuitComponent> componentPostset = StructureUtilsKt.getPostsetComponents(circuit, component);
            for (CircuitComponent succComponent: componentPostset) {
                if (succComponent instanceof FunctionComponent) {
                    FunctionComponent succFunctionComponent = (FunctionComponent) succComponent;
                    if (succFunctionComponent.getIsZeroDelay()) {
                        component.setIsZeroDelay(false);
                        throw new ArgumentException("Zero delay components cannot be connected to each other.");
                    }
                }
            }
            HashSet<Contact> portPostset = StructureUtilsKt.getPostsetPorts(circuit, component);
            for (Contact succContact: portPostset) {
                if (succContact.isPort()) {
                    component.setIsZeroDelay(false);
                    throw new ArgumentException("A component connected to an output port cannot be zero delay.");
                }
            }
            if (componentPostset.size() + portPostset.size() > 1) {
                component.setIsZeroDelay(false);
                throw new ArgumentException("A component with a fork at its output cannot be zero delay.");
            }
        }
    }

}
