package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.plugins.son.ONGroup;

public interface StructuralVerification {

	public void task(Collection<ONGroup> selectedGroups);

	public Collection<String> getRelationErrors();

	public Collection<ArrayList<String>> getCycleErrors();

	public Collection<String> getGroupErrors();

	public boolean hasErr();

	public int getErrNumber();

	public int getWarningNumber();

}
