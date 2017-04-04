package org.workcraft.gui.graph.tools;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.workcraft.Framework;
import org.workcraft.NodeTransformer;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.Flippable;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.Rotatable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualModelTransformer;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.connections.DefaultAnchorGenerator;
import org.workcraft.exceptions.ArgumentException;
import org.workcraft.gui.DesktopApi;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.actions.ActionMenuItem;
import org.workcraft.gui.actions.PopupToolAction;
import org.workcraft.gui.events.GraphEditorKeyEvent;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.Viewport;
import org.workcraft.gui.graph.commands.Command;
import org.workcraft.gui.layouts.WrapLayout;
import org.workcraft.util.Commands;
import org.workcraft.util.GUI;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.WorkspaceEntry;

public class SelectionTool extends AbstractGraphEditorTool {

    private enum DrugState { NONE, MOVE, SELECT };
    private enum SelectionMode { NONE, ADD, REMOVE, REPLACE };

    private static final Color selectionBorderColor = new Color(200, 200, 200);
    private static final Color selectionFillColor = new Color(99, 130, 191, 32);

    protected JPanel interfacePanel;
    protected JPanel controlPanel;
    protected JPanel infoPanel;
    protected JPanel statusPanel;

    private DrugState dragState = DrugState.NONE;
    private boolean ignoreMouseButton1 = false;
    private boolean ignoreMouseButton3 = false;

    private Point2D offset;
    private Set<Point2D> snaps = new HashSet<>();
    private final DefaultAnchorGenerator anchorGenerator = new DefaultAnchorGenerator();

    private final LinkedHashSet<Node> selected = new LinkedHashSet<>();
    private SelectionMode selectionMode = SelectionMode.NONE;
    private Rectangle2D selectionBox = null;

    private VisualNode currentNode = null;

    private boolean cancelInPlaceEdit = false;
    private boolean enableGroupping = true;
    private boolean enablePaging = true;
    private boolean enableFlipping = true;
    private boolean enableRotating = true;
    private boolean isPanMode = false;
    private boolean isTextMode = false;

    public SelectionTool() {
        this(true, true, true, true);
    }

    public SelectionTool(boolean enableGroupping, boolean enablePaging, boolean enableFlipping, boolean enableRotating) {
        super();
        this.enableGroupping = enableGroupping;
        this.enablePaging = enablePaging;
        this.enableFlipping = enableFlipping;
        this.enableRotating = enableRotating;
    }

    @Override
    public String getLabel() {
        return "Select";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_S;
    }

    @Override
    public Icon getIcon() {
        return GUI.createIconFromSVG("images/tool-selection.svg");
    }

    @Override
    public JPanel getInterfacePanel() {
        return interfacePanel;
    }

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        super.createInterfacePanel(editor);

        interfacePanel = new JPanel(new BorderLayout());

        controlPanel = new JPanel(new WrapLayout(WrapLayout.CENTER, 0, 0));
        interfacePanel.add(controlPanel, BorderLayout.PAGE_START);

        JPanel groupPanel = new JPanel(new FlowLayout());
        controlPanel.add(groupPanel);

