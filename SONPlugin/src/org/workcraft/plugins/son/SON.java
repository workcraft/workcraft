package org.workcraft.plugins.son;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.math.PageNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.Block;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.plugins.son.elements.PlaceNode;
import org.workcraft.plugins.son.elements.TransitionNode;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;


@VisualClass(org.workcraft.plugins.son.VisualSON.class)
public class SON extends AbstractMathModel {

	public SON(){
		this(null, null);
	}

	public SON(Container root, References refs) {
		super(root, new HierarchicalUniqueNameReferenceManager(refs) {
			@Override
			public String getPrefix(Node node) {
				if (node instanceof Condition) return "c";
				if (node instanceof Event) return "e";
				if (node instanceof ChannelPlace) return "q";
				if (node instanceof Block) return "b";
				if (node instanceof SONConnection) return "con";
				if (node instanceof ONGroup) return "g";
				if (node instanceof PageNode) return "p";
				return super.getPrefix(node);
			}
		});
	}

	public void validate() throws ModelValidationException {
	}

	@SuppressWarnings("deprecation")
	public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	public SONConnection connect(Node first, Node second, Semantics semantics) throws InvalidConnectionException {
		if (this.getSONConnection(first, second) != null){
			throw new InvalidConnectionException ("Duplicate Connections");
		}

		SONConnection con = new SONConnection((MathNode)first, (MathNode)second, semantics);
		Hierarchy.getNearestContainer(first, second).add(con);

		return con;
	}

	public Collection<Node> getComponents(){
		ArrayList<Node> result =  new ArrayList<Node>();

		for(Node node : Hierarchy.getDescendantsOfType(getRoot(), MathNode.class))
			if(node instanceof PlaceNode || node instanceof Event)
				result.add(node);

		//remove the nodes in isolate blocks
		for(Block block : this.getBlocks())
			if(!this.getSONConnections(block).isEmpty()){
				result.removeAll(block.getComponents());
				result.add(block);
			}

		return result;
	}

	public Collection<Condition> getConditions(){
		ArrayList<Condition> result =  new ArrayList<Condition>();
		for(Node node : getComponents())
			if(node instanceof Condition)
				result.add((Condition)node);

		return result;
	}

	public Collection<ChannelPlace> getChannelPlaces(){
		return Hierarchy.getDescendantsOfType(getRoot(), ChannelPlace.class);
	}

	public Collection<Event> getEvents(){
		ArrayList<Event> result =  new ArrayList<Event>();
		for(Node node : getComponents())
			if(node instanceof Event)
				result.add((Event)node);

		return result;
	}

	public Collection<PlaceNode> getPlaceNodes(){
		ArrayList<PlaceNode> result =  new ArrayList<PlaceNode>();
		for(Node node : getComponents())
			if(node instanceof PlaceNode)
				result.add((PlaceNode)node);

		return result;
	}

	public String getComponentLabel(Node n){
		if(n instanceof Condition)
			return ((Condition)n).getLabel();

		if(n instanceof Event)
			return ((Event)n).getLabel();

		if(n instanceof ChannelPlace)
			return ((ChannelPlace)n).getLabel();

		if(n instanceof Block)
			return ((Block)n).getLabel();

		else
			return null;
	}

	public void setForegroundColor(Node n, Color nodeColor){
		if(n instanceof Condition){
			((Condition) n).setForegroundColor(nodeColor);
		}
		if(n instanceof Event){
			((Event) n).setForegroundColor(nodeColor);
		}
		if (n instanceof ChannelPlace){
			((ChannelPlace) n).setForegroundColor(nodeColor);
		}
		if (n instanceof ONGroup)
			((ONGroup)n).setForegroundColor(nodeColor);

		if(n instanceof Block)
			((Block)n).setForegroundColor(nodeColor);

	}

	public void setFillColor(Node n, Color nodeColor){
		if(n instanceof Condition){
			((Condition) n).setFillColor(nodeColor);
		}
		if(n instanceof Event){
			((Event) n).setFillColor(nodeColor);
		}
		if(n instanceof ChannelPlace){
			((ChannelPlace) n).setFillColor(nodeColor);
		}
		if(n instanceof Block){
			((Block) n).setFillColor(nodeColor);
		}
	}

	public void refreshColor(){
		for(Node n:  getComponents()){
			setFillColor(n,CommonVisualSettings.getFillColor());
			setForegroundColor(n, CommonVisualSettings.getBorderColor());
		}
		for (ONGroup group : this.getGroups()){
			setForegroundColor(group, SONSettings.getGroupForegroundColor());
		}

		for (SONConnection con : this.getSONConnections())
			setForegroundColor(con, CommonVisualSettings.getBorderColor());

		for (Block block : this.getBlocks()){
			if(!block.getIsCollapsed()){
				this.setFillColor(block, SONSettings.getBlockFillColor());
				this.setForegroundColor(block,  SONSettings.getGroupForegroundColor());
			}else{
				this.setFillColor(block, CommonVisualSettings.getFillColor());
				this.setForegroundColor(block,  CommonVisualSettings.getBorderColor());
			}
		}
	}

