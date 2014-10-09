package org.workcraft.plugins.petri.tools;

import org.workcraft.Tool;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TestTool implements Tool{
	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, PetriNet.class);
	}

	public String getSection(){
		return "Test Tool";
	}

	public String getDisplayName(){
		return "Math level test";
	}

	public void run(WorkspaceEntry we){
		PetriNet net=(PetriNet)we.getModelEntry().getMathModel();
		System.out.println("math places");
		for(Place p : net.getPlaces())
			System.out.print(net.getName(p) + " ");

		System.out.println();

		VisualPetriNet vnet= (VisualPetriNet)we.getModelEntry().getVisualModel();
		System.out.println("visual places");
		for(VisualPlace vp : vnet.getVisualPlaces())
			System.out.print(vnet.getName(vp));
	}

}
