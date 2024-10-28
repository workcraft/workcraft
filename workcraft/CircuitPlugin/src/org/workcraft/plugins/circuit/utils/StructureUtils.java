package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.circuit.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public final class StructureUtils {

    private StructureUtils() {
    }

    public static Set<FunctionComponent> getPresetComponents(final Circuit circuit, MathNode curNode) {
        Set<FunctionComponent> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) curNode;
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
            if (node instanceof FunctionComponent) {
                FunctionComponent component = (FunctionComponent) node;
                result.add(component);
            } else if (node instanceof FunctionContact) {
                FunctionContact contact = (FunctionContact) node;
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

    public static Set<FunctionComponent> getPostsetComponents(final Circuit circuit, MathNode curNode) {
        Set<FunctionComponent> result = new HashSet<>();
        Set<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) curNode;
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
            if (node instanceof FunctionComponent) {
                FunctionComponent component = (FunctionComponent) node;
                result.add(component);
            } else if (node instanceof FunctionContact) {
                FunctionContact contact = (FunctionContact) node;
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
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) curNode;
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
            if (node instanceof FunctionContact) {
                FunctionContact contact = (FunctionContact) node;
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

    public static boolean hasSelfLoop(Circuit circuit, FunctionComponent component) {
        for (Contact inputPin : component.getInputs()) {
            Contact driver = CircuitUtils.findDriver(circuit, inputPin, false);
            if (driver.getParent() == component) {
                return true;
            }
        }
        return false;
    }

}
