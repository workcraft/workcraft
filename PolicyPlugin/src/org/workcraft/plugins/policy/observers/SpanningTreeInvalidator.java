package org.workcraft.plugins.policy.observers;

import org.workcraft.observation.*;
import org.workcraft.plugins.policy.VisualBundle;
import org.workcraft.plugins.policy.VisualPolicy;

public class SpanningTreeInvalidator extends StateSupervisor {

    private final VisualPolicy policy;

    public SpanningTreeInvalidator(VisualPolicy policy) {
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
