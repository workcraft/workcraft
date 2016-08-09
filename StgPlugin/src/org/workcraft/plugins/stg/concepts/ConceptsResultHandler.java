package org.workcraft.plugins.stg.concepts;

import java.awt.Container;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.gui.DockableWindowContentPanel;
import org.workcraft.gui.DockableWindowContentPanel.ViewAction;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.stg.VisualStg;
import org.workcraft.plugins.stg.interop.ConceptsImporter;
import org.workcraft.plugins.stg.interop.DotGImporter;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class ConceptsResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

    private final String name;
    private final Object sender;
    private final WorkspaceEntry we;

    public ConceptsResultHandler(Object sender, String inputName, WorkspaceEntry we) {
        this.sender = sender;
        name = inputName;
        this.we = we;
    }

    public ConceptsResultHandler(Object sender) {
        this.sender = sender;
        name = null;
        we = null;
    }

    public void finished(final Result<? extends ExternalProcessResult> result, String description) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {

                    try {
                        String output = new String(result.getReturnValue().getOutput());
                        if ((result.getOutcome() == Outcome.FINISHED) && (output.startsWith(".model out"))) {
                            if (!(sender instanceof ConceptsImporter)) {
                                final Framework framework = Framework.getInstance();
                                MainWindow mainWindow = framework.getMainWindow();
                                GraphEditorPanel editor = mainWindow.getEditor(we);
                                VisualStg visualStg = (VisualStg) we.getModelEntry().getVisualModel();
                                if (output.startsWith(".model out")) {
                                    we.captureMemento();
                                    ModelEntry me = Import.importFromByteArray(new DotGImporter(), result.getReturnValue().getOutput());
                                    String title = "Concepts - ";
                                    me.getModel().setTitle(title + name);
                                    boolean openInEditor = me.isVisual() || CommonEditorSettings.getOpenNonvisual();
                                    WorkspaceEntry newWe = framework.getWorkspace().add(Path.<String>empty(), title + name, me, false, openInEditor);
                                    if (sender instanceof ConceptsWritingTool) {
                                        VisualStg newVisualStg = (VisualStg) me.getVisualModel();
                                        newVisualStg.selectAll();
                                        VisualPage page = newVisualStg.groupPageSelection();
                                        Point2D.Double position = getPosition(visualStg, page);
                                        page.setLabel(name);
                                        page.setPosition(position);
                                        newWe.copy();
                                        we.paste();

                                        newWe.setChanged(false);
                                        closeWork(mainWindow.getEditor(newWe));

                                        editor.zoomFit();
                                    } else if (sender instanceof ConceptsTool) {
                                        if (isCurrentWorkEmpty(editor)) {
                                            closeWork(editor);
                                        }
                                    }
                                    we.saveMemento();
                                }
                            }
                        } else {
                            throw new ConceptsToolException(result);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        we.cancelMemento();
                    } catch (DeserialisationException e) {
                        e.printStackTrace();
                        we.cancelMemento();
                    } catch (NullPointerException e) {
                        new ConceptsToolException(result).handleConceptsError();
                        we.cancelMemento();
                    } catch (ConceptsToolException e) {
                        e.handleConceptsError();
                        we.cancelMemento();
                    }
                }
            });
        } catch (InvocationTargetException | InterruptedException e) {
            e.printStackTrace();
            we.cancelMemento();
        }
    }

    private void closeWork(GraphEditorPanel editor) {
        Container p = editor.getParent();

        while (!(p instanceof DockableWindowContentPanel) && (p != null)) {
            p = p.getParent();
        }

        if (p instanceof DockableWindowContentPanel) {
            DockableWindowContentPanel d = (DockableWindowContentPanel) p;
            new ViewAction(d.getID(), ViewAction.CLOSE_ACTION).run();
        }
    }

    private boolean isCurrentWorkEmpty(GraphEditorPanel editor) {
        VisualStg visualStg = (VisualStg) editor.getModel();
        visualStg.selectAll();
        if (visualStg.getSelection().isEmpty()) {
            return true;
        }
        return false;
    }

    private Point2D.Double getPosition(VisualStg visualStg, VisualPage page) {
        Collection<Node> nodes = findAllNodes((VisualTransformableNode) visualStg.getRoot());
        Point2D.Double rightmost = null;
        for (Node n : nodes) {
            VisualComponent v = (VisualComponent) n;
            if (rightmost == null || v.getRootSpacePosition().getX() > rightmost.getX()) {
                rightmost = (Double) v.getRootSpacePosition();
            }
        }
        return rightmost == null ? new Point2D.Double(0, 0) : new Point2D.Double(rightmost.getX() + 2 + (page.getBoundingBox().getWidth() / 2), rightmost.getY());
    }

    private Collection<Node> findAllNodes(VisualTransformableNode root) {
        ArrayList<Node> result = new ArrayList<>();
        for (Node n : root.getChildren()) {
            if ((n instanceof VisualTransition) || (n instanceof VisualPlace)) {
                result.add(n);
            } else if (n instanceof VisualTransformableNode) {
                result.addAll(findAllNodes((VisualTransformableNode) n));
            }
        }
        return result;
    }

}
