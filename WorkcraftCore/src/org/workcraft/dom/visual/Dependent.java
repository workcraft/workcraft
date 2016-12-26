package org.workcraft.dom.visual;

import java.util.Collection;

import org.workcraft.dom.math.MathNode;

public interface Dependent {
    Collection<MathNode> getMathReferences();
}
