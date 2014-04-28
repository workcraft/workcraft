package org.workcraft.plugins.son.tools;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.Annotations;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.BSONPathAlg;
import org.workcraft.plugins.son.algorithm.CSONPathAlg;
import org.workcraft.plugins.son.algorithm.RelationAlg;
import org.workcraft.plugins.son.algorithm.SimulationAlg;
import org.workcraft.plugins.son.components.Condition;
import org.workcraft.plugins.son.components.Event;
import org.workcraft.plugins.son.components.VisualChannelPlace;
import org.workcraft.plugins.son.connections.AsynLine;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.test.test;
import org.workcraft.plugins.son.verify.BSONStructureTask;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TestTool implements Tool{

	private final Framework framework;

	public TestTool(Framework framework){

		this.framework = framework;

	}


	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, SON.class);

	}

	public String getSection(){
		return "test";
	}

	public String getDisplayName(){
		return "Test";
	}

	public void run(WorkspaceEntry we){
		System.out.println("================================================================================");
		SONModel net=(SONModel)we.getModelEntry().getMathModel();
		VisualSON vnet = (VisualSON)we.getModelEntry().getVisualModel();

		mathLevelTest(net, vnet);


		/*
		Collection<Condition[]> before = new ArrayList<Condition[]>();
		for(Event e : net.getEvents()){
			before =  alg.before(e);

			if(!before.isEmpty()){
			System.out.println("before event = " + net.getName(e));
			System.out.println(before.size());
			for(Condition[] condition : before)
				System.out.print(" ["+net.getName(condition[0]) + " " + net.getName(condition[1])+ "], ");
			System.out.println();
					}
		}

		BSONPathAlg path = new BSONPathAlg(net);
		Collection<ArrayList<Node>> cycle = path.cycleTask(net.getComponents());

		List<ArrayList<Node>> delList = new ArrayList<ArrayList<Node>>();
		for(ArrayList<Node> filter : cycle)
			if(!net.getSONConnectionTypes( filter).contains("POLYLINE"))
				delList.add( filter);

		cycle.removeAll(delList);

		System.out.println("cycle size  "  + cycle.size());
		for(ArrayList<Node> c : cycle){
			System.out.println("cycle:  ");
			for(Node n : c)
				System.out.print(net.getName(n));
					System.out.println();
		}
		System.out.println();


		SimulationAlg alg = new SimulationAlg(net);
		for(Event e : net.getEvents()){
			System.out.println("start   " + net.getName(e) +  "  test");
			for(Event test : alg.getSyncRelate(e))
			System.out.println(net.getName(test));
			alg.clearEventSet();
		System.out.println();
		}

	System.out.println("visual Connections type test: ");
		System.out.println("size="+ vnet.getVisualConnections().size());

		for(Node node : vnet.getVisualConnections()){
			System.out.print(((VisualSONConnection)node).getSONConnectionType().toString());
			System.out.println();

		}
		System.out.println("");

		System.out.println("Connections test: ");

		for(Node node : net.getSONConnections()){
			System.out.print(net.getName(node)+",");
			System.out.print(((SONConnection)node).getType());
			System.out.println();

		}
		System.out.println("");


		System.out.println("visual component test: ");
		System.out.print(vnet.getVisualComponent().size());

		for(Node node : vnet.getVisualComponent()){
			System.out.print(((VisualComponent)node).getLabel());
			System.out.println();

		}
		System.out.println("");

		System.out.println("math nodes test: "+ net.getComponents().size());

		for(Node node : net.getComponents()){
			System.out.print(net.getName(node)+",");
			System.out.print("  " + node.toString());
			System.out.println(net.getRoot().toString());
			System.out.println();
		}
		System.out.println("");

		System.out.println("group test ");
		for(ONGroup group : net.getGroups()){

				System.out.println("group size"+group.getComponents().size()+ "    "+ group.getLabel());
				group.setForegroundColor(Color.red);
				for(Node node : group.getComponents()){
					System.out.print(net.getName(node)+",");
				}
				System.out.println("");

		}
		System.out.println("");

		System.out.println("visual group test ");
		for(VisualONGroup group : vnet.getVisualGroups()){

				System.out.println("group size"+group.getVisualComponents().size()+ "    "+ group.getLabel());
				group.setForegroundColor(Color.red);
				System.out.println("");

		}
		System.out.println("");

/*

	//		for (SONConnection con : net.getSONConnections())
	//			System.out.println(con.getType());

		Collection<Node> nodes = net.getComponents();

		CSONCycleAlg textCycle = new CSONCycleAlg(net);

		for (Node[] node : textCycle.createAdj(nodes)){
				System.out.println("");
				System.out.print(net.getName(node[0])+ net.getName(node[1]));
		}



		Collection<ArrayList<Node>> pathResult = textCycle.pathTask(nodes);
		Collection<ArrayList<Node>> cycleResult = textCycle.cycleTask(nodes);

		for (ArrayList<Node> list : cycleResult){
			System.out.println("cycle path:");
			for (Node node : list){
				System.out.print(net.getName(node) + " ");
			}
			System.out.println();

		}

		 */
	}

	private void mathLevelTest(SONModel net, VisualSON vnet){
		for(ONGroup group: net.getGroups()){
			System.out.println(group.toString());
			System.out.println("Page size = " + group.getPageNodes().size());
			System.out.println("block size = " + group.getBlock().size());
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

