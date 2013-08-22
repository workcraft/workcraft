	package org.workcraft.plugins.son;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.AbstractVisualModel;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.connections.VisualSONConnection.SONConnectionType;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.util.Hierarchy;


@DisplayName ("Structured Occurrence Nets")
@CustomTools ( SONToolProvider.class )
public class VisualSON extends AbstractVisualModel{

	private String title="Invalid Group Selection";
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

	public void validateConnection (Node first, Node second) throws InvalidConnectionException
	{
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
		for (VisualONGroup group : getVisualGroups()){
			if (group.getVisualComponents().contains(node))
				return true;
		}
		return false;
	}

	private boolean isInSameGroup (Node first, Node second){
		for (VisualONGroup group : getVisualGroups()){
			if (group.getVisualComponents().contains(first) && group.getVisualComponents().contains(second))
				return true;
		}
		return false;
	}

	@Override
	public void connect (Node first, Node second) throws InvalidConnectionException{
		System.out.print("non-implement");
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

	private Collection<Node> getGroupableSelection()
	{
		boolean validate = false;
		HashSet<Node> result = new HashSet<Node>();
		if (validateSelection ())
		{
			for(Node node : getOrderedCurrentLevelSelection()){
				if (node instanceof VisualCondition || node instanceof VisualEvent || node instanceof VisualSONConnection)
					result.add(node);
				else
				{
					JOptionPane.showMessageDialog(null, "Group Selection containing Channel Places or other groups are invaild",title, JOptionPane.WARNING_MESSAGE);
					result.removeAll(result);
					return result;
				}
			}
		}else
			{
					JOptionPane.showMessageDialog(null, "Partial Selection is not valid",title, JOptionPane.WARNING_MESSAGE);
					result.removeAll(result);
					return result;
			}
		for (Node node : result){
			if (node instanceof VisualEvent)
				validate = true;
			if (node instanceof VisualCondition)
				validate = true;
		}
		if (!validate) {
			JOptionPane.showMessageDialog(null, "Partial Selection is not valid",title, JOptionPane.WARNING_MESSAGE);
			result.removeAll(result);
			return result;
		}
		else
			return result;
	}

	private boolean validateSelection () {

		for (VisualSONConnection connect : getVisualConnections()){

				if (!(getOrderedCurrentLevelSelection().contains(connect.getFirst())) && ! (connect.getFirst() instanceof VisualChannelPlace)
						&&	getOrderedCurrentLevelSelection().contains(connect.getSecond()) && ! (connect.getSecond() instanceof VisualChannelPlace))
				return false;
				if (!(getOrderedCurrentLevelSelection().contains(connect.getSecond())) &&! (connect.getSecond() instanceof VisualChannelPlace)
						&&	getOrderedCurrentLevelSelection().contains(connect.getFirst()) &&! (connect.getSecond() instanceof VisualChannelPlace))
				return false;
		}
			return true;
	}


	@Override
	public void groupSelection(){

		Collection<Node> selected = getGroupableSelection();
		if (selected.size() < 1) return;
		//Math Group
		HashSet<Node> mathSelected = new HashSet<Node>();

		for (Node node : selected){
			if (node instanceof VisualComponent){
				mathSelected.add(((VisualComponent) node).getReferencedComponent());
			}
		}

		ONGroup mathGroup = new ONGroup();

		this.net.getRoot().add(mathGroup);

		this.net.getRoot().reparent(mathSelected, mathGroup);

		//Visual Group
		VisualONGroup group = new VisualONGroup(mathGroup);

		Container currentLevel = getCurrentLevel();

		currentLevel.add(group);

		currentLevel.reparent(selected, group);

		ArrayList<Node> connectionsToGroup = new ArrayList<Node>();

		for (VisualSONConnection connection : Hierarchy.getChildrenOfType(currentLevel, VisualSONConnection.class))
		{
			if (Hierarchy.isDescendant(connection.getFirst(), group) &&
				Hierarchy.isDescendant(connection.getSecond(), group))
			{
				connectionsToGroup.add(connection);
			}
		}

		currentLevel.reparent(connectionsToGroup, group);

		select(group);
	}

	@Override
	public void ungroupSelection() {
		ArrayList<Node> toSelect = new ArrayList<Node>();
		Collection<Node> mathNodes = new ArrayList<Node>();

		for(Node node : getOrderedCurrentLevelSelection())
		{
			if(node instanceof VisualONGroup)
			{
				VisualONGroup group = (VisualONGroup)node;
				for(Node subNode : group.unGroup()){
					toSelect.add(subNode);
				}

				for(Node child : group.getMathGroup().getChildren()){
					mathNodes.add(child);
				}
				group.getMathGroup().reparent(mathNodes, net.getRoot());
				this.net.remove(group.getMathGroup());
				getCurrentLevel().remove(group);

			}
			else
				toSelect.add(node);
		}

		select(toSelect);
	}

	@Override
	public void deleteSelection() {

		HashMap<Container, LinkedList<Node>> batches = new HashMap<Container, LinkedList<Node>>();
		LinkedList<Node> remainingSelection = new LinkedList<Node>();

		for(Node node : getOrderedCurrentLevelSelection())
		{
			if(node instanceof VisualONGroup)
			{
				VisualONGroup group = (VisualONGroup)node;
				this.net.getRoot().remove(group.getMathGroup());
			}
		}

		for (Node n : getSelection()) {
			if (n.getParent() instanceof Container) {
				Container c = (Container)n.getParent();
				LinkedList<Node> batch = batches.get(c);
				if (batch == null) {
					batch = new LinkedList<Node>();
					batches.put(c, batch);
				}
				batch.add(n);
			} else remainingSelection.add(n);
		}

		for (Container c : batches.keySet())
			c.remove(batches.get(c));

		select(remainingSelection);

	}

	public Collection<VisualONGroup> getVisualGroups()
	{
		return Hierarchy.getChildrenOfType(getRoot(), VisualONGroup.class);
	}

	public Collection<VisualComponent> getVisualComponent(){
		Collection<VisualComponent> result = new HashSet<VisualComponent>();
		result.addAll(Hierarchy.getChildrenOfType(getRoot(), VisualComponent.class));
		for(VisualONGroup vGroup : this.getVisualGroups())
			result.addAll(vGroup.getComponents());

		return result;
	}

	public Collection<VisualSONConnection> getVisualConnections()
	{
		Collection<VisualSONConnection> result = new HashSet<VisualSONConnection>();
		result.addAll(Hierarchy.getChildrenOfType(getRoot(), VisualSONConnection.class));
		for(VisualONGroup vGroup : this.getVisualGroups())
			result.addAll(vGroup.getVisualSONConnections());

		return result;
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

	public VisualChannelPlace createChannelPlace() {
		return createChannelPlace(null);
	}

	public VisualChannelPlace createChannelPlace(String name) {
		VisualChannelPlace cPlace = new VisualChannelPlace(net.createChannelPlace(name));
		cPlace.setX(0);
		cPlace.setY(0);
		add(cPlace);
		return cPlace;
	}

}
