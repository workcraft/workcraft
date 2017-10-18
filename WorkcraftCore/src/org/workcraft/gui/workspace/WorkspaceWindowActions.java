package org.workcraft.gui.workspace;

import org.workcraft.Framework;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.actions.Action;

public class WorkspaceWindowActions {

    public static final Action ADD_FILES_TO_WORKSPACE_ACTION = new Action() {
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().getWorkspaceView().addToWorkspace(Path.root(""));
        }
        @Override
        public String getText() {
            return "Link files to the root of workspace...";
        }
    };

    public static final Action OPEN_WORKSPACE_ACTION = new Action() {
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().getWorkspaceView().openWorkspace();
        }
        @Override
        public String getText() {
            return "Open workspace...";
        }
    };

    public static final Action SAVE_WORKSPACE_ACTION = new Action() {
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().getWorkspaceView().saveWorkspace();
            } catch (OperationCancelledException e) {
            }
        }
        @Override
        public String getText() {
            return "Save workspace";
        }
    };

    public static final Action SAVE_WORKSPACE_AS_ACTION = new Action() {
        @Override
        public void run() {
            try {
                Framework.getInstance().getMainWindow().getWorkspaceView().saveWorkspaceAs();
            } catch (OperationCancelledException e) {
            }
        }
        @Override
        public String getText() {
            return "Save workspace as...";
        }
    };

    public static final Action NEW_WORKSPACE_AS_ACTION = new Action() {
        @Override
        public void run() {
            Framework.getInstance().getMainWindow().getWorkspaceView().newWorkspace();
        }
        @Override
        public String getText() {
            return "New workspace";
        }
    };

}
