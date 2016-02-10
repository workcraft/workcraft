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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.gui.workspace.Path;

public class MountTree {
    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof MountTree))
            return false;
        MountTree other = (MountTree)obj;
        return other.mountTo.equals(mountTo);
    }

    public MountTree(File defaultPath, Map<Path<String>, File> mounts, Path<String> workspacePath) {
        path = workspacePath;

        if(defaultPath == null)
            throw new NullPointerException("defaultPath");
        File tmpMountTo = defaultPath;

        subDirs = new HashMap<String, MountTree>();

        if(mounts != null) {
            Map<String, Map<Path<String>, File>> perSubDir = new HashMap<String, Map<Path<String>, File>>();

            for(Path<String> s : mounts.keySet()) {
                File file = mounts.get(s);

                if(file == null)
                    throw new NullPointerException("file");

                List<String> pathItems = Path.getPath(s);

                if(pathItems.size() == 0)
                    tmpMountTo = file;
                else {
                    String folder = pathItems.get(0);
                    if(folder.length() == 0)
                        throw new RuntimeException("invalid mount path");
                    pathItems.remove(0);
                    Path<String> suffix = Path.create(pathItems);

                    if(!perSubDir.containsKey(folder))
                        perSubDir.put(folder, new HashMap<Path<String>, File>());
                    perSubDir.get(folder).put(suffix, file);
                }
            }

            for(String prefix : perSubDir.keySet())
                subDirs.put(prefix, new MountTree(new File(tmpMountTo, prefix), perSubDir.get(prefix), Path.append(path, prefix)));
        }
        mountTo = tmpMountTo;
    }

    public final File mountTo;
    public final Map<String, MountTree> subDirs;
    public final Path<String> path;

    public MountTree getSubtree(String name) {
        if(name.length() == 0)
            return this;

        MountTree res = subDirs.get(name);
        if(res!=null)
            return res;
        return new MountTree(new File(mountTo, name), null, Path.append(path, name));
    }
}
