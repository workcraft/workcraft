package org.workcraft.workspace;

import org.workcraft.Framework;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.MainWindowActions;
import org.workcraft.gui.workspace.Path;
import org.workcraft.observation.*;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.utils.DialogUtils;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkUtils;

import java.awt.geom.Point2D;
import java.util.*;

public class WorkspaceEntry implements ObservableState {

    private static final Point2D DEFAULT_PASTE_OFFSET = new Point2D.Double(1.0, 1.0);

    private final Map<String, Resource> resources = new HashMap<>();
    private ModelEntry modelEntry = null;
    private boolean changed = true;

    private boolean canSelect = true;
    private boolean canModify = true;
    private boolean canCopy = true;

    private final MementoManager history = new MementoManager();
    private Resource capturedMemento = null;
    private Resource savedMemento = null;

    private VisualNode templateNode = null;
    private VisualNode defaultNode = null;

    private Point2D pastePosition = null;
    private String details = null;
    private boolean temporary = false;

    public void setChanged(boolean changed) {
        if (this.changed != changed) {
            this.changed = changed;
            if (!changed) {
                savedMemento = null;
            }
            Framework framework = Framework.getInstance();
            Workspace workspace = framework.getWorkspace();
            workspace.fireEntryChanged(this);
            MainWindow mainWindow = framework.getMainWindow();
            if (mainWindow != null) {
                mainWindow.refreshWorkspaceEntryTitle(this);
            }
        }
    }

    public boolean isChanged() {
        return changed;
    }

    public void setTemporary(boolean temporary) {
        this.temporary = temporary;
    }

    public ModelEntry getModelEntry() {
        return modelEntry;
    }

    public Collection<Resource> getResources() {
        return new ArrayList<>(resources.values());
    }

    public Resource addResource(Resource resource) {
        return resources.put(resource.getName(), resource);
    }

    public Resource getResource(String name) {
        return resources.get(name);
    }

    public Resource removeResource(String name) {
        return resources.remove(name);
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

    public String getModelTitle() {
        return getModelEntry().getModel().getTitle();
    }

    public String getTitle() {
        Path<String> workspacePath = getWorkspacePath();
        if (workspacePath == null) {
            return null;
        }
        String node = workspacePath.getNode();
        int extensionIndex = node.lastIndexOf(FileFilters.DOCUMENT_EXTENSION);
        if (extensionIndex < 0) {
            return node;
        }
        return node.substring(0, extensionIndex);
    }

    public void setDetails(String value) {
        details = value;
    }

    public void clearDetails() {
        setDetails(null);
    }

    public String getDetails() {
        return details == null ? "" : " : " + details;
    }

    public String getTitleAndModelType() {
        return getTitle() + getModelTypeSuffix();
    }

    public String getHtmlDetailedTitle() {
        String prefix = isChanged() ? "*" : "";
        String suffix = getModelTypeSuffix();
        String formattedTitle = temporary ? "<i>" + getTitle() + "</i>" : getTitle();
        return "<html>" + prefix + formattedTitle + getDetails() + suffix + "</html>";
    }

    private String getModelTypeSuffix() {
        VisualModel model = getModelEntry().getVisualModel();
        if (model == null) {
            return "";
        }
        return switch (EditorCommonSettings.getTitleStyle()) {
            case LONG -> " - " + model.getDisplayName();
            case SHORT -> " [" + model.getShortName() + "]";
            default -> "";
        };
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
        return Framework.getInstance().getWorkspace().getPath(this);
    }

    private final ObservableStateImpl observableState = new ObservableStateImpl();

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
        MainWindow mainWindow = Framework.getInstance().getMainWindow();
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
        capturedMemento = WorkUtils.mementoModel(modelEntry);
        if (!changed) {
            savedMemento = capturedMemento;
        }
    }

    public void uncaptureMemento() {
        capturedMemento = null;
    }

    public void cancelMemento() {
        if (capturedMemento != null) {
            setModelEntry(WorkUtils.loadModel(capturedMemento));
            setChanged(savedMemento != capturedMemento);
            capturedMemento = null;
        }
    }

    public void saveMemento() {
        Resource currentMemento = capturedMemento;
        capturedMemento = null;
        if (currentMemento == null) {
            currentMemento = WorkUtils.mementoModel(modelEntry);
        }
        if (!changed) {
            savedMemento = currentMemento;
        }
        history.pushUndo(currentMemento);
        history.clearRedo();
        updateActionState();
    }

