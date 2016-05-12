package org.workcraft.plugins.son.util;

@SuppressWarnings("serial")
public class StepRef extends NodesRef {

    public boolean isReverse() {
        if (this.iterator().next() == ">") {
            return false;
        } else {
            return true;
        }
    }
}
