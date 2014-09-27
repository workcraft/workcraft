package org.workcraft.dom.references;

import org.workcraft.dom.Node;
import org.workcraft.dom.hierarchy.NamespaceProvider;

public interface ReferenceManager {
	public String getNodeReference(NamespaceProvider provider, Node node);
	public Node getNodeByReference(NamespaceProvider provider, String reference);
	public void attach(Node root);
	public void detach();
	public String getPrefix(Node node);
}
