package org.workcraft.dom.references;

import org.workcraft.dom.Node;

public interface NameManager {

    String getPrefix(Node node);
    void setPrefixCount(String prefix, Integer count);
    Integer getPrefixCount(String prefix);

    void setName(Node node, String name, boolean force);
    String getName(Node node);
    boolean isNamed(Node node);
    boolean isUnusedName(String name);

    Node getNode(String name);
    void remove(Node node);

    void setDefaultName(Node node);
    void setDefaultNameIfUnnamed(Node node);

    String getDerivedName(Node node, String candidate);
}
