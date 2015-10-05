package org.workcraft.plugins.son;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.plugins.son.algorithm.RelationAlgorithm;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.plugins.son.elements.VisualPlaceNode;
import org.workcraft.plugins.son.elements.VisualTransitionNode;
import org.workcraft.util.Hierarchy;


@DisplayName ("Structured Occurrence Nets")
@CustomTools ( SONToolProvider.class )

public class VisualSON extends AbstractVisualModel {

	private String group="Invalid Group Selection";
	private String block="Invalid Block Selection";
	private SON net;
	private Semantics currentConnectonSemantics;

	public VisualSON(SON model){
		this(model, null);
	}

	public VisualSON (SON model, VisualGroup root){
		super (model, root);
	//	currentmathLevel = getCurrentLevel();
		if (root == null)
			try {
				createDefaultFlatStructure();
			} catch (NodeCreationException e) {
				throw new RuntimeException(e);
			}

		this.net = model;
		BlockConnector.blockInternalConnector(this);
	}

	@Override
	public void validateConnection (Node first, Node second) throws InvalidConnectionException{
		if ((first instanceof VisualCondition) && (second instanceof VisualCondition) && (currentConnectonSemantics == Semantics.PNLINE))
			throw new InvalidConnectionException ("Connections between conditions are not valid(PN Connection)");
		if ((first instanceof VisualEvent) && (second instanceof VisualEvent))
			throw new InvalidConnectionException ("Connections between events are not valid (PN Connection)");
		if ((second instanceof VisualSONConnection) || (first instanceof VisualSONConnection))
			throw new InvalidConnectionException ("Invalid connection (Connection)");

		//asyn type
		if (!(first instanceof VisualChannelPlace) && !(second instanceof VisualChannelPlace)
				&& ((currentConnectonSemantics == Semantics.ASYNLINE) || (currentConnectonSemantics == Semantics.SYNCLINE))) {
			throw new InvalidConnectionException ("Invalid connection (A/Syn Communication)");
		}
		//Group
		if ((first instanceof VisualChannelPlace) && !isGrouped(second))
			throw new InvalidConnectionException ("Connections between channel places and un-grouped nodes are not valid (Group)");
		if ((second instanceof VisualChannelPlace) && !isGrouped(first))
			throw new InvalidConnectionException ("Connections between channel places and un-grouped nodes are not valid (Group)");
		if ((first instanceof VisualChannelPlace) && (second instanceof VisualChannelPlace))
			throw new InvalidConnectionException ("Connections between channel places are not valid (A/Syn Communication)");
		if (((first instanceof VisualChannelPlace) && (second instanceof VisualCondition))
				|| ((first instanceof VisualCondition) && (second instanceof VisualChannelPlace)))
			throw new InvalidConnectionException ("Connections between channel place and condition are not valid (A/Syn Communication)");

		if(isGrouped(first) && isGrouped(second) && !isInSameGroup(first, second)  &&
				(currentConnectonSemantics == Semantics.PNLINE || currentConnectonSemantics == Semantics.ASYNLINE || currentConnectonSemantics == Semantics.SYNCLINE) )
			throw new InvalidConnectionException ("Direct connections between two different groups are not valid (PN Connection, A/Syn Communication)");

		if(!(first instanceof VisualChannelPlace) &&  !(second instanceof VisualChannelPlace)){
			if (isGrouped(first) && !isGrouped(second) || isGrouped(second) && !isGrouped(first))
			throw new InvalidConnectionException ("Connections between grouped node and un-grouped nodes are not valid (Group)");

		//Bhv Type
		if (currentConnectonSemantics == Semantics.BHVLINE) {
			if ((first instanceof VisualEvent) || (second instanceof VisualEvent))
				throw new InvalidConnectionException ("Connections between non-conditions are not valid (Behavioural Abstraction)");
			if (!isGrouped(first) || !isGrouped(second) )
				throw new InvalidConnectionException ("Connections between ungrouped conditions are not valid (Behavioural Abstraction)");
			if (isInSameGroup(first, second))
				throw new InvalidConnectionException ("Connections between same grouped conditions are not valid (Behavioural Abstraction)");
			if (isInBlock(first) || this.isInBlock(second))
				throw new InvalidConnectionException ("Block cannot cross phases (Block)");
			if (hasInputBhv(first) || hasOutputBhv(second))
				throw new InvalidConnectionException ("Condition with both input and output behavioural relations is not valid  (Behavioural Abstraction)");
			}
		}

		//ChannelPlace
		if (first instanceof VisualChannelPlace)
			for (Node node : net.getPreset(((VisualChannelPlace) first).getReferencedComponent())){
				if (net.isInSameGroup(((VisualComponent)second).getReferencedComponent(), node)){
					throw new InvalidConnectionException ("The input and ouput nodes for a channel place belong to same group are not valid");
				}
			}

		if (second instanceof VisualChannelPlace)
			for (Node node : net.getPostset(((VisualChannelPlace) second).getReferencedComponent())){
				if (net.isInSameGroup(((VisualComponent)first).getReferencedComponent(), node)){
					throw new InvalidConnectionException ("The input and ouput nodes of a channel place belong to same group are not valid");
				}
			}

		//block
		if((first instanceof VisualEvent) && isInBlock(second))
			throw new InvalidConnectionException ("Block inputs must be conditions (Block)");

		if((second instanceof VisualEvent) && isInBlock(first))
			throw new InvalidConnectionException ("Block outputs must be conditions (Block)");
	}

