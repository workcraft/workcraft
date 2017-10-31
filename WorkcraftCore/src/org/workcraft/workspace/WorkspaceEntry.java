package org.workcraft.workspace;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.FileFilters;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.MainWindowActions;
import org.workcraft.gui.workspace.Path;
import org.workcraft.observation.ModelModifiedEvent;
import org.workcraft.observation.ObservableState;
import org.workcraft.observation.ObservableStateImpl;
import org.workcraft.observation.SelectionChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonDebugSettings;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Hierarchy;

public class WorkspaceEntry implements ObservableState {
    private ModelEntry modelEntry = null;
    private boolean changed = true;
    private final Workspace workspace;

    private boolean canSelect = true;
    private boolean canModify = true;
    private boolean canCopy = true;

    private final MementoManager history = new MementoManager();
    private Memento capturedMemento = null;
    private Memento savedMemento = null;

    private VisualNode templateNode = null;
    private VisualNode defaultNode = null;

    public WorkspaceEntry(Workspace workspace) {
        this.workspace = workspace;
    }

    public void setChanged(boolean changed) {
        if (this.changed != changed) {
            this.changed = changed;
            if (changed == false) {
                savedMemento = null;
            }
            if (workspace != null) {
                workspace.fireEntryChanged(this);
            }
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            if (mainWindow != null) {
                mainWindow.refreshWorkspaceEntryTitle(this, true);
            }
        }
    }

    public boolean isChanged() {
        return changed;
    }

    public ModelEntry getModelEntry() {
        return modelEntry;
    }

    private final StateObserver modelObserver = new StateObserver() {
        @Override
        public void notify(StateEvent e) {
            if (e instanceof ModelModifiedEvent) {
                setChanged(true);
            }
            observableState.sendNotification(e);
        }
    };

    public void setModelEntry(ModelEntry modelEntry) {
        if (this.modelEntry != null) {
            if (this.modelEntry.isVisual()) {
                this.modelEntry.getVisualModel().removeObserver(modelObserver);
            }
        }
        this.modelEntry = modelEntry;

        observableState.sendNotification(new StateEvent() {
            @Override
            public Object getSender() {
                return this;
            }
        });

        if (this.modelEntry.isVisual()) {
            this.modelEntry.getVisualModel().addObserver(modelObserver);
        }
    }

    public boolean isWork() {
        return (modelEntry != null) || (getWorkspacePath().getNode().endsWith(FileFilters.DOCUMENT_EXTENSION));
    }

    public String getTitle() {
        String result = null;
        Path<String> workspacePath = getWorkspacePath();
        if (workspacePath != null) {
            String name = workspacePath.getNode();
            if (!isWork()) {
                result = name;
            } else {
                int dot = name.lastIndexOf('.');
                if (dot == -1) {
                    result = name;
                } else {
                    result = name.substring(0, dot);
                }
            }
        }
        return result;
    }

    public String getFileName() {
        String fileName = getTitle();
        if ((fileName == null) || fileName.isEmpty()) {
            fileName = "Untitled";
        }
        return fileName
                .replace('\\', '_')
                .replace('/', '_')
                .replace(':', '_')
                .replace('"', '_')
                .replace('<', '_')
                .replace('>', '_')
                .replace('|', '_');
    }

    public Path<String> getWorkspacePath() {
        return workspace == null ? null : workspace.getPath(this);
    }

    public File getFile() {
        return workspace == null ? null : workspace.getFile(this);
    }

    ObservableStateImpl observableState = new ObservableStateImpl();

    @Override
    public void addObserver(StateObserver obs) {
        observableState.addObserver(obs);
    }

    @Override
    public void removeObserver(StateObserver obs) {
        observableState.removeObserver(obs);
    }

    @Override
    public void sendNotification(StateEvent e) {
        observableState.sendNotification(e);
    }

    public void updateActionState() {
        MainWindowActions.MERGE_WORK_ACTION.setEnabled(canModify);
        MainWindowActions.EDIT_UNDO_ACTION.setEnabled(canModify && history.canUndo());
        MainWindowActions.EDIT_REDO_ACTION.setEnabled(canModify && history.canRedo());
        MainWindowActions.EDIT_CUT_ACTION.setEnabled(canModify && canSelect && canCopy);
        MainWindowActions.EDIT_COPY_ACTION.setEnabled(canModify && canSelect && canCopy);
        MainWindowActions.EDIT_PASTE_ACTION.setEnabled(canModify && canSelect && canCopy);
        MainWindowActions.EDIT_DELETE_ACTION.setEnabled(canModify && canSelect);
        MainWindowActions.EDIT_SELECT_ALL_ACTION.setEnabled(canModify && canSelect);
        MainWindowActions.EDIT_SELECT_INVERSE_ACTION.setEnabled(canModify && canSelect);
        MainWindowActions.EDIT_SELECT_NONE_ACTION.setEnabled(canModify && canSelect);
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();
        if (mainWindow != null) {
            mainWindow.updateMainMenuState(canModify);
        }
    }

    public void setCanModify(boolean canModify) {
        this.canModify = canModify;
        updateActionState();
    }

    public void setCanSelect(boolean canSelect) {
        this.canSelect = canSelect;
        updateActionState();
    }

    public void setCanCopy(boolean canCopy) {
        this.canCopy = canCopy;
        updateActionState();
    }

