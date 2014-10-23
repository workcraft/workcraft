package org.workcraft.dom.references;

import org.workcraft.dom.Node;

public interface NameManager {

	public String getPrefix(Node node);
	public void setPrefixCount(String prefix, Integer count);
	public Integer getPrefixCount(String prefix);

	public void setName(Node node, String name);
	public String getName(Node node);
	public boolean isNamed(Node node);
	public boolean isUnusedName(String name);
	public Node getNode(String name);
	public void remove(Node node);

	public void setDefaultNameIfUnnamed(Node node);
	public String getDerivedName(Node node, String candidate);
}
