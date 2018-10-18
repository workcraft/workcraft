package org.workcraft.plugins.circuit;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;

@IdentifierPrefix(value = "joint", isInternal = true)
@VisualClass(org.workcraft.plugins.circuit.VisualJoint.class)
public class Joint extends MathNode {

}
