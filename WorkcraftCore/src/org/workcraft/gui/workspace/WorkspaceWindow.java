package org.workcraft.gui.workspace;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.MainWindowActions;
import org.workcraft.gui.WorkspaceTreeDecorator;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.actions.ScriptedActionListener;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.GUI;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class WorkspaceWindow extends JPanel {
    private static final String DIALOG_OPEN_WORKSPACE = "Open workspace";
    private static final String DIALOG_SAVE_WORKSPACE_AS = "Save workspace as...";

    public static class Actions {

        public static final Action ADD_FILES_TO_WORKSPACE_ACTION = new Action() {
            @Override
            public void run() {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                final WorkspaceWindow workspaceView = mainWindow.getWorkspaceView();
                workspaceView.addToWorkspace(Path.root(""));
            }

            public String getText() {
                return "Link files to the root of workspace...";
            }
        };

        public static final Action OPEN_WORKSPACE_ACTION = new Action() {
            @Override
            public void run() {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                final WorkspaceWindow workspaceView = mainWindow.getWorkspaceView();
                workspaceView.openWorkspace();
            }

            public String getText() {
                return "Open workspace...";
            }
        };

        public static final Action SAVE_WORKSPACE_ACTION = new Action() {
            @Override
            public void run() {
                try {
                    final Framework framework = Framework.getInstance();
                    final MainWindow mainWindow = framework.getMainWindow();
                    final WorkspaceWindow workspaceView = mainWindow.getWorkspaceView();
                    workspaceView.saveWorkspace();
                } catch (OperationCancelledException e) {
                }
            }

            public String getText() {
                return "Save workspace";
            }
        };
        public static final Action SAVE_WORKSPACE_AS_ACTION = new Action() {
            @Override
            public void run() {
                try {
                    final Framework framework = Framework.getInstance();
                    final MainWindow mainWindow = framework.getMainWindow();
                    final WorkspaceWindow workspaceView = mainWindow.getWorkspaceView();
                    workspaceView.saveWorkspaceAs();
                } catch (OperationCancelledException e) {
                }
            }

            public String getText() {
                return DIALOG_SAVE_WORKSPACE_AS;
            }
        };
        public static final Action NEW_WORKSPACE_AS_ACTION = new Action() {
            @Override
            public void run() {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                final WorkspaceWindow workspaceView = mainWindow.getWorkspaceView();
                workspaceView.newWorkspace();
            }

            public String getText() {
                return "New workspace";
            }
        };
    }

    private String lastSavePath = null;
    private String lastOpenPath = null;

    public WorkspaceWindow() {
        super();
        startup();
    }

    public void startup() {
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        TreeWindow<Path<String>> workspaceTree = TreeWindow.create(workspace.getTree(),
                new WorkspaceTreeDecorator(workspace),
                new WorkspacePopupProvider(this));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(8);

        scrollPane.setViewportView(workspaceTree);

        setLayout(new BorderLayout(0, 0));
        this.add(scrollPane, BorderLayout.CENTER);

        lastSavePath = framework.getConfigCoreVar("gui.workspace.lastSavePath");
        lastOpenPath = framework.getConfigCoreVar("gui.workspace.lastOpenPath");
    }

    public void shutdown() {
        final Framework framework = Framework.getInstance();
        if (lastSavePath != null) {
            framework.setConfigCoreVar("gui.workspace.lastSavePath", lastSavePath);
        }
        if (lastOpenPath != null) {
            framework.setConfigCoreVar("gui.workspace.lastOpenPath", lastOpenPath);
        }
    }

    protected int getInsertPoint(DefaultMutableTreeNode node, String caption) {
        if (node.getChildCount() == 0) {
            return 0;
        }

        int i;

        for (i = 0; i < node.getChildCount(); i++) {
            if (node.getChildAt(i).toString().compareToIgnoreCase(caption) > 0) {
                return i;
            }
        }

        return i;
    }

    public void workspaceSaved() {
        final Framework framework = Framework.getInstance();
        final Workspace workspace = framework.getWorkspace();
        String title = workspace.getWorkspaceFile().getAbsolutePath();
        if (title.isEmpty()) {
            title = "new workspace";
        }
        title = "(" + title + ")";
        if (workspace.isChanged()) {
            title = "*" + title;
        }
        repaint();
    }

    public JMenu createMenu() {
        JMenu menu = new JMenu("Workspace");

        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        final ScriptedActionListener listener = mainWindow.getDefaultActionListener();

        ActionMenuItem miNewModel = new ActionMenuItem(
                MainWindowActions.CREATE_WORK_ACTION);
        miNewModel.addScriptedActionListener(listener);

        ActionMenuItem miAdd = new ActionMenuItem(
                WorkspaceWindow.Actions.ADD_FILES_TO_WORKSPACE_ACTION);
        miAdd.addScriptedActionListener(listener);

        ActionMenuItem miSave = new ActionMenuItem(
                WorkspaceWindow.Actions.SAVE_WORKSPACE_ACTION);
        miSave.addScriptedActionListener(listener);

        ActionMenuItem miSaveAs = new ActionMenuItem(
                WorkspaceWindow.Actions.SAVE_WORKSPACE_AS_ACTION);
        miSaveAs.addScriptedActionListener(listener);

        menu.add(miNewModel);
        menu.addSeparator();
        menu.add(miAdd);
        menu.add(miSave);
        menu.add(miSaveAs);

        return menu;
    }

    public void addToWorkspace(Path<String> path) {
        JFileChooser fc;
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (lastOpenPath != null) {
            fc = new JFileChooser(lastOpenPath);
        } else {
            fc = new JFileChooser();
        }
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setDialogTitle("Link to workspace");
        fc.setMultiSelectionEnabled(true);
        fc.addChoosableFileFilter(FileFilters.DOCUMENT_FILES);
        fc.setFileFilter(fc.getAcceptAllFileFilter());
        GUI.sizeFileChooserToScreen(fc, mainWindow.getDisplayMode());
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            for (File file : fc.getSelectedFiles()) {
                Path<String> pathName = Path.append(path, file.getName());
                framework.getWorkspace().addMount(pathName, file, false);
            }
            lastOpenPath = fc.getCurrentDirectory().getPath();
        }
    }

    public void saveWorkspace() throws OperationCancelledException {
        final Framework framework = Framework.getInstance();
        if (!framework.getWorkspace().isTemporary()) {
            framework.getWorkspace().save();
        } else {
            saveWorkspaceAs();
        }
    }

    public void saveWorkspaceAs() throws OperationCancelledException {
        JFileChooser fc;
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (!framework.getWorkspace().isTemporary()) {
            fc = new JFileChooser(framework.getWorkspace().getWorkspaceFile());
        } else if (lastSavePath != null) {
            fc = new JFileChooser(lastSavePath);
        } else {
            fc = new JFileChooser();
        }
        fc.setDialogTitle(DIALOG_SAVE_WORKSPACE_AS);
        fc.setFileFilter(FileFilters.WORKSPACE_FILES);
        GUI.sizeFileChooserToScreen(fc, mainWindow.getDisplayMode());
        File file;
        while (true) {
            if (fc.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                String path = FileFilters.addExtension(fc.getSelectedFile().getPath(), FileFilters.WORKSPACE_EXTENSION);

                file = new File(path);
                if (!file.exists()) {
                    break;
                } else {
                    String msg = "The file '" + file.getName() + "' already exists.\n\n" + "Do you want to overwrite it?";
                    if (DialogUtils.showConfirm(msg, DIALOG_SAVE_WORKSPACE_AS)) {
                        break;
                    }
                }
            } else {
                throw new OperationCancelledException("Save operation cancelled by user.");
            }
        }
        framework.getWorkspace().saveAs(file);
        lastSavePath = fc.getCurrentDirectory().getPath();
    }

    private void checkSaved() throws OperationCancelledException {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (framework.getWorkspace().isChanged()) {
            int result = JOptionPane.showConfirmDialog(mainWindow,
                            "Current workspace is not saved.\n" + "Save before opening?",
                            DIALOG_OPEN_WORKSPACE, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

            switch (result) {
            case JOptionPane.YES_OPTION:
                mainWindow.closeEditorWindows();
                saveWorkspace();
                break;
            case JOptionPane.NO_OPTION:
                mainWindow.closeEditorWindows();
                break;
            default:
                throw new OperationCancelledException("Cancelled by user.");
            }
        }
    }

    public void newWorkspace() {
        try {
            checkSaved();
        } catch (OperationCancelledException e) {
            return;
        }
        final Framework framework = Framework.getInstance();
        framework.getWorkspace().clear();
    }

    public void openWorkspace() {
        try {
            checkSaved();
        } catch (OperationCancelledException e) {
            return;
        }

        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        JFileChooser fc = new JFileChooser();
        fc.setDialogType(JFileChooser.OPEN_DIALOG);
        fc.setFileFilter(FileFilters.WORKSPACE_FILES);

        if (lastOpenPath != null) {
            fc.setCurrentDirectory(new File(lastOpenPath));
        }

        fc.setMultiSelectionEnabled(false);
        fc.setDialogTitle(DIALOG_OPEN_WORKSPACE);
        if (fc.showDialog(mainWindow, "Open") == JFileChooser.APPROVE_OPTION) {
            try {
                framework.loadWorkspace(fc.getSelectedFile());
            } catch (DeserialisationException e) {
                DialogUtils.showError("Workspace load failed. See the Problems window for details.");
                e.printStackTrace();
            }
        }

        lastOpenPath = fc.getCurrentDirectory().getPath();
    }

    public void modelLoaded(WorkspaceEntry we) {
    }

    public void entryChanged(WorkspaceEntry we) {
        repaint();
    }

    public Workspace getWorkspace() {
        return Framework.getInstance().getWorkspace();
    }
}
