package org.workcraft.plugins.circuit;

import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanUtils;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.Hierarchy;

public class FunctionConsistencySupervisor extends HierarchySupervisor {

    private final Circuit circuit;

    public FunctionConsistencySupervisor(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            for (Node node: e.getAffectedNodes()) {
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

        for (final FunctionContact functionContact: functionContacts) {
            final BooleanFormula setFunction = BooleanUtils.cleverReplace(
                    functionContact.getSetFunction(), contact, Zero.instance());

            functionContact.setSetFunction(setFunction);

            final BooleanFormula resetFunction = BooleanUtils.cleverReplace(
                    functionContact.getResetFunction(), contact, Zero.instance());

            functionContact.setResetFunction(resetFunction);
        }
    }

}
