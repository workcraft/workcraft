package org.workcraft.plugins.circuit;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.util.Hierarchy;

public class CircuitUtils {

	public static VisualContact findDriver(VisualCircuit circuit, VisualContact contact) {
		Contact mathDriver = findDriver((Circuit)circuit.getMathModel(), contact.getReferencedContact());
		return getVisualContact(circuit, mathDriver);
	}

	public static Contact findDriver(Circuit circuit, Contact contact) {
		Contact result = null;
        Queue<Node> queue = new LinkedList<Node>(circuit.getPreset(contact));
        while (!queue.isEmpty()) {
			if (queue.size() != 1) {
				throw new RuntimeException("Found more than one potential driver for target "
						+ getContactName(circuit, contact) + "!");
			}
            Node node = queue.remove();
			if (node instanceof Contact) {
				Contact vc = (Contact)node;
				if (vc.isDriver()) {
					result = vc;
				}
			} else {
				queue.addAll(circuit.getPreset(node));
			}
		}
		return result;
	}

	public static Collection<Contact> findDriven(Circuit circuit, Contact contact) {
		Set<Contact> result = new HashSet<Contact>();
        Queue<Node> queue = new LinkedList<Node>(circuit.getPostset(contact));
        while (!queue.isEmpty()) {
            Node node = queue.remove();
			if (node instanceof Contact) {
				Contact vc = (Contact)node;
				if (vc.isDriven()) {
					result.add(vc);
				}
			} else {
                queue.addAll(circuit.getPostset(node));
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

	public static Type getSignalType(VisualCircuit circuit, VisualFunctionContact contact) {
		return getSignalType((Circuit)circuit.getMathModel(), contact.getReferencedFunctionContact());
	}

	public static Type getSignalType(Circuit circuit, FunctionContact contact) {
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
