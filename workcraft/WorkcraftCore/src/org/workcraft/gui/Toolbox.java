package org.workcraft.gui;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.editor.GraphEditorPanel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.tools.GraphEditorKeyListener;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.plugins.PluginManager;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Toolbox implements GraphEditorKeyListener {

    static class ToolTracker {
        private final ArrayList<GraphEditorTool> tools = new ArrayList<>();
        private int nextIndex = 0;

        public void addTool(GraphEditorTool tool) {
            tools.add(tool);
            nextIndex = 0;
        }

        public void reset() {
            nextIndex = 0;
        }

        public GraphEditorTool getNextTool() {
            GraphEditorTool ret = tools.get(nextIndex);
            setNext(nextIndex + 1);
            return ret;
        }

        private void setNext(int next) {
            if (next >= tools.size()) {
                next %= tools.size();
            }
            nextIndex = next;
        }

        public void track(GraphEditorTool tool) {
            setNext(tools.indexOf(tool) + 1);
        }
    }

    private final GraphEditorPanel editor;
    private final HashSet<GraphEditorTool> tools = new HashSet<>();
    private final LinkedHashMap<GraphEditorTool, JToggleButton> buttons = new LinkedHashMap<>();
    private final HashMap<Integer, ToolTracker> hotkeyMap = new HashMap<>();
    private GraphEditorTool defaultTool = null;
    private GraphEditorTool selectedTool = null;

    public Toolbox(GraphEditorPanel editor) {
        this.editor = editor;
        final PluginManager pm = Framework.getInstance().getPluginManager();
        WorkspaceEntry we = editor.getWorkspaceEntry();
        VisualModel model = editor.getModel();
        boolean isDefault = true;
        // Tools associated with the model
        for (GraphEditorTool tool : model.getGraphEditorTools()) {
            addTool(tool, isDefault);
            isDefault = false;
        }
        // Tools registered via PluginManager
        for (GraphEditorTool tool : pm.getGraphEditorTools()) {
            if (tool.isApplicableTo(we)) {
                addTool(tool, isDefault);
            }
        }
        // Select default tool
        selectTool(defaultTool);
    }

    private void addTool(final GraphEditorTool tool, boolean isDefault) {
        tools.add(tool);
        if (tool.requiresButton()) {
            JToggleButton button = createToolButton(tool);
            buttons.put(tool, button);
        }
        assignToolHotKey(tool, tool.getHotKeyCode());
        if (isDefault) {
            defaultTool = tool;
        }
    }

    private void assignToolHotKey(final GraphEditorTool tool, int hotKeyCode) {
        if (hotKeyCode != -1) {
            ToolTracker tracker = hotkeyMap.get(hotKeyCode);
            if (tracker == null) {
                tracker = new ToolTracker();
                hotkeyMap.put(hotKeyCode, tracker);
            }
            tracker.addTool(tool);
        }
    }

    private JToggleButton createToolButton(final GraphEditorTool tool) {
        JToggleButton button = new JToggleButton();

        button.setFocusable(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 0, 0, 0));

        int iconSize = SizeHelper.getToolIconSize();
        Insets insets = button.getInsets();
        int minSize = iconSize + Math.max(insets.left + insets.right, insets.top + insets.bottom);

        Icon icon = tool.getIcon();
        if (icon == null) {
            button.setText(tool.getLabel());
            button.setPreferredSize(new Dimension(120, minSize));
        } else {
            BufferedImage crop = new BufferedImage(iconSize, iconSize,
                    BufferedImage.TYPE_INT_ARGB);
            int x = (iconSize - icon.getIconWidth()) / 2;
            int y = (iconSize - icon.getIconHeight()) / 2;
            icon.paintIcon(button, crop.getGraphics(), x, y);
            button.setIcon(new ImageIcon(crop));
            button.setPreferredSize(new Dimension(minSize, minSize));
        }

        int hotKeyCode = tool.getHotKeyCode();
        if (hotKeyCode != -1) {
            button.setToolTipText(tool.getLabel() + " (" + (char) hotKeyCode + ")");
        } else {
            button.setToolTipText(tool.getLabel());
        }
        button.addActionListener(event -> selectTool(tool));
        return button;
    }

    @SuppressWarnings("unchecked")
    public <T extends GraphEditorTool> T getToolInstance(Class<T> cls) {
        for (GraphEditorTool tool : tools) {
            if (cls == tool.getClass()) {
                return (T) tool;
            }
        }
        for (GraphEditorTool tool : tools) {
            if (cls.isInstance(tool)) {
                return (T) tool;
            }
        }
        return null;
    }

    public <T extends GraphEditorTool> T selectToolInstance(Class<T> cls) {
        final T tool = getToolInstance(cls);
        if (tool != null) {
            selectTool(tool);
            return tool;
        }
        return null;
    }

    public void selectDefaultTool() {
        selectTool(defaultTool, false);
    }

    public void selectTool(GraphEditorTool tool) {
        selectTool(tool, true);
    }

    public void selectTool(GraphEditorTool tool, boolean updateDockableVisibility) {
        if ((tool == null) || tool.checkPrerequisites(editor)) {
            if (selectedTool != null) {
                ToolTracker oldTracker = hotkeyMap.get(selectedTool.getHotKeyCode());
                if (oldTracker != null) {
                    oldTracker.reset();
                }
                selectedTool.deactivated(editor);
                setToolButtonSelection(selectedTool, false);
            }
            if (tool != null) {
                ToolTracker tracker = hotkeyMap.get(tool.getHotKeyCode());
                if (tracker != null) {
                    tracker.track(tool);
                }
            }
            // Setup and activate the selected tool (before updating Property editor and Tool controls).
            selectedTool = tool;
            setToolButtonSelection(selectedTool, true);
            if (selectedTool != null) {
                selectedTool.activated(editor);
            }
            // Update the content of Property editor (first) and Tool controls (second).
            if (editor != null) {
                editor.updatePropertyView();
                editor.updateToolsView();
            }
            // Update visibility of Property editor and Tool controls.
            if (updateDockableVisibility) {
                MainWindow mainWindow = Framework.getInstance().getMainWindow();
                mainWindow.updateUtilityPanelDockableVisibility();
            }
        }
    }

    public void setToolsForModel(JToolBar toolbar) {
        for (JToggleButton button: buttons.values()) {
            toolbar.add(button);
        }
        // FIXME: Add separator to force the same toolbar height as the tool controls and global toolbars.
        toolbar.addSeparator();
    }

    public GraphEditorTool getSelectedTool() {
        return selectedTool;
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent event) {
        if ((selectedTool != null) && selectedTool.keyPressed(event)) {
            return true;
        }
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE) {
            selectTool(defaultTool);
            return true;
        }
        if (!event.isAltKeyDown() && !event.isMenuKeyDown() && !event.isShiftKeyDown()) {
            ToolTracker tracker = hotkeyMap.get(keyCode);
            if (tracker != null) {
                GraphEditorTool tool = tracker.getNextTool();
                selectTool(tool);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(GraphEditorKeyEvent event) {
        return (selectedTool != null) && selectedTool.keyReleased(event);
    }

    @Override
    public boolean keyTyped(GraphEditorKeyEvent event) {
        return (selectedTool != null) && selectedTool.keyTyped(event);
    }

    public void setToolButtonEnableness(GraphEditorTool tool, boolean state) {
        JToggleButton button = buttons.get(tool);
        if (button != null) {
            button.setEnabled(state);
        }
    }

    public void setToolButtonSelection(GraphEditorTool tool, boolean state) {
        JToggleButton button = buttons.get(tool);
        if (button != null) {
            button.setSelected(state);
        }
    }

}
