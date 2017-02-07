package org.workcraft.plugins.dtd;

import java.awt.BasicStroke;
import java.awt.Stroke;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.dtd.Signal.State;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualLevelConnection extends VisualConnection {

    public VisualLevelConnection() {
        this(null, null, null);
    }

    public VisualLevelConnection(MathConnection mathConnection) {
        this(mathConnection, null, null);
    }

    public VisualLevelConnection(MathConnection mathConnection, VisualNode first, VisualNode second) {
        super(mathConnection, first, second);
        removePropertyDeclarationByName(PROPERTY_ARROW_LENGTH);
        removePropertyDeclarationByName(PROPERTY_ARROW_WIDTH);
        removePropertyDeclarationByName(PROPERTY_CONNECTION_TYPE);
        removePropertyDeclarationByName(PROPERTY_LINE_WIDTH);
        removePropertyDeclarationByName(PROPERTY_SCALE_MODE);
    }

    @Override
    public double getLineWidth() {
        return 0.5 * CommonVisualSettings.getStrokeWidth();
    }

    @Override
    public boolean hasArrow() {
        return false;
    }

    @Override
    public ScaleMode getScaleMode() {
        return ScaleMode.LOCK_RELATIVELY;
    }

    @Override
    public Stroke getStroke() {
        VisualNode fromNode = getFirst();
        State state = null;
        if (fromNode instanceof VisualSignal) {
            VisualSignal fromSignal = (VisualSignal) fromNode;
            state = fromSignal.getInitialState();
        } else if (fromNode instanceof VisualTransition) {
            VisualTransition fromTransition = (VisualTransition) fromNode;
            state = fromTransition.getReferencedTransition().getNextState();
        }
        if (state == State.UNSTABLE) {
            float[] pattern = {0.1f, 0.1f};
            return new BasicStroke((float) getLineWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f);
        }
        return new BasicStroke((float) getLineWidth());
    }

}
