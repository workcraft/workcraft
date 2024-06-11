package org.workcraft.plugins.petri;

import org.workcraft.dom.visual.VisualReplica;

public class VisualReplicaPlace extends VisualReplica {

    @SuppressWarnings("unused") // Required for replica creation in ConnectionTool and is called via reflection
    public VisualReplicaPlace() {
        super();
    }

    @SuppressWarnings("unused") // Required for deserialisation and is called via reflection
    public VisualReplicaPlace(VisualPlace master) {
        super();
        setMaster(master);
    }

    @Override
    public Place getReferencedComponent() {
        return (Place) super.getReferencedComponent();
    }

}