	private boolean isGrouped(Node node){
		for (VisualONGroup group : getVisualONGroups()){
			if (group.getVisualComponents().contains(node)){
				return true;
			}
		}
		return false;
	}

	private boolean isInSameGroup (Node first, Node second){
		for (VisualONGroup group : getVisualONGroups()){
			if ( group.getVisualComponents().contains(first) && group.getVisualComponents().contains(second)){
				return true;
			}
		}
		return false;
	}

	private boolean isInBlock(Node node){
		for(VisualBlock block : getVisualBlocks())
			if(block.getComponents().contains(node))
				return true;
		return false;
	}

	private boolean hasInputBhv(Node first){
		if(first instanceof VisualCondition)
			for(VisualSONConnection con : getVisualConnections((VisualCondition)first)){
				if(con.getSemantics() == Semantics.BHVLINE){
					if(con.getSecond() == ((VisualCondition)first))
						return true;
				}
			}
		return false;
	}

	private boolean hasOutputBhv(Node second){
		if(second instanceof VisualCondition)
			for(VisualSONConnection con : getVisualConnections((VisualCondition)second)){
				if(con.getSemantics() == Semantics.BHVLINE){
					if(con.getFirst() == ((VisualCondition)second))
						return true;
				}
			}
		return false;
	}

	@Override
	public VisualConnection connect (Node first, Node second, MathConnection mConnection) throws InvalidConnectionException{
		validateConnection(first, second);
		VisualComponent c1= (VisualComponent)first;
		VisualComponent c2= (VisualComponent)second;

		Semantics semantics = currentConnectonSemantics;
		if ((c1 instanceof VisualChannelPlace) || (c2 instanceof VisualChannelPlace)) {
			if (semantics != Semantics.SYNCLINE) {
				semantics = Semantics.ASYNLINE;
			}
		}
		if (mConnection == null) {
			mConnection = net.connect(c1.getReferencedComponent(), c2.getReferencedComponent(), semantics);
		}
		VisualSONConnection ret = new VisualSONConnection((SONConnection)mConnection, c1, c2);
		Hierarchy.getNearestContainer(c1,c2).add(ret);

		return ret;
	}

	public VisualConnection connect (Node first, Node second, Semantics semantics) throws InvalidConnectionException{
		forceConnectionSemantics(semantics);
		return connect(first, second);
	}

