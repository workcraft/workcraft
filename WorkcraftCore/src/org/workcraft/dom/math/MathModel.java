package org.workcraft.dom.math;

import org.workcraft.dom.Container;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.InvalidConnectionException;

import java.util.Collection;

public interface MathModel extends Model {
    <T extends MathNode> T createNode(String name, Container container, Class<T> type);
    <T extends MathNode> T createNodeWithHierarchy(String ref, Container container, Class<T> type);
    <T extends MathNode> T createMergedNode(Collection<MathNode> srcNodes, Container container, Class<T> type);

    void validateConnection(MathNode first, MathNode second) throws InvalidConnectionException;
    MathConnection connect(MathNode first, MathNode second) throws InvalidConnectionException;
}
