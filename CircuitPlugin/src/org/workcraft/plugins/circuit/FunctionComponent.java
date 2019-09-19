package org.workcraft.plugins.circuit;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.formula.BooleanFormula;
import org.workcraft.formula.One;
import org.workcraft.formula.Zero;
import org.workcraft.formula.utils.BooleanUtils;
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

    private boolean isZeroDelay;

    private final class CircuitHierarchySupervisor extends HierarchySupervisor {
        @Override
        public void handleEvent(HierarchyEvent e) {
            if (e instanceof NodesDeletingEvent) {
                for (Node node: e.getAffectedNodes()) {
                    if (node instanceof Contact) {
                        final Contact contact = (Contact) node;
                        removeContactfromFunctions(contact);
                    }
                }
            }
        }

        private void removeContactfromFunctions(final Contact contact) {
            for (FunctionContact fc: new ArrayList<>(getFunctionContacts())) {
                BooleanFormula setFunction = BooleanUtils.replaceClever(fc.getSetFunction(), contact, Zero.getInstance());
                fc.setSetFunction(setFunction);
                BooleanFormula resetFunction = BooleanUtils.replaceClever(fc.getResetFunction(), contact, Zero.getInstance());
                fc.setResetFunction(resetFunction);
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
                BooleanFormula zeroReplace = BooleanUtils.replaceClever(setFunction, inputContact, Zero.getInstance());
                BooleanFormula oneReplace = BooleanUtils.replaceClever(setFunction, inputContact, One.getInstance());
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
                BooleanFormula zeroReplace = BooleanUtils.replaceClever(setFunction, inputContact, Zero.getInstance());
                BooleanFormula oneReplace = BooleanUtils.replaceClever(setFunction, inputContact, One.getInstance());
                result = (zeroReplace == One.getInstance()) && (oneReplace == Zero.getInstance());
            }
        }
        return result;
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
