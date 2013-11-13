package org.workcraft.plugins.dfs.tools;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.dfs.Dfs;
import org.workcraft.plugins.dfs.VisualDfs;
import org.workcraft.workspace.WorkspaceEntry;

public class ComponentCollapserTool implements Tool {
	private final Framework framework;

	public ComponentCollapserTool(Framework framework) {
		this.framework = framework;
	}

	@Override
	public String getDisplayName() {
		return "Collapse selected conmonents";
	}

	@Override
	public String getSection() {
		return "Transformstions";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		return we.getModelEntry().getMathModel() instanceof Dfs;
	}

	@Override
	public void run(WorkspaceEntry we) {
		final VisualDfs dfs = (VisualDfs)we.getModelEntry().getVisualModel();
		if (dfs.getSelection().size() > 0) {
			we.saveMemento();
			collapseSelection(dfs);
		}
	}

	private void collapseSelection(VisualDfs dfs) {
		for (Node cur: dfs.getSelection()) {
			if (cur instanceof VisualComponent) {
				for (Node pred: dfs.getPreset(cur)) {
					for (Node succ: dfs.getPostset(cur)) {
						try {
							dfs.connect(pred, succ);
						} catch (InvalidConnectionException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		dfs.deleteSelection();
	}

}