        if (enableGroupping) {
            JButton groupButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-group.svg"),
                    "Group selection (" + DesktopApi.getMenuKeyMaskName() + "+G)");
            groupButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    groupSelection(editor);
                    editor.requestFocus();
                }
            });
            groupPanel.add(groupButton);
        }

        if (enablePaging) {
            JButton groupPageButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-page.svg"),
                    "Combine selection into a page (Alt+G)");
            groupPageButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pageSelection(editor);
                    editor.requestFocus();
                }
            });
            groupPanel.add(groupPageButton);
        }

        if (enableGroupping || enablePaging) {
            JButton ungroupButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-ungroup.svg"),
                    "Ungroup selection (" + DesktopApi.getMenuKeyMaskName() + "+Shift+G)");
            ungroupButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ungroupSelection(editor);
                    editor.requestFocus();
                }
            });
            groupPanel.add(ungroupButton);

            JPanel levelPanel = new JPanel(new FlowLayout());
            controlPanel.add(levelPanel);
            JButton levelUpButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-level_up.svg"),
                    "Level up (PageUp)");
            levelUpButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    changeLevelUp(editor);
                    editor.requestFocus();
                }
            });
            levelPanel.add(levelUpButton);
            JButton levelDownButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-level_down.svg"),
                    "Level down (PageDown)");
            levelDownButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    changeLevelDown(editor);
                    editor.requestFocus();
                }
            });
            levelPanel.add(levelDownButton);
        }

        if (enableFlipping) {
            JPanel flipPanel = new JPanel(new FlowLayout());
            controlPanel.add(flipPanel);
            JButton flipHorizontalButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-flip_horizontal.svg"),
                    "Flip horizontal");
            flipHorizontalButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    flipSelectionHorizontal(editor);
                    editor.requestFocus();
                }
            });
            flipPanel.add(flipHorizontalButton);
            JButton flipVerticalButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-flip_vertical.svg"),
                    "Flip vertical");
            flipVerticalButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    flipSelectionVertical(editor);
                    editor.requestFocus();
                }
            });
            flipPanel.add(flipVerticalButton);
        }

        if (enableRotating) {
            JPanel rotatePanel = new JPanel(new FlowLayout());
            controlPanel.add(rotatePanel);
            JButton rotateClockwiseButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-rotate_clockwise.svg"),
                    "Rotate clockwise");
            rotateClockwiseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rotateSelectionClockwise(editor);
                    editor.requestFocus();
                }
            });
            rotatePanel.add(rotateClockwiseButton);
            JButton rotateCounterclockwiseButton = GUI.createIconButton(GUI.createIconFromSVG("images/selection-rotate_counterclockwise.svg"),
                    "Rotate counterclockwise");
            rotateCounterclockwiseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rotateSelectionCounterclockwise(editor);
                    editor.requestFocus();
                }
            });
            rotatePanel.add(rotateCounterclockwiseButton);
        }
    }

    @Override
    public void activated(final GraphEditor editor) {
        super.activated(editor);
        currentNode = null;
        isPanMode = false;
        isTextMode = false;
    }

    @Override
    public void deactivated(GraphEditor editor) {
        editor.getModel().selectNone();
        currentNode = null;
    }

    @Override
    public boolean isDragging() {
        return dragState != DrugState.NONE;
    }

    @Override
    public void mouseClicked(GraphEditorMouseEvent e) {
        if (ignoreMouseButton1 && (e.getButton() == MouseEvent.BUTTON1)) {
            return;
        }
        if (ignoreMouseButton3 && (e.getButton() == MouseEvent.BUTTON3)) {
            return;
        }

        VisualModel model = e.getModel();
        GraphEditor editor = e.getEditor();
        Point2D position = e.getPosition();
        if (e.getButton() == MouseEvent.BUTTON1) {
            Node node = HitMan.hitFirstInCurrentLevel(position, model);
            if (node == null) {
                if (e.getClickCount() > 1) {
                    if (model.getCurrentLevel() instanceof VisualGroup) {
                        VisualGroup currentGroup = (VisualGroup) model.getCurrentLevel();
                        Rectangle2D bbInLocalSpace = currentGroup.getBoundingBoxInLocalSpace();
                        Point2D posInRootSpace = currentGroup.getRootSpacePosition();
                        Rectangle2D bbInRootSpace = BoundingBoxHelper.move(bbInLocalSpace, posInRootSpace);
                        if (!bbInRootSpace.contains(position)) {
                            changeLevelUp(editor);
                            return;
                        }
                    }
                    if (model.getCurrentLevel() instanceof VisualPage) {
                        VisualPage currentPage = (VisualPage) model.getCurrentLevel();
                        Rectangle2D bbInLocalSpace = currentPage.getBoundingBoxInLocalSpace();
                        Point2D posInRootSpace = currentPage.getRootSpacePosition();
                        Rectangle2D bbInRootSpace = BoundingBoxHelper.move(bbInLocalSpace, posInRootSpace);
                        if (!bbInRootSpace.contains(position)) {
                            changeLevelUp(editor);
                            return;
                        }
                    }

                } else {
                    if (e.getKeyModifiers() == 0) {
                        model.selectNone();
                    }
                }
            } else {
                if (e.getClickCount() > 1) {
                    if (node instanceof VisualGroup || node instanceof VisualPage) {
                        changeLevelDown(editor);
                        return;

                    } else if (node instanceof VisualComment) {
                        VisualComment comment = (VisualComment) node;
                        editLabelInPlace(editor, comment, comment.getLabel());
                        return;
                    }
                } else {
                    switch (e.getKeyModifiers()) {
                    case 0:
                        model.select(node);
                        break;
                    case MouseEvent.SHIFT_DOWN_MASK:
                        model.addToSelection(node);
                        break;
                    case MouseEvent.CTRL_DOWN_MASK:
                        if (DesktopApi.getMenuKeyMouseMask() == MouseEvent.CTRL_DOWN_MASK) {
                            model.removeFromSelection(node);
                        }
                        break;
                    case MouseEvent.META_DOWN_MASK:
                        if (DesktopApi.getMenuKeyMouseMask() == MouseEvent.META_DOWN_MASK) {
                            model.removeFromSelection(node);
                        }
                        break;
                    }
                }
            }
            anchorGenerator.mouseClicked(e);
        }

        if (e.getButton() == MouseEvent.BUTTON3) {
            VisualNode node = hitTestPopup(model, position);
            JPopupMenu popup = createPopupMenu(node, editor);
            if (popup != null) {
                if (node == null) {
                    model.selectNone();
                } else {
                    model.select(node);
                }
                MouseEvent systemEvent = e.getSystemEvent();
                popup.show(systemEvent.getComponent(), systemEvent.getX(), systemEvent.getY());
            }
        }
    }

    public VisualNode hitTestPopup(VisualModel model, Point2D position) {
        return (VisualNode) HitMan.hitFirstInCurrentLevel(position, model);
    }

    public JPopupMenu createPopupMenu(Node node, final GraphEditor editor) {
        JPopupMenu popup = null;
        WorkspaceEntry we = editor.getWorkspaceEntry();
        List<Command> applicableTools = new ArrayList<>();
        HashSet<Command> enabledTools = new HashSet<>();
        for (Command tool: Commands.getApplicableCommands(we)) {
            if (tool instanceof NodeTransformer) {
                NodeTransformer nodeTransformer = (NodeTransformer) tool;
                if (nodeTransformer.isApplicableTo(node)) {
                    applicableTools.add(tool);
                    ModelEntry me = we.getModelEntry();
                    if (nodeTransformer.isEnabled(me, node)) {
                        enabledTools.add(tool);
                    }
                }
            }
        }
        if (!applicableTools.isEmpty()) {
            popup = new JPopupMenu();
            popup.setFocusable(false);
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            for (Command tool: applicableTools) {
                PopupToolAction toolAction = new PopupToolAction(tool);
                ActionMenuItem miTool = new ActionMenuItem(toolAction);
                miTool.addScriptedActionListener(mainWindow.getDefaultActionListener());
                miTool.setEnabled(enabledTools.contains(tool));
                popup.add(miTool);
            }
        }
        return popup;
    }

    @Override
    public void mouseMoved(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        if (dragState == DrugState.MOVE) {
            Point2D prevPos = e.getPrevPosition();
            Point2D.Double pos1 = new Point2D.Double(prevPos.getX() + offset.getX(), prevPos.getY() + offset.getY());
            Point2D snapPos1 = editor.snap(pos1, snaps);
            Point2D.Double pos2 = new Point2D.Double(e.getX() + offset.getX(), e.getY() + offset.getY());
            Point2D snapPos2 = editor.snap(pos2, snaps);
            // Intermediate move of the selection - no need for beforeSelectionModification or afterSelectionModification
            VisualModelTransformer.translateSelection(model, snapPos2.getX() - snapPos1.getX(), snapPos2.getY() - snapPos1.getY());
        } else if (dragState == DrugState.SELECT) {
            selected.clear();
            selected.addAll(model.hitBox(e.getStartPosition(), e.getPosition()));
            selectionBox = getSelectionRect(e.getStartPosition(), e.getPosition());
            editor.repaint();
        } else {
            Node node = HitMan.hitFirstInCurrentLevel(e.getPosition(), model);
            if (currentNode != node) {
                currentNode = (VisualNode) node;
                editor.repaint();
            }
        }
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            ignoreMouseButton1 = false;
        } else if (e.getButton() == MouseEvent.BUTTON3) {
            ignoreMouseButton3 = false;
            if (isDragging()) {
                cancelDrag(e.getEditor());
            }
        }
    }

    @Override
    public void startDrag(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        VisualModel model = editor.getModel();
        if (e.getButtonModifiers() == MouseEvent.BUTTON1_DOWN_MASK) {
            Point2D startPos = e.getStartPosition();
            Node hitNode = HitMan.hitFirstInCurrentLevel(startPos, model);

            if (hitNode == null) {
                // If hit nothing then start select-drag
                switch (e.getKeyModifiers()) {
                case 0:
                    selectionMode = SelectionMode.REPLACE;
                    break;
                case MouseEvent.CTRL_DOWN_MASK:
                    if (DesktopApi.getMenuKeyMouseMask() == MouseEvent.CTRL_DOWN_MASK) {
                        selectionMode = SelectionMode.REMOVE;
                    }
                    break;
                case MouseEvent.META_DOWN_MASK:
                    if (DesktopApi.getMenuKeyMouseMask() == MouseEvent.META_DOWN_MASK) {
                        selectionMode = SelectionMode.REMOVE;
                    }
                    break;
                case MouseEvent.SHIFT_DOWN_MASK:
                    selectionMode = SelectionMode.ADD;
                    break;
                default:
                    selectionMode = SelectionMode.NONE;
                }

                if (selectionMode != SelectionMode.NONE) {
                    // selection will not actually be changed until drag completes
                    dragState = DrugState.SELECT;
                    selected.clear();
                    if (selectionMode == SelectionMode.REPLACE) {
                        model.selectNone();
                    } else {
                        selected.addAll(model.getSelection());
                    }
                }
            } else if ((e.getKeyModifiers() == 0) && (hitNode instanceof VisualNode)) {
                // If mouse down without modifiers and hit something then begin move-drag
                dragState = DrugState.MOVE;
                VisualNode node = (VisualNode) hitNode;
                if ((node != null) && !model.getSelection().contains(node)) {
                    model.select(node);
                }
                AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(node);
                Point2D pos = TransformHelper.transform(node, localToRootTransform).getCenter();
                snaps = editor.getSnaps(node);
                Point2D snapPos = editor.snap(pos, snaps);
                offset = new Point2D.Double(snapPos.getX() - pos.getX(), snapPos.getY() - pos.getY());
                // Initial move of the selection - beforeSelectionModification is needed
                beforeSelectionModification(editor);
                VisualModelTransformer.translateSelection(model, offset.getX(), offset.getY());
            } else {
                // Do nothing if pressed on a node with modifiers
            }
        }
    }

    @Override
    public void finishDrag(GraphEditorMouseEvent e) {
        GraphEditor editor = e.getEditor();
        if (dragState == DrugState.MOVE) {
            // Final move of the selection - afterSelectionModification is needed
            afterSelectionModification(editor);
        } else if (dragState == DrugState.SELECT) {
            VisualModel model = e.getModel();
            if (selectionMode == SelectionMode.REPLACE) {
                model.select(selected);
            } else if (selectionMode == SelectionMode.ADD) {
                model.addToSelection(selected);
            } else if (selectionMode == SelectionMode.REMOVE) {
                model.removeFromSelection(selected);
            }
            selectionBox = null;
        }
        dragState = DrugState.NONE;
        selected.clear();
        editor.repaint();
    }

    private void cancelDrag(GraphEditor editor) {
        if (dragState == DrugState.MOVE) {
            editor.getWorkspaceEntry().cancelMemento();
        } else if (dragState == DrugState.SELECT) {
            selected.clear();
            selectionBox = null;
        }
        dragState = DrugState.NONE;
        ignoreMouseButton1 = true;
        ignoreMouseButton3 = true;
        editor.repaint();
    }

    @Override
    public boolean keyPressed(GraphEditorKeyEvent e) {
        GraphEditor editor = e.getEditor();
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
            if (isDragging()) {
                cancelDrag(editor);
            } else {
                cancelSelection(editor);
            }
            return true;
        case KeyEvent.VK_PAGE_UP:
            changeLevelUp(editor);
            return true;
        case KeyEvent.VK_PAGE_DOWN:
            changeLevelDown(editor);
            return true;
        }

        if (!e.isMenuKeyDown() && !e.isShiftDown()) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                offsetSelection(editor, -1, 0);
                return true;
            case KeyEvent.VK_RIGHT:
                offsetSelection(editor, 1, 0);
                return true;
            case KeyEvent.VK_UP:
                offsetSelection(editor, 0, -1);
                return true;
            case KeyEvent.VK_DOWN:
                offsetSelection(editor, 0, 1);
                return true;
            }
        }

        if (enablePaging && e.isAltDown() && !e.isMenuKeyDown()) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_G:
                if (e.isShiftDown()) {
                    ungroupSelection(editor);
                } else {
                    pageSelection(editor);
                }
                return true;
            }
        }

        if (enableGroupping && e.isMenuKeyDown() && !e.isAltDown()) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_G:
                if (e.isShiftDown()) {
                    ungroupSelection(editor);
                } else {
                    groupSelection(editor);
                }
                return true;
            }
        }

        if (e.getKeyCode() == DesktopApi.getMenuKeyCode()) {
            isPanMode = true;
            editor.forceRedraw();
            return true;
        }

        return super.keyPressed(e);
    }

    @Override
    public boolean keyReleased(GraphEditorKeyEvent e) {
        if (e.getKeyCode() == DesktopApi.getMenuKeyCode()) {
            isPanMode = false;
            e.getEditor().forceRedraw();
            return true;
        }
        return super.keyReleased(e);
    }

    @Override
    public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
        if ((dragState == DrugState.SELECT) && (selectionBox != null)) {
            Viewport viewport = editor.getViewport();
            g.setStroke(new BasicStroke((float) viewport.pixelSizeInUserSpace().getX()));
            g.setColor(selectionFillColor);
            g.fill(selectionBox);
            g.setColor(selectionBorderColor);
            g.draw(selectionBox);
        }
    }

    @Override
    public Decorator getDecorator(final GraphEditor editor) {
        return new Decorator() {

            @Override
            public Decoration getDecoration(Node node) {
                VisualModel model = editor.getModel();
                if (node == currentNode) {
                    return Decoration.Highlighted.INSTANCE;
                }

                if (node == model.getCurrentLevel()) {
                    return Decoration.Empty.INSTANCE;
                }

                if (node == model.getRoot()) {
                    return Decoration.Shaded.INSTANCE;
                }
                /*
                 * r & !c & s | !r & (c | s) <=> (!r & c) | (!c & s)
                 * where
                 *   r = (selectionMode == SelectionState.REMOVE)
                 *   c = selected.contains(node)
                 *   s = model.getSelection().contains(node)
                 */
                if (((selectionMode != SelectionMode.REMOVE) && selected.contains(node))
                        || (!selected.contains(node) && model.getSelection().contains(node))) {
                    return Decoration.Selected.INSTANCE;
                }

                //if (node == mouseOverNode) return selectedDecoration;

                return null;
            }
        };
    }

    public void editLabelInPlace(final GraphEditor editor, final VisualComponent component, String initialText) {
        // Create a text pane without wrapping
        final JTextPane textPane = new JTextPane() {
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };
        textPane.setText(initialText.replace("|", "\n"));

        // Align the text to centre
        SimpleAttributeSet alignment = new SimpleAttributeSet();
        StyleConstants.setAlignment(alignment, component.getLabelAlignment().toStyleConstant());
        StyledDocument document = textPane.getStyledDocument();
        document.setParagraphAttributes(0, document.getLength(), alignment, false);

        // Set font size similar to the current editor scale
        float fontSize = VisualComponent.labelFont.getSize2D() * (float) editor.getViewport().getTransform().getScaleY();
        textPane.setFont(VisualComponent.labelFont.deriveFont(fontSize));

        // Add scroll vertical bar (is necessary)
        final JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Set the size of the scrollable text editor panel
        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(component);
        Rectangle2D bbRoot = TransformHelper.transform(component, localToRootTransform).getBoundingBox();
        Rectangle bbScreen = editor.getViewport().userToScreen(BoundingBoxHelper.expand(bbRoot, bbRoot.getWidth(), 2.0));
        scrollPane.setBounds(bbScreen.x, bbScreen.y, bbScreen.width, bbScreen.height);

        // Show the text editor panel
        editor.getOverlay().add(scrollPane);
        textPane.requestFocusInWindow();

        textPane.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                if (arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelInPlaceEdit = true;
                    editor.requestFocus();
                }
                if ((arg0.getModifiers() == DesktopApi.getMenuKeyMask()) && (arg0.getKeyCode() == KeyEvent.VK_ENTER)) {
                    cancelInPlaceEdit = false;
                    editor.requestFocus();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyTyped(KeyEvent arg0) {
            }
        });

        textPane.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent arg0) {
                editor.getWorkspaceEntry().setCanModify(false);
                cancelInPlaceEdit = false;
                isTextMode = true;
            }

            @Override
            public void focusLost(FocusEvent arg0) {
                final String newText = textPane.getText().replace("\n", "|");
                scrollPane.getParent().remove(scrollPane);
                if (!cancelInPlaceEdit) {
                    try {
                        editor.getWorkspaceEntry().saveMemento();
                        component.setLabel(newText);
                    } catch (ArgumentException e) {
                        JOptionPane.showMessageDialog(null, e.getMessage());
                        editLabelInPlace(editor, component, newText);
                    }
                }
                isTextMode = false;
                editor.getWorkspaceEntry().setCanModify(true);
                editor.repaint();
            }
        });
    }

    protected void changeLevelDown(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        Collection<Node> selection = model.getSelection();
        if (selection.size() == 1) {
            Node node = selection.iterator().next();
            if (node instanceof Container) {
                editor.getWorkspaceEntry().saveMemento();
                model.setCurrentLevel((Container) node);
                currentNode = null;
                editor.repaint();
            }
        }
    }

    protected void changeLevelUp(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        Container level = model.getCurrentLevel();
        Container parent = Hierarchy.getNearestAncestor(level.getParent(), Container.class);
        if (parent != null) {
            editor.getWorkspaceEntry().saveMemento();
            model.setCurrentLevel(parent);
            model.addToSelection(level);
            currentNode = null;
            editor.repaint();
        }
    }

    private Rectangle2D getSelectionRect(Point2D startPosition, Point2D currentPosition) {
        return new Rectangle2D.Double(
                Math.min(startPosition.getX(), currentPosition.getX()),
                Math.min(startPosition.getY(), currentPosition.getY()),
                Math.abs(startPosition.getX() - currentPosition.getX()),
                Math.abs(startPosition.getY() - currentPosition.getY())
        );
    }

    protected void cancelSelection(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            final Framework framework = Framework.getInstance();
            final MainWindow mainWindow = framework.getMainWindow();
            mainWindow.selectNone();
            editor.forceRedraw();
        }
        editor.requestFocus();
    }

    private void offsetSelection(final GraphEditor editor, double dx, double dy) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.translateSelection(model, dx, dy);
            afterSelectionModification(editor);
        }
    }

    protected void groupSelection(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            model.groupSelection();
            afterSelectionModification(editor);
        }
    }

    protected void ungroupSelection(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            model.ungroupSelection();
            afterSelectionModification(editor);
        }
    }

    protected void pageSelection(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            model.groupPageSelection();
            afterSelectionModification(editor);
        }
    }

    protected void rotateSelectionClockwise(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.rotateSelection(model, Math.PI / 2);
            for (Node node : model.getSelection()) {
                if (node instanceof Rotatable) {
                    ((Rotatable) node).rotateClockwise();
                }
            }
            afterSelectionModification(editor);
        }
    }

    protected void rotateSelectionCounterclockwise(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.rotateSelection(model, -Math.PI / 2);
            for (Node node : model.getSelection()) {
                if (node instanceof Rotatable) {
                    ((Rotatable) node).rotateCounterclockwise();
                }
            }
            afterSelectionModification(editor);
        }
    }

    protected void flipSelectionHorizontal(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.scaleSelection(model, -1.0, 1.0);
            for (Node node : model.getSelection()) {
                if (node instanceof Flippable) {
                    ((Flippable) node).flipHorizontal();
                }
            }
            afterSelectionModification(editor);
        }
    }

    protected void flipSelectionVertical(final GraphEditor editor) {
        VisualModel model = editor.getModel();
        if (!model.getSelection().isEmpty()) {
            beforeSelectionModification(editor);
            VisualModelTransformer.scaleSelection(model, 1.0, -1.0);
            for (Node node : model.getSelection()) {
                if (node instanceof Flippable) {
                    ((Flippable) node).flipVertical();
                }
            }
            afterSelectionModification(editor);
        }
    }

    public void beforeSelectionModification(final GraphEditor editor) {
        // Capture model memento for use in afterSelectionModification
        editor.getWorkspaceEntry().captureMemento();
    }

    public void afterSelectionModification(final GraphEditor editor) {
        // Save memento that was captured in beforeSelectionModification
        editor.getWorkspaceEntry().saveMemento();
        // Redraw the editor window to recalculate all the bounding boxes
        editor.forceRedraw();
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        if (isPanMode) {
            return "Use " + DesktopApi.getMenuKeyMaskName() + " + RMB to pan the view.";
        } else if (isTextMode) {
            return "Use " + DesktopApi.getMenuKeyMaskName() + " + Enter to finish text editing.";
        }
        return super.getHintText(editor);
    }

}
