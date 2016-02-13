package org.workcraft.plugins.son.algorithm;

import java.util.ArrayList;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.SON;

@SuppressWarnings("serial")
public class Path extends ArrayList<Node> {

    public String toString(SON net) {
        StringBuffer result = new StringBuffer("");

        boolean first = true;
        for (Node node : this) {
            if (!first) {
                result.append(',');
                result.append(' ' + net.getNodeReference(node));
            }else{
                result.append(' ');
                result.append('[');
                result.append(net.getNodeReference(node));
                first = false;
            }
        }
        if(!this.isEmpty())
            result.append(']');
        return result.toString();
    }
}
