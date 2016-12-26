package org.workcraft.dom.math;

import java.util.Collection;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;

public interface MathModel extends Model {
    <T extends MathNode> T createNode(String name, Container container, Class<T> type);
    <T extends MathNode> T createNode(Collection<MathNode> srcNodes, Container container, Class<T> type);
}
