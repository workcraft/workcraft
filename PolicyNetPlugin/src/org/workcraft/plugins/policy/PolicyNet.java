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

import java.util.Collection;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

@VisualClass (org.workcraft.plugins.policy.VisualPolicyNet.class)
public class PolicyNet extends PetriNet implements PolicyNetModel {

	public PolicyNet() {
	}

	public PolicyNet(Container root) {
		super(root);
	}

	public PolicyNet(Container root, References refs) {
		super(root, refs);
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

	public void bundle(Collection<BundledTransition> transitions) {
		if (transitions != null && !transitions.isEmpty()) {
			Bundle bundle = createBundle();
			for (BundledTransition t: transitions) {
				bundle.add(t);
			}
		}
	}

	public void unbundle(Collection<BundledTransition> transitions) {
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

	public String getBundlesAsString(BundledTransition t) {
		String result = "";
		for (Bundle b: getBundles()) {
			if (b.contains(t)) {
				if (result != "") {
					result += ", ";
				}
				result += getName(b);
			}
		}
		return result;
	}

	public void setBundlesAsString(BundledTransition t, String s) {
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

	@Override
	public Properties getProperties(Node node) {
		Properties properties = super.getProperties(node);
		if (node instanceof BundledTransition) {
			BundledTransition t = (BundledTransition)node;
			properties = Properties.Merge.add(properties, new BundlesPropertyDescriptor(this, t));
		}
		return properties;
	}

}
