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

package org.workcraft.plugins.policy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.serialisation.References;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@VisualClass (org.workcraft.plugins.policy.VisualPolicyNet.class)
public class PolicyNet extends PetriNet implements PolicyNetModel {

	public PolicyNet() {
		this(null, null);
	}

	public PolicyNet(Container root) {
		this(root, null);
	}

	public PolicyNet(Container root, References refs) {
		super((root == null ? new Locality() : root), refs, new Func<Node, String>() {
			@Override
			public String eval(Node arg) {
				String result = null;
				if (arg instanceof Bundle) {
					result = "b";
				} else if (arg instanceof Locality) {
					result = "loc";
				}
				return result;
			}
		});
		// update all bundles when a transition is removed or re-parented
		new HierarchySupervisor() {
			@Override
			public void handleEvent(HierarchyEvent e) {
				if (e instanceof NodesDeletingEvent) {
					for (Node node: e.getAffectedNodes()) {
						if (node instanceof BundledTransition) {
							for (Bundle b: new ArrayList<Bundle>(getBundles())) {
								b.remove((BundledTransition)node);
							}
						}
					}
				}
			}
		}.attach(getRoot());
	}

	@Override
	public Collection<Bundle> getBundles() {
		return Hierarchy.getDescendantsOfType(getRoot(), Bundle.class);
	}

	public Bundle createBundle() {
		Bundle b = new Bundle();
		getRoot().add(b);
		return b;
	}

	public void bundleTransitions(Collection<BundledTransition> transitions) {
		if (transitions != null && !transitions.isEmpty()) {
			Bundle bundle = createBundle();
			for (BundledTransition t: transitions) {
				bundle.add(t);
			}
		}
	}

	public void unbundleTransition(BundledTransition transition) {
		for (Bundle bundle: getBundles()) {
			if (bundle.contains(transition)) {
				bundle.remove(transition);
				if (bundle.isEmpty()) {
					getRoot().remove(bundle);
				}
			}
		}
	}

	public Collection<Bundle> getBundlesOfTransition(BundledTransition t) {
		Collection<Bundle> result = new HashSet<Bundle>();
		for (Bundle b: getBundles()) {
			if (b.contains(t)) {
				result.add(b);
			}
		}
		return result;
	}

	public Locality createLocality(ArrayList<Node> nodes, Container parent) {
		Locality locality = new Locality();
		parent.add(locality);
		parent.reparent(nodes, locality);

		ArrayList<Node> connectionsToLocality = new ArrayList<Node>();
		for (Connection connection : Hierarchy.getChildrenOfType(parent, Connection.class)) {
			if (Hierarchy.isDescendant(connection.getFirst(), locality)	&& Hierarchy.isDescendant(connection.getSecond(), locality)) {
				connectionsToLocality.add(connection);
			}
		}
		parent.reparent(connectionsToLocality, locality);

		splitBundlesByLocalities(nodes);

		return locality;
	}

	private void splitBundlesByLocalities(ArrayList<Node> nodes) {
		HashMap<Bundle, HashSet<BundledTransition>> subBundles = new HashMap<Bundle, HashSet<BundledTransition>>();
		for (Node node: nodes) {
			if (node instanceof BundledTransition) {
				BundledTransition t = (BundledTransition)node;
				for (Bundle b: ((PolicyNet)getMathModel()).getBundlesOfTransition(t)) {
					HashSet<BundledTransition> transitions = subBundles.get(b);
					if (transitions == null) {
						transitions = new HashSet<BundledTransition>();
						subBundles.put(b, transitions);
					}
					transitions.add(t);
				}
			}
		}

		for (Bundle b: subBundles.keySet()) {
			HashSet<BundledTransition> transitions = subBundles.get(b);
			if (b.getTransitions().size() > transitions.size()) {
				((PolicyNet)getMathModel()).bundleTransitions(transitions);
				b.removeAll(subBundles.get(b));
			}
		}
	}

}
