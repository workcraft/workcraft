package org.workcraft.dom.visual;

import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.dom.math.MathNode;

public interface DependentNode extends Node {
	public Collection<MathNode> getMathReferences();
}
