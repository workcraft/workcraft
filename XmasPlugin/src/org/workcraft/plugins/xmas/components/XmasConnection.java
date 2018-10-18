package org.workcraft.plugins.xmas.components;

import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.dom.math.MathConnection;
import org.workcraft.dom.math.MathNode;

@IdentifierPrefix(value = "c", isInternal = true)
public class XmasConnection extends MathConnection {

    public XmasConnection() {
    }

    public XmasConnection(MathNode first, MathNode second) {
        super(first, second);
    }

}
