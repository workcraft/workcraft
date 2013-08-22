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
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.graph.tools.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.SONModel;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.CSONCycleAlg;
import org.workcraft.plugins.son.connections.AsynLine;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.test.test;
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
}

