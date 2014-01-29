package org.workcraft.plugins.son;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;

@VisualClass (org.workcraft.plugins.son.VisualONGroup.class)
public class ONGroup extends MathGroup{

	private String label="";
	private Color color = CommonVisualSettings.getBorderColor();

	public Collection<Node> getComponents(){
		HashSet<Node> result = new HashSet<Node>();
		for(Node node : this.getChildren())
			if((node instanceof Condition) || (node instanceof Event) )
				result.add(node);
		return result;
	}

	public boolean contains (Node node){
		if (this.getComponents().contains(node))
			return true;
		else
			return false;
	}

	public boolean containsAll(Collection<Node> nodes){
		for(Node node: nodes){
			if(!this.contains(node))
				return false;
			}
		return true;
	}
	@Override
	public void setParent(Node parent) {
		super.setParent(parent);
	}
	@Override
	public void add(Node node) {
		super.add(node);
	}
	@Override
	public void add(Collection<Node> nodes) {
		super.add(nodes);
	}
	@Override
	public void remove(Node node) {
		super.remove(node);
	}
	@Override
	public void remove(Collection<Node> nodes) {
		super.remove(nodes);
	}
	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		super.reparent(nodes, newParent);
	}
	@Override
	public void reparent(Collection<Node> nodes) {
		super.reparent(nodes);
	}

	public Collection<Condition> getConditions(){
		ArrayList<Condition>  result = new ArrayList<Condition>();

		for (Node node : this.getChildren())
			if (node instanceof Condition)
				result.add((Condition)node);
		return result;
	}

	public Collection<Event> getEvents(){
		ArrayList<Event>  result = new ArrayList<Event>();

		for (Node node : this.getChildren())
			if (node instanceof Event)
				result.add((Event)node);
		return result;
	}

	public void setForegroundColor(Color color){
		this.color = color;
		sendNotification(new PropertyChangedEvent(this, "foregroundColor"));
	}

	public Color getForegroundColor(){
		return color;
	}

	public void setLabel(String label){
		this.label = label;
		sendNotification(new PropertyChangedEvent(this, "label"));
	}

	public String getLabel(){
		return label;
	}

}
