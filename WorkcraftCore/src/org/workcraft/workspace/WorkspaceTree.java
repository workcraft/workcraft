package org.workcraft.workspace;

import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.trees.TreeListener;
import org.workcraft.gui.trees.TreeSource;
import org.workcraft.gui.workspace.Path;

import java.util.*;

public class WorkspaceTree implements TreeSource<Path<String>> {

    private final class WorkspaceListenerWrapper implements WorkspaceListener {
        private final TreeListener<Path<String>> listener;

        private WorkspaceListenerWrapper(TreeListener<Path<String>> listener) {
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
        public void workspaceSaved() {
            listener.restructured(Path.root(getRoot()));
        }

        @Override
        public void workspaceLoaded() {
            listener.restructured(Path.root(getRoot()));
        }
    }

    Workspace workspace;

    public WorkspaceTree(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public void addListener(final TreeListener<Path<String>> listener) {
        workspace.addListener(wrap(listener));
    }

    Map<TreeListener<Path<String>>, WorkspaceListener> wrappers = new HashMap<>();

    private WorkspaceListener wrap(TreeListener<Path<String>> listener) {
        WorkspaceListener res = wrappers.get(listener);
        if (res == null) {
            res = new WorkspaceListenerWrapper(listener);
            wrappers.put(listener, res);
        }
        return res;
    }

    @Override
    public List<Path<String>> getChildren(Path<String> node) {
        MountTree mount = workspace.getMountTree(node);
        Map<String, Path<String>> res = new TreeMap<>();
        String[] list = mount.mountTo.list();
        if (list != null) {
            for (String name : list) {
                res.put(name, mount.getSubtree(name).path);
            }
        }
        for (String name : mount.subDirs.keySet()) {
            if (!res.containsKey(name)) {
                res.put(name, mount.subDirs.get(name).path);
            }
        }
        return new ArrayList<>(res.values());
    }

    @Override
    public Path<String> getRoot() {
        return Path.empty();
    }

    @Override
    public boolean isLeaf(Path<String> node) {
        return isLeaf(workspace, node);
    }

    public static boolean isLeaf(Workspace workspace, Path<String> path) {
        MountTree mount = workspace.getMountTree(path);
        return mount.subDirs.size() == 0 && !mount.mountTo.isDirectory();
    }

    @Override
    public void removeListener(TreeListener<Path<String>> listener) {
        throw new NotSupportedException(); //TODO
    }

    @Override
    public Path<Path<String>> getPath(Path<String> node) {
        Path<Path<String>> result = Path.empty();
        Deque<Path<String>> qq = new ArrayDeque<>();
        while (true) {
            qq.push(node);
            if (node.isEmpty()) {
                break;
            }
            node = node.getParent();
        }
        for (Path<String> n : qq) {
            result = Path.append(result, n);
        }
        return result;
    }

}
