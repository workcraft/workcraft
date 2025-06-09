package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

import java.awt.*;

public abstract class VisualAbstractRegister extends VisualDelayComponent {

    public static final String PROPERTY_TOKEN_COLOR = "Token color";
    protected Color tokenColor = VisualCommonSettings.getBorderColor();

    public VisualAbstractRegister(MathDelayNode ref) {
        super(ref);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Color.class, PROPERTY_TOKEN_COLOR,
                this::setTokenColor, this::getTokenColor).setCombinable().setTemplatable());
    }

    public Color getTokenColor() {
        return tokenColor;
    }

    public void setTokenColor(Color value) {
        if (!tokenColor.equals(value)) {
            tokenColor = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TOKEN_COLOR));
        }
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualAbstractRegister srcComponent) {
            setTokenColor(srcComponent.getTokenColor());
        }
    }

}
