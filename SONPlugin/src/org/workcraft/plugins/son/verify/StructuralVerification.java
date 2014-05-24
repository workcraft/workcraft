package org.workcraft.plugins.son.verify;

import java.util.Collection;

import org.workcraft.plugins.son.ONGroup;

public interface StructuralVerification {

	public void task(Collection<ONGroup> selectedGroups);

	public void errNodesHighlight();

	public boolean hasErr();

	public int getErrNumber();

	public int getWarningNumber();

}