	private Collection<Node> getGroupableSelection(){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> selection = new HashSet<Node>();
		boolean validate = false;

		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();

		if(getCurrentLevel() instanceof VisualONGroup){
			JOptionPane.showMessageDialog(mainWindow,
					"Grouping inside a group is invalid",group, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
		}

		for(Node node : SelectionHelper.getOrderedCurrentLevelSelection(this)){
			if(node instanceof VisualPage){
				selection.addAll(Hierarchy.getDescendantsOfType(node, VisualComponent.class));
			}
			if(node instanceof VisualTransformableNode) {
				selection.add((VisualTransformableNode)node);
			}
		}

		if(isPure(selection)){
			for(Node node : SelectionHelper.getOrderedCurrentLevelSelection(this)){
				if(node instanceof VisualTransformableNode){
					if (!(node instanceof VisualChannelPlace) && !(node instanceof VisualONGroup) ){
							result.add(node);
					}else{
						JOptionPane.showMessageDialog(mainWindow,
								"Group Selection containing Channel Places or other groups is invaild",group, JOptionPane.WARNING_MESSAGE);
						result.clear();
						return result;
					}
				}
			}
		}else{
			JOptionPane.showMessageDialog(mainWindow,
					"Grouping a partial occurrence net is invalid",group, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
		}

		for (Node node : result){
			if (node instanceof VisualCondition)
				validate = true;
			if (node instanceof VisualPage && !Hierarchy.getDescendantsOfType(node, VisualCondition.class).isEmpty()){
				validate = true;
			}
		}
		if (!validate) {
			JOptionPane.showMessageDialog(mainWindow,
					"An occurrence net must contain at least one condition",group, JOptionPane.WARNING_MESSAGE);
			result.removeAll(result);
			return result;
		}
		else
			return result;

	}

	private boolean isPure (Collection<Node> nodes) {
		for (VisualSONConnection connect : getVisualSONConnections()){
			if(nodes.contains(connect.getFirst()) && !(connect.getFirst() instanceof VisualChannelPlace)
				&& ! nodes.contains(connect.getSecond()) && !(connect.getSecond() instanceof VisualChannelPlace))
			return false;

			if(!nodes.contains(connect.getFirst()) && !(connect.getFirst() instanceof VisualChannelPlace)
					&& nodes.contains(connect.getSecond()) && !(connect.getSecond() instanceof VisualChannelPlace))
			return false;
		}
		return true;
	}

	public VisualGroup groupSelection(){
		Collection<Node> selected = getGroupableSelection();

		if (selected.size() > 0) {

			ONGroup mathGroup = new ONGroup();
			VisualONGroup group = new VisualONGroup(mathGroup);
			Container currentLevel = getCurrentLevel();

			currentLevel.add(group);
			currentLevel.reparent(selected, group);

			VisualComponent visualContainer = (VisualComponent)Hierarchy.getNearestAncestor(currentLevel, VisualComponent.class);

			Container currentMathLevel;
			if (visualContainer == null) {
				currentMathLevel = getMathModel().getRoot();
			} else {
				currentMathLevel = (Container)visualContainer.getReferencedComponent();
			}
			currentMathLevel.add(mathGroup);

			ArrayList<Node> connectionsToGroup = new ArrayList<Node>();
			for(VisualConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualConnection.class)) {
				if(Hierarchy.isDescendant(connection.getFirst(), group) &&
						Hierarchy.isDescendant(connection.getSecond(), group)) {
					connectionsToGroup.add(connection);
				}
			}
			currentLevel.reparent(connectionsToGroup, group);

			// Reparenting for the math model nodes
			ArrayList<Node> selectedMath = new ArrayList<Node>();
			for (Node node:selected) {
				if (node instanceof VisualComponent) {
					selectedMath.add(((VisualComponent)node).getReferencedComponent());
				}
			}
			for (Node node:connectionsToGroup) {
				if (node instanceof VisualConnection) {
					selectedMath.add(((VisualConnection)node).getReferencedConnection());
				}
			}

			// Reparenting at the level of the reference manager
			ReferenceManager refMan = getMathModel().getReferenceManager();
			if (refMan instanceof HierarchicalUniqueNameReferenceManager) {
				HierarchicalUniqueNameReferenceManager hierRefMan = (HierarchicalUniqueNameReferenceManager)refMan;
				for (Node node: selectedMath) {
					Container parent = (Container)node.getParent();
					hierRefMan.setNamespaceProvider(Arrays.asList(node), mathGroup);
					parent.reparent(Arrays.asList(node), mathGroup);
				}
			}

			// Final touch on visual part
			if (group != null) {
				Point2D centre = TransformHelper.getSnappedCentre(selected);
				VisualModelTransformer.translateNodes(selected, -centre.getX(), -centre.getY());
				group.setPosition(centre);
				select(group);
			}
		}
		return null;
	}

	//Block
	public void groupBlockSelection() {
		Collection<Node> selected = getBlockSelection();

		if (selected.size() > 1) {

			Block mathBlock = new Block();
			VisualBlock block = new VisualBlock(mathBlock);

			getCurrentLevel().add(block);
			getCurrentLevel().reparent(selected, block);

			VisualComponent visualContainer = (VisualComponent)Hierarchy.getNearestAncestor(getCurrentLevel(), VisualComponent.class);

			Container currentMathLevel;
			if (visualContainer == null) {
				currentMathLevel = getMathModel().getRoot();
			} else {
				currentMathLevel = (Container)visualContainer.getReferencedComponent();
			}
			currentMathLevel.add(mathBlock);

			ArrayList<Node> connectionsToGroup = new ArrayList<Node>();

			for(VisualConnection connection : Hierarchy.getChildrenOfType(getCurrentLevel(), VisualConnection.class)) {
				if(Hierarchy.isDescendant(connection.getFirst(), block) &&
						Hierarchy.isDescendant(connection.getSecond(), block)) {
					connectionsToGroup.add(connection);
				}
			}
			getCurrentLevel().reparent(connectionsToGroup, block);

			// Reparenting for the math model nodes
			ArrayList<Node> selectedMath = new ArrayList<Node>();
			for (Node node:selected) {
				if (node instanceof VisualComponent) {
					selectedMath.add(((VisualComponent)node).getReferencedComponent());
				}
			}
			for (Node node:connectionsToGroup) {
				if (node instanceof VisualConnection) {
					selectedMath.add(((VisualConnection)node).getReferencedConnection());
				}
			}

			// Reparenting at the level of the reference manager
			ReferenceManager refMan = getMathModel().getReferenceManager();
			if (refMan instanceof HierarchicalUniqueNameReferenceManager) {
				HierarchicalUniqueNameReferenceManager hierRefMan = (HierarchicalUniqueNameReferenceManager)refMan;
				for (Node node: selectedMath) {
					Container parent = (Container)node.getParent();
					hierRefMan.setNamespaceProvider(Arrays.asList(node), mathBlock);
					parent.reparent(Arrays.asList(node), mathBlock);
				}
			}

			// Final touch on visual part
			if (block != null) {
				Point2D centre = TransformHelper.getSnappedCentre(selected);
				VisualModelTransformer.translateNodes(selected, -centre.getX(), -centre.getY());
				block.setPosition(centre);
				select(block);
			}
		}
	}

	private Collection<Node> getBlockSelection(){
		Collection<Node> result = new HashSet<Node>();
		RelationAlgorithm relationAlg = new RelationAlgorithm(net);

		final Framework framework = Framework.getInstance();
		MainWindow mainWindow = framework.getMainWindow();

		int errorType = 0;

		for(Node node : SelectionHelper.getOrderedCurrentLevelSelection(this)){
			if((node instanceof VisualCondition) || (node instanceof VisualEvent)) {
				if(relationAlg.isFinal(((VisualComponent)node).getReferencedComponent())
						|| relationAlg.isInitial(((VisualComponent)node).getReferencedComponent()))
					errorType = 1;
				else
					result.add(node);
			}
			else if(node instanceof VisualComment){
				result.add(node);
			}
			else if(!(node instanceof VisualSONConnection))
				errorType = 2;
		}

		if(errorType==1){
			JOptionPane.showMessageDialog(mainWindow,
					"Block contianing initial or final node is invalid", block, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
		}


		if(errorType==2){
			JOptionPane.showMessageDialog(mainWindow,
					"Only condition and event can be set as a Block", block, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
			}

		for (VisualSONConnection connect : getVisualSONConnections()){
			if(connect.getReferencedSONConnection().getSemantics() == Semantics.PNLINE){
				if(result.contains(connect.getFirst()) && !result.contains(connect.getSecond())){
					if(connect.getSecond() instanceof VisualEvent)
						errorType = 3;
				}

				if(!result.contains(connect.getFirst()) && result.contains(connect.getSecond())){
					if(connect.getFirst() instanceof VisualEvent)
						errorType = 3;
				}
			}
			if(connect.getReferencedSONConnection().getSemantics() == Semantics.BHVLINE){
				if(result.contains(connect.getFirst()) || result.contains(connect.getSecond()))
					errorType =4;
			}
		}

		if(errorType==3){
			JOptionPane.showMessageDialog(mainWindow,
					"The inputs and outputs of a block must be conditions", block, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
			}

		if(errorType==4){
			JOptionPane.showMessageDialog(mainWindow,
					"Block cannot cross phases", block, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
			}

		if (result.size() == 1) {
			JOptionPane.showMessageDialog(mainWindow,
					"A single component cannot be set as a block",group, JOptionPane.WARNING_MESSAGE);
			result.removeAll(result);
			return result;
		}

		return result;
	}

	public Collection<VisualONGroup> getVisualONGroups()
	{
		return Hierarchy.getDescendantsOfType(getRoot(), VisualONGroup.class);
	}

	public Collection<VisualSuperGroup> getVisualSuperGroups()
	{
		return Hierarchy.getChildrenOfType(getRoot(), VisualSuperGroup.class);
	}

	public Collection<VisualComponent> getVisualComponent(){
		return Hierarchy.getDescendantsOfType(getRoot(), VisualComponent.class);
	}

	public Collection<VisualCondition> getVisualCondition(){
		return Hierarchy.getDescendantsOfType(getRoot(), VisualCondition.class);
	}

	public Collection<VisualPlaceNode> getVisualPlaceNode(){
		return Hierarchy.getDescendantsOfType(getRoot(), VisualPlaceNode.class);
	}

	public Collection<VisualEvent> getVisualEvent(){
		return Hierarchy.getDescendantsOfType(getRoot(), VisualEvent.class);
	}

	public Collection<VisualSONConnection> getVisualSONConnections()
	{
		return Hierarchy.getDescendantsOfType(getRoot(), VisualSONConnection.class);
	}

	public Collection<VisualBlock> getVisualBlocks()
	{
		return Hierarchy.getDescendantsOfType(getRoot(), VisualBlock.class);
	}

	public Collection<VisualPage> getVisualPages()
	{
		return Hierarchy.getDescendantsOfType(getRoot(), VisualPage.class);
	}

	public Collection<VisualSONConnection> getVisualConnections(VisualComponent node)
	{
		//input value
		ArrayList<VisualSONConnection> result = new ArrayList<VisualSONConnection>();
		for (VisualSONConnection con : this.getVisualSONConnections()){
			if (con.getFirst() == node)
				result.add(con);
			if (con.getSecond() == node)
				result.add(con);
		}
		return result;
	}

	public Collection<VisualSONConnection> getVisualConnections(VisualComponent first, VisualComponent second)
	{
		ArrayList<VisualSONConnection> result = new ArrayList<VisualSONConnection>();
		for (VisualSONConnection con : this.getVisualSONConnections()){
			if (con.getFirst() == first && con.getSecond() == second)
				result.add(con);
		}
		return result;
	}

	public void forceConnectionSemantics(Semantics currentSemantics) {
		this.currentConnectonSemantics = currentSemantics;
	}

	public void setForegroundColor(Node n, Color nodeColor){
		if(n instanceof VisualPlaceNode){
			((VisualPlaceNode) n).setForegroundColor(nodeColor);
		}
		if(n instanceof VisualTransitionNode){
			((VisualTransitionNode) n).setForegroundColor(nodeColor);
		}
		if(n instanceof VisualSONConnection){
			((VisualSONConnection) n).setColor(nodeColor);
		}
		if (n instanceof VisualONGroup)
			((VisualONGroup)n).setForegroundColor(nodeColor);
	}
}
