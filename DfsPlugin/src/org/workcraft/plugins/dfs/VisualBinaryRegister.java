package org.workcraft.plugins.dfs;

import org.workcraft.dom.visual.Stylable;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.plugins.dfs.BinaryRegister.Marking;

public abstract class VisualBinaryRegister extends VisualAbstractRegister {

    public VisualBinaryRegister(BinaryRegister register) {
        super(register);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Marking.class, BinaryRegister.PROPERTY_MARKING,
                value -> getReferencedComponent().setMarking(value),
                () -> getReferencedComponent().getMarking())
                .setCombinable().setTemplatable());
    }

    @Override
    public BinaryRegister getReferencedComponent() {
        return (BinaryRegister) super.getReferencedComponent();
    }

    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualBinaryRegister) {
            BinaryRegister srcBinaryRegister = ((VisualBinaryRegister) src).getReferencedComponent();
            getReferencedComponent().setMarking(srcBinaryRegister.getMarking());
        }
    }

}
