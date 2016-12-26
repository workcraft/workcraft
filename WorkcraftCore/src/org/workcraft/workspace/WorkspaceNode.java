package org.workcraft.workspace;

public class WorkspaceNode {
    public WorkspaceNode(MountTree mounts) {
        this.mounts = mounts;
    }
    public MountTree getMounts() {
        return mounts;
    }
    private final MountTree mounts;
}
