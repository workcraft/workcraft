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
import org.workcraft.dom.references.UniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;


@VisualClass(org.workcraft.plugins.son.VisualSON.class)
public class SON extends AbstractMathModel implements SONModel {

	public SON(){
		this(null);
	}

	public SON(Container root){
		this(root, null);
	}

	public SON(Container root, References refs) {
		super(root, new UniqueNameReferenceManager(refs, new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				if (arg instanceof Condition)
					return "c";
				if (arg instanceof Event)
					return "e";
				if (arg instanceof SONConnection)
					return "con";
				if (arg instanceof ChannelPlace)
					return "q";
				if (arg instanceof Block)
					return "block";
				return "node";
			}
		}));
	}

	public void validate() throws ModelValidationException {
	}

	public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	public SONConnection connect(Node first, Node second, String conType) throws InvalidConnectionException {
		/*if (first instanceof Condition && second instanceof Condition)
			throw new InvalidConnectionException ("Connections between places are not valid");
		*/
		if (!this.getSONConnections(first, second).isEmpty())
			throw new InvalidConnectionException ("Duplicate Connections");
		if (first instanceof Event && second instanceof Event)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
		if (first instanceof ChannelPlace && second instanceof ChannelPlace)
			throw new InvalidConnectionException ("Connections between channel places are not valid");
		if ((first instanceof ChannelPlace && second instanceof Condition) || (first instanceof Condition && second instanceof ChannelPlace))
			throw new InvalidConnectionException ("Connections between channel place and condition are not valid");

		SONConnection con = new SONConnection((MathNode)first, (MathNode)second, conType);
		Hierarchy.getNearestContainer(first, second).add(con);
		return con;
	}

	public void setName(Node node, String name) {
		((UniqueNameReferenceManager)getReferenceManager()).setName(node, name);
	}

	public String getName(Node node) {
		return ((UniqueNameReferenceManager)getReferenceManager()).getName(node);
	}

	public Collection<Condition> getConditions(){
		return Hierarchy.getDescendantsOfType(getRoot(), Condition.class);
	}

	public Collection<ChannelPlace> getChannelPlace(){
		return Hierarchy.getDescendantsOfType(getRoot(), ChannelPlace.class);
	}

	public Collection<Event> getEvents(){
		return Hierarchy.getDescendantsOfType(getRoot(),Event.class);
	}

	public Collection<Node> getComponents(){
		ArrayList<Node> result =  new ArrayList<Node>();
		for (Node node : Hierarchy.getDescendantsOfType(getRoot(), MathNode.class)){
			if (node instanceof Condition)
				result.add(node);
			if (node instanceof Event)
				result.add(node);
		    if (node instanceof ChannelPlace)
		    	result.add(node);
		}
		return result;
	}

	public String getComponentLabel(Node n){
		if(n instanceof Condition)
			return ((Condition)n).getLabel();

		if(n instanceof Event)
			return ((Event)n).getLabel();

		if(n instanceof ChannelPlace)
			return ((ChannelPlace)n).getLabel();

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

		if (n instanceof SONConnection)
			((SONConnection)n).setColor(nodeColor);

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
	}

	public void refreshColor(){
		for(Node n:  getComponents()){
			setFillColor(n,CommonVisualSettings.getFillColor());
			setForegroundColor(n, CommonVisualSettings.getBorderColor());
		}
		for (ONGroup group : this.getGroups()){
			setForegroundColor(group, CommonVisualSettings.getBorderColor());
		}

		for (SONConnection con : this.getSONConnections())
			setForegroundColor(con, CommonVisualSettings.getBorderColor());
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

	@Override
	public Properties getProperties(Node node) {
		if (node != null) {
			return Properties.Mix.from(new NamePropertyDescriptor(this, node));
		}
		return null;
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

	public Collection<SONConnection> getSONConnections(Node first, Node second){
		ArrayList<SONConnection> result =  new ArrayList<SONConnection>();
		for (SONConnection con : this.getSONConnections(first))
			if (this.getSONConnections(second).contains(con))
				result.add(con);
		return result;
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

	public Collection<String> getSONConnectionTypes (Node node){
		Collection<String> result =  new HashSet<String>();
		for (SONConnection con : getSONConnections(node)){
			result.add(con.getType());
		}

		return result;
	}

	public Collection<String> getSONConnectionTypes (Collection<Node> nodes){
		Collection<String> result =  new HashSet<String>();
		for(Node node : nodes){
			for (SONConnection con : getSONConnections(node)){
				if (nodes.contains(con.getFirst()) && nodes.contains(con.getSecond()))
					result.add(con.getType());
			}
		}
		return result;
	}

	public Collection<String> getSONConnectionTypes (Node first, Node second){
		Collection<String> result =  new HashSet<String>();
		for (SONConnection con : getSONConnections(first, second)){
			result.add(con.getType());
		}

		return result;
	}

	public Collection<String> getInputSONConnectionTypes(Node node){
		Collection<String> result =  new HashSet<String>();
		for (SONConnection con : this.getSONConnections(node)){
			if (node instanceof MathNode)
				if (con.getSecond() == node)
					result.add(con.getType());
		}
		return result;
	}

	public Collection<String> getOutputSONConnectionTypes(Node node){
		Collection<String> result =  new HashSet<String>();
		for (SONConnection con : this.getSONConnections(node)){
			if (node instanceof MathNode)
				if (con.getFirst() == node)
					result.add(con.getType());
		}
		return result;
	}

	//Group Methods

	public Collection<Block> getBlocks(){
		return Hierarchy.getChildrenOfType(getRoot(), Block.class);
	}

	public Collection<PageNode> getPageNodes(){
		return Hierarchy.getChildrenOfType(getRoot(), PageNode.class);
	}

	public Collection<ONGroup> getGroups(){
		return Hierarchy.getChildrenOfType(getRoot(), ONGroup.class);
	}

	public boolean isInSameGroup (Node first, Node second){
		for (ONGroup group : getGroups()){
			if (group.contains(first) && group.contains(second))
				return true;
		}
		return false;
	}
}
