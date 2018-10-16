package org.workcraft.gui.graph.generators;

import org.workcraft.NodeFactory;
import org.workcraft.annotations.Annotations;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.util.GUI;

import javax.swing.*;

public class DefaultNodeGenerator extends AbstractNodeGenerator {

    private final Class<?> cls;
    private final String displayName;
    private final int hk;
    private final Icon icon;

    public DefaultNodeGenerator(Class<?> cls) {
        this.cls = cls;
        Class<?> vcls = Annotations.getVisualClass(cls);

        displayName = (vcls == null) ? null : Annotations.getDisplayName(vcls);
        hk = (vcls == null) ? null : Annotations.getHotKeyCode(vcls);

        String iconPath = (vcls == null) ? null : Annotations.getSVGIconPath(vcls);
        icon = (iconPath == null) ? null : GUI.createIconFromSVG(iconPath);
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public MathNode createMathNode() throws NodeCreationException {
        if (MathNode.class.isAssignableFrom(cls)) {
            return NodeFactory.createNode(cls.asSubclass(MathNode.class));
        }
        return null;
    }

    @Override
    public VisualNode createVisualNode(MathNode mathNode) throws NodeCreationException {
        return NodeFactory.createVisualComponent(mathNode);
    }

    @Override
    public int getHotKeyCode() {
        return hk;
    }

    @Override
    public String getLabel() {
        return displayName;
    }

}
