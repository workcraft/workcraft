package org.workcraft.plugins.dfs;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@VisualClass(org.workcraft.plugins.dfs.VisualRegister.class)
public class Register extends MathDelayNode {
    public static final String PROPERTY_MARKED = "Marked";

    private boolean marked = false;

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
        sendNotification(new PropertyChangedEvent(this, PROPERTY_MARKED));
    }

}
