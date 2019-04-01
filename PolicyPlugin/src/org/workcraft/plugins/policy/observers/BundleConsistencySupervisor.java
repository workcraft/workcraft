package org.workcraft.plugins.policy.observers;

import org.workcraft.dom.Node;
import org.workcraft.observation.HierarchyEvent;
import org.workcraft.observation.HierarchySupervisor;
import org.workcraft.observation.NodesDeletingEvent;
import org.workcraft.plugins.policy.Bundle;
import org.workcraft.plugins.policy.BundledTransition;
import org.workcraft.plugins.policy.Policy;

import java.util.ArrayList;

public class BundleConsistencySupervisor extends HierarchySupervisor {

    private final Policy policy;

    public BundleConsistencySupervisor(Policy policy) {
        this.policy = policy;
    }

    @Override
    public void handleEvent(HierarchyEvent e) {
        if (e instanceof NodesDeletingEvent) {
            for (Node node: e.getAffectedNodes()) {
                if (node instanceof BundledTransition) {
                    removeBundledTransition((BundledTransition) node);
                }
            }
        }
    }

    private void removeBundledTransition(BundledTransition node) {
        ArrayList<Bundle> copyBundles = new ArrayList<>(policy.getBundles());
        for (Bundle bundle : copyBundles) {
            bundle.remove(node);
        }
    }

}
