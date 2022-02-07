package org.workcraft.dom;

import org.workcraft.Info;
import org.workcraft.Version;
import org.workcraft.dom.math.MathModel;

public interface ModelDescriptor {

    enum Rating { TRIVIAL, EXPERIMENTAL, NORMAL, ESSENTIAL };

    String getDisplayName();
    MathModel createMathModel();
    VisualModelDescriptor getVisualModelDescriptor();

    // By default, assume NORMAL rating of the model
    default Rating getRating() {
        return Rating.NORMAL;
    }

    // By default, assume backward compatibility of file format with the same Major/Minor versions and any Revision
    default Version getCompatibilityVersion() {
        Version version = Info.getVersion();
        return new Version(version.major, version.minor, 0, Version.Status.RELEASE);
    }

}
