package org.workcraft.plugins.son.tools;


import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.VisualSON;

import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TestTool2 implements Tool{

	private final Framework framework;

	public TestTool2(Framework framework){

		this.framework = framework;

	}


	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);

	}

	public String getSection(){
		return "test";
	}

	public String getDisplayName(){
		return "Test2";
	}

	public void run(WorkspaceEntry we){
		System.out.println("================================================================================");
		SONModel net=(SONModel)we.getModelEntry().getMathModel();
		VisualSON vnet = (VisualSON)we.getModelEntry().getVisualModel();

		//mathLevelTest(net, vnet);
		//this.convertBlockTest(net, vnet);
	}

/*	private void convertBlockTest(SONModel net, VisualSON vnet){
		System.out.println("vcon size " + vnet.getVisualConnections().size());

				vnet.connectToBlocksInside();

	}*/

	private void mathLevelTest(SONModel net, VisualSON vnet){
		for(ONGroup group: net.getGroups()){
			System.out.println(group.toString());
			System.out.println("Page size = " + group.getPageNodes().size());
			System.out.println("block size = " + group.getBlocks().size());
			System.out.println("Condition size = " + group.getConditions().size());
			System.out.println("Event size = " + group.getEvents().size());
			System.out.println("Connection size = " + group.getSONConnections().size());
			System.out.println();
		}

		for(PageNode page : net.getPageNodes()){
			System.out.println("page parent  "+ page.getParent().toString());
		}

		for(VisualONGroup vgroup: vnet.getVisualONGroups()){
			System.out.println(vgroup.toString());
			System.out.println("Page size = " + vgroup.getVisualPages().size());
			System.out.println("block size = " + vgroup.getVisualBlocks().size());

		}

		for(VisualPage page : vnet.getVisualPages()){
			System.out.println();
			System.out.println("visual page parent  "+ page.getParent().toString());
		}
	}
}

