package org.workcraft.plugins.cpog.expressions;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualVertex;

public class CpogConnector implements CpogVisitor< Set<VisualVertex> > {

    VisualCPOG cpog = null;

    public CpogConnector(VisualCPOG cpog) {
        this.cpog = cpog;
    }

    @Override
    public Set<VisualVertex> visit(Overlay node) {
        Set<VisualVertex> left = node.getX().accept(this);
        Set<VisualVertex> right = node.getY().accept(this);
        left.addAll(right);
        return left;
    }

    @Override
    public Set<VisualVertex> visit(Sequence node) {
        Set<VisualVertex> left = node.getX().accept(this);
        Set<VisualVertex> right = node.getY().accept(this);

        for(VisualVertex l : left)
            for(VisualVertex r : right) {
                // TODO: handle self-loops properly
                cpog.connect(l, r);
            }

        left.addAll(right);
        return left;
    }

    @Override
    public Set<VisualVertex> visit(CpogFormulaVariable variable) {
        Set<VisualVertex> singleton = new HashSet<VisualVertex>();

        singleton.add((VisualVertex)variable);

        return singleton;
    }

}
