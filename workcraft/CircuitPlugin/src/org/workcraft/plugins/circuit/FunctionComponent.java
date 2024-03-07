package org.workcraft.plugins.circuit;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.FormulaUtils;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.utils.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;

@IdentifierPrefix("g")
@VisualClass(VisualFunctionComponent.class)
public class FunctionComponent extends CircuitComponent {

    public static final String PROPERTY_IS_ZERO_DELAY = "Zero delay";
    public static final String PROPERTY_ARBITRATION_PRIMITIVE = "Arbitration";

    private boolean isZeroDelay;
    private boolean isArbitrationPrimitive;

    private final class CircuitHierarchySupervisor extends HierarchySupervisor {
        @Override
        public void handleEvent(HierarchyEvent e) {
            if (e instanceof NodesDeletingEvent) {
                for (Node node: e.getAffectedNodes()) {
                    if (node instanceof Contact) {
                        final Contact contact = (Contact) node;
                        removeContactFromFunctions(contact);
                    }
                }
            }
        }

        private void removeContactFromFunctions(final Contact contact) {
            for (FunctionContact fc: new ArrayList<>(getFunctionContacts())) {
                BooleanFormula setFunction = FormulaUtils.remove(fc.getSetFunction(), contact);
                BooleanFormula resetFunction = FormulaUtils.remove(fc.getResetFunction(), contact);
                fc.setBothFunctions(setFunction, resetFunction);
            }
        }
    }

    public FunctionComponent() {
        // Update all set/reset functions of the component when its contact is removed
        new CircuitHierarchySupervisor().attach(this);
    }

    public void setIsZeroDelay(boolean value) {
        if (isZeroDelay != value) {
            isZeroDelay = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_IS_ZERO_DELAY));
        }
    }

    public boolean getIsZeroDelay() {
        return isZeroDelay;
    }

    public void setIsArbitrationPrimitive(boolean value) {
        if (isArbitrationPrimitive != value) {
            isArbitrationPrimitive = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_ARBITRATION_PRIMITIVE));
        }
    }

    public boolean getIsArbitrationPrimitive() {
        return isArbitrationPrimitive;
    }

    public Collection<FunctionContact> getFunctionContacts() {
        return Hierarchy.getChildrenOfType(this, FunctionContact.class);
    }

    public Collection<FunctionContact> getFunctionInputs() {
        ArrayList<FunctionContact> result = new ArrayList<>();
        for (FunctionContact contact: getFunctionContacts()) {
            if (contact.isInput()) {
                result.add(contact);
            }
        }
        return result;
    }

    public Collection<FunctionContact> getFunctionOutputs() {
        ArrayList<FunctionContact> result = new ArrayList<>();
        for (FunctionContact contact: getFunctionContacts()) {
            if (contact.isOutput()) {
                result.add(contact);
            }
        }
        return result;
    }

    public boolean isBlackbox() {
        return getFunctionOutputs().stream().noneMatch(FunctionContact::hasFunction);
    }

    public boolean isGate() {
        Collection<Contact> outputs = getOutputs();
        return (outputs != null) && (outputs.size() == 1);
    }

    public boolean isBuffer() {
        boolean result = false;
        Collection<FunctionContact> contacts = getFunctionContacts();
        FunctionContact inputContact = null;
        FunctionContact outputContact = null;
        if (contacts.size() == 2) {
            inputContact = (FunctionContact) getFirstInput();
            outputContact = (FunctionContact) getFirstOutput();
        }
        if ((inputContact != null) && (outputContact != null)) {
            BooleanFormula setFunction = outputContact.getSetFunction();
            if ((setFunction != null) && (outputContact.getResetFunction() == null)) {
                BooleanFormula zeroReplace = FormulaUtils.replaceZero(setFunction, inputContact);
                BooleanFormula oneReplace = FormulaUtils.replaceOne(setFunction, inputContact);
                result = (zeroReplace == Zero.getInstance()) && (oneReplace == One.getInstance());
            }
        }
        return result;
    }

    public boolean isInverter() {
        boolean result = false;
        Collection<Contact> contacts = getContacts();
        FunctionContact inputContact = null;
        FunctionContact outputContact = null;
        if (contacts.size() == 2) {
            inputContact = (FunctionContact) getFirstInput();
            outputContact = (FunctionContact) getFirstOutput();
        }
        if ((inputContact != null) && (outputContact != null)) {
            BooleanFormula setFunction = outputContact.getSetFunction();
            if ((setFunction != null) && (outputContact.getResetFunction() == null)) {
                BooleanFormula zeroReplace = FormulaUtils.replaceZero(setFunction, inputContact);
                BooleanFormula oneReplace = FormulaUtils.replaceOne(setFunction, inputContact);
                result = (zeroReplace == One.getInstance()) && (oneReplace == Zero.getInstance());
            }
        }
        return result;
    }

    public boolean isTie1() {
        Collection<FunctionContact> contacts = getFunctionContacts();
        FunctionContact contact = contacts.size() != 1 ? null : contacts.iterator().next();
        if ((contact != null) && contact.isOutput()) {
            BooleanFormula resetFunction = contact.getResetFunction();
            return contact.getInitToOne() && Zero.getInstance().equals(resetFunction);
        }
        return false;
    }

    public boolean isTie0() {
        Collection<FunctionContact> contacts = getFunctionContacts();
        FunctionContact contact = contacts.size() != 1 ? null : contacts.iterator().next();
        if ((contact != null) && contact.isOutput()) {
            BooleanFormula setFunction = contact.getSetFunction();
            return !contact.getInitToOne() && Zero.getInstance().equals(setFunction);
        }
        return false;
    }

    public boolean isConst() {
        return isTie1() || isTie0();
    }

    public FunctionContact getGateOutput() {
        FunctionContact gateOutput = null;
        for (Node node: getChildren()) {
            if (!(node instanceof FunctionContact)) continue;
            FunctionContact contact = (FunctionContact) node;
            if (!contact.isOutput()) continue;
            if (gateOutput == null) {
                gateOutput = contact;
            } else {
                // more than one output - not a gate
                gateOutput = null;
                break;
            }
        }
        return gateOutput;
    }

    public boolean isSequentialGate() {
        FunctionContact gateOutput = getGateOutput();
        return (gateOutput != null) && gateOutput.isSequential();
    }

}
