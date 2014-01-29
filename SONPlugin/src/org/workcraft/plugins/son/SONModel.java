package org.workcraft.plugins.son;

import java.awt.Color;
import java.util.Collection;

import org.workcraft.dom.Model;
import org.workcraft.dom.Node;
import org.workcraft.plugins.son.connections.SONConnection;
import org.workcraft.plugins.son.elements.ChannelPlace;
import org.workcraft.plugins.son.elements.Condition;
import org.workcraft.plugins.son.elements.Event;

public interface SONModel extends Model{

	/**
	 *
	 * get all conditions
	 * @return return a condition set
	 *
	 */
	public Collection<Condition> getConditions();
	/**
	 *
	 * get all events
	 * @return return an event set
	 *
	 */
	public Collection<Event> getEvents();
	/**
	 *
	 * get all channel place
	 * @return return a channel place set
	 *
	 */
	public Collection<ChannelPlace> getChannelPlace();

	public Collection<Node> getComponents();

	/**
	 *
	 * get the label of a given node
	 * @param given node n
	 * @return node label
	 *
	 */
	public String getNodeLabel(Node n);

	public ChannelPlace createChannelPlace();
	public ChannelPlace createChannelPlace(String name);

	public void setFillColor(Node n, Color nodeColor);
	public void setForegroundColor(Node n, Color nodeColor);

	public void refreshColor();

	public String getName(Node n);
	public void setName(Node n, String name);

	public void resetErrStates();
	public void resetConditionErrStates();

	//connection

	public Collection<SONConnection> getSONConnections();
	public Collection<SONConnection> getSONConnections(Node node);
	public Collection<SONConnection> getSONConnections(Node first, Node second);

	public Collection<SONConnection> getInputSONConnections(Node node);
	public Collection<SONConnection> getOutputSONConnections(Node node);

	public Collection<String> getSONConnectionTypes (Node node);
	public Collection<String> getSONConnectionTypes (Node first, Node second);
	public Collection<String> getSONConnectionTypes (Collection<Node> nodes);

	public Collection<String> getInputSONConnectionTypes(Node node);
	public Collection<String> getOutputSONConnectionTypes(Node node);

	//Group methods;
	public Collection<ONGroup> getGroups();

	public boolean isInSameGroup (Node first, Node second);
}
