package org.workcraft.plugins.circuit.observers;

import org.workcraft.dom.Node;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.workers.BooleanWorker;
import org.workcraft.formula.workers.CleverBooleanWorker;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;

public class FunctionConsistencySupervisor extends HierarchySupervisor {

    private static final BooleanWorker WORKER = new CleverBooleanWorker();

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            for (Node node : e.getAffectedNodes()) {
                if (node instanceof Contact) {
                    // Update all set/reset functions when a contact is removed
                    final Contact contact = (Contact) node;
                    handleContactRemoval(contact);
                }
            }
        }
    }

    private void handleContactRemoval(final Contact contact) {
        final ArrayList<FunctionContact> functionContacts = new ArrayList<>(
                Hierarchy.getChildrenOfType(getRoot(), FunctionContact.class));

        for (final FunctionContact functionContact : functionContacts) {

            functionContact.setSetFunction(FormulaUtils.replaceZero(
                    functionContact.getSetFunction(), contact, WORKER));

            functionContact.setResetFunction(FormulaUtils.replaceZero(
                    functionContact.getResetFunction(), contact, WORKER));
        }
    }

}
