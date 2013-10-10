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
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.policy.propertydescriptors.BundleColorPropertyDescriptor;
import org.workcraft.plugins.policy.propertydescriptors.BundleNamePropertyDescriptor;
import org.workcraft.plugins.policy.propertydescriptors.BundleTransitionsPropertyDescriptor;
import org.workcraft.plugins.policy.propertydescriptors.TransitionBundlesPropertyDescriptor;
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
					result = "bundle";
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

	public Bundle createBundle(String ref) {
		Bundle b = createBundle();
		setName(b, ref);
		return b;
	}

	public void deleteBundle(Bundle b) {
		getRoot().remove(b);
	}

	public void bundleTransitions(Collection<BundledTransition> transitions) {
		if (transitions != null && !transitions.isEmpty()) {
			Bundle bundle = createBundle();
			for (BundledTransition t: transitions) {
				bundle.add(t);
			}
		}
	}

	public void unbundleTransitions(Collection<BundledTransition> transitions) {
		for (BundledTransition t: transitions) {
			for (Bundle b: getBundles()) {
				if (b.contains(t)) {
					b.remove(t);
					if (b.isEmpty()) {
						deleteBundle(b);
					}
				}
			}
		}
	}

	public void addToBundles(BundledTransition transition, Collection<Bundle> bundles) {
		if (transition != null && bundles != null) {
			for (Bundle b: bundles) {
				b.add(transition);
			}
		}
	}

	public void removeFromBundles(BundledTransition transition, Collection<Bundle> bundles) {
		if (transition != null && bundles != null) {
			for (Bundle b: bundles) {
				b.remove(transition);
				if (b.isEmpty()) {
					deleteBundle(b);
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

	public String getBundlesOfTransitionAsString(BundledTransition t) {
		String result = "";
		for (Bundle b: getBundlesOfTransition(t)) {
			if (result != "") {
				result += ", ";
			}
			result += getNodeReference(b);
		}
		return result;
	}

	public void setBundlesOfTransitionAsString(BundledTransition t, String s) {
		for (Bundle b: getBundles()) {
			b.remove(t);
		}
		for (String ref : s.split("\\s*,\\s*")) {
			Node node = getNodeByReference(ref);
			if (node == null) {
				node = createBundle(ref);
			}
			if (node instanceof Bundle) {
				Bundle b = (Bundle)node;
				b.add(t);
			}
		}
	}

	public String getTransitionsOfBundleAsString(Bundle b) {
		String result = "";
		for (BundledTransition t: b.getTransitions()) {
			if (result != "") {
				result += ", ";
			}
			result += getNodeReference(t);
		}
		return result;
	}

	public void setTransitionsOfBundleAsString(Bundle b, String s) {
		for (BundledTransition t: new ArrayList<BundledTransition>(b.getTransitions())) {
			b.remove(t);
		}
		for (String ref : s.split("\\s*,\\s*")) {
			Node node = getNodeByReference(ref);
			if (node instanceof BundledTransition) {
				b.add((BundledTransition)node);
			}
		}
	}

	public Locality createLocality(ArrayList<Node> selection, Container currentLevel) {
		Locality locality = new Locality();
		currentLevel.add(locality);
		currentLevel.reparent(selection, locality);

		ArrayList<Node> connectionsToLocality = new ArrayList<Node>();
		for (Connection connection : Hierarchy.getChildrenOfType(currentLevel, Connection.class)) {
			if (Hierarchy.isDescendant(connection.getFirst(), locality)	&& Hierarchy.isDescendant(connection.getSecond(), locality)) {
				connectionsToLocality.add(connection);
			}
		}
		currentLevel.reparent(connectionsToLocality, locality);

		HashMap<Bundle, HashSet<BundledTransition>> subBundles = new HashMap<Bundle, HashSet<BundledTransition>>();
		for (Node node: selection) {
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

		return locality;
	}


	@Override
	public Properties getProperties(Node node) {
		Properties properties = super.getProperties(node);
		if (node == null) {
			for (Bundle b: getBundles()) {
				properties = Properties.Merge.add(properties,
						new BundleNamePropertyDescriptor(this, b),
						new BundleColorPropertyDescriptor(this, b),
						new BundleTransitionsPropertyDescriptor(this, b));
			}
		} else if (node instanceof BundledTransition) {
			BundledTransition t = (BundledTransition)node;
			properties = Properties.Merge.add(properties, new TransitionBundlesPropertyDescriptor(this, t));
		}
		return properties;
	}

}
