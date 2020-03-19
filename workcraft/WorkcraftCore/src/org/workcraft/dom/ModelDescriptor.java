package org.workcraft.dom;

import org.workcraft.dom.math.MathModel;

public interface ModelDescriptor {

    enum Rating { TRIVIAL, EXPERIMENTAL, NORMAL, ESSENTIAL };

    String getDisplayName();
    MathModel createMathModel();
    VisualModelDescriptor getVisualModelDescriptor();

    default Rating getRating() {
        return Rating.NORMAL;
    }

}
