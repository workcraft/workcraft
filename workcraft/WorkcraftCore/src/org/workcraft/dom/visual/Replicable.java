package org.workcraft.dom.visual;

import java.util.Collection;

public interface Replicable {
    void addReplica(Replica replica);
    void removeReplica(Replica replica);
    Collection<Replica> getReplicas();
}