    public void captureMemento() {
        final Framework framework = Framework.getInstance();
        capturedMemento = framework.saveModel(modelEntry);
        if (changed == false) {
            savedMemento = capturedMemento;
        }

        if (CommonDebugSettings.getCopyModelOnChange()) {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            String str = unzipInputStream(new ZipInputStream(capturedMemento.getStream()));
            clipboard.setContents(new StringSelection(str), null);
        }
    }

    public void cancelMemento() {
        final Framework framework = Framework.getInstance();
        if (capturedMemento != null) {
            setModelEntry(framework.loadModel(capturedMemento));
            setChanged(savedMemento != capturedMemento);
        }
        capturedMemento = null;
    }

    public void saveMemento() {
        Memento currentMemento = capturedMemento;
        capturedMemento = null;
        if (currentMemento == null) {
            final Framework framework = Framework.getInstance();
            currentMemento = framework.saveModel(modelEntry);
        }
        if (changed == false) {
            savedMemento = currentMemento;
        }
        history.pushUndo(currentMemento);
        history.clearRedo();
        updateActionState();
    }

    public void undo() {
        if (history.canUndo()) {
            Memento undoMemento = history.pullUndo();
            if (undoMemento != null) {
                final Framework framework = Framework.getInstance();
                Memento currentMemento = framework.saveModel(modelEntry);
                if (changed == false) {
                    savedMemento = currentMemento;
                }
                history.pushRedo(currentMemento);
                setModelEntry(framework.loadModel(undoMemento));
                setChanged(undoMemento != savedMemento);
            }
        }
        updateActionState();
    }

    public void redo() {
        if (history.canRedo()) {
            Memento redoMemento = history.pullRedo();
            if (redoMemento != null) {
                final Framework framework = Framework.getInstance();
                Memento currentMemento = framework.saveModel(modelEntry);
                if (changed == false) {
                    savedMemento = currentMemento;
                }
                history.pushUndo(currentMemento);
                setModelEntry(framework.loadModel(redoMemento));
                setChanged(redoMemento != savedMemento);
            }
        }
        updateActionState();
    }

    public void insert(ModelEntry me) {
        final Framework framework = Framework.getInstance();
        try {
            Memento currentMemento = framework.saveModel(modelEntry);
            Memento insertMemento = framework.saveModel(me);
            ModelEntry result = framework.loadModel(currentMemento.getStream(), insertMemento.getStream());
            saveMemento();
            setModelEntry(result);
            setChanged(true);
        } catch (DeserialisationException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    public String unzipInputStream(ZipInputStream zis) {
        String result = "";
        try {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                StringBuilder isb = new StringBuilder();
                CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();
                BufferedReader br = new BufferedReader(new InputStreamReader(zis, utf8Decoder));
                String line = "=== " + ze.getName() + " ===";
                while (line != null) {
                    isb.append(line);
                    isb.append('\n');
                    line = br.readLine();
                }
                result += isb.toString();
                zis.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String getClipboardAsString() {
        final Framework framework = Framework.getInstance();
        return unzipInputStream(new ZipInputStream(framework.clipboard.getStream()));
    }

    public void copy() {
        VisualModel model = modelEntry.getVisualModel();
        if (model.getSelection().size() > 0) {
            captureMemento();
            try {
                // Copy selected nodes inside a group as if it was the root.
                while (model.getCurrentLevel() != model.getRoot()) {
                    Collection<Node> nodes = new HashSet<>(model.getSelection());
                    Container level = model.getCurrentLevel();
                    Container parent = Hierarchy.getNearestAncestor(level.getParent(), Container.class);
                    if (parent != null) {
                        model.setCurrentLevel(parent);
                        model.addToSelection(level);
                    }
                    model.ungroupSelection();
                    model.select(nodes);
                }
                model.selectInverse();
                model.deleteSelection();
                final Framework framework = Framework.getInstance();
                framework.clipboard = framework.saveModel(modelEntry);
                if (CommonDebugSettings.getCopyModelOnChange()) {
                    // Copy the memento clipboard into the system-wide clipboard as a string.
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(getClipboardAsString()), null);
                }
            } finally {
                cancelMemento();
            }
        }
    }

    public void cut() {
        copy();
        delete();
    }

    public void paste() {
        final Framework framework = Framework.getInstance();
        if (framework.clipboard != null) {
            try {
                Memento memento = framework.saveModel(modelEntry);
                ModelEntry result = framework.loadModel(memento.getStream(), framework.clipboard.getStream());
                saveMemento();
                setModelEntry(result);
                setChanged(true);

                VisualModel model = result.getVisualModel();
                VisualModelTransformer.translateSelection(model, 1.0, 1.0);
            } catch (DeserialisationException e) {
                DialogUtils.showError(e.getMessage());
            }
        }
    }

    public void delete() {
        VisualModel model = modelEntry.getVisualModel();
        if (model.getSelection().size() > 0) {
            saveMemento();
            model.deleteSelection();
            setChanged(true);
        }
    }

    public void setTemplateNode(VisualNode value) {
        if (templateNode != value) {
            templateNode = value;
            VisualModel visualModel = getModelEntry().getVisualModel();
            visualModel.sendNotification(new SelectionChangedEvent(visualModel, null));
        }
    }

    public VisualNode getTemplateNode() {
        return templateNode;
    }

    public void setDefaultNode(VisualNode node) {
        defaultNode = node;
    }

    public VisualNode getDefaultNode() {
        return defaultNode;
    }

}
