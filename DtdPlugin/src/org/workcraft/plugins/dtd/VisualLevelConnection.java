package org.workcraft.plugins.dtd;

import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.shared.CommonVisualSettings;

import java.awt.*;

public class VisualLevelConnection extends VisualConnection {

    public static final String PROPERTY_STATE = "State";

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

        addPropertyDeclaration(new PropertyDeclaration<VisualLevelConnection, Signal.State>(
                this, PROPERTY_STATE, Signal.State.class, true, true, false) {
            protected void setter(VisualLevelConnection level, Signal.State state) {
                VisualEvent event = (VisualEvent) getFirst();
                if (event instanceof VisualEntryEvent) {
                    VisualSignal signal = event.getVisualSignal();
                    signal.setInitialState(state);
                } else if (event instanceof VisualTransitionEvent) {
                    VisualTransitionEvent transition = (VisualTransitionEvent) event;
                    transition.setDirection(DtdUtils.getPreviousDirection(state).reverse());
                }
            }
            protected Signal.State getter(VisualLevelConnection level) {
                VisualEvent fromEvent = (VisualEvent) level.getFirst();
                return DtdUtils.getNextState(fromEvent.getReferencedSignalEvent());
            }
        });
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
        Signal.State state = getState();
        float width = 0.5f * (float) CommonVisualSettings.getStrokeWidth();
        if (state == Signal.State.UNSTABLE) {
            float[] pattern = {0.1f, 0.1f};
            return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f);
        }
        return new BasicStroke(width);
    }

    private Signal.State getState() {
        VisualNode node = getFirst();
        if (node instanceof VisualEvent) {
            VisualEvent event = (VisualEvent) node;
            return DtdUtils.getNextState(event.getReferencedSignalEvent());
        }
        return null;
    }

}
