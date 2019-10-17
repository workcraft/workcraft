package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;

public abstract class VisualDelayComponent extends VisualComponent {

    public double size = VisualCommonSettings.getNodeSize();
    public double strokeWidth = VisualCommonSettings.getStrokeWidth();

    public VisualDelayComponent(MathDelayNode ref) {
        super(ref);
        addPropertyDeclarations();
    }

    @Override
    public MathDelayNode getReferencedComponent() {
        return (MathDelayNode) super.getReferencedComponent();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Double.class, MathDelayNode.PROPERTY_DELAY,
                (value) -> getReferencedComponent().setDelay(value),
                () -> getReferencedComponent().getDelay())
                .setCombinable().setTemplatable());
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualDelayComponent) {
            MathDelayNode srcDelay = ((VisualDelayComponent) src).getReferencedComponent();
            getReferencedComponent().setDelay(srcDelay.getDelay());
        }
    }

}
