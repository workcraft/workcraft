package org.workcraft.plugins.modelchecking;

import org.workcraft.dom.Model;
import org.workcraft.plugins.petri.PetriNet;

public class PetriNetDeadlockChecker implements ModelChecker {

	public String getDisplayName() {
		return "Deadlock";
	}

	public boolean isApplicableTo(Model model) {
		if (PetriNet.class.isAssignableFrom(model.getMathModel().getClass()))
				return true;
		return false;
	}

	public void run(Model model) {
		System.out.println ("deadlock *search*");
	}
}
