package org.workcraft.plugins.stg;

import org.workcraft.dom.math.MathConnection;

public interface ConnectionResult {
    MathConnection getSimpleResult();
    MathConnection getCon1();
    MathConnection getCon2();
    STGPlace getImplicitPlace();
}
