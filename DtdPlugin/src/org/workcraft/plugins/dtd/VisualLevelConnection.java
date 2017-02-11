package org.workcraft.plugins.dtd;

import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.dtd.Signal.State;
import org.workcraft.plugins.shared.CommonVisualSettings;

public class VisualLevelConnection extends VisualConnection {

    class OffsetStroke implements Stroke {
        private final Stroke stroke;
        private final float offset;

        OffsetStroke(Stroke stroke, float offset) {
            this.stroke = stroke;
            this.offset = offset;
        }

        @Override
        public Shape createStrokedShape(Shape shape) {
            AffineTransform at = AffineTransform.getTranslateInstance(0.0, offset);
            return stroke.createStrokedShape(at.createTransformedShape(shape));
        }
    }

    class CompositeStroke implements Stroke {
        private final Stroke stroke1;
        private final Stroke stroke2;

        CompositeStroke(Stroke stroke1, Stroke stroke2) {
            this.stroke1 = stroke1;
            this.stroke2 = stroke2;
        }

        @Override
        public Shape createStrokedShape(Shape shape) {
            return stroke2.createStrokedShape(stroke1.createStrokedShape(shape));
        }
    }

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
        State state = getBeforeState();
        float width = 0.5f * (float) CommonVisualSettings.getStrokeWidth();
        switch (state) {
        case UNSTABLE:
            float[] pattern = {0.1f, 0.1f};
            return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f);
        default:
            return new BasicStroke(width);
        }
    }

    private State getBeforeState() {
        VisualNode fromNode = getFirst();
        State state = null;
        if (fromNode instanceof VisualSignal) {
            VisualSignal fromSignal = (VisualSignal) fromNode;
            state = fromSignal.getInitialState();
        } else if (fromNode instanceof VisualTransition) {
            VisualTransition fromTransition = (VisualTransition) fromNode;
            state = fromTransition.getReferencedTransition().getNextState();
        }
        return state;
    }

}
