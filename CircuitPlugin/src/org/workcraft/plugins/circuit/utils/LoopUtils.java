package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.plugins.circuit.*;
import org.workcraft.utils.DirectedGraphUtils;

import java.util.*;

public class LoopUtils {

    public static Set<FunctionComponent> clearPathBreakerComponents(Circuit circuit) {
        Set<FunctionComponent> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getPathBreaker()) {
                result.add(component);
                component.setPathBreaker(false);
            }
        }
        return result;
    }

    public static Set<FunctionContact> clearPathBreakerContacts(Circuit circuit) {
        Set<FunctionContact> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (FunctionContact contact : component.getFunctionContacts()) {
                if (contact.getPathBreaker()) {
                    result.add(contact);
                    contact.setPathBreaker(false);
                }
            }
        }
        return result;
    }

    public static Collection<VisualFunctionComponent> insertLoopBreakerBuffers(VisualCircuit circuit) {
        clearPathBreakerContacts(circuit.getMathModel());
        Map<Contact, Set<Contact>> graph = buildGraph(circuit.getMathModel());
        Set<Contact> feedbackContacts = DirectedGraphUtils.findFeedbackVertices(graph);
        Collection<VisualFunctionComponent> result = new HashSet<>();
        for (Contact feedbackContact : feedbackContacts) {
            VisualContact contact = circuit.getVisualComponent(feedbackContact, VisualContact.class);
            VisualFunctionComponent buffer = insertOrReuseBuffer(circuit, contact);
            buffer.getReferencedComponent().setPathBreaker(true);
            result.add(buffer);
        }
        return result;
    }

    private static VisualFunctionComponent insertOrReuseBuffer(VisualCircuit circuit, VisualContact contact) {
        VisualFunctionComponent result = null;
        Node parent = contact.getParent();
        if (parent instanceof VisualFunctionComponent) {
            VisualFunctionComponent component = (VisualFunctionComponent) parent;
            if (component.isBuffer()) {
                result = component;
            }
        }
        if ((result == null) && contact.isOutput()) {
            Collection<VisualContact> drivenContacts = CircuitUtils.findDriven(circuit, contact, false);
            if (drivenContacts.size() == 1) {
                VisualContact drivenContact = drivenContacts.iterator().next();
                result = insertOrReuseBuffer(circuit, drivenContact);
            }
        }
        if ((result == null) && contact.isOutput()) {
            SpaceUtils.makeSpaceAfterContact(circuit, contact, 3.0);
            result = GateUtils.createBufferGate(circuit);
            GateUtils.insertGateAfter(circuit, result, contact);
            GateUtils.propagateInitialState(circuit, result);
        }
        return result;
    }

    private static Map<Contact, Set<Contact>> buildGraph(Circuit circuit) {
        Map<Contact, Set<Contact>> contactToDriversMap = new HashMap<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            if (component.getPathBreaker()) continue;
            HashSet<Contact> drivers = new HashSet<>();
            for (Contact contact : component.getInputs()) {
                Contact driver = CircuitUtils.findDriver(circuit, contact, true);
                if (driver.isPin()) {
                    drivers.add(driver);
                }
            }
            for (Contact contact : component.getOutputs()) {
                contactToDriversMap.put(contact, drivers);
            }
        }
        return contactToDriversMap;
    }

}
