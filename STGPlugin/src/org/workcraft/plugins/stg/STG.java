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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.propertydescriptors.DirectionPropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.InstancePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.NamePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.SignalNamePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.SignalPropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.SignalTypePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.TypePropertyDescriptor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;
import org.workcraft.util.Pair;
import org.workcraft.util.SetUtils;
import org.workcraft.util.Triple;

@VisualClass(org.workcraft.plugins.stg.VisualSTG.class)
public class STG extends AbstractMathModel implements STGModel {
	private STGReferenceManager referenceManager;

	public STG() {
		this(null);
	}

	public STG(Container root) {
		this(root, null);
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
		return createDummyTransition(null);
	}

	final public SignalTransition createSignalTransition() {
		return createSignalTransition(null);
	}

	final public STGPlace createPlace(String name) {
		STGPlace newPlace = new STGPlace();
		if (name != null) {
			setName(newPlace, name);
		}
		getRoot().add(newPlace);
		return newPlace;
	}

	final public DummyTransition createDummyTransition(String name) {
		DummyTransition newTransition = new DummyTransition();
		if (name != null) {
			setName(newTransition, name);
		}
		getRoot().add(newTransition);
		return newTransition;
	}

	final public SignalTransition createSignalTransition(String name) {
		SignalTransition ret = new SignalTransition();
		if (name != null) {
			setName(ret, name);
		}
		getRoot().add(ret);
		return ret;
	}

	public boolean isEnabled(Transition t) {
		return PetriNet.isEnabled(this, t);
	}

	public boolean isUnfireEnabled(Transition t) {
		return PetriNet.isUnfireEnabled(this, t);
	}

	final public void fire(Transition t) {
		PetriNet.fire(this, t);
	}

	final public void unFire(Transition t) {
		PetriNet.unFire(this, t);
	}

	final public Collection<SignalTransition> getSignalTransitions() {
		return Hierarchy
				.getDescendantsOfType(getRoot(), SignalTransition.class);
	}

