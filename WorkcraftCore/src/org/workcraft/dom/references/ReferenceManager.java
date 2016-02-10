package org.workcraft.dom.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;

public interface ReferenceManager {
    String getNodeReference(NamespaceProvider provider, Node node);
    Node getNodeByReference(NamespaceProvider provider, String reference);
    void attach(Node root);
    void detach();
    String getPrefix(Node node);
}
