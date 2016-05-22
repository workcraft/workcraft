package org.workcraft.plugins.son.exception;

import org.workcraft.dom.Node;

public class UnboundedException extends Exception {

    private static final long serialVersionUID = 1L;
    private final Node node;

    public UnboundedException(String msg, Node node) {
        super("Occurrence net is unsafe, marking " + msg + " twice.");
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
