package org.workcraft.dom;

import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.util.Func;

import java.util.Collection;
import java.util.Set;

public interface Model<N extends Node, C extends Connection>  extends NodeContext<N, C> {

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
    N getNodeByReference(NamespaceProvider provider, String reference);
    String getNodeReference(NamespaceProvider provider, Node node);
    N getNodeByReference(String reference);
    String getNodeReference(Node node);

    String getName(Node node);
    void setName(Node node, String name);

    Container createDefaultRoot();
    Container getRoot();
    ReferenceManager createDefaultReferenceManager();
    boolean reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<? extends N> srcChildren);

    <T> Set<T> getPreset(N node, Class<T> type);
    <T> Set<T> getPostset(N node, Class<T> type);
    <T> Set<T> getPreset(N node, Class<T> type, Func<N, Boolean> through);
    <T> Set<T> getPostset(N node, Class<T> type, Func<N, Boolean> through);

    void add(N node);
    void remove(N node);
    void remove(Collection<? extends N> nodes);

    default void beforeSerialisation() {
    }

    default void afterDeserialisation() {
    }

}
