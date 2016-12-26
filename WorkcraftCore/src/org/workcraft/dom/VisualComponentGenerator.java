package org.workcraft.dom;

import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualComponent;

public interface VisualComponentGenerator {
    VisualComponent createComponent(MathNode component, Object ... constructorParameters);

}
