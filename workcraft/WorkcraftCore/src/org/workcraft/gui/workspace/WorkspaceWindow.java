package org.workcraft.gui.workspace;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.MainWindowActions;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.FileFilters;
import org.workcraft.workspace.Workspace;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.File;

@SuppressWarnings("serial")
public class WorkspaceWindow extends JPanel {
    private static final String CONFIG_WORKSPACE_LAST_DIRECTORY = "gui.workspace.lastDirectory";
    private static final String DIALOG_OPEN_WORKSPACE = "Open workspace";
    private static final String DIALOG_SAVE_WORKSPACE_AS = "Save workspace as...";

    private String lastDirectory = null;

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

        setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);

        lastDirectory = framework.getConfigVar(CONFIG_WORKSPACE_LAST_DIRECTORY, false);
    }

    public void shutdown() {
        final Framework framework = Framework.getInstance();
        if (lastDirectory != null) {
            framework.setConfigVar(CONFIG_WORKSPACE_LAST_DIRECTORY, lastDirectory, false);
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

        ActionMenuItem miNewModel = new ActionMenuItem(MainWindowActions.CREATE_WORK_ACTION);
        menu.add(miNewModel);

        menu.addSeparator();

        ActionMenuItem miAdd = new ActionMenuItem(WorkspaceWindowActions.ADD_FILES_TO_WORKSPACE_ACTION);
        menu.add(miAdd);

        ActionMenuItem miSave = new ActionMenuItem(WorkspaceWindowActions.SAVE_WORKSPACE_ACTION);
        menu.add(miSave);

        ActionMenuItem miSaveAs = new ActionMenuItem(WorkspaceWindowActions.SAVE_WORKSPACE_AS_ACTION);
        menu.add(miSaveAs);

        return menu;
    }

    public void addToWorkspace(Path<String> path) {
        JFileChooser fc;
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (lastDirectory != null) {
            fc = new JFileChooser(lastDirectory);
        } else {
            fc = new JFileChooser();
        }
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setDialogTitle("Link to workspace");
        fc.setMultiSelectionEnabled(true);
        fc.addChoosableFileFilter(FileFilters.DOCUMENT_FILES);
        fc.setFileFilter(fc.getAcceptAllFileFilter());
        GuiUtils.sizeFileChooserToScreen(fc, mainWindow.getDisplayMode());
        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            for (File file : fc.getSelectedFiles()) {
                Path<String> pathName = Path.append(path, file.getName());
                framework.getWorkspace().addMount(pathName, file, false);
            }
            lastDirectory = fc.getCurrentDirectory().getPath();
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
        } else if (lastDirectory != null) {
            fc = new JFileChooser(lastDirectory);
        } else {
            fc = new JFileChooser();
        }
        fc.setDialogTitle(DIALOG_SAVE_WORKSPACE_AS);
        fc.setFileFilter(FileFilters.WORKSPACE_FILES);
        GuiUtils.sizeFileChooserToScreen(fc, mainWindow.getDisplayMode());
        File file;
        while (true) {
            if (fc.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
                String path = fc.getSelectedFile().getPath();
                if (!path.endsWith(FileFilters.WORKSPACE_EXTENSION)) {
                    path += FileFilters.WORKSPACE_EXTENSION;
                }
                file = new File(path);
                if (!file.exists()) {
                    break;
                } else {
                    String msg = "The file '" + file.getName() + "' already exists.\n\n" + "Do you want to overwrite it?";
                    if (DialogUtils.showConfirmWarning(msg, DIALOG_SAVE_WORKSPACE_AS, false)) {
                        break;
                    }
                }
            } else {
                throw new OperationCancelledException("Save operation cancelled by user.");
            }
        }
        framework.getWorkspace().saveAs(file);
        lastDirectory = fc.getCurrentDirectory().getPath();
    }

    private void checkSaved() throws OperationCancelledException {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (framework.getWorkspace().isChanged()) {
            int result = JOptionPane.showConfirmDialog(mainWindow,
                            "Current workspace is not saved.\n" + "Save before opening another workspace?",
                            DIALOG_OPEN_WORKSPACE, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

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

        if (lastDirectory != null) {
            fc.setCurrentDirectory(new File(lastDirectory));
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

        lastDirectory = fc.getCurrentDirectory().getPath();
    }

}
