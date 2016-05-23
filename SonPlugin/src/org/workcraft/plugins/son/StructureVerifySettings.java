package org.workcraft.plugins.son;

import java.util.ArrayList;

public class StructureVerifySettings {

    private final boolean errNodesHighlight, outputBefore;
    private final ArrayList<ONGroup> selectedGroups;
    private final int type;

    public StructureVerifySettings(boolean errNodesHighlight, boolean outputBefore, ArrayList<ONGroup> selectedGroups, int type) {
        this.errNodesHighlight = errNodesHighlight;
        this.outputBefore = outputBefore;
        this.selectedGroups = selectedGroups;
        this.type = type;
    }

    public boolean getErrNodesHighlight() {
        return errNodesHighlight;
    }

    public boolean getOuputBefore() {
        return outputBefore;
    }

    public ArrayList<ONGroup> getSelectedGroups() {
        return selectedGroups;
    }

    public int getType() {
        return type;
    }
}
