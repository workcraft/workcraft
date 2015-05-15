package org.workcraft.plugins.son.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.workcraft.plugins.son.ONGroup;
import org.workcraft.plugins.son.Phase;
import org.workcraft.plugins.son.elements.Condition;

public interface StructuralVerification {

	public void task(Collection<ONGroup> selectedGroups);

	public Collection<String> getRelationErrors();

	public Collection<ArrayList<String>> getCycleErrors();

	public Collection<String> getGroupErrors();

	public int getErrNumber();

	public int getWarningNumber();

	public Map<Condition, Collection<Phase>> getAllPhases();

}
