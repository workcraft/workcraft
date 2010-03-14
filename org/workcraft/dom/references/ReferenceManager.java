package org.workcraft.dom.references;

import org.workcraft.dom.Node;

public interface ReferenceManager {
	public String getNodeReference(Node node);
	public Node getNodeByReference(String reference);
	public void attach (Node root);
	public void detach ();
}