    public void undo() {
        if (history.canUndo()) {
            Resource undoMemento = history.pullUndo();
            if (undoMemento != null) {
                Resource currentMemento = WorkUtils.mementoModel(modelEntry);
                if (!changed) {
                    savedMemento = currentMemento;
                }
                history.pushRedo(currentMemento);
                setModelEntry(WorkUtils.loadModel(undoMemento));
                setChanged(undoMemento != savedMemento);
            }
        }
        updateActionState();
    }

    public void redo() {
        if (history.canRedo()) {
            Resource redoMemento = history.pullRedo();
            if (redoMemento != null) {
                Resource currentMemento = WorkUtils.mementoModel(modelEntry);
                if (!changed) {
                    savedMemento = currentMemento;
                }
                history.pushUndo(currentMemento);
                setModelEntry(WorkUtils.loadModel(redoMemento));
                setChanged(redoMemento != savedMemento);
            }
        }
        updateActionState();
    }

    public void insert(ModelEntry me) {
        try {
            Resource currentMemento = WorkUtils.mementoModel(modelEntry);
            Resource insertMemento = WorkUtils.mementoModel(me);
            ModelEntry result = WorkUtils.loadModel(currentMemento.toStream(), insertMemento.toStream());
            saveMemento();
            setModelEntry(result);
            setChanged(true);
        } catch (DeserialisationException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    public void copy() {
        VisualModel model = modelEntry.getVisualModel();
        if (!model.getSelection().isEmpty()) {
            captureMemento();
            model.beforeCopy();
            // Remember the current level, selected nodes, and then jump to the root level.
            Container currentLevel = model.getCurrentLevel();
            Collection<VisualNode> selectedNodes = new HashSet<>(model.getSelection());
            model.setCurrentLevel(model.getRoot());
            // Starting from the root, delete irrelevant containers and ungroup the containers of the selected nodes.
            for (Node container : Hierarchy.getPath(currentLevel)) {
                if ((container != model.getRoot()) && (container instanceof VisualNode)) {
                    model.select((VisualNode) container);
                    model.selectInverse();
                    model.deleteSelection();
                    model.select((VisualNode) container);
                    model.ungroupSelection();
                }
            }
            // Now the selected nodes should be at the root level; delete everything except the selected nodes.
            model.select(selectedNodes);
            model.selectInverse();
            model.deleteSelection();
            // Save the remaining nodes to clipboard.
            final Framework framework = Framework.getInstance();
            framework.clipboard = WorkUtils.mementoModel(modelEntry);
            cancelMemento();
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
                Resource memento = WorkUtils.mementoModel(modelEntry);
                ModelEntry me = WorkUtils.loadModel(memento.toStream(), framework.clipboard.toStream());

                VisualModel model = me.getVisualModel();
                Point2D offset = getPasteOffset(model);
                if (offset != null) {
                    VisualModelTransformer.translateSelection(model, offset.getX(), offset.getY());
                }

                model.afterPaste();
                saveMemento();
                setModelEntry(me);
                setChanged(true);
            } catch (DeserialisationException e) {
                DialogUtils.showError(e.getMessage());
            }
        }
    }

    private Point2D getPasteOffset(VisualModel model) {
        Point2D result = null;
        if (pastePosition != null) {
            Point2D pos = getSelectionPivotPosition(model);
            if (pos != null) {
                double dx = pastePosition.getX() - pos.getX();
                double dy = pastePosition.getY() - pos.getY();
                result = new Point2D.Double(dx, dy);
            }
        }
        if (result == null) {
            result = DEFAULT_PASTE_OFFSET;
        }
        return result;
    }

    private Point2D getSelectionPivotPosition(VisualModel model) {
        Point2D result = null;
        for (VisualNode node : model.getSelection()) {
            if (node instanceof VisualTransformableNode) {
                Point2D pos = ((VisualTransformableNode) node).getRootSpacePosition();
                if ((result == null) || (result.getY() > pos.getY())) {
                    result = pos;
                }
            }
        }
        return result;
    }

    public void delete() {
        VisualModel model = modelEntry.getVisualModel();
        if (!model.getSelection().isEmpty()) {
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

    public void setPastePosition(Point2D pastePosition) {
        this.pastePosition = pastePosition;
    }

}
