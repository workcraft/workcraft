package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.utils.DirectedGraphUtils;
import org.workcraft.utils.Hierarchy;

import java.util.*;

public class CycleUtils {

    public static Collection<Contact> tagPathBreakerClearAll(Circuit circuit) {
        Collection<Contact> contacts = Hierarchy.getDescendantsOfType(circuit.getRoot(),
                Contact.class, contact -> contact.isOutput() && contact.isPin());

        return setPathBreaker(contacts, false);
    }

    public static Collection<Contact> tagPathBreakerSelfloopPins(Circuit circuit) {
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        Set<Contact> contacts = DirectedGraphUtils.findSelfloopVertices(graph);
        return setPathBreaker(contacts, true);
    }

    public static Collection<Contact> tagPathBreakerAutoAppend(Circuit circuit) {
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        Set<Contact> contacts = DirectedGraphUtils.findFeedbackVertices(graph);
        return setPathBreaker(contacts, true);
    }

    private static Set<Contact> setPathBreaker(Collection<? extends Contact> contacts, boolean value) {
        HashSet<Contact> result = new HashSet<>();
        for (Contact contact : contacts) {
            if (contact.getPathBreaker() != value) {
                contact.setPathBreaker(value);
                result.add(contact);
            }
        }
        return result;
    }

    public static Collection<Contact> tagPathBreakerAutoDiscard(Circuit circuit) {
        Set<Contact> result = new HashSet<>();
        boolean progress = true;
        int initCount = getCycledDrivers(circuit).size();
        while (progress) {
            progress = false;
            for (Contact contact : getPathBreakerDrivers(circuit)) {
                contact.setPathBreaker(false);
                int curCount = getCycledDrivers(circuit).size();
                if (curCount == initCount) {
                    result.add(contact);
                    progress = true;
                } else {
                    contact.setPathBreaker(true);
                }
            }
        }
        return result;
    }

    private static Set<Contact> getPathBreakerDrivers(Circuit circuit) {
        Set<Contact> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (Contact contact : component.getOutputs()) {
                if (contact.getPathBreaker()) {
                    result.add(contact);
                }
            }
        }
        return result;
    }

    public static Set<Contact> getCycledDrivers(Circuit circuit) {
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        return DirectedGraphUtils.findLoopedVertices(graph);
    }

    public static Set<FunctionComponent> getCycledComponents(Circuit circuit) {
        Set<FunctionComponent> result = new HashSet<>();
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        for (Contact contact : DirectedGraphUtils.findLoopedVertices(graph)) {
            Node parent = contact.getParent();
            if (parent instanceof FunctionComponent) {
                result.add((FunctionComponent) parent);
            }
        }
        return result;
    }

    private static Map<Contact, Set<Contact>> buildGraph(Circuit circuit) {
        Map<Contact, Set<Contact>> result = new HashMap<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            HashSet<Contact> drivers = new HashSet<>();
            for (Contact contact : component.getInputs()) {
                if (contact.getPathBreaker()) continue;
                Contact driver = CircuitUtils.findDriver(circuit, contact, true);
                if ((driver != null) && driver.isPin() && !driver.getPathBreaker()) {
                    drivers.add(driver);
                }
            }
            for (Contact contact : component.getOutputs()) {
                if (contact.getPathBreaker()) continue;
                result.put(contact, drivers);
            }
        }
        return result;
    }

}
