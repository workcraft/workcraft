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
package org.workcraft.gui.workspace;

import java.util.ArrayList;
import java.util.List;

class EmptyPath<Node> extends Path<Node> {
    static EmptyPath<?> instance = new EmptyPath<Object>();
    @SuppressWarnings("unchecked")
    public static <Node> EmptyPath<Node> instance() {
        return (EmptyPath<Node>) instance;
    }

    @Override
    public Node getNode() {
        throw new org.workcraft.exceptions.NotSupportedException();
    }

    @Override
    public Path<Node> getParent() {
        throw new org.workcraft.exceptions.NotSupportedException();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }
}

class RootPath<Node> extends Path<Node> {
    private final Node root;

    RootPath(Node root) {
        this.root = root;
    }

    @Override
    public Node getNode() {
        return root;
    }

    @Override
    public Path<Node> getParent() {
        return EmptyPath.instance();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}

class NormalPath<Node> extends Path<Node> {
    private final Path<Node> parent;
    private final Node node;

    NormalPath(Path<Node> parent, Node node) {
        if (node == null || parent == null) {
            throw new NullPointerException();
        }
        this.parent = parent;
        this.node = node;
    }

    public Path<Node> getParent() {
        return parent;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}

public abstract class Path<Node> {
    public abstract boolean isEmpty();
    public abstract Node getNode();
    public abstract Path<Node> getParent();

    public static <Node> List<Node> getPath(Path<Node> path) {
        ArrayList<Node> list = new ArrayList<Node>();
        Path<Node> p = path;
        while ((p != null) && !p.isEmpty()) {
            list.add(p.getNode());
            p = p.getParent();
        }
        int n = list.size();
        List<Node> result = new ArrayList<Node>();
        for (int i = 0; i < n; i++) {
            result.add(list.get(n - 1 - i));
        }
        return result;
    }

    public static <Node> Path<Node> root(Node root) {
        return new RootPath<Node>(root);
    }

    public static <Node> Path<Node> append(Path<Node> path, Node suffix) {
        if (path.isEmpty()) {
            return root(suffix);
        } else {
            return new NormalPath<Node>(path, suffix);
        }
    }

    private static boolean equal(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Path<?>)) {
            return false;
        }
        Path<?> other = (Path<?>) obj;
        if (isEmpty()) {
            return other.isEmpty();
        }

        return !other.isEmpty() && equal(getNode(), other.getNode()) && equal(getParent(), other.getParent());
    }

    @Override
    public int hashCode() {
        return isEmpty() ? 5675678 : getParent().hashCode() * 10241 + getNode().hashCode();
    }

    public static <Node> Path<Node> combine(Path<Node> left, Path<Node> right) {
        if (left.isEmpty()) {
            return right;
        }
        if (right.isEmpty()) {
            return left;
        }
        Path<Node> current = left;
        for (Node node : getPath(right)) {
            current = new NormalPath<Node>(current, node);
        }
        return current;
    }

    public static Path<String> fromString(String relative) {
        if (relative.length() == 0) {
            return empty();
        }
        return create(relative.split("/"));
    }

    public static <Node> Path<Node> create(List<Node> values) {
        Path<Node> result = EmptyPath.instance();
        for (Node val : values) {
            result = append(result, val);
        }
        return result;
    }

    public static <Node> Path<Node> create(Node[] values) {
        Path<Node> result = empty();
        for (Node val : values) {
            result = append(result, val);
        }
        return result;
    }

    public static <Node> Path<Node> empty() {
        return EmptyPath.instance();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        final Path<Node> parent = getParent();
        final Node node = getNode();
        if (parent.isEmpty()) {
            return node.toString();
        }
        return parent.toString() + "/" + node.toString();
    }
}