	public void resetErrStates(){
		for(Condition con : this.getConditions())
			con.setErrors(0);
		for(Event event : this.getEvents())
			event.setFaulty(false);
	}

	public void resetConditionErrStates(){
		for(Condition con : this.getConditions())
			con.setErrors(0);
	}

	final public ChannelPlace createChannelPlace() {
		return createChannelPlace(null);
	}

	final public ChannelPlace createChannelPlace(String name) {
		ChannelPlace newCP = new ChannelPlace();
		if (name!=null)
			setName(newCP, name);
		getRoot().add(newCP);
		return newCP;
	}

	//Connection
	public Collection<SONConnection> getSONConnections(){
		return Hierarchy.getDescendantsOfType(getRoot(), SONConnection.class);
	}

	public Collection<SONConnection> getSONConnections(Node node){
		ArrayList<SONConnection> result =  new ArrayList<SONConnection>();
		for (Connection con : this.getConnections(node))
			if(con instanceof SONConnection)
				result.add((SONConnection)con);

		return result;
	}

	public SONConnection getSONConnection(Node first, Node second){
		ArrayList<SONConnection> connection =  new ArrayList<SONConnection>();

		for (SONConnection con : this.getSONConnections(first))
			if (this.getSONConnections(second).contains(con))
				connection.add(con);
		if(connection.size() > 1)
			throw new RuntimeException("Conection size between"+ first.toString() + "and"+ second.toString()+ "> 1");

		if(connection.size()  == 0)
		return null;

		return connection.iterator().next();
	}


	public Collection<SONConnection> getInputSONConnections(Node node){
		ArrayList<SONConnection> result =  new ArrayList<SONConnection>();
		for (SONConnection con : this.getSONConnections(node)){
			if (node instanceof MathNode)
				if (con.getSecond() == node)
					result.add(con);
		}
		return result;
	}

	public Collection<SONConnection> getOutputSONConnections(Node node){
		Collection<SONConnection> result =  new ArrayList<SONConnection>();
		for (SONConnection con : this.getSONConnections(node)){
			if (node instanceof MathNode)
				if (con.getFirst() == node)
					result.add(con);
		}
		return result;
	}

	public Collection<Semantics> getSONConnectionTypes (Node node){
		Collection<Semantics> result =  new HashSet<Semantics>();
		for (SONConnection con : getSONConnections(node)){
			result.add(con.getSemantics());
		}

		return result;
	}

	public Collection<Semantics> getSONConnectionTypes (Collection<Node> nodes){
		Collection<Semantics> result =  new HashSet<Semantics>();
		for(Node node : nodes){
			for (SONConnection con : getSONConnections(node)){
				if (nodes.contains(con.getFirst()) && nodes.contains(con.getSecond()))
					result.add(con.getSemantics());
			}
		}
		return result;
	}

	public Semantics getSONConnectionType (Node first, Node second){
		SONConnection con = getSONConnection(first, second);
		return con.getSemantics();
	}

	public Collection<Semantics> getInputSONConnectionTypes(Node node){
		Collection<Semantics> result =  new HashSet<Semantics>();
		for (SONConnection con : this.getSONConnections(node)){
			if (node instanceof MathNode)
				if (con.getSecond() == node)
					result.add(con.getSemantics());
		}
		return result;
	}

	public Collection<Semantics> getOutputSONConnectionTypes(Node node){
		Collection<Semantics> result =  new HashSet<Semantics>();
		for (SONConnection con : this.getSONConnections(node)){
			if (node instanceof MathNode)
				if (con.getFirst() == node)
					result.add(con.getSemantics());
		}
		return result;
	}

	//Group based
	public Collection<Block> getBlocks(){
		return Hierarchy.getDescendantsOfType(getRoot(), Block.class);
	}

	public Collection<TransitionNode> getTransitionNodes(){
		ArrayList<TransitionNode> result = new ArrayList<TransitionNode>();
		for(TransitionNode node :  Hierarchy.getDescendantsOfType(getRoot(), TransitionNode.class)){
			if(node instanceof Block){
				if(((Block)node).getIsCollapsed())
					result.add(node);
			}
			if(node instanceof Event)
				result.add(node);
		}
		return result;
	}

	public Collection<PageNode> getPageNodes(){
		return Hierarchy.getDescendantsOfType(getRoot(), PageNode.class);
	}

	public Collection<ONGroup> getGroups(){
		return Hierarchy.getDescendantsOfType(getRoot(), ONGroup.class);
	}

	public boolean isInSameGroup (Node first, Node second){
		for (ONGroup group : getGroups()){
			if (group.contains(first) && group.contains(second))
				return true;
		}
		return false;
	}

	public String toString(Collection<? extends Node> nodes){

		StringBuffer result = new StringBuffer("");

		boolean first = true;
		for (Node node : nodes) {
			if (!first) {
				result.append(',');
				result.append(' ' + getNodeReference(node));
			}else{
				result.append(' ');
				result.append('[');
				result.append(getNodeReference(node));
				first = false;
			}
		}
		if(!nodes.isEmpty())
			result.append(']');
		return result.toString();

	}
}
