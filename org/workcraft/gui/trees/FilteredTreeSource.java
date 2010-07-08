package org.workcraft.gui.trees;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.workcraft.gui.workspace.Path;
import org.workcraft.util.Func;

public class FilteredTreeSource<T> implements TreeSource<T> {
	private final TreeSource<T> source;
	private HashMap<T, Boolean> acceptableCache = new HashMap<T, Boolean>();
	private Func<T, Boolean> filter;
	private final LinkedList<TreeListener<T>> listeners = new LinkedList<TreeListener<T>>();

	public FilteredTreeSource (TreeSource<T> sourceTree, Func<T, Boolean> filter) {
		this.source = sourceTree;
		this.filter = filter;
	}

	public void setFilter(Func<T, Boolean> filter) {
		this.filter = filter;
		acceptableCache.clear();

		for (TreeListener<T> l : listeners)
			l.restructured(Path.root(getRoot()));
	}

	@Override
	public void addListener(TreeListener<T> listener) {
		listeners.add(listener);
	}

	private boolean isAcceptable(T node, int depth) {
		if (acceptableCache.containsKey(node))
			return acceptableCache.get(node);

		boolean result;

		if (isLeaf(node))
			result = filter.eval(node);
		else
			if (depth == 0)
				result = true;
			else
				result = hasAcceptableChildren(node, depth);

		acceptableCache.put(node, result);

		return result;
	}

	private boolean hasAcceptableChildren(T node, int depth) {
		for (T t : source.getChildren(node))
			if (isAcceptable(t, depth-1))
				return true;
		return false;
	}

	@Override
	public List<T> getChildren(T node) {

		LinkedList<T> result = new LinkedList<T>();
		for (T t : source.getChildren(node))
			if (isAcceptable(t, 10))
				result.add(t);

		return result;
	}

	@Override
	public T getRoot() {
		return source.getRoot();
	}

	@Override
	public boolean isLeaf(T node) {
		return source.isLeaf(node);
	}

	@Override
	public void removeListener(TreeListener<T> listener) {
		listeners.remove(listener);
	}

	@Override
	public Path<T> getPath(T node) {
		return source.getPath(node);
	}
}
