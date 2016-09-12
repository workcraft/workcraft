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

package org.workcraft.plugins.xmas;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.AbstractMathModel;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathGroup;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.references.HierarchicalUniqueNameReferenceManager;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.xmas.components.CreditComponent;
import org.workcraft.plugins.xmas.components.ForkComponent;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.JoinComponent;
import org.workcraft.plugins.xmas.components.MergeComponent;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SinkComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;
import org.workcraft.plugins.xmas.components.SyncComponent;
import org.workcraft.plugins.xmas.components.XmasConnection;
import org.workcraft.plugins.xmas.components.XmasContact;
import org.workcraft.serialisation.References;
import org.workcraft.util.Hierarchy;

public class Xmas extends AbstractMathModel {

    public Xmas() {
        this(new MathGroup(), (References) null);
    }

    public Xmas(Container root, References refs) {
        super(root, new HierarchicalUniqueNameReferenceManager(refs) {
            @Override
            public String getPrefix(Node node) {
                if (node instanceof SourceComponent) return "src";
                if (node instanceof FunctionComponent) return "fun";
                if (node instanceof QueueComponent) return "qu";
                if (node instanceof ForkComponent) return "frk";
                if (node instanceof JoinComponent) return "jn";
                if (node instanceof SwitchComponent) return "sw";
                if (node instanceof MergeComponent) return "mrg";
                if (node instanceof SinkComponent) return "snk";
                if (node instanceof SyncComponent) return "sync";
                if (node instanceof XmasContact) return "contact";
                if (node instanceof XmasConnection) return "con";
                return super.getPrefix(node);
            }
        });
    }

    public MathConnection connect(Node first, Node second) throws InvalidConnectionException {
        MathConnection con = new MathConnection((MathNode) first, (MathNode) second);
        Hierarchy.getNearestContainer(first, second).add(con);
        return con;
    }

    public Collection<Node> getNodes() {
        ArrayList<Node> result = new ArrayList<>();
        for (Node node : Hierarchy.getDescendantsOfType(getRoot(), Node.class)) {
            if (node instanceof SourceComponent) {
                result.add(node);
            }
            if (node instanceof FunctionComponent) {
                result.add(node);
            }
            if (node instanceof QueueComponent) {
                result.add(node);
            }
            if (node instanceof ForkComponent) {
                result.add(node);
            }
            if (node instanceof JoinComponent) {
                result.add(node);
            }
            if (node instanceof SwitchComponent) {
                result.add(node);
            }
            if (node instanceof MergeComponent) {
                result.add(node);
            }
            if (node instanceof SinkComponent) {
                result.add(node);
            }
        }
        return result;
    }

    public String getType(Node node) {
        String result = null;
        if (node instanceof SourceComponent) result = "source";
        if (node instanceof FunctionComponent) result = "function";
        if (node instanceof QueueComponent) result = "queue";
        if (node instanceof ForkComponent) result = "fork";
        if (node instanceof JoinComponent) result = "join";
        if (node instanceof SwitchComponent) result = "switch";
        if (node instanceof MergeComponent) result = "merge";
        if (node instanceof SwitchComponent) result = "switch";
        if (node instanceof SinkComponent) result = "sink";
        if (node instanceof CreditComponent) result = "credit";
        if (node instanceof SyncComponent) result = "sync";
        return result;
    }

    public Collection<SourceComponent> getSourceComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), SourceComponent.class);
    }

    public Collection<FunctionComponent> getFunctionComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), FunctionComponent.class);
    }

    public Collection<SwitchComponent> getSwitchComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), SwitchComponent.class);
    }

    public Collection<SinkComponent> getSinkComponents() {
        return Hierarchy.getDescendantsOfType(getRoot(), SinkComponent.class);
    }

}
