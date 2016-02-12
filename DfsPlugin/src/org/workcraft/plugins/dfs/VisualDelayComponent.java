package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.Stylable;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;

public abstract class VisualDelayComponent extends VisualComponent {

    public VisualDelayComponent(MathDelayNode ref) {
        super(ref);
        addPropertyDeclarations();
    }

    public MathDelayNode getReferencedDelayComponent() {
        return (MathDelayNode) getReferencedComponent();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<VisualDelayComponent, Double>(
                this, MathDelayNode.PROPERTY_DELAY, Double.class, true, true, true) {
            public void setter(VisualDelayComponent object, Double value) {
                object.getReferencedDelayComponent().setDelay(value);
            }
            public Double getter(VisualDelayComponent object) {
                return object.getReferencedDelayComponent().getDelay();
            }
        });
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualDelayComponent) {
            MathDelayNode srcDelay = ((VisualDelayComponent) src).getReferencedDelayComponent();
            getReferencedDelayComponent().setDelay(srcDelay.getDelay());
        }
    }

}
