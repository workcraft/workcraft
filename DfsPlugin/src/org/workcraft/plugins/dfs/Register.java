package org.workcraft.plugins.dfs;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.observation.PropertyChangedEvent;

@IdentifierPrefix("r")
@VisualClass(org.workcraft.plugins.dfs.VisualRegister.class)
public class Register extends MathDelayNode {
    public static final String PROPERTY_MARKED = "Marked";

    private boolean marked = false;

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean value) {
        if (marked != value) {
            marked = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_MARKED));
        }
    }

}
