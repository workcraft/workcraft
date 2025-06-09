package org.workcraft.workspace;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.workcraft.gui.workspace.Path;

public class MountTree {

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MountTree other)) {
            return false;
        }
        return other.mountTo.equals(mountTo);
    }

    public MountTree(File defaultPath, Map<Path<String>, File> mounts, Path<String> workspacePath) {
        path = workspacePath;

        if (defaultPath == null) {
            throw new IllegalArgumentException("defaultPath is null");
        }
        File tmpMountTo = defaultPath;

        subDirs = new HashMap<>();

        if (mounts != null) {
            Map<String, Map<Path<String>, File>> perSubDir = new HashMap<>();

            for (Path<String> s : mounts.keySet()) {
                File file = mounts.get(s);

                if (file == null) {
                    throw new IllegalArgumentException("file is null");
                }

                List<String> pathItems = Path.getPath(s);

                if (pathItems.isEmpty()) {
                    tmpMountTo = file;
                } else {
                    String folder = pathItems.get(0);
                    if (folder.isEmpty()) {
                        throw new RuntimeException("invalid mount path");
                    }
                    pathItems.remove(0);
                    Path<String> suffix = Path.create(pathItems);

                    if (!perSubDir.containsKey(folder)) {
                        perSubDir.put(folder, new HashMap<>());
                    }
                    perSubDir.get(folder).put(suffix, file);
                }
            }

            for (String prefix : perSubDir.keySet()) {
                subDirs.put(prefix, new MountTree(new File(tmpMountTo, prefix), perSubDir.get(prefix), Path.append(path, prefix)));
            }
        }
        mountTo = tmpMountTo;
    }

    public final File mountTo;
    public final Map<String, MountTree> subDirs;
    public final Path<String> path;

    public MountTree getSubtree(String name) {
        if (name.isEmpty()) {
            return this;
        }

        MountTree res = subDirs.get(name);
        if (res != null) {
            return res;
        }
        return new MountTree(new File(mountTo, name), null, Path.append(path, name));
    }
}
