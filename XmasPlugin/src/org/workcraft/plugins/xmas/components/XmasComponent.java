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

package org.workcraft.plugins.xmas.components;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.DefaultGroupImpl;
import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;
import org.workcraft.observation.HierarchyObserver;
import org.workcraft.observation.ObservableHierarchy;
import org.workcraft.plugins.xmas.components.XmasContact.IOType;
import org.workcraft.util.Hierarchy;

public class XmasComponent extends MathNode implements Container, ObservableHierarchy {

    DefaultGroupImpl groupImpl = new DefaultGroupImpl(this);

    private int gr = 0;
    private String gp = "0";

    public Node getParent() {
        return groupImpl.getParent();
    }

    public void setParent(Node parent) {
        groupImpl.setParent(parent);
    }

    public void addObserver(HierarchyObserver obs) {
        groupImpl.addObserver(obs);
    }

    public void removeObserver(HierarchyObserver obs) {
        groupImpl.removeObserver(obs);
    }

    public void removeAllObservers() {
        groupImpl.removeAllObservers();
    }

    @Override
    public void add(Node node) {
        groupImpl.add(node);
    }

    @Override
    public void add(Collection<Node> nodes) {
        groupImpl.add(nodes);
    }

    @Override
    public void remove(Node node) {
        groupImpl.remove(node);
    }

    @Override
    public void remove(Collection<Node> node) {
        groupImpl.remove(node);
    }

    @Override
    public void reparent(Collection<Node> nodes) {
        groupImpl.reparent(nodes);
    }

    @Override
    public void reparent(Collection<Node> nodes, Container newParent) {
        groupImpl.reparent(nodes, newParent);
    }

    @Override
    public Collection<Node> getChildren() {
        return groupImpl.getChildren();
    }

    public Collection<XmasContact> getContacts() {
        return Hierarchy.filterNodesByType(getChildren(), XmasContact.class);
    }

    public Collection<XmasContact> getInputs() {
        ArrayList<XmasContact> result = new ArrayList<XmasContact>();
        for (XmasContact c : getContacts()) {
            if (c.getIOType() == IOType.INPUT) {
                result.add(c);
            }
        }
        return result;
    }

    public Collection<XmasContact> getOutputs() {
        ArrayList<XmasContact> result = new ArrayList<XmasContact>();
        for (XmasContact c : getContacts()) {
            if (c.getIOType() == IOType.OUTPUT) {
                result.add(c);
            }
        }
        return result;
    }

    public void setGr(int gr) {
        this.gr = gr;
    }

    public int getGr() {
        return gr;
    }

    public void setGp(String gp) {
        this.gp = gp;
    }

    public String getGp() {
        return gp;
    }

}
