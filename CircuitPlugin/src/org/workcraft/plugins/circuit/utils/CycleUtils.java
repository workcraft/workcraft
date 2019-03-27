package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.utils.DirectedGraphUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CycleUtils {

    public static Set<? extends Contact> clearPathBreakers(Circuit circuit) {
        Set<Contact> result = new HashSet<>();
        for (FunctionComponent component : circuit.getFunctionComponents()) {
            for (Contact contact : component.getContacts()) {
                if (contact.getPathBreaker()) {
                    result.add(contact);
                    contact.setPathBreaker(false);
                }
            }
        }
        return result;
    }

    public static Set<? extends Contact> setSelfLoopPathBreakers(Circuit circuit) {
        Set<Contact> result = new HashSet<>();
        for (Contact contact : getSelfloopDrivers(circuit)) {
            if (!contact.getPathBreaker()) {
                contact.setPathBreaker(true);
                result.add(contact);
            }
        }
        return result;
    }

    public static Set<? extends Contact> tagNecessaryPathBreakers(Circuit circuit) {
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        Set<Contact> feedbackContacts = DirectedGraphUtils.findFeedbackVertices(graph);
        Set<Contact> result = new HashSet<>();
        for (Contact feedbackContact : feedbackContacts) {
            feedbackContact.setPathBreaker(true);
            result.add(feedbackContact);
        }
        return result;
    }

    public static Set<? extends Contact> untagRedundantPathBreakers(Circuit circuit) {
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

    public static Set<Contact> getPathBreakerDrivers(Circuit circuit) {
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

    public static Set<Contact> getSelfloopDrivers(Circuit circuit) {
        Map<Contact, Set<Contact>> graph = buildGraph(circuit);
        return DirectedGraphUtils.findSelfloopVertices(graph);
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
                if (driver.isPin() && !driver.getPathBreaker()) {
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
