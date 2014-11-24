/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.circuit;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.util.Hierarchy;

@VisualClass(org.workcraft.plugins.circuit.VisualCircuitComponent.class)
public class CircuitComponent extends MathGroup implements Container, ObservableHierarchy {

	DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);
	private String name = "";

	public CircuitComponent() {
		// Update all set/reset functions of the component when its contact is removed
		new HierarchySupervisor() {
			@Override
			public void handleEvent(HierarchyEvent e) {
				if (e instanceof NodesDeletingEvent) {
					for (Node node: e.getAffectedNodes()) {
						if (node instanceof Contact) {
							final Contact contact = (Contact)node;
							for (FunctionContact fc: new ArrayList<FunctionContact>(getFunctionContact())) {
								BooleanFormula setFunction = BooleanReplacer.replace(fc.getSetFunction(), contact, Zero.instance());
								fc.setSetFunction(setFunction);
								BooleanFormula resetFunction = BooleanReplacer.replace(fc.getResetFunction(), contact, Zero.instance());
								fc.setResetFunction(resetFunction);
							}
						}
					}
				}
			}
		}.attach(this);
	}

	private boolean isEnvironment;

	public boolean getIsEnvironment() {
		return isEnvironment;
	}

	public void setIsEnvironment(boolean isEnvironment) {
		this.isEnvironment = isEnvironment;
	}

	@Override
	public Node getParent() {
		return groupImpl.getParent();
	}

	@Override
	public void setParent(Node parent) {
		groupImpl.setParent(parent);
	}

	@Override
	public void addObserver(HierarchyObserver obs) {
		groupImpl.addObserver(obs);
	}

	@Override
	public void removeObserver(HierarchyObserver obs) {
		groupImpl.removeObserver(obs);
	}

	@Override
	public void add(Node node) {
		groupImpl.add(node);
	}

	@Override
	public void add(Collection<Node> nodes) {
		groupImpl.add(nodes);
	}

	@Override
	public void remove(Node node) {
		groupImpl.remove(node);
	}

	@Override
	public void remove(Collection<Node> node) {
		groupImpl.remove(node);
	}

	@Override
	public void reparent(Collection<Node> nodes) {
		groupImpl.reparent(nodes);
	}

	@Override
	public void reparent(Collection<Node> nodes, Container newParent) {
		groupImpl.reparent(nodes, newParent);
	}

	@Override
	public Collection<Node> getChildren() {
		return groupImpl.getChildren();
	}

	public Collection<Contact> getContacts() {
		return Hierarchy.filterNodesByType(getChildren(), Contact.class);
	}

	public Collection<FunctionContact> getFunctionContact() {
		return Hierarchy.getChildrenOfType(this, FunctionContact.class);
	}

	public Collection<Contact> getInputs() {
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(Contact c : getContacts()) {
			if(c.getIOType() == IOType.INPUT) {
				result.add(c);
			}
		}
		return result;
	}

	public Collection<Contact> getOutputs() {
		ArrayList<Contact> result = new ArrayList<Contact>();
		for(Contact c : getContacts()) {
			if(c.getIOType() == IOType.OUTPUT) {
				result.add(c);
			}
		}
		return result;
	}

	public void setName(String name) {
		this.name = name;
		sendNotification(new PropertyChangedEvent(this, "name"));
	}

	public String getName() {
		return name;
	}

}
