package org.workcraft.plugins.son.util;

@SuppressWarnings("serial")
public class StepRef extends NodesRef {

    public boolean isReverse() {
        return  !">".equals(iterator().next());
    }

}
