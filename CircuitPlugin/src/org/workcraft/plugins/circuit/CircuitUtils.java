package org.workcraft.plugins.circuit;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.jj.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.jj.ParseException;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

public class CircuitUtils {

    public static VisualContact findDriver(VisualCircuit circuit, VisualContact contact) {
        return findDriver(circuit, contact, true);
    }

    public static VisualContact findDriver(VisualCircuit circuit, VisualContact contact, boolean transparentZeroDelayComponents) {
        Contact mathDriver = findDriver((Circuit) circuit.getMathModel(), contact.getReferencedContact(), transparentZeroDelayComponents);
        return circuit.getVisualComponent(mathDriver, VisualContact.class);
    }

    public static Contact findDriver(Circuit circuit, MathNode curNode, boolean transparentZeroDelayComponents) {
        Contact result = null;
        HashSet<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof MathConnection) {
            queue.add(((MathConnection) curNode).getFirst());
        } else {
            queue.add(curNode);
        }
        while (!queue.isEmpty()) {
            if (queue.size() != 1) {
                throw new RuntimeException("Found more than one potential driver for '"
                        + circuit.getNodeReference(curNode) + "'!");
            }
            Node node = queue.remove();
            if (visited.contains(node)) {
                continue;
            }
            visited.add(node);
            if (node instanceof Joint) {
                queue.addAll(circuit.getPreset(node));
            } else if (node instanceof Contact) {
                Contact contact = (Contact) node;
                // Support for zero-delay buffers and inverters.
                Contact zeroDelayInput = transparentZeroDelayComponents ? findZeroDelayInput(contact) : null;
                if (zeroDelayInput != null) {
                    queue.addAll(circuit.getPreset(zeroDelayInput));
                } else if (contact.isDriver()) {
                    result = contact;
                } else if (node == curNode) {
                    queue.addAll(circuit.getPreset(contact));
                }
            } else {
                throw new RuntimeException("Unexpected node '" + circuit.getNodeReference(node)
                + "' in the driver trace for node '" + circuit.getNodeReference(curNode) + "'!");
            }
        }
        return result;
    }

    private static Contact findZeroDelayInput(Contact contact) {
        Contact zeroDelayInput = null;
        Node parent = contact.getParent();
        if (contact.isOutput() && (parent instanceof FunctionComponent)) {
            FunctionComponent component = (FunctionComponent) parent;
            if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
                zeroDelayInput = component.getFirstInput();
            }
        }
        return zeroDelayInput;
    }

    public static Collection<VisualContact> findDriven(VisualCircuit circuit, VisualContact contact) {
        return findDriven(circuit, contact, true);
    }

    public static Collection<VisualContact> findDriven(VisualCircuit circuit, VisualContact contact, boolean transparentZeroDelayComponents) {
        Collection<Contact> drivenContacts = findDriven((Circuit) circuit.getMathModel(), contact.getReferencedContact(), transparentZeroDelayComponents);
        return getVisualContacts(circuit, drivenContacts);
    }

    public static Collection<Contact> findDriven(Circuit circuit, MathNode curNode, boolean transparentZeroDelayComponents) {
        Set<Contact> result = new HashSet<>();
        HashSet<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof MathConnection) {
            queue.add(((MathConnection) curNode).getSecond());
        } else {
            queue.add(curNode);
        }
        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if (!visited.contains(node)) {
                visited.add(node);
                if (node instanceof Joint) {
                    queue.addAll(circuit.getPostset(node));
                } else if (node instanceof Contact) {
                    Contact contact = (Contact) node;
                    // Support for zero-delay buffers and inverters.
                    Contact zeroDelayOutput = transparentZeroDelayComponents ? findZeroDelayOutput(contact) : null;
                    if (zeroDelayOutput != null) {
                        queue.addAll(circuit.getPostset(zeroDelayOutput));
                    } else if (contact.isDriven()) {
                        result.add(contact);
                    } else if (node == curNode) {
                        queue.addAll(circuit.getPostset(contact));
                    }
                } else {
                    throw new RuntimeException("Unexpected node '" + circuit.getNodeReference(node)
                            + "' in the driven trace for node '" + circuit.getNodeReference(curNode) + "'!");
                }
            }
        }
        return result;
    }

    private static Contact findZeroDelayOutput(Contact contact) {
        Contact zeroDelayOutput = null;
        Node parent = contact.getParent();
        if (contact.isInput() && (parent instanceof FunctionComponent)) {
            FunctionComponent component = (FunctionComponent) parent;
            if (component.getIsZeroDelay() && (component.isBuffer() || component.isInverter())) {
                zeroDelayOutput = component.getFirstOutput();
            }
        }
        return zeroDelayOutput;
    }

    public static Contact findSignal(Circuit circuit, Contact contact, boolean transparentZeroDelayComponents) {
        Contact result = contact;
        Contact driver = findDriver(circuit, contact, transparentZeroDelayComponents);
        if (driver != null) {
            result = driver;
            for (Contact signal : Hierarchy.getDescendantsOfType(circuit.getRoot(), Contact.class)) {
                if (signal.isPort() && signal.isOutput()) {
                    if (driver == CircuitUtils.findDriver(circuit, signal, transparentZeroDelayComponents)) {
                        result = signal;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static VisualContact findSignal(VisualCircuit circuit, VisualContact contact, boolean transparentZeroDelayComponents) {
        Contact mathSignal = findSignal((Circuit) circuit.getMathModel(), contact.getReferencedContact(), transparentZeroDelayComponents);
        return circuit.getVisualComponent(mathSignal, VisualContact.class);
    }

    public static String getWireName(Circuit circuit, Contact contact) {
        String result = null;
        if (!circuit.getPreset(contact).isEmpty() || !circuit.getPostset(contact).isEmpty()) {
            Contact signal = findSignal(circuit, contact, false);
            result = getContactName(circuit, signal);
        }
        return result;
    }

    public static String getWireName(VisualCircuit circuit, VisualContact contact) {
        return getWireName((Circuit) circuit.getMathModel(), contact.getReferencedContact());
    }

    public static String getSignalName(Circuit circuit, Contact contact) {
        String result = null;
        if (contact.isPort() || contact.isInput()) {
            result = getContactName(circuit, contact);
        } else {
            result = getOutputContactName(circuit, contact);
        }
        return result;
    }

    public static String getSignalName(VisualCircuit circuit, VisualContact contact) {
        return getSignalName((Circuit) circuit.getMathModel(), contact.getReferencedContact());
    }

    private static String getContactName(Circuit circuit, Contact contact) {
        String result = null;
        if (contact != null) {
            String contactRef = circuit.getNodeReference(contact);
            result = NamespaceHelper.hierarchicalToFlatName(contactRef);
        }
        return result;
    }

    private static String getOutputContactName(Circuit circuit, Contact contact) {
        String result = null;
        Node parent = contact.getParent();
        if (parent instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) parent;

            Contact outputPort = getDrivenOutputPort(circuit, contact);
            if (outputPort != null) {
                // If a single output port is driven, then take its name.
                String outputPortRef = circuit.getNodeReference(outputPort);
                result = NamespaceHelper.hierarchicalToFlatName(outputPortRef);
            } else {
                // If the component has a single output, use the component name. Otherwise append the contact.
                int outputCount = 0;
                for (Node node: component.getChildren()) {
                    if (node instanceof Contact) {
                        Contact vc = (Contact) node;
                        if (vc.isOutput()) {
                            outputCount++;
                        }
                    }
                }
                if (outputCount == 1) {
                    String componentRef = circuit.getNodeReference(component);
                    result = NamespaceHelper.hierarchicalToFlatName(componentRef);
                } else {
                    String contactRef = circuit.getNodeReference(contact);
                    result = NamespaceHelper.hierarchicalToFlatName(contactRef);
                }
            }
        }
        return result;
    }

    public static Contact getDrivenOutputPort(Circuit circuit, Contact contact) {
        Contact result = null;
        boolean multipleOutputPorts = false;
        for (Contact vc: findDriven(circuit, contact, true)) {
            if (vc.isPort() && vc.isOutput()) {
                if (result != null) {
                    multipleOutputPorts = true;
                }
                result = vc;
            }
        }
        if (multipleOutputPorts) {
            result = null;
        }
        return result;
    }

    public static Type getSignalType(VisualCircuit circuit, VisualContact contact) {
        return getSignalType((Circuit) circuit.getMathModel(), contact.getReferencedContact());
    }

    public static Type getSignalType(Circuit circuit, Contact contact) {
        Type result = Type.INTERNAL;
        if (contact.isPort()) {
            // Primary port
            if (contact.isInput()) {
                result = Type.INPUT;
            } else if (contact.isOutput()) {
                result = Type.OUTPUT;
            }
        } else {
            CircuitComponent component = (CircuitComponent) contact.getParent();
            if (component.getIsEnvironment()) {
                // Contact of an environment component
                if (contact.isInput()) {
                    result = Type.OUTPUT;
                } else if (contact.isOutput()) {
                    result = Type.INPUT;
                }
            } else {
                // Contact of an ordinary component
                if (contact.isOutput() && (getDrivenOutputPort(circuit, contact) != null)) {
                    result = Type.OUTPUT;
                }
            }
        }
        return result;
    }

    public static BooleanFormula parseContactFuncton(final Circuit circuit,
            final FunctionComponent component, String function) throws ParseException {
        if (function == null) {
            return null;
        }
        return BooleanParser.parse(function, new Func<String, BooleanFormula>() {
            @Override
            public BooleanFormula eval(String name) {
                FunctionContact contact = (FunctionContact) circuit.getNodeByReference(component, name);
                if (contact == null) {
                    contact = new FunctionContact();
                    contact.setIOType(IOType.INPUT);
                    component.add(contact);
                    circuit.setName(contact, name);
                }
                return contact;
            }
        });
    }

    public static BooleanFormula parsePortFuncton(final Circuit circuit, String function) throws ParseException {
        if (function == null) {
            return null;
        }
        return BooleanParser.parse(function, new Func<String, BooleanFormula>() {
            @Override
            public BooleanFormula eval(String name) {
                FunctionContact port = (FunctionContact) circuit.getNodeByReference(null, name);
                if (port == null) {
                    port = new FunctionContact();
                    port.setIOType(IOType.OUTPUT);
                    circuit.add(port);
                    circuit.setName(port, name);
                }
                return port;
            }
        });
    }

    public static BooleanFormula parseContactFuncton(final VisualCircuit circuit,
            final VisualFunctionComponent component, String function) throws ParseException {
        if (function == null) {
            return null;
        }
        return BooleanParser.parse(function, new Func<String, BooleanFormula>() {
            @Override
            public BooleanFormula eval(String name) {
                BooleanFormula result = null;
                VisualFunctionContact contact = circuit.getOrCreateContact(component, name, IOType.INPUT);
                if ((contact != null) && (contact.getReferencedContact() instanceof BooleanFormula)) {
                    result = (BooleanFormula) contact.getReferencedContact();
                }
                return result;
            }
        });
    }

    public static BooleanFormula parsePortFuncton(final VisualCircuit circuit, String function) throws ParseException {
        if (function == null) {
            return null;
        }
        return BooleanParser.parse(function, new Func<String, BooleanFormula>() {
            @Override
            public BooleanFormula eval(String name) {
                BooleanFormula result = null;
                VisualFunctionContact port = circuit.getOrCreateContact(null, name, IOType.OUTPUT);
                if ((port != null) && (port.getReferencedContact() instanceof BooleanFormula)) {
                    result = (BooleanFormula) port.getReferencedContact();
                }
                return result;
            }
        });
    }

    public static HashSet<CircuitComponent> getComponentPreset(final Circuit circuit, MathNode curNode) {
        HashSet<CircuitComponent> result = new HashSet<>();
        HashSet<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof CircuitComponent) {
            CircuitComponent component = (CircuitComponent) curNode;
            queue.addAll(component.getInputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if ((node != null) && !visited.contains(node)) {
                visited.add(node);
                if (node instanceof CircuitComponent) {
                    result.add((CircuitComponent) node);
                } else if (node instanceof Contact) {
                    Contact contact = (Contact) node;
                    if (contact.isPort() == contact.isOutput()) {
                        queue.addAll(circuit.getPreset(node));
                    } else {
                        queue.add(contact.getParent());
                    }
                } else if (node instanceof Joint) {
                    queue.addAll(circuit.getPreset(node));
                } else if (node instanceof MathConnection) {
                    MathConnection connection = (MathConnection) node;
                    queue.add(connection.getFirst());
                }
            }
        }
        return result;
    }

    public static HashSet<VisualComponent> getComponentPreset(final VisualCircuit visualCircuit, VisualComponent visualComponent) {
        HashSet<VisualComponent> result = new HashSet<>();
        Circuit circuit = (Circuit) visualCircuit.getMathModel();
        MathNode mathComponent = visualComponent.getReferencedComponent();
        for (MathNode node: getComponentPreset(circuit, mathComponent)) {
            result.add(visualCircuit.getVisualComponent(node, VisualComponent.class));
        }
        return result;
    }

    public static HashSet<CircuitComponent> getComponentPostset(final Circuit circuit, MathNode curNode) {
        HashSet<CircuitComponent> result = new HashSet<>();
        HashSet<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof CircuitComponent) {
            CircuitComponent component = (CircuitComponent) curNode;
            queue.addAll(component.getOutputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if ((node != null) && !visited.contains(node)) {
                visited.add(node);
                if (node instanceof CircuitComponent) {
                    result.add((CircuitComponent) node);
                } else if (node instanceof Contact) {
                    Contact contact = (Contact) node;
                    if (contact.isPort() == contact.isInput()) {
                        queue.addAll(circuit.getPostset(node));
                    } else {
                        queue.add(contact.getParent());
                    }
                } else if (node instanceof Joint) {
                    queue.addAll(circuit.getPostset(node));
                } else if (node instanceof MathConnection) {
                    MathConnection connection = (MathConnection) node;
                    queue.add(connection.getSecond());
                }
            }
        }
        return result;
    }

    public static HashSet<VisualComponent> getComponentPostset(final VisualCircuit visualCircuit, VisualComponent visualComponent) {
        HashSet<VisualComponent> result = new HashSet<>();
        Circuit circuit = (Circuit) visualCircuit.getMathModel();
        MathNode mathComponent = visualComponent.getReferencedComponent();
        for (MathNode node: getComponentPostset(circuit, mathComponent)) {
            result.add(visualCircuit.getVisualComponent(node, VisualComponent.class));
        }
        return result;
    }

    public static HashSet<Contact> getPortPostset(final Circuit circuit, MathNode curNode) {
        HashSet<Contact> result = new HashSet<>();
        HashSet<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof CircuitComponent) {
            CircuitComponent component = (CircuitComponent) curNode;
            queue.addAll(component.getOutputs());
        } else {
            queue.add(curNode);
        }

        while (!queue.isEmpty()) {
            Node node = queue.remove();
            if ((node != null) && !visited.contains(node)) {
                visited.add(node);
                if (node instanceof Contact) {
                    Contact contact = (Contact) node;
                    if (contact.isOutput() && contact.isPort()) {
                        result.add(contact);
                    } else {
                        queue.addAll(circuit.getPostset(node));
                    }
                } else if (node instanceof Joint) {
                    queue.addAll(circuit.getPostset(node));
                } else if (node instanceof MathConnection) {
                    MathConnection connection = (MathConnection) node;
                    queue.add(connection.getSecond());
                }
            }
        }
        return result;
    }

    public static HashSet<VisualContact> getPortPostset(final VisualCircuit visualCircuit, VisualComponent visualComponent) {
        Circuit circuit = (Circuit) visualCircuit.getMathModel();
        MathNode mathComponent = visualComponent.getReferencedComponent();
        HashSet<Contact> ports = getPortPostset(circuit, mathComponent);
        return getVisualContacts(visualCircuit, ports);
    }

    private static HashSet<VisualContact> getVisualContacts(final VisualCircuit visualCircuit, Collection<Contact> mathContacts) {
        HashSet<VisualContact> result = new HashSet<>();
        for (Contact mathContact: mathContacts) {
            VisualContact visualContact = visualCircuit.getVisualComponent(mathContact, VisualContact.class);
            if (visualContact != null) {
                result.add(visualContact);
            }
        }
        return result;
    }

}