	final public Collection<Place> getPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
	}

	final public Collection<Transition> getTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
	}

	@Override
	public Collection<Transition> getDummies() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class,
				new Func<Transition, Boolean>() {
					@Override
					public Boolean eval(Transition arg) {
						if (arg instanceof SignalTransition)
							return false;
						return true;
					}
				});
	}

	public Collection<SignalTransition> getSignalTransitions(final Type t) {
		return Hierarchy.getDescendantsOfType(getRoot(),
				SignalTransition.class, new Func<SignalTransition, Boolean>() {
					@Override
					public Boolean eval(SignalTransition arg) {
						return arg.getSignalType() == t;
					}
				});
	}

	public Set<String> getSignalNames(Type type) {
		return getUniqueNames(getSignalTransitions(type));
	}

	public Set<String> getDummyNames() {
		Set<String> result = new HashSet<String>();
		for (Transition t : getDummies()) {
			result.add(referenceManager.getNamePair(t).getFirst());
		}
		return result;
	}

	private Set<String> getUniqueNames(Collection<SignalTransition> transitions) {
		Set<String> result = new HashSet<String>();
		for (SignalTransition st : transitions) {
			result.add(st.getSignalName());
		}
		return result;
	}

	public int getInstanceNumber(Node st) {
		return referenceManager.getInstanceNumber(st);
	}

	public void setInstanceNumber(Node st, int number) {
		referenceManager.setInstanceNumber(st, number);
	}

	public Direction getDirection(Node t) {
		String name = referenceManager.getName(t);
		return LabelParser.parseSignalTransition(name).getSecond();
	}

	public void setDirectionWithAutoInstance(Node t, Direction direction) {
		String name = referenceManager.getName(t);
		Triple<String, Direction, Integer> old = LabelParser
				.parseSignalTransition(name);
		referenceManager.setName(t, old.getFirst() + direction.toString());
	}

	public String makeReference(Pair<String, Integer> label) {
		String name = label.getFirst();
		Integer instance = label.getSecond();
		return name + "/" + ((instance == null) ? 0 : instance);
	}

	public String makeReference(Triple<String, Direction, Integer> label) {
		String name = label.getFirst();
		Integer instance = label.getThird();
		return name + label.getSecond() + "/"
				+ ((instance == null) ? 0 : instance);
	}

	public String getName(Node node) {
		return referenceManager.getName(node);
	}

	public void setName(Node node, String name) {
		this.setName(node, name, false);
	}

	public void setName(Node node, String name, boolean forceInstance) {
		referenceManager.setName(node, name, forceInstance);
	}

	@Override
	public Properties getProperties(Node node) {
		Properties properties = super.getProperties(node);
		if (node == null) {
			for (Type type : Type.values()) {
				for (final String signal : getSignalNames(type)) {
					if (getSignalTransitions(signal).isEmpty() ) continue;
					properties = Properties.Merge.add(properties,
							new SignalNamePropertyDescriptor(this, signal),
							new SignalTypePropertyDescriptor(this, signal));
				}
			}
			System.out.println("===");
		} else {
			if (node instanceof STGPlace) {
				STGPlace place = (STGPlace) node;
				if (!place.isImplicit()) {
					properties = Properties.Merge.add(properties,
							new NamePropertyDescriptor(this, place));
				}
			} else if (node instanceof SignalTransition) {
				SignalTransition transition = (SignalTransition) node;
				properties = Properties.Merge.add(properties,
						new TypePropertyDescriptor(this, transition),
						new SignalPropertyDescriptor(this, transition),
						new DirectionPropertyDescriptor(this, transition),
						new InstancePropertyDescriptor(this, transition));
			} else if (node instanceof DummyTransition) {
				DummyTransition dummy = (DummyTransition) node;
				properties = Properties.Merge.add(properties,
						new NamePropertyDescriptor(this, dummy),
						new InstancePropertyDescriptor(this, dummy));
			}
		}
		return properties;
	}

	public Collection<SignalTransition> getSignalTransitions(String signalName) {
		return referenceManager.getSignalTransitions(signalName);
	}

	public Type getSignalType(String signalName) {
		Type type = null;
		Collection<SignalTransition> transitions = getSignalTransitions(signalName);
		if ( !transitions.isEmpty() ) {
			type = transitions.iterator().next().getSignalType();
		}
		return type;
	}

	public void setSignalType(String signalName, Type signalType) {
		for (SignalTransition transition : getSignalTransitions(signalName)) {
			transition.setSignalType(signalType);
			// It is sufficient to change the type of a single transition
			// - all the others will be notified.
			break;
		}
	}

	public ConnectionResult connect(Node first, Node second)
			throws InvalidConnectionException {
		if (first instanceof Transition && second instanceof Transition) {
			STGPlace p = new STGPlace();
			p.setImplicit(true);

			MathConnection con1 = new MathConnection((Transition) first, p);
			MathConnection con2 = new MathConnection(p, (Transition) second);

			Hierarchy.getNearestContainer(first, second).add(
					Arrays.asList(new Node[] { p, con1, con2 }));

			return new ComplexResult(p, con1, con2);
		} else if (first instanceof Place && second instanceof Place)
			throw new InvalidConnectionException(
					"Connections between places are not valid");
		else {
			MathConnection con = new MathConnection((MathNode) first,
					(MathNode) second);
			Hierarchy.getNearestContainer(first, second).add(con);
			return new SimpleResult(con);
		}
	}

	public String getNodeReference(Node node) {
		if (node instanceof STGPlace) {
			if (((STGPlace) node).isImplicit()) {
				Set<Node> preset = getPreset(node);
				Set<Node> postset = getPostset(node);

				if (!(preset.size() == 1 && postset.size() == 1)) {
					throw new RuntimeException(
							"An implicit place cannot have more that one transition in its preset or postset.");
				}
				return "<"
						+ referenceManager.getNodeReference(preset.iterator()
								.next())
						+ ","
						+ referenceManager.getNodeReference(postset.iterator()
								.next()) + ">";
			} else {
				return referenceManager.getNodeReference(node);
			}
		} else {
			return referenceManager.getNodeReference(node);
		}
	}

	public Node getNodeByReference(String reference) {
		Pair<String, String> implicitPlaceTransitions = LabelParser
				.parseImplicitPlaceReference(reference);
		if (implicitPlaceTransitions != null) {
			Node t1 = referenceManager
					.getNodeByReference(implicitPlaceTransitions.getFirst());
			Node t2 = referenceManager
					.getNodeByReference(implicitPlaceTransitions.getSecond());

			Set<Node> implicitPlaceCandidates = SetUtils.intersection(
					getPreset(t2), getPostset(t1));

			for (Node node : implicitPlaceCandidates) {
				if (node instanceof STGPlace && ((STGPlace) node).isImplicit()) {
					return node;
				}
			}

			throw new NotFoundException("Implicit place between "
					+ implicitPlaceTransitions.getFirst() + " and "
					+ implicitPlaceTransitions.getSecond() + " does not exist.");
		} else
			return referenceManager.getNodeByReference(reference);
	}

	public void makeExplicit(STGPlace implicitPlace) {
		implicitPlace.setImplicit(false);
		referenceManager.setDefaultNameIfUnnamed(implicitPlace);
	}

}
