package org.workcraft.dom.visual;

import java.util.Collection;

public interface Replicable {
    public void addReplica(Replica replica);
    public void removeReplica(Replica replica);
    public Collection<Replica> getReplicas();
}
