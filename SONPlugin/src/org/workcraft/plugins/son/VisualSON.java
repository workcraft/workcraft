	package org.workcraft.plugins.son;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JOptionPane;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.plugins.son.components.VisualChannelPlace;
import org.workcraft.plugins.son.components.VisualCondition;
import org.workcraft.plugins.son.components.VisualEvent;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection.SONConnectionType;
import org.workcraft.util.Hierarchy;


@DisplayName ("Structured Occurrence Nets")
@CustomTools ( SONToolProvider.class )

public class VisualSON extends AbstractVisualModel{

	private String title="Invalid Group Selection";
	private String title2="Invalid Super Group Selection";
	private SON net;

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
	}

	public void validateConnection (Node first, Node second) throws InvalidConnectionException{
	}

	public void validateConnection (Node first, Node second, SONConnectionType type) throws InvalidConnectionException{
		if (first instanceof VisualCondition && second instanceof VisualCondition && type == VisualSONConnection.SONConnectionType.POLYLINE)
			throw new InvalidConnectionException ("Connections between conditions are not valid(PN Connection)");
		if (first instanceof VisualEvent && second instanceof VisualEvent)
			throw new InvalidConnectionException ("Connections between events are not valid (PN Connection)");
		if (second instanceof VisualSONConnection || first instanceof VisualSONConnection)
			throw new InvalidConnectionException ("Invalid connection (Connection)");

		//asyn type
		if (!(first instanceof VisualChannelPlace) && !(second instanceof VisualChannelPlace)
				&& (type == VisualSONConnection.SONConnectionType.ASYNLINE || type == VisualSONConnection.SONConnectionType.SYNCLINE))
			throw new InvalidConnectionException ("Invalid connection (A/Syn Communication)");

		//Group
		if (first instanceof VisualChannelPlace && !isGrouped(second))
			throw new InvalidConnectionException ("Connections between channel places and un-grouped nodes are not valid (Group)");
		if (second instanceof VisualChannelPlace && !isGrouped(first))
			throw new InvalidConnectionException ("Connections between channel places and un-grouped nodes are not valid (Group)");
		if (first instanceof VisualChannelPlace && second instanceof VisualChannelPlace)
			throw new InvalidConnectionException ("Connections between channel places are not valid (A/Syn Communication)");
		if ((first instanceof VisualChannelPlace && second instanceof VisualCondition)
				|| (first instanceof VisualCondition && second instanceof VisualChannelPlace))
			throw new InvalidConnectionException ("Connections between channel place and condition are not valid (A/Syn Communication)");

		if(isGrouped(first) && isGrouped(second) && !isInSameGroup(first, second)  &&
				(type == VisualSONConnection.SONConnectionType.POLYLINE || type == VisualSONConnection.SONConnectionType.ASYNLINE ||
					type == VisualSONConnection.SONConnectionType.SYNCLINE) )
			throw new InvalidConnectionException ("Direct connections between two different groups are not valid (PN Connection, A/Syn Communication)");

		if(!(first instanceof VisualChannelPlace) &&  !(second instanceof VisualChannelPlace)){
			if (isGrouped(first) && !isGrouped(second) || isGrouped(second) && !isGrouped(first))
			throw new InvalidConnectionException ("Connections between grouped node and un-grouped nodes are not valid (Group)");

		//Bhv Type
		if(type == VisualSONConnection.SONConnectionType.BHVLINE){
			if (first instanceof VisualEvent || second instanceof VisualEvent)
				throw new InvalidConnectionException ("Connections between non-conditions are not valid (Behavioural Abstraction)");
			if (!isGrouped(first) || !isGrouped(second) )
				throw new InvalidConnectionException ("Connections between ungrouped conditions are not valid (Behavioural Abstraction)");
			if (this.isInSameGroup(first, second))
				throw new InvalidConnectionException ("Connections between same grouped conditions are not valid (Behavioural Abstraction)");
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

	@Override
	public void connect (Node first, Node second) throws InvalidConnectionException{
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	public void connect (Node first, Node second, SONConnectionType type) throws InvalidConnectionException{
		validateConnection(first, second, type);

		VisualComponent c1= (VisualComponent)first;
		VisualComponent c2= (VisualComponent)second;

		SONConnection con=(SONConnection)net.connect(c1.getReferencedComponent(), c2.getReferencedComponent(), type.toString());

		VisualSONConnection ret =new VisualSONConnection(con,c1,c2);

		if (type == VisualSONConnection.SONConnectionType.POLYLINE)
			ret.setSONConnectionType(VisualSONConnection.SONConnectionType.POLYLINE);

		if (type == VisualSONConnection.SONConnectionType.SYNCLINE)
			ret.setSONConnectionType(VisualSONConnection.SONConnectionType.SYNCLINE);

		if (type == VisualSONConnection.SONConnectionType.ASYNLINE)
			ret.setSONConnectionType(VisualSONConnection.SONConnectionType.ASYNLINE);

		if (type == VisualSONConnection.SONConnectionType.BHVLINE)
			ret.setSONConnectionType(VisualSONConnection.SONConnectionType.BHVLINE);

		if (c1 instanceof VisualChannelPlace || c2 instanceof VisualChannelPlace){
			ret.setSONConnectionType(VisualSONConnection.SONConnectionType.ASYNLINE);
			con.setType("ASYNLINE");
		}

		Hierarchy.getNearestContainer(c1,c2).add(ret);

	}

	private Collection<Node> getGroupableSelection(){
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> validateSet = getOrderedCurrentLevelSelection();
		boolean validate = false;

		for(Node node : getOrderedCurrentLevelSelection()){
			if(node instanceof VisualPage){
				validateSet.addAll(Hierarchy.getDescendantsOfType(node, VisualComponent.class));
			}
		}

		if(validateSelection(validateSet)){
			for(Node node : getOrderedCurrentLevelSelection()){
				if (!(node instanceof VisualChannelPlace) && !(node instanceof VisualONGroup)){
					result.add(node);
				}
				else{
					JOptionPane.showMessageDialog(null, "Group Selection containing Channel Places or other groups is invaild",title, JOptionPane.WARNING_MESSAGE);
					result.clear();
					return result;
				}
			}
		}
		else{
			JOptionPane.showMessageDialog(null, "Grouping a partial occurrence net is invalid",title, JOptionPane.WARNING_MESSAGE);
			result.clear();
			return result;
		}

		for (Node node : result){
			if (node instanceof VisualCondition)
				validate = true;
		}
		if (!validate) {
			JOptionPane.showMessageDialog(null, "An occurrence net must contain at least one condition",title, JOptionPane.WARNING_MESSAGE);
			result.removeAll(result);
			return result;
		}
		else
			return result;

	}

	private boolean validateSelection (Collection<Node> nodes) {
		for (VisualSONConnection connect : getVisualConnections()){
			if(nodes.contains(connect.getFirst()) && !(connect.getFirst() instanceof VisualChannelPlace)
				&& ! nodes.contains(connect.getSecond()) && !(connect.getSecond() instanceof VisualChannelPlace))
			return false;

			if(!nodes.contains(connect.getFirst()) && !(connect.getFirst() instanceof VisualChannelPlace)
					&& nodes.contains(connect.getSecond()) && !(connect.getSecond() instanceof VisualChannelPlace))
			return false;
		}

		return true;
	}

	public void groupSelection(){
		Collection<Node> selected = getGroupableSelection();

		if (selected.size() > 0) {

			ONGroup mathGroup = new ONGroup();
			VisualONGroup group = new VisualONGroup(mathGroup);
			Container currentLevel = getCurrentLevel();

			currentLevel.add(group);
			currentLevel.reparent(selected, group);

			VisualComponent visualContainer = (VisualComponent)Hierarchy.getNearestAncestor(currentLevel, VisualComponent.class);

			Container currentMathLevel;
			if(visualContainer==null)
				currentMathLevel = getMathModel().getRoot();
			else
				currentMathLevel = (Container)visualContainer.getReferencedComponent();
			currentMathLevel.add(mathGroup);

			ArrayList<Node> connectionsToGroup = new ArrayList<Node>();
			for(VisualConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualConnection.class)) {
				if(Hierarchy.isDescendant(connection.getFirst(), group) &&
						Hierarchy.isDescendant(connection.getSecond(), group)) {
					connectionsToGroup.add(connection);
				}
			}
			currentLevel.reparent(connectionsToGroup, group);

			// reparenting for the math model nodes
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

			for (Node node: selectedMath) {
				Container parent = (Container)node.getParent();
				ArrayList<Node> re = new ArrayList<Node>();
				re.add(node);


				// reparenting at the level of the reference manager
				ReferenceManager refMan = getMathModel().getReferenceManager();
				if (refMan instanceof HierarchicalUniqueNameReferenceManager) {
					HierarchicalUniqueNameReferenceManager manager = (HierarchicalUniqueNameReferenceManager)refMan;
					manager.setNamespaceProvider(node, mathGroup);
				}
				parent.reparent(re, mathGroup);

			}

			// final touch on visual part
			if (group != null) {
				Point2D groupCenter = centralizeComponents(selected);
				group.setPosition(groupCenter);
				select(group);
			}
		}
	}

	//super group
	private Collection<Node> getSuperGroupableSelection()
	{
		Collection<Node> result = new HashSet<Node>();
		Collection<Node> groupComponents = new HashSet<Node>();

		for(Node node : getOrderedCurrentLevelSelection()){
			if(node instanceof VisualComponent || node instanceof VisualSONConnection)
				result.add(node);
			if (node instanceof VisualONGroup){
				result.add(node);
				groupComponents.addAll(((VisualONGroup)node).getVisualComponents());
			}
			if(node instanceof VisualSuperGroup){
				result.removeAll(result);
				return result;}
		}

		if(getOrderedCurrentLevelSelection().isEmpty())
			return result;

		boolean validateSelection = true;
		for (VisualSONConnection connect : getVisualConnections()){
			if(connect.getSONConnectionType()==VisualSONConnection.SONConnectionType.POLYLINE){
				if (result.contains(connect.getFirst()) && !result.contains(connect.getSecond()))
					validateSelection = false;
				if (!result.contains(connect.getFirst()) && result.contains(connect.getSecond()))
					validateSelection = false;
			}
			if(connect.getSONConnectionType()==VisualSONConnection.SONConnectionType.SYNCLINE
					|| connect.getSONConnectionType()==VisualSONConnection.SONConnectionType.ASYNLINE){
				if(result.contains(connect.getFirst()) && !groupComponents.contains(connect.getSecond()))
					validateSelection = false;
				if(!result.contains(connect.getFirst()) && groupComponents.contains(connect.getSecond()))
					validateSelection = false;
				if(result.contains(connect.getSecond()) && !groupComponents.contains(connect.getFirst()))
					validateSelection = false;
				if(!result.contains(connect.getSecond()) && groupComponents.contains(connect.getFirst()))
					validateSelection = false;
			}

			if(connect.getSONConnectionType()==VisualSONConnection.SONConnectionType.BHVLINE)
				if(groupComponents.contains(connect.getFirst()) || groupComponents.contains(connect.getSecond()) && !result.contains(connect))
					result.add(connect);

		}
		boolean hasComponent = false;
		for (Node node : result){
			if (node instanceof VisualComponent)
				hasComponent = true;
			if (node instanceof VisualONGroup)
				hasComponent = true;
		}

		if(!hasComponent)
			validateSelection=false;

		if(!validateSelection){
			JOptionPane.showMessageDialog(null, "Partial Selection is not valid",title2, JOptionPane.WARNING_MESSAGE);
			result.removeAll(result);
			return result;
		}


		return result;
	}

	public void superGroupSelection(){
		Collection<Node> selected = getSuperGroupableSelection();
		Collection<Node> cons = new HashSet<Node>();
		Collection<Node> bhv = new HashSet<Node>();

		if (selected.size() < 1) return;
		VisualGroup vsgroup = new VisualSuperGroup();

		Container currentLevel = getCurrentLevel();

		currentLevel.add(vsgroup);

		for(Node n : selected){
			if(n instanceof VisualSONConnection)
				cons.add(n);
			if(n instanceof VisualSONConnection && ((VisualSONConnection)n).getSONConnectionType()==VisualSONConnection.SONConnectionType.BHVLINE)
				bhv.add(n);
		}

		currentLevel.reparent(selected, vsgroup);
		// && ((VisualSONConnection)n).getSONConnectionType()==VisualSONConnection.SONConnectionType.BHVLINE

		vsgroup.reparent(cons, vsgroup);
		vsgroup.reparent(bhv, currentLevel);

		select(vsgroup);
	}

	//Block
	public void groupBlockSelection() {
		ArrayList<Node> selected = new ArrayList<Node>();
		for (Node node : getOrderedCurrentLevelSelection()) {
			if (node instanceof VisualTransformableNode) {
				selected.add((VisualTransformableNode)node);
			}
		}

		if (selected.size() > 1) {

			Block mathBlock = new Block();
			VisualBlock block = new VisualBlock(mathBlock);

			getCurrentLevel().add(block);
			getCurrentLevel().reparent(selected, block);

			VisualComponent visualContainer = (VisualComponent)Hierarchy.getNearestAncestor(getCurrentLevel(), VisualComponent.class);

			Container currentMathLevel;
			if(visualContainer==null)
				currentMathLevel = getMathModel().getRoot();
			else
				currentMathLevel = (Container)visualContainer.getReferencedComponent();
			currentMathLevel.add(mathBlock);

			ArrayList<Node> connectionsToGroup = new ArrayList<Node>();
			for(VisualConnection connection : Hierarchy.getChildrenOfType(getCurrentLevel(), VisualConnection.class)) {
				if(Hierarchy.isDescendant(connection.getFirst(), block) &&
						Hierarchy.isDescendant(connection.getSecond(), block)) {
					connectionsToGroup.add(connection);
				}
			}
			getCurrentLevel().reparent(connectionsToGroup, block);

			// reparenting for the math model nodes
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

			for (Node node: selectedMath) {
				Container parent = (Container)node.getParent();
				ArrayList<Node> re = new ArrayList<Node>();
				re.add(node);


				// reparenting at the level of the reference manager
				ReferenceManager refMan = getMathModel().getReferenceManager();
				if (refMan instanceof HierarchicalUniqueNameReferenceManager) {
					HierarchicalUniqueNameReferenceManager manager = (HierarchicalUniqueNameReferenceManager)refMan;
					manager.setNamespaceProvider(node, mathBlock);
				}
				parent.reparent(re, mathBlock);

			}


			// final touch on visual part
			if (block != null) {
				Point2D groupCenter = centralizeComponents(selected);
				block.setPosition(groupCenter);
				select(block);
			}
		}
	}


	@Override
	public void ungroupSelection(){
		ArrayList<Node> toSelect = new ArrayList<Node>();
		for(Node node : getOrderedCurrentLevelSelection()) {

			if(node instanceof VisualONGroup) {

				VisualONGroup group = (VisualONGroup)node;
				for(Node subNode : group.unGroup(getMathModel().getReferenceManager())) {
					toSelect.add(subNode);
				}
				getCurrentLevel().remove(group);
			} else if(node instanceof VisualPage) {

				VisualPage page = (VisualPage)node;

				for(Node subNode : page.unGroup(getMathModel().getReferenceManager())) {
					toSelect.add(subNode);
				}
				getCurrentLevel().remove(page);

			} else if(node instanceof VisualSuperGroup){
				VisualSuperGroup sGroup = (VisualSuperGroup)node;
				for(Node subNode : sGroup.unGroup()){
					toSelect.add(subNode);
				}
				getCurrentLevel().remove(sGroup);
			}
			else {
				toSelect.add(node);
			}
		}
		select(toSelect);
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

	public Collection<VisualSONConnection> getVisualConnections()
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
		ArrayList<VisualSONConnection> result = new ArrayList<VisualSONConnection>();
		for (VisualSONConnection con : this.getVisualConnections()){
			if (con.getFirst() == node)
				result.add(con);
		}
		return result;
	}

	public Collection<VisualSONConnection> getVisualConnections(VisualComponent first, VisualComponent second)
	{
		ArrayList<VisualSONConnection> result = new ArrayList<VisualSONConnection>();
		for (VisualSONConnection con : this.getVisualConnections()){
			if (con.getFirst() == first && con.getSecond() == second)
				result.add(con);
		}
		return result;
	}

}
