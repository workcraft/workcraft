package org.workcraft.plugins.policy;

import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.plugins.petri.PetriNet;
import org.workcraft.plugins.policy.observers.BundleConsistencySupervisor;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

@VisualClass(org.workcraft.plugins.policy.VisualPolicyNet.class)
public class PolicyNet extends PetriNet implements PolicyNetModel {

    public PolicyNet() {
        this(null, null);
    }

    public PolicyNet(Container root, References refs) {
        super(root, refs);
        new BundleConsistencySupervisor(this).attach(getRoot());
    }

    @Override
    public Locality createDefaultRoot() {
        return new Locality();
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
        Collection<Bundle> result = new HashSet<>();
        for (Bundle b: getBundles()) {
            if (b.contains(t)) {
                result.add(b);
            }
        }
        return result;
    }

    public Locality createLocality(ArrayList<? extends MathNode> nodes, Container parent) {
        Locality locality = new Locality();
        parent.add(locality);
        parent.reparent(nodes, locality);

        ArrayList<Node> connectionsToLocality = new ArrayList<>();
        for (Connection connection : Hierarchy.getChildrenOfType(parent, Connection.class)) {
            if (Hierarchy.isDescendant(connection.getFirst(), locality)    && Hierarchy.isDescendant(connection.getSecond(), locality)) {
                connectionsToLocality.add(connection);
            }
        }
        parent.reparent(connectionsToLocality, locality);

        splitBundlesByLocalities(nodes);

        return locality;
    }

    private void splitBundlesByLocalities(ArrayList<? extends MathNode> nodes) {
        HashMap<Bundle, HashSet<BundledTransition>> subBundles = new HashMap<>();
        for (Node node: nodes) {
            if (node instanceof BundledTransition) {
                BundledTransition t = (BundledTransition) node;
                for (Bundle b: getBundlesOfTransition(t)) {
                    HashSet<BundledTransition> transitions = subBundles.get(b);
                    if (transitions == null) {
                        transitions = new HashSet<>();
                        subBundles.put(b, transitions);
                    }
                    transitions.add(t);
                }
            }
        }

        for (Bundle b: subBundles.keySet()) {
            HashSet<BundledTransition> transitions = subBundles.get(b);
            if (b.getTransitions().size() > transitions.size()) {
                bundleTransitions(transitions);
                b.removeAll(subBundles.get(b));
            }
        }
    }

    public String getTransitionsOfBundleAsString(Bundle b) {
        String result = "";
        for (BundledTransition t: b.getTransitions()) {
            if (result != "") {
                result += ", ";
            }
            result += getName(t);
        }
        return result;
    }

}
