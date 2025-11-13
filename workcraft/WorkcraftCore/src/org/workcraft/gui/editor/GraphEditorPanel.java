package org.workcraft.gui.editor;

import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.gui.*;
import org.workcraft.gui.actions.ActionButton;
import org.workcraft.gui.properties.*;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.gui.tools.GraphEditorTool;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.DesktopApi;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GraphEditorPanel extends JPanel implements StateObserver, GraphEditor {

    private static final String RESET_TO_DEFAULTS = "Reset to defaults";
    private static final String TITLE_PROPERTY_EDITOR = "Property editor";
    private static final String TITLE_SUFFIX_TEMPLATE = "template";
    private static final String TITLE_SUFFIX_MODEL = "model";
    private static final String TITLE_SUFFIX_SINGLE_ITEM = "single item";
    private static final String TITLE_SUFFIX_SELECTED_ITEMS = " selected items";
    private static final int VIEWPORT_MARGIN = 25;
    private static final int STEP = 20;

    private final class UpdateEditorActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (updateEditorPanelRequested) {
                SwingUtilities.invokeLater(GraphEditorPanel.this::updateEditor);
            }
        }
    }

    private final class UpdatePropertyActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (updatePropertyViewRequested) {
                SwingUtilities.invokeLater(GraphEditorPanel.this::updatePropertyView);
            }
        }
    }

    public class GraphEditorFocusListener implements FocusListener {
        private final GraphEditorPanel editor;

        public GraphEditorFocusListener(GraphEditorPanel editor) {
            this.editor = editor;
        }

        @Override
        public void focusGained(FocusEvent e) {
            final Framework framework = Framework.getInstance();
            MainWindow mainWindow = framework.getMainWindow();
            mainWindow.requestFocus(editor);
            repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            repaint();
        }
    }

    class Resizer implements ComponentListener {

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            reshape();
            repaint();
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }
    }

    private static final long serialVersionUID = 1L;

    public WorkspaceEntry we;

    protected final Toolbox toolbox;

    protected Viewport view;
    protected Grid grid;
    protected Ruler ruler;
    protected ActionButton centerButton;

    private static final int size = SizeHelper.getRulerSize();
    protected Stroke borderStroke = new BasicStroke(2);
    private final EditorOverlay overlay = new EditorOverlay();
    private boolean firstPaint = true;
    private boolean updateEditorPanelRequested = true;
    private boolean updatePropertyViewRequested = true;

    public GraphEditorPanel(WorkspaceEntry we) {
        super(new BorderLayout());
        this.we = we;

        we.addObserver(this);

        view = new Viewport(0, 0, getWidth(), getHeight());
        grid = new Grid();

        ruler = new Ruler(size);
        view.addListener(grid);
        grid.addListener(ruler);

        centerButton = new ActionButton(null, MainWindowActions.VIEW_PAN_CENTER);
        centerButton.setSize(size, size);
        this.add(centerButton);

        toolbox = new Toolbox(this);

        GraphEditorPanelMouseListener mouseListener = new GraphEditorPanelMouseListener(this, toolbox);
        GraphEditorPanelKeyListener keyListener = new GraphEditorPanelKeyListener(this, toolbox);
        GraphEditorFocusListener focusListener = new GraphEditorFocusListener(this);

        addMouseMotionListener(mouseListener);
        addMouseListener(mouseListener);
        addMouseWheelListener(mouseListener);
        addKeyListener(keyListener);
        addFocusListener(focusListener);
        addComponentListener(new Resizer());

        add(overlay, BorderLayout.CENTER);

        // FIXME: timers need to be stopped at some point
        Timer updateEditorPanelTimer = new Timer(EditorCommonSettings.getRedrawInterval(), new UpdateEditorActionListener());
        updateEditorPanelTimer.start();
        Timer updatePropertyTimer = new Timer(EditorCommonSettings.getRedrawInterval(), new UpdatePropertyActionListener());
        updatePropertyTimer.start();

        // This is a hack to prevent editor panel from loosing focus on Ctrl-UP key combination
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, DesktopApi.getMenuKeyMask()), "doNothing");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, DesktopApi.getMenuKeyMask()), "doNothing");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, DesktopApi.getMenuKeyMask()), "doNothing");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, DesktopApi.getMenuKeyMask()), "doNothing");
        this.getActionMap().put("doNothing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //do nothing
            }
        });
    }

    private void reshape() {
        view.setShape(0, 0, getWidth(), getHeight());
        ruler.setShape(0, 0, getWidth(), getHeight());
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        AffineTransform screenTransform = (AffineTransform) g2d.getTransform().clone();

        g2d.setBackground(EditorCommonSettings.getBackgroundColor());
        g2d.clearRect(0, 0, getWidth(), getHeight());

        if (EditorCommonSettings.getGridVisibility()) {
            grid.draw(g2d);
        }
        g2d.setTransform(screenTransform);

        if (firstPaint) {
            reshape();
            firstPaint = false;
        }
        g2d.transform(view.getTransform());

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        //g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        GraphEditorTool tool = toolbox.getSelectedTool();
        if (tool != null) {
            getModel().draw(g2d, tool.getDecorator(this));
            if (hasFocus()) {
                tool.drawInUserSpace(this, g2d);
            }
        }
        g2d.setTransform(screenTransform);

        boolean rulerVisibility = EditorCommonSettings.getRulerVisibility();
        if (rulerVisibility) {
            ruler.draw(g2d);
        }
        centerButton.setVisible(rulerVisibility);

        if (hasFocus()) {
            if (tool != null) {
                tool.drawInScreenSpace(this, g2d);
            }
            g2d.setTransform(screenTransform);
            g2d.setStroke(borderStroke);
            g2d.setColor(VisualCommonSettings.getBorderColor());
            g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }
        paintChildren(g2d);
    }

    @Override
    /*
      Redraw one pixel to force redrawing of the whole model. This is usually necessary
      to recalculate bounding boxes of children components and correctly estimate the
      bounding boxes of their parents.
     */
    public void forceRedraw() {
        super.paintImmediately(0, 0, 1, 1);
        repaint();
    }

    @Override
    public VisualModel getModel() {
        return we.getModelEntry().getVisualModel();
    }

    @Override
    public Viewport getViewport() {
        return view;
    }

    private Set<Point2D> calcConnectionSnaps(VisualConnection vc) {
        Set<Point2D> result = new HashSet<>();
        result.add(vc.getSecondCenter());
        result.add(vc.getFirstCenter());
        return result;
    }

    private Set<Point2D> getTransformableNodeSnaps(VisualTransformableNode transformableNode) {
        Set<Point2D> result = new HashSet<>();
        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(transformableNode);
        Point2D pos = TransformHelper.transform(transformableNode, localToRootTransform).getCenter();
        result.add(pos);
        for (VisualConnection connection : getModel().getConnections(transformableNode)) {
            if (connection.getConnectionType() == ConnectionType.POLYLINE) {
                Polyline polyline = (Polyline) connection.getGraphic();
                ControlPoint firstControlPoint = polyline.getFirstControlPoint();
                if ((transformableNode == connection.getFirst()) && (firstControlPoint != null)) {
                    result.add(firstControlPoint.getPosition());
                }
                ControlPoint lastControlPoint = polyline.getLastControlPoint();
                if ((transformableNode == connection.getSecond()) && (lastControlPoint != null)) {
                    result.add(lastControlPoint.getPosition());
                }
            }
            result.addAll(calcConnectionSnaps(connection));
        }
        return result;
    }

    private Set<Point2D> getControlPointSnaps(ControlPoint cp) {
        Set<Point2D> result = new HashSet<>();
        Node graphics = cp.getParent();
        if (graphics instanceof Polyline) {
            Polyline polyline = (Polyline) cp.getParent();
            result.add(cp.getPosition());
            result.add(polyline.getPrevAnchorPointLocation(cp));
            result.add(polyline.getNextAnchorPointLocation(cp));
        }
        Node parent = graphics.getParent();
        if (parent instanceof VisualConnection vc) {
            result.addAll(calcConnectionSnaps(vc));
        }
        return result;
    }

    @Override
    public Set<Point2D> getSnaps(VisualNode node) {
        Set<Point2D> result = new HashSet<>();
        if (node instanceof ControlPoint) {
            result.addAll(getControlPointSnaps((ControlPoint) node));
        } else if (node instanceof VisualTransformableNode) {
            result.addAll(getTransformableNodeSnaps((VisualTransformableNode) node));
        }
        return result;
    }

    @Override
    public Point2D snap(Point2D pos, Set<Point2D> snaps) {
        double x = grid.snapCoordinate(pos.getX(), 2);
        double y = grid.snapCoordinate(pos.getY(), 2);
        if (snaps != null) {
            for (Point2D snap: snaps) {
                if (Math.abs(pos.getX() - snap.getX()) < Math.abs(pos.getX() - x)) {
                    x = snap.getX();
                }
                if (Math.abs(pos.getY() - snap.getY()) < Math.abs(pos.getY() - y)) {
                    y = snap.getY();
                }
            }
        }
        return new Point2D.Double(x, y);
    }

    @Override
    public WorkspaceEntry getWorkspaceEntry() {
        return we;
    }

    private Properties wrapProperties(final ModelProperties properties) {
        return () -> {
            Collection<PropertyDescriptor<?>> list = new ArrayList<>();
            for (final PropertyDescriptor<?> descriptor : properties.getDescriptors()) {
                if (descriptor.isVisible()) {
                    list.add(wrapProperty(descriptor));
                }
            }
            return list;
        };
    }

    private <V> PropertyDescriptor<V> wrapProperty(PropertyDescriptor<V> descriptor) {
        return new PropertyDerivative<>(descriptor) {
            @Override
            public void setValue(V value) {
                if (descriptor.getType().isEnum() && (value == null)) {
                    // Prevent null value in ComboBox-style properties (e.g. due to canceled assignment to several nodes)
                    return;
                }
                we.saveMemento();
                we.setChanged(true);
                super.setValue(value);
            }
        };
    }

    public void updateToolsView() {
        final Framework framework = Framework.getInstance();
        final MainWindow mainWindow = framework.getMainWindow();

        JToolBar modelToolbar = mainWindow.getModelToolbar();
        modelToolbar.removeAll();
        if (toolbox != null) {
            toolbox.setToolsForModel(modelToolbar);

            GraphEditorTool selectedTool = toolbox.getSelectedTool();
            if (selectedTool != null) {
                JToolBar controlToolbar = mainWindow.getControlToolbar();
                controlToolbar.removeAll();
                selectedTool.updateControlsToolbar(controlToolbar, this);

                JPanel panel = selectedTool.getControlsPanel(this);
                ToolControlsWindow controlWindow = mainWindow.getControlsView();
                controlWindow.setContent(panel);
            }
        }
    }

    public void updatePropertyView() {
        final Framework framework = Framework.getInstance();
        if (!framework.isInGuiMode()) {
            return;
        }
        ModelProperties properties;
        final VisualNode defaultNode = we.getDefaultNode();
        final VisualNode templateNode = we.getTemplateNode();
        String title = TITLE_PROPERTY_EDITOR;
        if (templateNode != null) {
            properties = ModelPropertyUtils.getTemplateProperties(getModel(), templateNode);
            title += " [" + TITLE_SUFFIX_TEMPLATE + "]";
        } else {
            final VisualModel model = getModel();
            properties = ModelPropertyUtils.getSelectionProperties(model);
            final Collection<? extends VisualNode> selection = model.getSelection();
            if (selection.isEmpty()) {
                title += " [" + TITLE_SUFFIX_MODEL + "]";
            } else if (selection.size() == 1) {
                title += " [" + TITLE_SUFFIX_SINGLE_ITEM + "]";
            } else {
                final int nodeCount = selection.size();
                title += " [" + nodeCount + " " + TITLE_SUFFIX_SELECTED_ITEMS + "]";
            }
        }

        final MainWindow mainWindow = framework.getMainWindow();
        final PropertyEditorWindow propertyEditorWindow = mainWindow.getPropertyView();
        GraphEditorTool tool = (toolbox == null) ? null : toolbox.getSelectedTool();
        if ((tool == null) || !tool.requiresPropertyEditor() || properties.getDescriptors().isEmpty()) {
            propertyEditorWindow.clear();
        } else {
            propertyEditorWindow.set(wrapProperties(properties));
            if ((templateNode != null) && (defaultNode != null)) {
                JButton resetButton = new JButton(RESET_TO_DEFAULTS);
                resetButton.addActionListener(event -> {
                    templateNode.copyStyle(defaultNode);
                    updatePropertyViewRequested = true;
                });
                propertyEditorWindow.add(resetButton, BorderLayout.SOUTH);
                // A hack to display reset button: toggle its visibility a couple of times.
                resetButton.setVisible(false);
                resetButton.setVisible(true);
            }
        }
        mainWindow.setPropertyEditorTitle(title);
        updatePropertyViewRequested = false;
    }


    private void updateEditor() {
        super.repaint();
        updateEditorPanelRequested = false;
    }

    @Override
    public void repaint() {
        updateEditorPanelRequested = true;
    }

    @Override
    public void notify(StateEvent e) {
        updatePropertyViewRequested = true;
        updateEditorPanelRequested = true;
    }

    @Override
    public Overlay getOverlay() {
        return overlay;
    }

    @Override
    public Toolbox getToolBox() {
        return toolbox;
    }

    @Override
    public void zoomIn() {
        SwingUtilities.invokeLater(() -> {
            getViewport().zoom(1);
            repaint();
            requestFocus();
        });
    }

    @Override
    public void zoomOut() {
        SwingUtilities.invokeLater(() -> {
            getViewport().zoom(-1);
            repaint();
            requestFocus();
        });
    }

    @Override
    public void zoomDefault() {
        SwingUtilities.invokeLater(() -> {
            getViewport().scaleDefault();
            repaint();
            requestFocus();
        });
    }

    @Override
    public void zoomFit() {
        SwingUtilities.invokeLater(() -> {
            Viewport viewport = getViewport();
            Rectangle2D viewportBox = viewport.getShape();
            VisualModel model = getModel();
            Collection<Touchable> nodes = Hierarchy.getChildrenOfType(model.getRoot(), Touchable.class);
            if (!model.getSelection().isEmpty()) {
                nodes.retainAll(model.getSelection());
            }
            Rectangle2D modelBox = BoundingBoxHelper.mergeBoundingBoxes(nodes);
            if ((modelBox != null) && (viewportBox != null)) {
                double scale = getScale(viewportBox, modelBox);
                viewport.scale(scale);
                panCenter();
            }
        });
    }

    private static double getScale(Rectangle2D viewportBox, Rectangle2D modelBox) {
        double ratioX = 1.0;
        double ratioY = 1.0;
        if ((viewportBox.getWidth() > VIEWPORT_MARGIN) && (viewportBox.getHeight() > VIEWPORT_MARGIN)) {
            double minDimension = Math.min(viewportBox.getWidth(), viewportBox.getHeight());
            ratioX = (viewportBox.getWidth() - VIEWPORT_MARGIN) / minDimension;
            ratioY = (viewportBox.getHeight() - VIEWPORT_MARGIN) / minDimension;
        }
        double scaleX = ratioX / modelBox.getWidth();
        double scaleY = ratioY / modelBox.getHeight();
        return Math.min(scaleX, scaleY);
    }

    @Override
    public void panLeft(boolean largeStep) {
        int step = largeStep ? getWidth() : STEP;
        panInEventDispatchThread(step, 0);
    }

    @Override
    public void panUp(boolean largeStep) {
        int step = largeStep ? getHeight() : STEP;
        panInEventDispatchThread(0, step);
    }

    @Override
    public void panRight(boolean largeStep) {
        int step = largeStep ? getWidth() : STEP;
        panInEventDispatchThread(-step, 0);
    }

    @Override
    public void panDown(boolean largeStep) {
        int step = largeStep ? getHeight() : STEP;
        panInEventDispatchThread(0, -step);
    }

    @Override
    public void panCenter() {
        Viewport viewport = getViewport();
        Rectangle2D viewportBox = viewport.getShape();
        VisualModel model = getModel();
        Collection<Touchable> nodes = Hierarchy.getChildrenOfType(model.getRoot(), Touchable.class);
        if (!model.getSelection().isEmpty()) {
            nodes.retainAll(model.getSelection());
        }
        Rectangle2D modelBox = BoundingBoxHelper.mergeBoundingBoxes(nodes);
        if ((modelBox != null) && (viewportBox != null)) {
            int viewportCenterX = (int) Math.round(viewportBox.getCenterX());
            int viewportCenterY = (int) Math.round(viewportBox.getCenterY());
            Point2D modelCenter = new Point2D.Double(modelBox.getCenterX(), modelBox.getCenterY());
            Point modelCenterInScreenSpace = viewport.userToScreen(modelCenter);
            panInEventDispatchThread(viewportCenterX - modelCenterInScreenSpace.x, viewportCenterY - modelCenterInScreenSpace.y);
        }
    }

    public void panInEventDispatchThread(int dx, int dy) {
        SwingUtilities.invokeLater(() -> {
            getViewport().pan(dx, dy);
            repaint();
            requestFocus();
        });
    }

}
