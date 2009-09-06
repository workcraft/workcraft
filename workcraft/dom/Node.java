package org.workcraft.dom;
import java.util.Collection;

public interface Node {
	public Node getParent();
	public void setParent(Node parent);

	public Collection<Node> getChildren();
}
