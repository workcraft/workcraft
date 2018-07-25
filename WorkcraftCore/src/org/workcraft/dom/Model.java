package org.workcraft.dom;

import java.util.Collection;
import java.util.Set;

import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.util.Func;

public interface Model extends NodeContext {

    void setTitle(String title);
    String getTitle();

    /**
     * @return a user-friendly display name for this model, which is either
     * read from <type>DisplayName</type> annotation, or, if the annotation
     * is missing, taken from the name of the model class.
     */
    String getDisplayName();

    /**
     * @return a short name for this model, which is either read from
     * <type>ShortName</type> annotation, or, if the annotation is
     * missing, made as an acronym from the geDisplayName.
     */
    String getShortName();

    // Methods for work with node references
    ReferenceManager getReferenceManager();
    Node getNodeByReference(NamespaceProvider provider, String reference);
    String getNodeReference(NamespaceProvider provider, Node node);
    Node getNodeByReference(String reference);
    String getNodeReference(Node node);

    String getName(Node node);
    void setName(Node node, String name);
    String getDerivedName(Node node, Container container, String candidate);

    boolean reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren);

    Container getRoot();
    <T> Set<T> getPreset(Node node, Class<T> type);
    <T> Set<T> getPostset(Node node, Class<T> type);
    <T> Set<T> getPreset(Node node, Class<T> type, Func<Node, Boolean> through);
    <T> Set<T> getPostset(Node node, Class<T> type, Func<Node, Boolean> through);

    void add(Node node);
    void remove(Node node);
    void remove(Collection<Node> nodes);

    ModelProperties getProperties(Node node);

    default void beforeSerialisation() {
    }

    default void afterDeserialisation() {
    }
}
