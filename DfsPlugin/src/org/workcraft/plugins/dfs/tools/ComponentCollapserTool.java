package org.workcraft.plugins.dfs.tools;

import java.util.HashSet;

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
		HashSet<Node> selectedComponents = new HashSet<Node>();
		for (Node node: dfs.getSelection()) {
			if (node instanceof VisualComponent) {
				selectedComponents.add(node);
			}
		}
		HashSet<Node> preset = new HashSet<Node>();
		HashSet<Node> postset = new HashSet<Node>();
		for (Node cur: selectedComponents) {
			for (Node pred: dfs.getPreset(cur)) {
				if (!selectedComponents.contains(pred)) {
					preset.add(pred);
				}
			}
			for (Node succ: dfs.getPostset(cur)) {
				if (!selectedComponents.contains(succ)) {
					postset.add(succ);
				}
			}
		}
		for (Node pred: preset) {
			for (Node succ: postset) {
				try {
					dfs.connect(pred, succ);
				} catch (InvalidConnectionException e) {
					e.printStackTrace();
				}
			}
		}
		dfs.select(selectedComponents);
		dfs.deleteSelection();
	}

}
