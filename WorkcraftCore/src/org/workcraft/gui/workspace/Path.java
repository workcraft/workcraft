package org.workcraft.gui.workspace;

import java.util.ArrayList;
import java.util.List;

public abstract class Path<T> {

    public abstract boolean isEmpty();
    public abstract T getNode();
    public abstract Path<T> getParent();

    public static <T> List<T> getPath(Path<T> path) {
        ArrayList<T> list = new ArrayList<>();
        Path<T> p = path;
        while ((p != null) && !p.isEmpty()) {
            list.add(p.getNode());
            p = p.getParent();
        }
        int n = list.size();
        List<T> result = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            result.add(list.get(n - 1 - i));
        }
        return result;
    }

    public static <T> Path<T> root(T root) {
        return new RootPath<>(root);
    }

    public static <Node> Path<Node> append(Path<Node> path, Node suffix) {
        if (path.isEmpty()) {
            return root(suffix);
        } else {
            return new NormalPath<>(path, suffix);
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

    public static <T> Path<T> combine(Path<T> left, Path<T> right) {
        if (left.isEmpty()) {
            return right;
        }
        if (right.isEmpty()) {
            return left;
        }
        Path<T> current = left;
        for (T node : getPath(right)) {
            current = new NormalPath<>(current, node);
        }
        return current;
    }

    public static Path<String> fromString(String relative) {
        if (relative.length() == 0) {
            return empty();
        }
        return create(relative.split("/"));
    }

    public static <T> Path<T> create(List<T> values) {
        Path<T> result = EmptyPath.getInstance();
        for (T val : values) {
            result = append(result, val);
        }
        return result;
    }

    public static <T> Path<T> create(T[] values) {
        Path<T> result = empty();
        for (T val : values) {
            result = append(result, val);
        }
        return result;
    }

    public static <T> Path<T> empty() {
        return EmptyPath.getInstance();
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "";
        }
        final Path<T> parent = getParent();
        final T node = getNode();
        if (parent.isEmpty()) {
            return node.toString();
        }
        return parent.toString() + "/" + node.toString();
    }

    private static class EmptyPath<T> extends Path<T> {
        private static EmptyPath<?> instance = new EmptyPath<>();
        @SuppressWarnings("unchecked")
        public static <T> EmptyPath<T> getInstance() {
            return (EmptyPath<T>) instance;
        }

        @Override
        public T getNode() {
            throw new org.workcraft.exceptions.NotSupportedException();
        }

        @Override
        public Path<T> getParent() {
            throw new org.workcraft.exceptions.NotSupportedException();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    private static class RootPath<T> extends Path<T> {
        private final T root;

        RootPath(T root) {
            this.root = root;
        }

        @Override
        public T getNode() {
            return root;
        }

        @Override
        public Path<T> getParent() {
            return EmptyPath.getInstance();
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private static class NormalPath<T> extends Path<T> {
        private final Path<T> parent;
        private final T node;

        NormalPath(Path<T> parent, T node) {
            this.parent = parent;
            this.node = node;
        }

        @Override
        public Path<T> getParent() {
            return parent;
        }

        @Override
        public T getNode() {
            return node;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

}
