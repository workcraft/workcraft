/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
*
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.workcraft.workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.TreeListener;
import org.workcraft.gui.workspace.TreeSource;

public class WorkspaceTree implements TreeSource<Path<String>>
{
	private final class WorkspaceListenerWrapper implements
			WorkspaceListener {
		private final TreeListener<Path<String>> listener;

		private WorkspaceListenerWrapper(
				TreeListener<Path<String>> listener) {
			this.listener = listener;
		}

		@Override
		public void entryAdded(WorkspaceEntry we) {
			listener.restructured(Path.root(getRoot()));
		}

		@Override
		public void entryChanged(WorkspaceEntry we) {
			listener.restructured(Path.root(getRoot()));
		}

		@Override
		public void entryRemoved(WorkspaceEntry we) {
			listener.restructured(Path.root(getRoot()));
		}

		@Override
		public void modelLoaded(WorkspaceEntry we) {
			listener.restructured(Path.root(getRoot()));
		}

		@Override
		public void workspaceSaved() {
			listener.restructured(Path.root(getRoot()));
		}
	}

	Workspace workspace;

	public WorkspaceTree(Workspace workspace)
	{
		this.workspace = workspace;
	}

	@Override
	public void addListener(final TreeListener<Path<String>> listener) {
		workspace.addListener(wrap(listener));
	}

	Map<TreeListener<Path<String>>, WorkspaceListener> wrappers = new HashMap<TreeListener<Path<String>>, WorkspaceListener>();

	private WorkspaceListener wrap(TreeListener<Path<String>> listener) {
		WorkspaceListener res = wrappers.get(listener);
		if(res == null)
		{
			res = new WorkspaceListenerWrapper(listener);
			wrappers.put(listener, res);
		}
		return res;
	}

	@Override
	public List<Path<String>> getChildren(Path<String> node) {
		MountTree mount = workspace.getMountTree(node);

		Map<String, Path<String>> res = new TreeMap<String, Path<String>>();
		String[] list = mount.mountTo.list();
		if(list != null)
			for(String name : list)
				res.put(name, mount.getSubtree(name).path);
		for(String name : mount.subDirs.keySet())
		{
			if(!res.containsKey(name))
				res.put(name, mount.subDirs.get(name).path);
		}
		List<Path<String>> result = new ArrayList<Path<String>>(res.values());

		return result;
	}

	@Override
	public Path<String> getRoot() {
		return Path.empty();
	}

	@Override
	public boolean isLeaf(Path<String> node) {
		return isLeaf(workspace, node);
	}

	public static boolean isLeaf(Workspace workspace, Path<String> path)
	{
		MountTree mount = workspace.getMountTree(path);
		return mount.subDirs.size() == 0 && !mount.mountTo.isDirectory();
	}

	@Override
	public void removeListener(TreeListener<Path<String>> listener) {
		throw new NotSupportedException();//TODO
	}
}
