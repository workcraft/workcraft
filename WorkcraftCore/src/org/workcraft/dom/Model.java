/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.dom;

import java.util.Collection;
import java.util.Set;

import org.workcraft.dom.hierarchy.NamespaceProvider;
import org.workcraft.dom.references.ReferenceManager;
import org.workcraft.gui.propertyeditor.ModelProperties;

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

    /// methods for work with referenced component names
    ReferenceManager getReferenceManager();
    Node getNodeByReference(NamespaceProvider provider, String reference);
    String getNodeReference(NamespaceProvider provider, Node node);
    Node getNodeByReference(String reference);
    String getNodeReference(Node node);
    String getName(Node node);
    void setName(Node node, String name);

    void reparent(Container dstContainer, Model srcModel, Container srcRoot, Collection<Node> srcChildren);

    Container getRoot();
    <R> Set<R> getPreset(Node node, Class<R> type);
    <R> Set<R> getPostset(Node node, Class<R> type);

    void add(Node node);
    void remove(Node node);
    void remove(Collection<Node> nodes);

    ModelProperties getProperties(Node node);
}
