package org.workcraft.gui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.workcraft.annotations.Annotations;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;
import org.workcraft.gui.graph.tools.CustomToolsProvider;
import org.workcraft.gui.graph.tools.GraphEditorKeyListener;
import org.workcraft.gui.graph.tools.GraphEditorTool;
import org.workcraft.gui.graph.tools.NodeGeneratorTool;
import org.workcraft.gui.graph.tools.ToolProvider;

@SuppressWarnings("serial")
public class ToolboxPanel extends JPanel implements ToolProvider, GraphEditorKeyListener {

    class ToolTracker {
        ArrayList<GraphEditorTool> tools = new ArrayList<>();
        int nextIndex = 0;

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

    private GraphEditorTool defaultTool = null;
    private GraphEditorTool selectedTool = null;

    private final HashSet<GraphEditorTool> tools = new HashSet<>();
    private final HashMap<GraphEditorTool, JToggleButton> buttons = new HashMap<>();
    private final HashMap<Integer, ToolTracker> hotkeyMap = new HashMap<>();

    private final GraphEditorPanel editor;

    public ToolboxPanel(GraphEditorPanel editor) {
        this.editor = editor;
        this.setFocusable(false);
        selectedTool = null;
        setToolsForModel(editor.getModel());
    }

    public void addTool(final GraphEditorTool tool) {
        addTool(tool, false);
    }

    public void addTool(final GraphEditorTool tool, boolean isDefault) {
        tools.add(tool);
        if (tool.requiresButton()) {
            JToggleButton button = createToolButton(tool);
            buttons.put(tool, button);
        }
        assignToolHotKey(tool, tool.getHotKeyCode());
        if (isDefault) {
            defaultTool = tool;
            selectTool(tool);
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

    public JToggleButton createToolButton(final GraphEditorTool tool) {
        JToggleButton button = new JToggleButton();

        button.setFocusable(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setMargin(new Insets(0, 0, 0, 0));

        Insets insets = button.getInsets();
        int iconSize = SizeHelper.getToolIconSize();
        int minSize = iconSize + Math.max(insets.left + insets.right, insets.top + insets.bottom);

        Icon icon = tool.getIcon();
        if (icon == null) {
            button.setText(tool.getLabel());
            button.setPreferredSize(new Dimension(120, minSize));
        } else {
            BufferedImage crop = new BufferedImage(iconSize, iconSize,
                    BufferedImage.TYPE_INT_ARGB);
            icon.paintIcon(button, crop.getGraphics(), (iconSize - icon.getIconWidth()) / 2, (iconSize - icon.getIconHeight()) / 2);
            button.setIcon(new ImageIcon(crop));
            button.setPreferredSize(new Dimension(minSize, minSize));
        }

        int hotKeyCode = tool.getHotKeyCode();
        if (hotKeyCode != -1) {
            button.setToolTipText("[" + Character.toString((char) hotKeyCode) + "] " + tool.getLabel());
        } else {
            button.setToolTipText(tool.getLabel());
        }
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectTool(tool);
            }
        });
        this.add(button);
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
            return (T) tool;
        }
        return null;
    }

    public void selectTool(GraphEditorTool tool) {
        if (selectedTool != null) {
            ToolTracker oldTracker = hotkeyMap.get(selectedTool.getHotKeyCode());
            if (oldTracker != null) {
                oldTracker.reset();
            }
            selectedTool.deactivated(editor);
            setToolButtonSelected(selectedTool, false);
        }

        ToolTracker tracker = hotkeyMap.get(tool.getHotKeyCode());
        if (tracker != null) {
            tracker.track(tool);
        }

        if (tool == selectedTool) {
            selectedTool.reactivated(editor);
        }
        selectedTool = tool;

        controlPanel.setTool(selectedTool, editor);
        setToolButtonSelected(selectedTool, true);
        selectedTool.setup(editor);
        selectedTool.activated(editor);
        editor.updatePropertyView();
        editor.repaint();
    }

    private void setToolsForModel(VisualModel model) {
        setLayout(new SimpleFlowLayout(5, 5));

        Class<? extends CustomToolsProvider> customTools = Annotations.getCustomToolsProvider(model.getClass());
        if (customTools != null) {
            boolean isDefault = true;
            CustomToolsProvider provider = null;
            try {
                provider = customTools.getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (provider != null) {
                for (GraphEditorTool tool : provider.getTools()) {
                    addTool(tool, isDefault);
                    isDefault = false;
                }
            }
        }

        for (Class<?> cls : Annotations.getDefaultCreateButtons(model.getClass())) {
            NodeGeneratorTool tool = new NodeGeneratorTool(new DefaultNodeGenerator(cls));
            addTool(tool);
        }

        for (Class<? extends GraphEditorTool>  tool : Annotations.getCustomTools(model.getClass())) {
            try {
                addTool(tool.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        doLayout();
        this.repaint();
    }

    @Override
    public GraphEditorTool getDefaultTool() {
        return defaultTool;
    }

    @Override
    public GraphEditorTool getSelectedTool() {
        return selectedTool;
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent event) {
        if (selectedTool.keyPressed(event)) {
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
        return selectedTool.keyReleased(event);
    }

    @Override
    public boolean keyTyped(GraphEditorKeyEvent event) {
        return selectedTool.keyTyped(event);
    }

    ToolInterfaceWindow controlPanel = new ToolInterfaceWindow();

    public ToolInterfaceWindow getControlPanel() {
        return controlPanel;
    }

    public void setToolButtonState(GraphEditorTool tool, boolean state) {
        JToggleButton button = buttons.get(tool);
        if (button != null) {
            button.setEnabled(state);
        }
    }

    public void setToolButtonSelected(GraphEditorTool tool, boolean state) {
        JToggleButton button = buttons.get(tool);
        if (button != null) {
            button.setSelected(state);
        }
    }

}
