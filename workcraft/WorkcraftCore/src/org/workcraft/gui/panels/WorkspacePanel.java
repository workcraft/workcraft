package org.workcraft.gui.panels;

import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.MainWindowActions;
import org.workcraft.gui.actions.Action;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceTreeDecorator;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.FileUtils;
import org.workcraft.workspace.FileFilters;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class WorkspacePanel extends JPanel {

    private static final String DIALOG_OPEN_WORKSPACE = "Open workspace";
    private static final String DIALOG_SAVE_WORKSPACE_AS = "Save workspace as...";

    private final TreeWindow<Path<String>> workspaceTree;

    public WorkspacePanel() {
        super();
        Framework framework = Framework.getInstance();
        Workspace workspace = framework.getWorkspace();
        MainWindow mainWindow = framework.getMainWindow();

        workspaceTree = new TreeWindow<>(workspace.getTree(),
                new WorkspaceTreeDecorator(workspace),
                new WorkspacePopupProvider(this),
                path -> {
                    WorkspaceEntry we = workspace.getWork(path);
                    if (we == null) {
                        File file = workspace.getFile(path);
                        if ((file != null) && file.isFile() && file.exists()) {
                            mainWindow.openWork(file);
                        }
                    } else {
                        mainWindow.getOrCreateEditor(we);
                    }
                });

        workspaceTree.setAutoExpand(true);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getVerticalScrollBar().setUnitIncrement(8);

        scrollPane.setViewportView(workspaceTree);

        setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);
    }

    public JMenu createMenu() {
        JMenu menu = new JMenu("Workspace");

        ActionMenuItem miNewModel = new ActionMenuItem(MainWindowActions.CREATE_WORK_ACTION);
        menu.add(miNewModel);

        menu.addSeparator();

        menu.add(new ActionMenuItem(new Action("Link files to the root of workspace...",
                () -> addToWorkspace(Path.root("")))));

        menu.add(new ActionMenuItem(new Action("Save workspace", this::saveWorkspace)));
        menu.add(new ActionMenuItem(new Action("Save workspace as...", this::saveWorkspaceAs)));
        return menu;
    }

    public void addToWorkspace(Path<String> path) {
        JFileChooser fc = new JFileChooser();
        Framework framework = Framework.getInstance();
        fc.setCurrentDirectory(framework.getLastDirectory());
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setDialogTitle("Link to workspace");
        fc.setMultiSelectionEnabled(true);
        fc.addChoosableFileFilter(FileFilters.DOCUMENT_FILES);
        fc.setFileFilter(fc.getAcceptAllFileFilter());
        if (DialogUtils.showFileOpener(fc)) {
            for (File file : fc.getSelectedFiles()) {
                Path<String> pathName = Path.append(path, file.getName());
                framework.getWorkspace().addMount(pathName, file, false);
            }
            framework.setLastDirectory(fc.getCurrentDirectory());
        }
    }

    public void saveWorkspace() {
        Framework framework = Framework.getInstance();
        if (!framework.getWorkspace().isTemporary()) {
            framework.getWorkspace().save();
        } else {
            saveWorkspaceAs();
        }
    }

    public void saveWorkspaceAs() {
        try {
            File file = chooseValidFileOrCancel();
            Framework framework = Framework.getInstance();
            framework.getWorkspace().saveAs(file);
            framework.setLastDirectory(FileUtils.getFileDirectory(file));
        } catch (OperationCancelledException ignored) {
            // Operation cancelled by the user
        }
    }

    private File chooseValidFileOrCancel() throws OperationCancelledException {
        JFileChooser fc = new JFileChooser();
        Framework framework = Framework.getInstance();
        if (framework.getWorkspace().isTemporary()) {
            fc.setCurrentDirectory(framework.getLastDirectory());
        } else {
            fc.setCurrentDirectory(framework.getWorkspace().getWorkspaceFile());
        }

        fc.setDialogTitle(DIALOG_SAVE_WORKSPACE_AS);
        fc.setFileFilter(FileFilters.WORKSPACE_FILES);
        while (DialogUtils.showFileSaver(fc)) {
            String path = fc.getSelectedFile().getPath();
            if (!path.endsWith(FileFilters.WORKSPACE_EXTENSION)) {
                path += FileFilters.WORKSPACE_EXTENSION;
            }
            File file = new File(path);
            if (!file.exists()) {
                return file;
            }

            if (DialogUtils.showConfirmWarning("The file '" + file.getName() + "' already exists",
                    ".\n\nDo you want to overwrite it?", DIALOG_SAVE_WORKSPACE_AS, false)) {

                return file;
            }
        }
        throw new OperationCancelledException();
    }

    private void saveChangedOrCancel() throws OperationCancelledException {
        Framework framework = Framework.getInstance();
        if (framework.getWorkspace().isChanged()) {
            MainWindow mainWindow = framework.getMainWindow();
            String message = "Current workspace is not saved.\n" + "Save before opening another workspace?";
            switch (DialogUtils.showYesNoCancelWarning(message, DIALOG_OPEN_WORKSPACE)) {
                case JOptionPane.YES_OPTION -> {
                    mainWindow.closeEditorPanelDockables();
                    saveWorkspace();
                }
                case JOptionPane.NO_OPTION -> mainWindow.closeEditorPanelDockables();
                default -> throw new OperationCancelledException();
            }
        }
    }

    public void newWorkspace() {
        try {
            saveChangedOrCancel();
            Framework framework = Framework.getInstance();
            framework.getWorkspace().clear();
        } catch (OperationCancelledException ignored) {
            // Operation cancelled by the user
        }
    }

    public void openWorkspace() {
        try {
            saveChangedOrCancel();
            JFileChooser fc = new JFileChooser();
            Framework framework = Framework.getInstance();
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setFileFilter(FileFilters.WORKSPACE_FILES);
            fc.setCurrentDirectory(framework.getLastDirectory());
            fc.setMultiSelectionEnabled(false);
            fc.setDialogTitle(DIALOG_OPEN_WORKSPACE);
            if (DialogUtils.showFileOpener(fc)) {
                File file = fc.getSelectedFile();
                try {
                    framework.loadWorkspace(file);
                } catch (DeserialisationException e) {
                    DialogUtils.showError("Workspace load failed. See the Problems window for details.");
                    e.printStackTrace();
                }
                framework.setLastDirectory(FileUtils.getFileDirectory(file));
            }
        } catch (OperationCancelledException ignored) {
            // Operation cancelled by the user
        }
    }

    public void setAutoExpand(boolean value) {
        workspaceTree.setAutoExpand(value);
    }

    public void refresh() {
        workspaceTree.refresh();
    }

}
