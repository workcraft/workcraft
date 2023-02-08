package org.workcraft.dom;

import org.workcraft.Version;
import org.workcraft.dom.math.MathModel;

public interface ModelDescriptor {

    enum Rating { TRIVIAL, EXPERIMENTAL, NORMAL, ESSENTIAL }

    String getDisplayName();
    MathModel createMathModel();
    VisualModelDescriptor getVisualModelDescriptor();

    // By default, assume NORMAL rating of the model
    default Rating getRating() {
        return Rating.NORMAL;
    }

    // By default, assume backward compatibility of file format with v3.3.0
    default Version getCompatibilityVersion() {
        return new Version(3, 3, 0, Version.Status.RELEASE);
    }

}
