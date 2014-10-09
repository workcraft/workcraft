package org.workcraft.plugins.son.tools;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.HashSet;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.algorithm.SimulationAlg;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.util.GUI;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class TestTool extends AbstractTool implements Tool{

	private final Framework framework;
	private String message = "";

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

	GraphEditor editor1;

	public void run(WorkspaceEntry we){
		System.out.println("================================================================================");
		SON net=(SON)we.getModelEntry().getMathModel();
		VisualSON vnet = (VisualSON)we.getModelEntry().getVisualModel();
		Graphics2D g = null;
		GraphEditorPanel editor = null;
		for(GraphEditorPanel ed : framework.getMainWindow().getEditors(we)){
			editor = ed;
			g = (Graphics2D)editor.getGraphics();
		}
		for(int i=0; i< 5000; i++){
			GUI.drawEditorMessage(editor, g, Color.BLACK, "afdasfasd");
			System.out.println(i);
		}
		activated(editor);
		editor.repaint();

		//GUI.drawEditorMessage(editor, g, Color.red, "sfasdfadsfa");
		//syncCycleTest(net);
		//blockMathLevelTest(net, vnet);
		//mathLevelTest(net, vnet);
		//connectionTypeTest(net, vnet);
		//this.convertBlockTest(net, vnet);
		//relation(net, vnet);
		//conditionOutputTest(vnet);
	}


	@Override
	public void drawInScreenSpace(final GraphEditor editor, Graphics2D g) {
		System.out.println("editor1111111");
		int a =0;
		if(a == 0)
			GUI.drawEditorMessage(editor, g, Color.BLACK, "afdasfasd");
	}

	private void relation(SON net, VisualSON vnet){
		for(Node node : net.getComponents()){
			System.out.println("node name: "+net.getName(node) + "  node pre size:" + net.getPreset(node).size()
					+ "  node post size:" + net.getPostset(node).size());
		}
	}

	private void conditionOutputTest(VisualSON vnet){
		vnet.connectToBlocks();
	}

	private void syncCycleTest(SON net){
		SimulationAlg simuAlg = new SimulationAlg(net);
		HashSet<Node> nodes = new HashSet<Node>();
		nodes.addAll(net.getConditions());
		nodes.addAll(net.getTransitionNodes());

		simuAlg.getSyncCycles(nodes);
	}

	private void exceptionTest() throws InvalidConnectionException{
		boolean a = true;
		if(a){
			message = "adfa";
			throw new InvalidConnectionException(message);
		}
	}

/*	private void convertBlockTest(SONModel net, VisualSON vnet){
		for(Node node : net.getSONConnections()){
			System.out.println("before "+net.getName(node)+ " parent "+ node.getParent().toString() + " type = " + ((SONConnection)node).getType());
	}
			vnet.connectToBlocks();
			System.out.println("node size =" + net.getComponents().size());
			for(Node node : net.getSONConnections()){
					System.out.println("after "+net.getName(node)+ " parent "+ node.getParent().toString() + " type = " + ((SONConnection)node).getType());
			}
	}
	*/

	private void blockMathLevelTest(SON net, VisualSON vnet){
		for(Block block : net.getBlocks()){
			System.out.println("block name :" + net.getName(block));
			System.out.println("connection size : " + block.getSONConnections().size());
		}

/*		for(VisualBlock block : vnet.getVisualBlocks()){
			System.out.println("visual block name :" + vnet.getName(block));
			System.out.
			println("visual connection size : " + block.getVisualSONConnections().size());
		}*/

	}

	private void mathLevelTest(SON net, VisualSON vnet){
		for(ONGroup group: net.getGroups()){
			System.out.println(group.toString());
			System.out.println("Page size = " + group.getPageNodes().size());
			System.out.println("block size = " + group.getBlocks().size());
			System.out.println("Condition size = " + group.getConditions().size());
			System.out.println("Event size = " + group.getEvents().size());
			System.out.println("Connection size = " + group.getSONConnections().size());
			System.out.println();
		}

/*		for(PageNode page : net.getPageNodes()){
			System.out.println("page parent  "+ page.getParent().toString());
		}
		*/
/*		for(VisualONGroup vgroup: vnet.getVisualONGroups()){
			System.out.println(vgroup.toString());
			System.out.println("Visual Page size = " + vgroup.getVisualPages().size());
			System.out.println("Visual Condition size = " + vgroup.getVisualConditions().size());
			System.out.println("Visual Connection size = " + vgroup.getVisualSONConnections().size());
			System.out.println("Visual block size = " + vgroup.getVisualBlocks().size());

		}*/

/*		for(VisualPage page : vnet.getVisualPages()){
			System.out.println();
			System.out.println("visual page parent  "+ page.getParent().toString());
		}*/
	}

	private void connectionTypeTest(SON net, VisualSON vnet){
		for(SONConnection con : net.getSONConnections()){
			System.out.println("con type "+ con.getSemantics());
			System.out.println("con fisrt "+ con.getFirst());
			System.out.println("con fisrt "+ con.getSecond());
		}
		for(VisualSONConnection con : vnet.getVisualSONConnections()){
			System.out.println("con type "+ con.getSemantics());
			System.out.println("con fisrt "+ con.getFirst());
			System.out.println("con fisrt "+ con.getSecond());
		}
	}


	@Override
	public Decorator getDecorator(GraphEditor editor) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}
}

