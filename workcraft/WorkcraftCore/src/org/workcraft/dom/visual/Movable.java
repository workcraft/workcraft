package org.workcraft.dom.visual;

import java.awt.geom.AffineTransform;

import org.workcraft.dom.Node;

public interface Movable extends Node {
    AffineTransform getTransform();
    void applyTransform(AffineTransform transform);
    void copyPosition(Movable src);
}
