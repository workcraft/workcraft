package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.plugins.son.ONGroup;

public interface StructuralVerification {

    void task(Collection<ONGroup> selectedGroups);

    Collection<String> getRelationErrors();

    Collection<ArrayList<String>> getCycleErrors();

    Collection<String> getGroupErrors();

    int getErrNumber();

    int getWarningNumber();
}
