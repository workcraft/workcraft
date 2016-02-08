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
import java.util.Collections;
import java.util.LinkedHashSet;

import org.workcraft.observation.ObservableHierarchy;

public class DefaultGroupImpl extends AbstractGroup implements ObservableHierarchy, Container {
    private Collection<Node> children = new LinkedHashSet<Node> ();

    public DefaultGroupImpl (Container groupRef) {
        super(groupRef);
    }

    public Collection<Node> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    @Override
    protected void addInternal(Node node) {
        children.add(node);
    }

    @Override
    protected void removeInternal(Node node) {
        children.remove(node);
    }

}
