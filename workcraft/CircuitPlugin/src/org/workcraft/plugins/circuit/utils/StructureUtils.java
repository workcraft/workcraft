package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.CircuitComponent;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.Joint;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public final class StructureUtils {

    private StructureUtils() {
    }

    public static Set<CircuitComponent> getPresetComponents(final Circuit circuit, MathNode curNode) {
        Set<CircuitComponent> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof CircuitComponent) {
            CircuitComponent component = (CircuitComponent) curNode;
            queue.addAll(component.getInputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if ((node == null) || visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof CircuitComponent) {
                CircuitComponent component = (CircuitComponent) node;
                result.add(component);
            } else if (node instanceof Contact) {
                Contact contact = (Contact) node;
                if (contact.isPort() == contact.isOutput()) {
                    queue.addAll(circuit.getPreset(contact));
                } else {
                    queue.add(contact.getParent());
                }
            } else if (node instanceof Joint) {
                Joint joint = (Joint) node;
                queue.addAll(circuit.getPreset(joint));
            } else if (node instanceof MathConnection) {
                MathConnection connection = (MathConnection) node;
                queue.add(connection.getFirst());
            }
        }
        return result;
    }

    public static Set<CircuitComponent> getPostsetComponents(final Circuit circuit, MathNode curNode) {
        Set<CircuitComponent> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof CircuitComponent) {
            CircuitComponent component = (CircuitComponent) curNode;
            queue.addAll(component.getOutputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if ((node == null) || visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof CircuitComponent) {
                CircuitComponent component = (CircuitComponent) node;
                result.add(component);
            } else if (node instanceof Contact) {
                Contact contact = (Contact) node;
                if (contact.isPort() == contact.isInput()) {
                    queue.addAll(circuit.getPostset(contact));
                } else {
                    queue.add(contact.getParent());
                }
            } else if (node instanceof Joint) {
                Joint joint = (Joint) node;
                queue.addAll(circuit.getPostset(joint));
            } else if (node instanceof MathConnection) {
                MathConnection connection = (MathConnection) node;
                queue.add(connection.getSecond());
            }
        }
        return result;
    }

    public static Set<Contact> getPostsetPorts(final Circuit circuit, MathNode curNode) {
        Set<Contact> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof CircuitComponent) {
            CircuitComponent component = (CircuitComponent) curNode;
            queue.addAll(component.getOutputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if ((node == null) || visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof Contact) {
                Contact contact = (Contact) node;
                if (contact.isOutput() && contact.isPort()) {
                    result.add(contact);
                } else {
                    queue.addAll(circuit.getPostset(contact));
                }
            } else if (node instanceof Joint) {
                Joint joint = (Joint) node;
                queue.addAll(circuit.getPostset(joint));
            } else if (node instanceof MathConnection) {
                MathConnection connection = (MathConnection) node;
                queue.add(connection.getSecond());
            }
        }
        return result;
    }

}
