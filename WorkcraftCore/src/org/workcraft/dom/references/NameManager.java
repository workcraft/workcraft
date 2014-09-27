package org.workcraft.dom.references;

import org.workcraft.dom.Node;

public interface NameManager {
	public boolean isNamed(Node node);
	public String getName(Node node);
	public void setName(Node node, String name);
	public Node get(String name);
	public void remove(Node node);
	public String getPrefix(Node node);
	public void setDefaultNameIfUnnamed(Node node);
}
