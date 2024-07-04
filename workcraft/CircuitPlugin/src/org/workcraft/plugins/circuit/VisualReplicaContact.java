package org.workcraft.plugins.circuit;

import org.workcraft.dom.visual.VisualReplica;

public class VisualReplicaContact extends VisualReplica {

    @SuppressWarnings("unused") // Required for replica creation in ConnectionTool and is called via reflection
    public VisualReplicaContact() {
        super();
    }

    @SuppressWarnings("unused") // Required for deserialisation and is called via reflection
    public VisualReplicaContact(VisualContact contact) {
        super();
        setMaster(contact);
    }

    @Override
    public Contact getReferencedComponent() {
        return (Contact) super.getReferencedComponent();
    }

    @Override
    public VisualContact getMaster() {
        return (VisualContact) super.getMaster();
    }

}
