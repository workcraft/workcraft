package org.workcraft.plugins.son.tools;

import org.workcraft.dom.Container;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.utils.DesktopApi;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.gui.tools.SelectionTool;
import org.workcraft.plugins.son.VisualONGroup;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.connections.VisualSONConnection;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.plugins.son.elements.VisualChannelPlace;
import org.workcraft.plugins.son.elements.VisualCondition;
import org.workcraft.plugins.son.elements.VisualEvent;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.Hierarchy;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Collection;

public class SONSelectionTool extends SelectionTool {

    private final GraphEditorTool channelPlaceTool;
    private boolean asyn = true;
    private boolean sync = true;

    public SONSelectionTool(GraphEditorTool channelPlaceTool) {
        this.channelPlaceTool = channelPlaceTool;
    }

    @Override
    public void updateControlsToolbar(JToolBar toolbar, final GraphEditor editor) {
        //Create groupButton
        final JButton groupButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-group.svg"),
                "Group selection (" + DesktopApi.getMenuKeyName() + "-G)");
        groupButton.addActionListener(event -> groupSelection(editor));
        toolbar.add(groupButton);

        //Create blockButton
        JButton blockButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-block.svg"),
                "Group selection into a block (Alt-B)");
        blockButton.addActionListener(event -> selectionBlock(editor));
        toolbar.add(blockButton);

        //Create pageButton
        JButton groupPageButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-page.svg"),
                "Group selection into a page (Alt-G)");
        groupPageButton.addActionListener(event -> pageSelection(editor));
        toolbar.add(groupPageButton);

        //Create ungroupButton
        JButton ungroupButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-ungroup.svg"),
                "Ungroup selection (" + DesktopApi.getMenuKeyName() + "+Shift-G)");
        ungroupButton.addActionListener(event -> ungroupSelection(editor));
        toolbar.add(ungroupButton);

        JButton levelUpButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-level_up.svg"), "Level up (PageUp)");
        levelUpButton.addActionListener(event -> changeLevelUp(editor));
        toolbar.add(levelUpButton);

        JButton levelDownButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-level_down.svg"), "Level down (PageDown)");
        levelDownButton.addActionListener(event -> changeLevelDown(editor));
        toolbar.add(levelDownButton);

        toolbar.addSeparator();

        JButton flipHorizontalButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-flip_horizontal.svg"),
                "Flip horizontal (" + DesktopApi.getMenuKeyName() + "-F)");
        flipHorizontalButton.addActionListener(event -> flipSelectionHorizontal(editor));
        toolbar.add(flipHorizontalButton);

        JButton flipVerticalButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-flip_vertical.svg"),
                "Flip vertical (" + DesktopApi.getMenuKeyName() + "+Shift-F)");
        flipVerticalButton.addActionListener(event -> flipSelectionVertical(editor));
        toolbar.add(flipVerticalButton);

        JButton rotateClockwiseButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-rotate_clockwise.svg"),
                "Rotate clockwise (" + DesktopApi.getMenuKeyName() + "-R)");
        rotateClockwiseButton.addActionListener(event -> rotateSelectionClockwise(editor));
        toolbar.add(rotateClockwiseButton);

        JButton rotateCounterclockwiseButton = GuiUtils.createIconButton(
                GuiUtils.createIconFromSVG("images/son-selection-rotate_counterclockwise.svg"),
                "Rotate counterclockwise (" + DesktopApi.getMenuKeyName() + "+Shift-R)");
        rotateCounterclockwiseButton.addActionListener(event -> rotateSelectionCounterclockwise(editor));
        toolbar.add(rotateCounterclockwiseButton);

        toolbar.addSeparator();
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        VisualSON model = (VisualSON) e.getEditor().getModel();

        if (e.getClickCount() > 1) {
            VisualNode node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            Collection<VisualNode> selection = e.getModel().getSelection();

            if (selection.size() == 1) {
                VisualNode selectedNode = selection.iterator().next();
                selectedNode = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);

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
                    vc.setMarked(!vc.isMarked());
                }

                if (selectedNode instanceof VisualEvent) {
                    VisualEvent ve = (VisualEvent) selectedNode;
                    ve.setFaulty(!ve.isFaulty());
                }

                if (selectedNode instanceof VisualChannelPlace) {
                    VisualChannelPlace cPlace = (VisualChannelPlace) node;
                    for (VisualConnection con : model.getConnections(cPlace)) {

                        if (((VisualSONConnection) con).getSemantics() == Semantics.ASYNLINE) {
                            this.sync = false;
                        }
                        if (((VisualSONConnection) con).getSemantics() == Semantics.SYNCLINE) {
                            this.asyn = false;
                        }
                    }
                    if (sync && !asyn) {
                        for (VisualConnection con : model.getConnections(cPlace)) {
                            ((VisualSONConnection) con).setSemantics(Semantics.ASYNLINE);
                        }
                    }

                    if (!sync && asyn) {
                        for (VisualConnection con : model.getConnections(cPlace)) {
                            ((VisualSONConnection) con).setSemantics(Semantics.SYNCLINE);
                        }
                    }
                    if (!sync && !asyn) {
                        for (VisualConnection con : model.getConnections(cPlace)) {
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
        Collection<VisualNode> selection = model.getSelection();
        if (selection.size() == 1) {
            VisualNode node = selection.iterator().next();
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
        if ((parent != null) && (level instanceof VisualNode) && !(level instanceof VisualBlock)) {
            model.setCurrentLevel(parent);
            if (parent instanceof VisualONGroup) {
                setChannelPlaceToolState(editor, false);
            } else {
                setChannelPlaceToolState(editor, true);
            }
            model.addToSelection((VisualNode) level);
            editor.repaint();
        }
    }

    private void selectionBlock(final GraphEditor editor) {
        ((VisualSON) editor.getModel()).groupBlockSelection();
        editor.repaint();
    }

    private void setChannelPlaceToolState(final GraphEditor editor, boolean state) {
        if (editor instanceof GraphEditorPanel) {
            Toolbox toolbox = ((GraphEditorPanel) editor).getToolBox();
            toolbox.setToolButtonEnableness(channelPlaceTool, state);
        }
    }

}
