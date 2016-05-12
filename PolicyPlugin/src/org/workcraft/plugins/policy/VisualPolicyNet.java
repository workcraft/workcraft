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
import java.util.HashSet;

import org.workcraft.annotations.CustomTools;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.ShortName;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.SelectionHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateSupervisor;
import org.workcraft.observation.TransformChangedEvent;
import org.workcraft.plugins.petri.VisualPetriNet;
import org.workcraft.plugins.policy.propertydescriptors.BundleColorPropertyDescriptor;
import org.workcraft.plugins.policy.propertydescriptors.BundleNamePropertyDescriptor;
import org.workcraft.plugins.policy.propertydescriptors.BundlesOfTransitionPropertyDescriptor;
import org.workcraft.plugins.policy.propertydescriptors.TransitionsOfBundlePropertyDescriptor;
import org.workcraft.util.ColorGenerator;
import org.workcraft.util.ColorUtils;
import org.workcraft.util.Hierarchy;

@DisplayName ("Policy Net")
@ShortName("policy")
@CustomTools (PolicyNetToolProvider.class)
public class VisualPolicyNet extends VisualPetriNet {
    private final ColorGenerator bundleColorGenerator = new ColorGenerator(ColorUtils.getHsbPalette(
            new float[]{0.05f, 0.15f, 0.25f, 0.35f, 0.45f, 0.55f, 0.65f, 0.75f, 0.85f, 0.95f},
            new float[]{0.50f}, new float[]{0.9f, 0.7f, 0.5f}));

    public VisualPolicyNet(PolicyNet model) {
        this(model, null);
    }

    public VisualPolicyNet(PolicyNet model, VisualGroup root) {
        super(model, root == null ? new VisualLocality((Locality) model.getRoot()) : root);
        // invalidate spanning trees of all VisualBundles when the the model is changed
        new StateSupervisor() {
            @Override
            public void handleEvent(StateEvent e) {
                if (e instanceof ModelModifiedEvent
                        || e instanceof PropertyChangedEvent
                        || e instanceof TransformChangedEvent) {
                    for (VisualBundle b: getVisualBundles()) {
                        b.invalidateSpanningTree();
                    }
                }
            }
        }.attach(getRoot());
    }

    public PolicyNet getPolicyNet() {
        return (PolicyNet) getMathModel();
    }

    @Override
    public VisualGroup groupSelection() {
        ArrayList<Node> selected = new ArrayList<>();
        ArrayList<Node> refSelected = new ArrayList<>();
        for (Node node : SelectionHelper.getOrderedCurrentLevelSelection(this)) {
            if (node instanceof VisualTransformableNode) {
                selected.add((VisualTransformableNode) node);
                if (node instanceof VisualComponent) {
                    refSelected.add(((VisualComponent) node).getReferencedComponent());
                } else if (node instanceof VisualLocality) {
                    refSelected.add(((VisualLocality) node).getLocality());
                }
            }
        }
        VisualLocality newLocality = null;
        if (selected.size() > 0) {
            VisualLocality curLocality = (VisualLocality) getCurrentLevel();
            newLocality = new VisualLocality(getPolicyNet().createLocality(refSelected, curLocality.getLocality()));
            curLocality.add(newLocality);
            curLocality.reparent(selected, newLocality);

            ArrayList<Node> connectionsToLocality = new ArrayList<>();
            for (VisualConnection connection : Hierarchy.getChildrenOfType(curLocality, VisualConnection.class)) {
                if (Hierarchy.isDescendant(connection.getFirst(), newLocality) && Hierarchy.isDescendant(connection.getSecond(), newLocality)) {
                    connectionsToLocality.add(connection);
                }
            }
            curLocality.reparent(connectionsToLocality, newLocality);
            select(newLocality);
        }
        return newLocality;
    }

    @Override
    public void ungroupSelection() {
        int count = 0;
        for (Node node : SelectionHelper.getOrderedCurrentLevelSelection(this)) {
            if (node instanceof VisualLocality) {
                count++;
            }
        }
        if (count == 1) {
            ArrayList<Node> toSelect = new ArrayList<>();
            Collection<Node> mathNodes = new ArrayList<>();
            for (Node node : SelectionHelper.getOrderedCurrentLevelSelection(this)) {
                if (node instanceof VisualLocality) {
                    VisualLocality locality = (VisualLocality) node;
                    for (Node subNode : locality.unGroup()) {
                        toSelect.add(subNode);
                    }
                    for (Node child : locality.getLocality().getChildren()) {
                        mathNodes.add(child);
                    }
                    locality.getLocality().reparent(mathNodes, ((VisualLocality) getCurrentLevel()).getLocality());
                    getMathModel().remove(locality.getLocality());
                    getCurrentLevel().remove(locality);

                } else {
                    toSelect.add(node);
                }
            }
            select(toSelect);
        }
    }

