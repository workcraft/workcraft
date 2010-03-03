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
package org.workcraft.gui.workspace;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileSystemTreeSource implements TreeSource<File>
{
	public FileSystemTreeSource(File root)
	{
		this.root = root;
	}

	private final File root;

	@Override
	public void addListener(TreeListener<File> listener) {
		//throw new org.workcraft.exceptions.NotImplementedException();
	}

	@Override
	public List<File> getChildren(File node) {
		if(!node.isDirectory())
			throw new RuntimeException("can't enumerate a file's children!");
		return Arrays.asList(node.listFiles());
	}

	@Override
	public File getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(File node) {
		return !node.isDirectory();
	}

	@Override
	public void removeListener(TreeListener<File> listener) {
		//throw new org.workcraft.exceptions.NotImplementedException();
	}
}
