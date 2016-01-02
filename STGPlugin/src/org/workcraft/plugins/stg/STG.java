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
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceHelper;
import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.NotFoundException;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;
import org.workcraft.plugins.stg.propertydescriptors.DirectionPropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.InstancePropertyDescriptor;
import org.workcraft.plugins.stg.propertydescriptors.SignalPropertyDescriptor;
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
		this(null, null);
	}

	public STG(Container root) {
		this(root, null);
	}

	public STG(Container root, References refs) {
		super(root, new STGReferenceManager(refs));
		referenceManager = (STGReferenceManager)getReferenceManager();
		new SignalTypeConsistencySupervisor(this).attach(getRoot());
	}

	final public Place createPlace() {
		return createPlace(null, null);
	}

	final public STGPlace createPlace(String name, Container container) {
		return createNode(name, container, STGPlace.class);
	}

	final public DummyTransition createDummyTransition(String name, Container container) {
		DummyTransition transition = createNode(name, container, DummyTransition.class);
		if (name == null) {
			name = transition.getName();
		}
		setName(transition, name);
		return transition;
	}

	final public SignalTransition createSignalTransition() {
		return createSignalTransition(null, null);
	}

	final public SignalTransition createSignalTransition(String name, Container container) {
		SignalTransition transition = createNode(name, container, SignalTransition.class);
		if (name == null) {
			name = transition.getName();
		}
		setName(transition, name);
		return transition;
	}

	@Override
	public boolean isEnabled(Transition t) {
		return PetriNet.isEnabled(this, t);
	}

	@Override
	public boolean isUnfireEnabled(Transition t) {
		return PetriNet.isUnfireEnabled(this, t);
	}

	@Override
	final public void fire(Transition t) {
		PetriNet.fire(this, t);
	}

	@Override
	final public void unFire(Transition t) {
		PetriNet.unFire(this, t);
	}

	@Override
	final public Collection<SignalTransition> getSignalTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), SignalTransition.class);
	}

	@Override
	final public Collection<Place> getPlaces() {
		return Hierarchy.getDescendantsOfType(getRoot(), Place.class);
	}

	@Override
	final public Collection<Connection> getConnections() {
		return Hierarchy.getDescendantsOfType(getRoot(), Connection.class);
	}

	@Override
	final public Collection<Transition> getTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class);
	}

	@Override
	public Collection<Transition> getDummyTransitions() {
		return Hierarchy.getDescendantsOfType(getRoot(), Transition.class,
				new Func<Transition, Boolean>() {
					@Override
					public Boolean eval(Transition arg) {
						return !(arg instanceof SignalTransition);
					}
				});
	}

	public Collection<SignalTransition> getSignalTransitions(final Type type) {
		return Hierarchy.getDescendantsOfType(getRoot(),
				SignalTransition.class, new Func<SignalTransition, Boolean>() {
					@Override
					public Boolean eval(SignalTransition arg) {
						return (arg.getSignalType() == type);
					}
				});
	}

	public Set<String> getSignalNames(Container container) {
		if (container == null) {
			container = getRoot();
		}
		Set<String> result = new HashSet<String>();
		for (SignalTransition st : Hierarchy.getChildrenOfType(container, SignalTransition.class)) {
			result.add(st.getSignalName());
		}
		return result;
	}

	public Set<String> getSignalNames(final Type type, Container container) {
		if (container == null) {
			container = getRoot();
		}
		Set<String> result = new HashSet<String>();
		for (SignalTransition st : getSignalTransitions(type, container)) {
			result.add(st.getSignalName());
		}
		return result;
	}

	public Collection<SignalTransition> getSignalTransitions(final Type type, Container container) {
		if (container == null) {
			container = getRoot();
		}
		return Hierarchy.getChildrenOfType(container,
				SignalTransition.class, new Func<SignalTransition, Boolean>() {
					@Override
					public Boolean eval(SignalTransition arg) {
						return (type.equals(arg.getSignalType()));
					}
				});
	}

	public Collection<SignalTransition> getSignalTransitions(final String signalName, Container container) {
		if (container == null) {
			container = getRoot();
		}
		return Hierarchy.getChildrenOfType(container,
				SignalTransition.class, new Func<SignalTransition, Boolean>() {
					@Override
					public Boolean eval(SignalTransition arg) {
						return (signalName.equals(arg.getSignalName()));
					}
				});
	}

	public Set<String> getSignalFlatNames(Type type) {
		Set<String> result = new HashSet<String>();
		for (SignalTransition st : getSignalTransitions(type)) {
			String ref = getSignalReference(st);
			String flatName = NamespaceHelper.hierarchicalToFlatName(ref);
			result.add(flatName);
		}
		return result;
	}

	@Override
	public Set<String> getSignalReferences(Type type) {
		Set<String> result = new HashSet<String>();
		for (SignalTransition st : getSignalTransitions(type)) {
			result.add(getSignalReference(st));
		}
		return result;
	}

	@Override
	public Set<String> getDummyReferences() {
		Set<String> result = new HashSet<String>();
		for (Transition t : getDummyTransitions()) {
			result.add(referenceManager.getNamePair(t).getFirst());
		}
		return result;
	}

	public String getSignalReference(SignalTransition st) {
		String reference = referenceManager.getNodeReference(null, st);
		String path = NamespaceHelper.getReferencePath(reference);
		return (path + st.getSignalName());
	}

	public int getInstanceNumber(Node st) {
		return referenceManager.getInstanceNumber(st);
	}

	public void setInstanceNumber(Node st, int number) {
		referenceManager.setInstanceNumber(st, number);
	}

	public Direction getDirection(Node t) {
		Direction result = null;
		String name = referenceManager.getName(t);
		if (name != null) {
			result = LabelParser.parseSignalTransition(name).getSecond();
		}
		return result;
	}

	public void setDirectionWithAutoInstance(Node t, Direction direction) {
		String name = referenceManager.getName(t);
		Triple<String, Direction, Integer> old = LabelParser.parseSignalTransition(name);
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
		return name + label.getSecond() + "/" + ((instance == null) ? 0 : instance);
	}

	@Override
	public String getName(Node node) {
		return referenceManager.getName(node);
	}

	@Override
	public void setName(Node node, String name) {
		this.setName(node, name, false);
	}

	public void setName(Node node, String name, boolean forceInstance) {
		referenceManager.setName(node, name, forceInstance);
	}

	public Collection<SignalTransition> getSignalTransitions(String signalReference) {
		return referenceManager.getSignalTransitions(signalReference);
	}

	public Type getSignalType(String signalReference) {
		Type type = null;
		Collection<SignalTransition> transitions = getSignalTransitions(signalReference);
		if ( !transitions.isEmpty() ) {
			type = transitions.iterator().next().getSignalType();
		}
		return type;
	}

	public void setSignalType(String signalReference, Type signalType) {
		for (SignalTransition transition : getSignalTransitions(signalReference)) {
			transition.setSignalType(signalType);
			// It is sufficient to change the type of a single transition
			// - all the others will be notified.
			break;
		}
	}

	public Type getSignalType(String signalName, Container container) {
		Type type = null;
		Collection<SignalTransition> transitions = getSignalTransitions(signalName, container);
		if ( !transitions.isEmpty() ) {
			type = transitions.iterator().next().getSignalType();
		}
		return type;
	}

	public void setSignalType(String signalName, Type signalType, Container container) {
		for (SignalTransition transition : getSignalTransitions(signalName, container)) {
			transition.setSignalType(signalType);
			// It is sufficient to change the type of a single transition
			// - all the others will be notified.
			break;
		}
	}

	public ConnectionResult connect(Node first, Node second)
			throws InvalidConnectionException {
		if ((first instanceof Transition) && (second instanceof Transition)) {
			STGPlace p = new STGPlace();
			p.setImplicit(true);

			MathConnection con1 = new MathConnection((Transition) first, p);
			MathConnection con2 = new MathConnection(p, (Transition) second);

			Hierarchy.getNearestContainer(first, second).add(
					Arrays.asList(new Node[] { p, con1, con2 }));

			ComplexResult complexResult = new ComplexResult(p, con1, con2);
			return complexResult;
		} else if (first instanceof Place && second instanceof Place)
			throw new InvalidConnectionException("Connections between places are not valid");
		else {
			MathConnection con = new MathConnection((MathNode) first, (MathNode) second);
			Hierarchy.getNearestContainer(first, second).add(con);
			return new SimpleResult(con);
		}
	}

	@Override
	public String getNodeReference(NamespaceProvider provider, Node node) {
		if (node instanceof STGPlace) {
			if (((STGPlace) node).isImplicit()) {
				Set<Node> preset = getPreset(node);
				Set<Node> postset = getPostset(node);
				if (!(preset.size() == 1 && postset.size() == 1)) {
					throw new RuntimeException("An implicit place cannot have more that one transition in its preset or postset.");
				}

				return "<" + NamespaceHelper.hierarchicalToFlatName(referenceManager.getNodeReference(null, preset.iterator().next()))
						+ "," + NamespaceHelper.hierarchicalToFlatName(referenceManager.getNodeReference(null, postset.iterator().next())) + ">";
			}
		}
		return super.getNodeReference(provider, node);
	}

	@Override
	public Node getNodeByReference(NamespaceProvider provider, String reference) {
		Pair<String, String> implicitPlaceTransitions = LabelParser.parseImplicitPlaceReference(reference);
		if (implicitPlaceTransitions != null) {
			Node t1 = referenceManager.getNodeByReference(provider,
					NamespaceHelper.flatToHierarchicalName(implicitPlaceTransitions.getFirst())	);

			Node t2 = referenceManager.getNodeByReference(provider,
					NamespaceHelper.flatToHierarchicalName(implicitPlaceTransitions.getSecond()) );
			if ((t1 != null) && (t2 != null)) {
				Set<Node> implicitPlaceCandidates = SetUtils.intersection(getPreset(t2), getPostset(t1));

				for (Node node : implicitPlaceCandidates) {
					if ((node instanceof STGPlace) && ((STGPlace)node).isImplicit()) {
						return node;
					}
				}
			}
			throw new NotFoundException("Implicit place between "
					+ implicitPlaceTransitions.getFirst() + " and "
					+ implicitPlaceTransitions.getSecond() + " does not exist.");
		} else {
			return super.getNodeByReference(provider, reference);
		}
	}

	public void makeExplicit(STGPlace implicitPlace) {
		implicitPlace.setImplicit(false);
		referenceManager.setDefaultNameIfUnnamed(implicitPlace);
	}

	@Override
	public ModelProperties getProperties(Node node) {
		ModelProperties properties = super.getProperties(node);
		if (node != null) {
			if (node instanceof STGPlace) {
				STGPlace place = (STGPlace)node;
				if (place.isImplicit()) {
					properties.removeByName("Name");
				}
			} else if (node instanceof SignalTransition) {
				SignalTransition transition = (SignalTransition) node;
				properties.add(new TypePropertyDescriptor(this, transition));
				properties.add(new SignalPropertyDescriptor(this, transition));
				properties.add(new DirectionPropertyDescriptor(this, transition));
				properties.add(new InstancePropertyDescriptor(this, transition));
			} else if (node instanceof DummyTransition) {
				DummyTransition dummy = (DummyTransition) node;
				properties.add(new InstancePropertyDescriptor(this, dummy));
			}
		}
		return properties;
	}

}
