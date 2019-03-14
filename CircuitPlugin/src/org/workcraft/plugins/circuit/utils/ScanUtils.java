package org.workcraft.plugins.circuit.utils;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.formula.Zero;
import org.workcraft.plugins.circuit.*;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ScanUtils {

    public static void insertScan(VisualCircuit circuit, Collection<VisualFunctionComponent> components) {
        List<VisualFunctionContact> ports = new ArrayList<>();
        for (String name : CircuitSettings.parseScanPorts()) {
            ports.add(CircuitUtils.getOrCreatePort(circuit, name, Contact.IOType.INPUT));
        }

        for (VisualFunctionComponent component : components) {
            if (component.getReferencedComponent().getPathBreaker()) {
                insertScan(circuit, component, ports);
            }
        }

        for (VisualFunctionContact port : ports) {
            SpaceUtils.positionPort(circuit, port);
            port.setSetFunction(Zero.instance());
        }
    }

    public static void insertScan(VisualCircuit circuit, VisualFunctionComponent component, List<VisualFunctionContact> ports) {
        String moduleName = component.getReferencedComponent().getModule();
        String suffix = CircuitSettings.getScanSuffix();
        if (!moduleName.isEmpty() && !suffix.isEmpty()) {
            component.getReferencedComponent().setModule(moduleName + suffix);
        }
        component.setRenderType(ComponentRenderingResult.RenderType.BOX);
        Iterator<VisualFunctionContact> iterator = ports.iterator();
        for (String name : CircuitSettings.parseScanPins()) {
            VisualFunctionContact contact = circuit.getOrCreateContact(component, name, Contact.IOType.INPUT);
            component.setPositionByDirection(contact, VisualContact.Direction.WEST, false);
            if (iterator.hasNext()) {
                VisualFunctionContact port = iterator.next();
                try {
                    circuit.connect(port, contact);
                } catch (InvalidConnectionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
