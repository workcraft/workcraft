package org.workcraft.plugins.circuit;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanUtils;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.Hierarchy;

@VisualClass(org.workcraft.plugins.circuit.VisualFunctionComponent.class)
public class FunctionComponent extends CircuitComponent {
	public static final String PROPERTY_IS_ZERO_DELAY = "Zero delay";

	private boolean isZeroDelay;

	private final class CircuitHierarchySupervisor extends HierarchySupervisor {
		@Override
		public void handleEvent(HierarchyEvent e) {
			if (e instanceof NodesDeletingEvent) {
				for (Node node: e.getAffectedNodes()) {
					if (node instanceof Contact) {
						final Contact contact = (Contact)node;
						removeContactfromFunctions(contact);
					}
				}
			}
		}

		private void removeContactfromFunctions(final Contact contact) {
			for (FunctionContact fc: new ArrayList<FunctionContact>(getFunctionContacts())) {
				BooleanFormula setFunction = BooleanUtils.cleverReplace(fc.getSetFunction(), contact, Zero.instance());
				fc.setSetFunction(setFunction);
				BooleanFormula resetFunction = BooleanUtils.cleverReplace(fc.getResetFunction(), contact, Zero.instance());
				fc.setResetFunction(resetFunction);
			}
		}
	}

	public FunctionComponent() {
		// Update all set/reset functions of the component when its contact is removed
		new CircuitHierarchySupervisor().attach(this);
	}


	public void setIsZeroDelay(boolean value) {
		if (this.isZeroDelay != value) {
			this.isZeroDelay = value;
			sendNotification(new PropertyChangedEvent(this, PROPERTY_IS_ZERO_DELAY));
		}
	}

	public boolean getIsZeroDelay() {
		return isZeroDelay;
	}

	public Collection<FunctionContact> getFunctionContacts() {
		return Hierarchy.getChildrenOfType(this, FunctionContact.class);
	}

	public boolean isBuffer() {
		boolean result = false;
		Collection<FunctionContact> contacts = getFunctionContacts();
		FunctionContact inputContact = null;
		FunctionContact outputContact = null;
		if (contacts.size() == 2) {
			inputContact = (FunctionContact)getFirstInput();
			outputContact = (FunctionContact)getFirstOutput();
		}
		if ((inputContact != null) && (outputContact != null)) {
			BooleanFormula setFunction = outputContact.getSetFunction();
			if ((setFunction != null) && (outputContact.getResetFunction() == null)) {
				BooleanFormula zeroReplace = BooleanUtils.cleverReplace(setFunction, inputContact, Zero.instance());
				BooleanFormula oneReplace = BooleanUtils.cleverReplace(setFunction, inputContact, One.instance());
				result = ((zeroReplace == Zero.instance()) && (oneReplace == One.instance()));
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
			inputContact = (FunctionContact)getFirstInput();
			outputContact = (FunctionContact)getFirstOutput();
		}
		if ((inputContact != null) && (outputContact != null)) {
			BooleanFormula setFunction = outputContact.getSetFunction();
			if ((setFunction != null) && (outputContact.getResetFunction() == null)) {
				BooleanFormula zeroReplace = BooleanUtils.cleverReplace(setFunction, inputContact, Zero.instance());
				BooleanFormula oneReplace = BooleanUtils.cleverReplace(setFunction, inputContact, One.instance());
				result = ((zeroReplace == One.instance()) && (oneReplace == Zero.instance()));
			}
		}
		return result;
	}

}
