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

package org.workcraft.plugins.stg;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@VisualClass("org.workcraft.plugins.stg.VisualSTG")
@DisplayName("Signal Transition Graph")
public class STG extends AbstractMathModel implements STGModel {
	private STGReferenceManager referenceManager;

	public STG() {
		this(null);
	}

	public STG(Container root) {
		this (root, null);
	}

	public STG(Container root, References refs) {
		super(root, new STGReferenceManager(refs));
		referenceManager = (STGReferenceManager) getReferenceManager();

		new SignalTypeConsistencySupervisor(this).attach(getRoot());
	}

	final public Place createPlace() {
		return createPlace(null);
	}

	final public Transition createTransition() {
		return createTransition(null);
	}

	final public SignalTransition createSignalTransition() {
		return createSignalTransition(null);
	}

	final public Place createPlace(String name) {
		Place newPlace = new Place();
		if (name!=null)
			setName(newPlace, name);
		getRoot().add(newPlace);
		return newPlace;
	}

	final public Transition createTransition(String name) {
		Transition newTransition = new Transition();
		if (name!=null)
			setName(newTransition, name);
		getRoot().add(newTransition);
		return newTransition;
	}

	final public SignalTransition createSignalTransition(String name) {
		SignalTransition ret = new SignalTransition();
		if (name!=null)
			setName(ret, name);
		getRoot().add(ret);
		return ret;
	}

	@Override
	public boolean isEnabled(Transition t) {
		return PetriNet.isEnabled(this, t);
	}

	final public void fire (Transition t) {
		PetriNet.fire(this, t);
	}

	final public Collection<SignalTransition> getSignalTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class);
	}

	final public Collection<Place> getPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
	}

	final public Collection<Transition> getTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
	}

	@Override
	public Collection<Transition> getDummies() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class, new Func<Transition, Boolean>(){
			@Override
			public Boolean eval(Transition arg) {
				if (arg instanceof SignalTransition)
					return false;
				return true;
			}
		});
	}

	public Collection<SignalTransition> getSignalTransitions(final Type t) {
		return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class, new Func<SignalTransition, Boolean>(){
			@Override
			public Boolean eval(SignalTransition arg) {
				return arg.getSignalType() == t;
			}}
		);
	}

	public Set<String> getSignalNames(Type type) {
		return getUniqueNames(getSignalTransitions(type));
	}

	public Set<String> getDummyNames() {
		Set<String> result = new HashSet<String>();
		for (Transition t : getDummies())
			result.add(getName(t));
		return result;
	}

	private Set<String> getUniqueNames(Collection<SignalTransition> transitions) {
		Set<String> result = new HashSet<String>();
		for (SignalTransition st : transitions)
			result.add(st.getSignalName());
		return result;
	}

	public int getInstanceNumber (SignalTransition st) {
		return referenceManager.getInstanceNumber(st);
	}

	public String getName(Node node) {
		return referenceManager.getName(node);
	}

	public void setName(Node node, String name) {
		referenceManager.setName(node, name);
	}

	@Override
	public Properties getProperties(Node node) {
		Properties.Mix result = new Properties.Mix(new NamePropertyDescriptor(this, node));
		if (node instanceof SignalTransition)
			result.add(new InstancePropertyDescriptor(this, (SignalTransition)node));
		return result;
	}

	public Collection<SignalTransition> getSignalTransitions(String signalName) {
		return referenceManager.getSignalTransitions(signalName);
	}

	@Override
	public void validateConnection(Node first, Node second)
			throws InvalidConnectionException {
		if (first instanceof Place && second instanceof Place)
			throw new InvalidConnectionException ("Connections between places are not valid");
		if (first instanceof Transition && second instanceof Transition)
			throw new InvalidConnectionException ("Connections between transitions are not valid");
	}

	@Override
	public void validate() throws ModelValidationException {

	}
}