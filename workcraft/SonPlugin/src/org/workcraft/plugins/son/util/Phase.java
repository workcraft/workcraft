package org.workcraft.plugins.son.util;

import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;

import java.util.HashSet;

public class Phase extends HashSet<Condition> {

    public String toString(SON net) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Condition node : this) {
            if (!first) {
                result.append(' ');
                result.append(',');
                result.append(net.getNodeReference(node));
            } else {
                result.append(' ');
                result.append(net.getNodeReference(node));
                first = false;
            }
        }
        return result.toString();
    }

}
