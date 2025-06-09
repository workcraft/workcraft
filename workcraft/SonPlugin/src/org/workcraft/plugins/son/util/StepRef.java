package org.workcraft.plugins.son.util;

public class StepRef extends NodesRef {

    public boolean isReverse() {
        return  !">".equals(iterator().next());
    }

}
