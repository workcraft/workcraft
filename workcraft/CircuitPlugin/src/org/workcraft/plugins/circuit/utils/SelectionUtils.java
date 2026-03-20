package org.workcraft.plugins.circuit.utils;

import org.workcraft.dom.visual.VisualNode;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualCircuitComponent;
import org.workcraft.plugins.circuit.VisualContact;

import java.util.Collection;
import java.util.HashSet;

public final class SelectionUtils {

    private SelectionUtils() {
    }

    public static void retainSelectedContacts(VisualCircuit circuit, Collection<VisualContact> contacts) {
        Collection<VisualNode> selection = circuit.getSelection();
        if (!selection.isEmpty()) {
            HashSet<VisualContact> selectedContacts = new HashSet<>();
            for (VisualNode node : selection) {
                if (node instanceof VisualContact contact) {
                    selectedContacts.add(contact);
                } else if (node instanceof VisualCircuitComponent component) {
                    selectedContacts.addAll(component.getVisualContacts());
                }
            }
            contacts.retainAll(selectedContacts);
        }
    }

}
