package org.workcraft.plugins.son.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JToolBar;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.SelectionTool;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;

public class SONSelectionTool extends SelectionTool {

    private final GraphEditorTool channelPlaceTool;
    private boolean asyn = true;
    private boolean sync = true;

    public SONSelectionTool(GraphEditorTool channelPlaceTool) {
        this.channelPlaceTool = channelPlaceTool;
    }

    @Override
    public void updateToolbar(JToolBar toolbar, final GraphEditor editor) {
        super.updateToolbar(toolbar, editor);

        //Create groupButton
        final JButton groupButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-group.svg"), "Group selection (" + DesktopApi.getMenuKeyMaskName() + "+G)");
        groupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                groupSelection(editor);
            }
        });
        toolbar.add(groupButton);

        //Create blockButton
        JButton blockButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-block.svg"), "Group selection into a block (Alt+B)");
        blockButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectionBlock(editor);
            }
        });
        toolbar.add(blockButton);

        //Create pageButton
        JButton groupPageButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-page.svg"), "Group selection into a page (Alt+G)");
        groupPageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pageSelection(editor);
            }
        });
        toolbar.add(groupPageButton);

        //Create ungroupButton
        JButton ungroupButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-ungroup.svg"), "Ungroup selection (" + DesktopApi.getMenuKeyMaskName() + "+Shift+G)");
        ungroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ungroupSelection(editor);
            }
        });
        toolbar.add(ungroupButton);

        JButton levelUpButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-level_up.svg"), "Level up (PageUp)");
        levelUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeLevelUp(editor);
            }
        });
        toolbar.add(levelUpButton);

        JButton levelDownButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-level_down.svg"), "Level down (PageDown)");
        levelDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeLevelDown(editor);
            }
        });
        toolbar.add(levelDownButton);

        toolbar.addSeparator();

        JButton flipHorizontalButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-flip_horizontal.svg"), "Flip horizontal (" + DesktopApi.getMenuKeyMaskName() + "+F)");
        flipHorizontalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flipSelectionHorizontal(editor);
            }
        });
        toolbar.add(flipHorizontalButton);

        JButton flipVerticalButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-flip_vertical.svg"), "Flip vertical (" + DesktopApi.getMenuKeyMaskName() + "+Shift+F)");
        flipVerticalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flipSelectionVertical(editor);
            }
        });
        toolbar.add(flipVerticalButton);

        JButton rotateClockwiseButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-rotate_clockwise.svg"), "Rotate clockwise (" + DesktopApi.getMenuKeyMaskName() + "+R)");
        rotateClockwiseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotateSelectionClockwise(editor);
            }
        });
        toolbar.add(rotateClockwiseButton);

        JButton rotateCounterclockwiseButton = GUI.createIconButton(GUI.createIconFromSVG(
                "images/son-selection-rotate_counterclockwise.svg"), "Rotate counterclockwise (" + DesktopApi.getMenuKeyMaskName() + "+Shift+R)");
        rotateCounterclockwiseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rotateSelectionCounterclockwise(editor);
            }
        });
        toolbar.add(rotateCounterclockwiseButton);
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        VisualSON model = (VisualSON) e.getEditor().getModel();

        if (e.getClickCount() > 1) {
            VisualNode node = (VisualNode) HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            Collection<Node> selection = e.getModel().getSelection();

            if (selection.size() == 1) {
                Node selectedNode = selection.iterator().next();
                selectedNode = (VisualNode) HitMan.hitFirstInCurrentLevel(e.getPosition(), model);

                if (selectedNode instanceof VisualBlock) {
                    if (!((VisualBlock) selectedNode).getIsCollapsed()) {
                        ((VisualBlock) selectedNode).setIsCollapsed(true);
                    } else {
                        ((VisualBlock) selectedNode).setIsCollapsed(false);
                    }

                    return;
                }

                if (selectedNode instanceof VisualCondition) {
                    VisualCondition vc = (VisualCondition) selectedNode;
                    if (vc.isMarked() == false) {
                        vc.setIsMarked(true);
                    } else if (vc.isMarked() == true) {
                        vc.setIsMarked(false);
                    }
                }

                if (selectedNode instanceof VisualEvent) {
                    VisualEvent ve = (VisualEvent) selectedNode;
                    if (ve.isFaulty() == false) {
                        ve.setFaulty(true);
                    } else if (ve.isFaulty() == true) {
                        ve.setFaulty(false);
                    }
                }

                if (selectedNode instanceof VisualChannelPlace) {
                    VisualChannelPlace cPlace = (VisualChannelPlace) node;
                    for (Connection con : model.getConnections(cPlace)) {

                        if (((VisualSONConnection) con).getSemantics() == Semantics.ASYNLINE) {
                            this.sync = false;
                        }
                        if (((VisualSONConnection) con).getSemantics() == Semantics.SYNCLINE) {
                            this.asyn = false;
                        }
                    }
                    if (sync && !asyn) {
                        for (Connection con : model.getConnections(cPlace)) {
                            ((VisualSONConnection) con).setSemantics(Semantics.ASYNLINE);
                        }
                    }

                    if (!sync && asyn) {
                        for (Connection con : model.getConnections(cPlace)) {
                            ((VisualSONConnection) con).setSemantics(Semantics.SYNCLINE);
                        }
                    }
                    if (!sync && !asyn) {
                        for (Connection con : model.getConnections(cPlace)) {
                            ((VisualSONConnection) con).setSemantics(Semantics.SYNCLINE);
                        }
                    }
                    asyn = true;
                    sync = true;
                }
            }
        }
        super.mouseClicked(e);
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        if (e.isAltKeyDown() && !e.isMenuKeyDown() && !e.isShiftKeyDown()) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_B:
                selectionBlock(e.getEditor());
                return true;
            }
        }
        return super.keyPressed(e);
    }

    @Override
    protected void changeLevelDown(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        Collection<Node> selection = model.getSelection();
        if (selection.size() == 1) {
            Node node = selection.iterator().next();
            if (node instanceof Container && !(node instanceof VisualBlock)) {
                model.setCurrentLevel((Container) node);
                if (node instanceof VisualONGroup) {
                    setChannelPlaceToolState(editor, false);
                } else {
                    setChannelPlaceToolState(editor, true);
                }
                editor.repaint();
            }
        }
    }

    @Override
    protected void changeLevelUp(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        Container level = model.getCurrentLevel();
        Container parent = Hierarchy.getNearestAncestor(level.getParent(), Container.class);
        if (parent != null && !(level instanceof VisualBlock)) {
            model.setCurrentLevel(parent);
            if (parent instanceof VisualONGroup) {
                setChannelPlaceToolState(editor, false);
            } else {
                setChannelPlaceToolState(editor, true);
            }
            model.addToSelection(level);
            editor.repaint();
        }
    }

    private void selectionBlock(final GraphEditor editor) {
        ((VisualSON) editor.getModel()).groupBlockSelection();
        editor.repaint();
    }

    private void setChannelPlaceToolState(final GraphEditor editor, boolean state) {
        if (editor instanceof GraphEditorPanel) {
            ToolboxPanel toolbox = ((GraphEditorPanel) editor).getToolBox();
            toolbox.setToolButtonState(channelPlaceTool, state);
        }
    }

}
