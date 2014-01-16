package org.workcraft.gui.trees;

import org.workcraft.gui.workspace.Path;

public class TreeListenerAdapter<Node> implements TreeListener<Node> {
	private final TreeListener<Node> chain;

	public TreeListenerAdapter(TreeListener<Node> chain) {
		this.chain = chain;
	}

	@Override
	public void added(Path<Node> path) {
		chain.added(path);
	}

	@Override
	public void changed(Path<Node> path) {
		chain.changed(path);
	}

	@Override
	public void removed(Path<Node> path) {
		chain.removed(path);
	}

	@Override
	public void restructured(Path<Node> path) {
		chain.restructured(path);
	}
}
