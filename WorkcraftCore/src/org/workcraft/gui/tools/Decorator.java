package org.workcraft.gui.tools;

import org.workcraft.dom.Node;

public interface Decorator {
    /**
     * Returns decoration to apply to this node and its children.
     * Overrides decoration applied by parents unless the returned decoration is null.
     * @param node
     * The node to be decorated
     * @return
     * Decoration to be applied
     */
    Decoration getDecoration(Node node);

    class Empty implements Decorator {

        public static final Empty INSTANCE = new Empty();

        @Override
        public Decoration getDecoration(Node node) {

            return null;
        }

    }
}
