package org.workcraft.plugins.son.verify;

import java.util.ArrayList;
import java.util.Collection;

import org.workcraft.dom.Node;
import org.workcraft.plugins.son.ONGroup;

public interface StructuralVerification {

	public void task(Collection<ONGroup> selectedGroups);

	public void errNodesHighlight();

	public Collection<Node> getRelationErrors();

	public Collection<ArrayList<Node>> getCycleErrors();

	public Collection<ONGroup> getGroupErrors();

	public boolean hasErr();

	public int getErrNumber();

	public int getWarningNumber();

}