    public Collection<VisualBundledTransition> getVisualBundledTransitions() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualBundledTransition.class);
    }

    public Collection<VisualBundle> getVisualBundles() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualBundle.class);
    }

    public Collection<VisualLocality> getVisualLocalities() {
        return Hierarchy.getDescendantsOfType(getRoot(), VisualLocality.class);
    }

    public VisualBundle createVisualBundle() {
        Bundle bundle = getPolicyNet().createBundle();
        VisualBundle visualBundle = new VisualBundle(bundle);
        getRoot().add(visualBundle);
        visualBundle.setColor(bundleColorGenerator.updateColor());
        return visualBundle;
    }

    public VisualBundle createVisualBundle(String name) {
        VisualBundle b = createVisualBundle();
        getPolicyNet().setName(b.getReferencedBundle(), name);
        return b;
    }

    public void bundleTransitions(Collection<VisualBundledTransition> transitions) {
        if (transitions != null && !transitions.isEmpty()) {
            VisualBundle bundle = createVisualBundle();
            for (VisualBundledTransition t: transitions) {
                bundle.getReferencedBundle().add(t.getReferencedTransition());
            }
        }
    }

    public void unbundleTransitions(Collection<VisualBundledTransition> transitions) {
        for (VisualBundledTransition t: transitions) {
            getPolicyNet().unbundleTransition(t.getReferencedTransition());
        }
        for (VisualBundle b: getVisualBundles()) {
            if (b.getReferencedBundle().isEmpty()) {
                getRoot().remove(b);
            }
        }
    }

    public String getBundlesOfTransitionAsString(VisualBundledTransition t) {
        String result = "";
        for (VisualBundle b: getBundlesOfTransition(t)) {
            if (result != "") {
                result += ", ";
            }
            result += getPolicyNet().getName(b.getReferencedBundle());
        }
        return result;
    }

    public void setBundlesOfTransitionAsString(VisualBundledTransition t, String s) {
        for (Bundle b: getPolicyNet().getBundles()) {
            b.remove(t.getReferencedTransition());
        }
        for (String ref : s.split("\\s*,\\s*")) {
            Node node = getPolicyNet().getNodeByReference(ref);
            if (node == null) {
                node = createVisualBundle(ref).getReferencedBundle();
            }
            if (node instanceof Bundle) {
                ((Bundle) node).add(t.getReferencedTransition());
            }
        }
        for (VisualBundle b: getVisualBundles()) {
            if (b.getReferencedBundle().isEmpty()) {
                getRoot().remove(b);
            }
        }
    }

    public String getTransitionsOfBundleAsString(VisualBundle b) {
        String result = "";
        for (VisualBundledTransition t: getTransitionsOfBundle(b)) {
            if (result != "") {
                result += ", ";
            }
            result += getPolicyNet().getName(t.getReferencedTransition());
        }
        return result;
    }

    public void setTransitionsOfBundleAsString(VisualBundle vb, String s) {
        Bundle b = vb.getReferencedBundle();
        for (BundledTransition t: new ArrayList<BundledTransition>(b.getTransitions())) {
            b.remove(t);
        }
        for (String ref : s.split("\\s*,\\s*")) {
            Node node = getPetriNet().getNodeByReference(ref);
            if (node instanceof BundledTransition) {
                b.add((BundledTransition) node);
            }
        }
    }

    public Collection<VisualBundle> getBundlesOfTransition(VisualBundledTransition t) {
        Collection<VisualBundle> result = new HashSet<>();
        if (t != null) {
            for (VisualBundle b: getVisualBundles()) {
                if (b.getReferencedBundle().contains(t.getReferencedTransition())) {
                    result.add(b);
                }
            }
        }
        return result;
    }

    public Collection<VisualBundledTransition> getTransitionsOfBundle(VisualBundle b) {
        Collection<VisualBundledTransition> result = new HashSet<>();
        for (VisualBundledTransition t: getVisualBundledTransitions()) {
            if (b.getReferencedBundle().contains(t.getReferencedTransition())) {
                result.add(t);
            }
        }
        return result;
    }

    @Override
    public ModelProperties getProperties(Node node) {
        ModelProperties properties = super.getProperties(node);
        if (node == null) {
            for (VisualBundle vb: getVisualBundles()) {
                properties.add(new BundleNamePropertyDescriptor(this, vb));
                properties.add(new BundleColorPropertyDescriptor(this, vb));
                properties.add(new TransitionsOfBundlePropertyDescriptor(this, vb));
            }
        } else if (node instanceof VisualBundledTransition) {
            VisualBundledTransition t = (VisualBundledTransition) node;
            properties.add(new BundlesOfTransitionPropertyDescriptor(this, t));
        }
        return properties;
    }

}
