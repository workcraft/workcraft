package org.workcraft.plugins.circuit.observers;

import org.workcraft.dom.Node;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.Contact;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.FunctionContact;
import org.workcraft.plugins.circuit.utils.StructureUtils;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.SortUtils;
import org.workcraft.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class FunctionConsistencySupervisor extends StateSupervisor {

    private final Circuit circuit;

    public FunctionConsistencySupervisor(Circuit circuit) {
        this.circuit = circuit;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if (e instanceof PropertyChangedEvent) {
            PropertyChangedEvent pce = (PropertyChangedEvent) e;
            Object sender = e.getSender();
            String propertyName = pce.getPropertyName();
            if ((sender instanceof FunctionContact)
                    && (propertyName.equals(FunctionContact.PROPERTY_FUNCTION))) {

                handleFunctionChange((FunctionContact) sender);
            }
        }
        if (e instanceof NodesDeletingEvent) {
            NodesDeletingEvent nde = (NodesDeletingEvent) e;
            for (Node node : nde.getAffectedNodes()) {
                if (node instanceof Contact) {
                    // Update all set/reset functions when a contact is removed
                    Contact contact = (Contact) node;
                    handleContactRemoval(contact);
                }
            }
        }
    }

    private void handleFunctionChange(FunctionContact contact) {
        Node parent = contact.getParent();
        if (parent instanceof FunctionComponent) {
            FunctionComponent component = (FunctionComponent) parent;
            if (!component.isBlackbox() && (component.getRefinement() != null)) {
                component.setRefinement(null);
                component.setModule("");
            }
            List<String> invalidZeroDelayComponentRefs = new ArrayList<>();
            if (component.getIsZeroDelay() && !component.isBuffer() && !component.isInverter()) {
                component.setIsZeroDelay(false);
                invalidZeroDelayComponentRefs.add(circuit.getComponentReference(component));
            }
            if (!component.isCell()) {
                for (FunctionComponent predComponent : StructureUtils.getPresetComponents(circuit, component)) {
                    if (predComponent.getIsZeroDelay()) {
                        predComponent.setIsZeroDelay(false);
                        invalidZeroDelayComponentRefs.add(circuit.getComponentReference(predComponent));
                    }
                }
            }
            if (!invalidZeroDelayComponentRefs.isEmpty()) {
                SortUtils.sortNatural(invalidZeroDelayComponentRefs);
                DialogUtils.showWarning(TextUtils.wrapMessageWithItems(
                        "Zero delay property is removed from component", invalidZeroDelayComponentRefs));
            }
        }
    }

    private void handleContactRemoval(Contact contact) {
        ArrayList<FunctionContact> functionContacts = new ArrayList<>(
                Hierarchy.getChildrenOfType(getRoot(), FunctionContact.class));

        for (FunctionContact functionContact : functionContacts) {
            BooleanFormula setFunction = FormulaUtils.remove(functionContact.getSetFunction(), contact);
            BooleanFormula resetFunction = FormulaUtils.remove(functionContact.getResetFunction(), contact);
            functionContact.setBothFunctions(setFunction, resetFunction);
        }
    }

}
