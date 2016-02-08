package org.workcraft.plugins.son.util;

import java.util.HashSet;

import org.workcraft.plugins.son.SON;
import org.workcraft.plugins.son.elements.Condition;

@SuppressWarnings("serial")
public class Phase extends HashSet<Condition>{

    public String toString(SON net) {
        StringBuffer result = new StringBuffer("");

        boolean first = true;
        for (Condition node : this) {
            if (!first) {
                result.append(' ');
                result.append(',' + net.getNodeReference(node));
            }else{
                result.append(' ');
                result.append(net.getNodeReference(node));
                first = false;
            }
        }
        return result.toString();
    }

}
