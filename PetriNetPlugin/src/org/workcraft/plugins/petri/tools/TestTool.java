package org.workcraft.plugins.petri.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TestTool implements Tool{

	private final Framework framework;

	public TestTool(Framework framework){

		this.framework = framework;

	}


	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		// TODO Auto-generated method stub
	return WorkspaceUtils.canHas(we, PetriNet.class);
	}

	@Override
	public String getSection() {
		// TODO Auto-generated method stub
		return "test";
	}

	@Override
	public String getDisplayName() {
		// TODO Auto-generated method stub
		return "Test";
	}

	@Override
	public void run(WorkspaceEntry we) {
		System.out.println("================================================================================");

		PetriNet net=(PetriNet)we.getModelEntry().getMathModel();
		VisualPetriNet vnet = (VisualPetriNet)we.getModelEntry().getVisualModel();

		System.out.println("places size"+ net.getPlaces().size());

	}

}
