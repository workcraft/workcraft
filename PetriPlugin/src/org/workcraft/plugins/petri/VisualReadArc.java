package org.workcraft.plugins.petri;

import java.util.HashSet;
import java.util.Set;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.Undirected;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;

public class VisualReadArc extends VisualConnection implements Undirected {
    private MathConnection mathConsumingArc;
    private MathConnection mathProducingArc;

    public VisualReadArc() {
        this(null, null, null, null);
    }

    public VisualReadArc(VisualNode place, VisualNode transition,
            MathConnection mathConsumingArc, MathConnection mathProducingArc) {
        super(null, place, transition);
        this.mathConsumingArc = mathConsumingArc;
        this.mathProducingArc = mathProducingArc;
        removePropertyDeclarations();
    }

    private void removePropertyDeclarations() {
        removePropertyDeclarationByName(VisualConnection.PROPERTY_ARROW_LENGTH);
        removePropertyDeclarationByName(VisualConnection.PROPERTY_ARROW_WIDTH);
    }

    public MathConnection getMathConsumingArc() {
        return mathConsumingArc;
    }

    public MathConnection getMathProducingArc() {
        return mathProducingArc;
    }

    @Override
    public Set<MathNode> getMathReferences() {
        Set<MathNode> ret = new HashSet<>();
        ret.add(mathConsumingArc);
        ret.add(mathProducingArc);
        return ret;
    }

    public void setDependencies(MathConnection mathConsumingArc, MathConnection mathProducingArc) {
        this.mathConsumingArc = mathConsumingArc;
        this.mathProducingArc = mathProducingArc;
    }

    @Override
    public boolean hasArrow() {
        return false;
    }

}
