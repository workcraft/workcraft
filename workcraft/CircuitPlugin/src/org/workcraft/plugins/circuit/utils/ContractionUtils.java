package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.circuit.*;
import org.workcraft.utils.LogUtils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

public final class ContractionUtils {

    private ContractionUtils() {
    }

    public static void contractComponentIfPossible(VisualCircuit circuit, VisualCircuitComponent component) {
        if (isValidContraction(circuit, component)) {
            VisualContact inputContact = component.getFirstVisualInput();
            if (inputContact != null) {
                VisualFunctionComponent inputDriverComponent = null;
                boolean originalZeroDelayForInputDriverComponent = false;
                VisualContact inputDriver = CircuitUtils.findDriver(circuit, inputContact, false);
                if ((inputDriver != null) && (inputDriver.getParent() instanceof VisualFunctionComponent)) {
                    // Temporary clear zero delay attribute of the driver component
                    inputDriverComponent = (VisualFunctionComponent) inputDriver.getParent();
                    originalZeroDelayForInputDriverComponent = inputDriverComponent.getIsZeroDelay();
                    inputDriverComponent.setIsZeroDelay(false);
                }
                for (VisualContact outputContact : component.getVisualOutputs()) {
                    fuseContacts(circuit, inputContact, outputContact);
                }
                circuit.remove(component);
                if (inputDriverComponent != null) {
                    // Restore zero delay attribute of the driver component
                    inputDriverComponent.setIsZeroDelay(originalZeroDelayForInputDriverComponent);
                }
            }
        }
    }

    private static boolean isValidContraction(VisualCircuit circuit, VisualCircuitComponent component) {
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

    private static void fuseContacts(VisualCircuit circuit, VisualContact inputContact, VisualContact outputContact) {
        for (VisualConnection inputConnection : circuit.getConnections(inputContact)) {
            VisualNode fromNode = inputConnection.getFirst();
            for (VisualConnection outputConnection : new ArrayList<>(circuit.getConnections(outputContact))) {
                VisualNode toNode = outputConnection.getSecond();
                LinkedList<Point2D> locations = ConnectionHelper.getMergedControlPoints(outputContact,
                        inputConnection, outputConnection);

                circuit.remove(outputConnection);
                try {
                    VisualConnection newConnection = circuit.connect(fromNode, toNode);
                    newConnection.mixStyle(inputConnection, outputConnection);
                    ConnectionHelper.addControlPoints(newConnection, locations);
                } catch (InvalidConnectionException e) {
                    LogUtils.logWarning(e.getMessage());
                }
            }
            VisualContact inputDriver = CircuitUtils.findDriver(circuit, inputContact, false);
            if (inputDriver != null) {
                ConversionUtils.updateReplicas(circuit, outputContact, inputDriver);
            }
        }
    }

}
