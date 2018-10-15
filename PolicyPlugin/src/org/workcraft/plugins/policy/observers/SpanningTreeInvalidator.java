package org.workcraft.plugins.policy.observers;

import org.workcraft.observation.*;
import org.workcraft.plugins.policy.VisualBundle;
import org.workcraft.plugins.policy.VisualPolicyNet;

public class SpanningTreeInvalidator extends StateSupervisor {

    private final VisualPolicyNet policy;

    public SpanningTreeInvalidator(VisualPolicyNet policy) {
        this.policy = policy;
    }

    @Override
    public void handleEvent(StateEvent e) {
        if ((e instanceof ModelModifiedEvent) || (e instanceof PropertyChangedEvent) || (e instanceof TransformChangedEvent)) {
            for (VisualBundle b: policy.getVisualBundles()) {
                b.invalidateSpanningTree();
            }
        }
    }

}
