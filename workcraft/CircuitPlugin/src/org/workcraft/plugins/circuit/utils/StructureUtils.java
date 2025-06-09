package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.circuit.*;

import java.util.*;

public final class StructureUtils {

    private StructureUtils() {
    }

    public static Set<FunctionComponent> getPresetComponents(final Circuit circuit, MathNode curNode) {
        Set<FunctionComponent> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        if (curNode instanceof FunctionComponent component) {
            queue.addAll(component.getInputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof FunctionComponent component) {
                result.add(component);
            } else if (node instanceof FunctionContact contact) {
                if (contact.isPort() == contact.isOutput()) {
                    queue.addAll(circuit.getPreset(contact));
                } else {
                    queue.add(contact.getParent());
                }
            } else if (node instanceof Joint joint) {
                queue.addAll(circuit.getPreset(joint));
            } else if (node instanceof MathConnection connection) {
                queue.add(connection.getFirst());
            }
        }
        return result;
    }

    public static Set<FunctionComponent> getPostsetComponents(final Circuit circuit, MathNode curNode) {
        Set<FunctionComponent> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        if (curNode instanceof FunctionComponent component) {
            queue.addAll(component.getOutputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof FunctionComponent component) {
                result.add(component);
            } else if (node instanceof FunctionContact contact) {
                if (contact.isPort() == contact.isInput()) {
                    queue.addAll(circuit.getPostset(contact));
                } else {
                    queue.add(contact.getParent());
                }
            } else if (node instanceof Joint joint) {
                queue.addAll(circuit.getPostset(joint));
            } else if (node instanceof MathConnection connection) {
                queue.add(connection.getSecond());
            }
        }
        return result;
    }

    public static Set<FunctionComponent> getAdjacentComponents(Circuit circuit, FunctionComponent component) {
        Set<FunctionComponent> result = new HashSet<>();
        result.addAll(StructureUtils.getPresetComponents(circuit, component));
        result.addAll(StructureUtils.getPostsetComponents(circuit, component));
        result.remove(component);
        return result;
    }

    public static Set<FunctionContact> getPostsetPorts(final Circuit circuit, MathNode curNode) {
        Set<FunctionContact> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        if (curNode instanceof FunctionComponent component) {
            queue.addAll(component.getOutputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof FunctionContact contact) {
                if (contact.isOutput() && contact.isPort()) {
                    result.add(contact);
                } else {
                    queue.addAll(circuit.getPostset(contact));
                }
            } else if (node instanceof Joint joint) {
                queue.addAll(circuit.getPostset(joint));
            } else if (node instanceof MathConnection connection) {
                queue.add(connection.getSecond());
            }
        }
        return result;
    }

    public static boolean hasSelfLoop(Circuit circuit, FunctionComponent component) {
        for (Contact inputPin : component.getInputs()) {
            Contact driver = CircuitUtils.findDriver(circuit, inputPin, false);
            if ((driver != null) && (driver.getParent() == component)) {
                return true;
            }
        }
        return false;
    }

}
