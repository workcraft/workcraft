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
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.util.Hierarchy;

public class CircuitUtils {

	public static VisualContact findDriver(VisualCircuit circuit, VisualContact contact) {
		Contact mathDriver = findDriver((Circuit)circuit.getMathModel(), contact.getReferencedContact());
		return getVisualContact(circuit, mathDriver);
	}

	public static Contact findDriver(Circuit circuit, MathNode curNode) {
		Contact result = null;
		HashSet<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof MathConnection) {
        	queue.add(((MathConnection)curNode).getFirst());
        } else {
        	queue.add(curNode);
        }
        while (!queue.isEmpty()) {
			if (queue.size() != 1) {
				throw new RuntimeException("Found more than one potential driver for '"
						+ circuit.getNodeReference(curNode) + "'!");
			}
            Node node = queue.remove();
            if (!visited.contains(node)) {
            	visited.add(node);
            	if (node instanceof Joint) {
            		queue.addAll(circuit.getPreset(node));
            	} else if (node instanceof Contact) {
            		Contact contact = (Contact)node;
//            		Node parent = contact.getParent();
//            		if (contact.isOutput() && (parent instanceof CircuitComponent)) {
//            			CircuitComponent component = (CircuitComponent)parent;
//            			if (component.isBufferOrInverter() && component.getIsZeroDelay()) {
//            				contact = component.getInputs().iterator().next();
//            			}
//            		}
            		if (contact.isDriver()) {
            			result = contact;
            		} else if (node == curNode) {
            			queue.addAll(circuit.getPreset(node));
            		}
            	} else {
            		throw new RuntimeException("Unexpected node '" + circuit.getNodeReference(node)
            				+ "' in the driver trace for node '" + circuit.getNodeReference(curNode) + "'!");
            	}
            }
		}
		return result;
	}

	public static Collection<VisualContact> findDriven(VisualCircuit circuit, VisualContact contact) {
		Collection<VisualContact> result = new HashSet<>();
		for (Contact mathContact: findDriven((Circuit)circuit.getMathModel(), contact.getReferencedContact())) {
			VisualContact visualContact = getVisualContact(circuit, mathContact);
			if (visualContact != null) {
				result.add(visualContact);
			}
		}
		return result;
	}

	public static Collection<Contact> findDriven(Circuit circuit, MathNode curNode) {
		Set<Contact> result = new HashSet<Contact>();
		HashSet<Node> visited = new HashSet<>();
        Queue<Node> queue = new LinkedList<>();
        if (curNode instanceof MathConnection) {
        	queue.add(((MathConnection)curNode).getSecond());
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
            		Contact contact = (Contact)node;
            		if (contact.isDriven()) {
            			result.add(contact);
            		} else if (node == curNode) {
            			queue.addAll(circuit.getPostset(node));
            		}
            	} else {
            		throw new RuntimeException("Unexpected node '" + circuit.getNodeReference(node)
            				+ "' in the driven trace for node '" + circuit.getNodeReference(curNode) + "'!");
            	}
            }
        }
		return result;
	}

	public static String getContactName(Circuit circuit, Contact contact) {
		String result = null;
		if (contact.isPort()) {
			result = circuit.getName(contact);
		} else {
			if (contact.isInput()) {
				result = getInputContactName(circuit, contact);
			} else if (contact.isOutput()) {
				result = getOutputContactName(circuit, contact);
			}
		}
		return result;
	}

	public static String getContactName(VisualCircuit circuit, VisualContact contact) {
		return getContactName((Circuit)circuit.getMathModel(), contact.getReferencedContact());
	}

	private static String getInputContactName(Circuit circuit, Contact contact) {
		String result = null;
		Node parent = contact.getParent();
		if (parent instanceof FunctionComponent) {
			FunctionComponent component = (FunctionComponent)parent;
			String componentName = circuit.getName(component);
			String componentFlatName = NamespaceHelper.hierarchicalToFlatName(componentName);
			String contactName = circuit.getName(contact);
			result = componentFlatName + "_" + contactName;
		}
		return result;
	}

	private static String getOutputContactName(Circuit circuit, Contact contact) {
		String result = null;
		Node parent = contact.getParent();
		if (parent instanceof FunctionComponent) {
			FunctionComponent component = (FunctionComponent)parent;

			Contact outputPort = getDrivenOutputPort(circuit, contact);
			if (outputPort != null) {
				// If a single output port is driven, then take its name.
				result = circuit.getName(outputPort);
			} else {
				// If the component has a single output, use the component name. Otherwise append the contact.
				String componentName = circuit.getName(component);
				result = NamespaceHelper.hierarchicalToFlatName(componentName);
				int output_cnt = 0;
				for (Node node: component.getChildren()) {
					if (node instanceof Contact) {
						Contact vc = (Contact)node;
						if (vc.isOutput()) {
							output_cnt++;
						}
					}
				}
				if (output_cnt > 1) {
					String suffix = "_" + circuit.getName(contact);
					result += suffix;
				}
			}
		}
		return result;
	}

	public static Contact getDrivenOutputPort(Circuit circuit, Contact contact) {
		Contact result = null;
		boolean multipleOutputPorts = false;
		for (Contact vc: findDriven(circuit, contact)) {
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

	public static VisualContact getVisualContact(VisualCircuit visualCircuit, Contact contact) {
		VisualContact result = null;
		if (contact != null) {
			Collection<VisualContact> visualContacts = Hierarchy.getDescendantsOfType(visualCircuit.getRoot(), VisualContact.class);
			for (VisualContact visualContact: visualContacts) {
				if (visualContact.getReferencedContact() == contact) {
					result = visualContact;
					break;
				}
			}
		}
		return result;
	}

	public static Type getSignalType(VisualCircuit circuit, VisualContact contact) {
		return getSignalType((Circuit)circuit.getMathModel(), contact.getReferencedContact());
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
			CircuitComponent component = (CircuitComponent)contact.getParent();
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

}
