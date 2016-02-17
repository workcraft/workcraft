/*
 *
 * Copyright 2008,2009 Newcastle University
 *
 * This file is part of Workcraft.
 *
 * Workcraft is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Workcraft is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.workcraft.gui.graph;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.BoundingBoxHelper;
import org.workcraft.dom.visual.Dependent;
import org.workcraft.dom.visual.Touchable;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Overlay;
import org.workcraft.gui.PropertyEditorWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.gui.propertyeditor.ModelProperties;
import org.workcraft.gui.propertyeditor.Properties;
import org.workcraft.gui.propertyeditor.PropertyDescriptor;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.shared.CommonEditorSettings;
import org.workcraft.plugins.shared.CommonVisualSettings;
import org.workcraft.util.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;

public class GraphEditorPanel extends JPanel implements StateObserver, GraphEditor {

    public static final String TITLE_SUFFIX_TEMPLATE = "template";
    public static final String TITLE_SUFFIX_MODEL = "model";
    public static final String TITLE_SUFFIX_SINGLE_ELEMENT = "single element";
    public static final String TITLE_SUFFIX_SELECTED_ELEMENTS = " selected elements";
    private static final int VIEWPORT_MARGIN = 25;

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

    public class GraphEditorFocusListener implements FocusListener {
        @Override
        public void focusGained(FocusEvent e) {
            repaint();
        }

        @Override
        public void focusLost(FocusEvent e) {
            repaint();
        }
    }

    private static final long serialVersionUID = 1L;

    public WorkspaceEntry workspaceEntry;

    protected final MainWindow mainWindow;
    protected final ToolboxPanel toolboxPanel;

    protected Viewport view;
    protected Grid grid;
    protected Ruler ruler;

    protected JPanel modeSelector;
    protected JToggleButton gridToggler;
    protected JToggleButton nameToggler;
    protected JToggleButton labelToggler;
    protected JToggleButton rulerToggler;
    private int size = 15;
    protected Stroke borderStroke = new BasicStroke(2);
    private Overlay overlay = new Overlay();
    private boolean firstPaint = true;
    private boolean updateEditorPanelRequested = true;
    private boolean updatePropertyViewRequested = true;

    public GraphEditorPanel(MainWindow mainWindow, WorkspaceEntry workspaceEntry) {
        super(new BorderLayout());
        this.mainWindow = mainWindow;
        this.workspaceEntry = workspaceEntry;

        workspaceEntry.addObserver(this);

        view = new Viewport(0, 0, getWidth(), getHeight());
        grid = new Grid();

        ruler = new Ruler(size);
        view.addListener(grid);
        grid.addListener(ruler);

        modeSelector = new JPanel(new GridLayout(2, 2));
        modeSelector.setSize(size, size);
        this.add(modeSelector);

        int size2 = size / 2 + 1;
        gridToggler = new JToggleButton();
        gridToggler.setSize(size2, size2);
        gridToggler.setFocusable(false);
        gridToggler.setToolTipText("Show/hide grid");
        gridToggler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CommonEditorSettings.setShowGrid(!CommonEditorSettings.getShowGrid());
                repaint();
            }
        });
        modeSelector.add(gridToggler);

        nameToggler = new JToggleButton();
        nameToggler.setSize(size2, size2);
        nameToggler.setFocusable(false);
        nameToggler.setToolTipText("Show/hide node names");
        nameToggler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CommonVisualSettings.setNameVisibility(!CommonVisualSettings.getNameVisibility());
                repaint();
            }
        });
        modeSelector.add(nameToggler);

        labelToggler = new JToggleButton();
        labelToggler.setSize(size2, size2);
        labelToggler.setFocusable(false);
        labelToggler.setToolTipText("Show/hide node labels");
        labelToggler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CommonVisualSettings.setLabelVisibility(!CommonVisualSettings.getLabelVisibility());
                repaint();
            }
        });
        modeSelector.add(labelToggler);

        rulerToggler = new JToggleButton();
        rulerToggler.setSize(size2, size2);
        rulerToggler.setFocusable(false);
        rulerToggler.setToolTipText("Show/hide rulers");
        rulerToggler.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CommonEditorSettings.setShowRulers(!CommonEditorSettings.getShowRulers());
                repaint();
            }
        });
        modeSelector.add(rulerToggler);

        toolboxPanel = new ToolboxPanel(this);

        GraphEditorPanelMouseListener mouseListener = new GraphEditorPanelMouseListener(this, toolboxPanel);
        GraphEditorPanelKeyListener keyListener = new GraphEditorPanelKeyListener(this, toolboxPanel);

        addMouseMotionListener(mouseListener);
        addMouseListener(mouseListener);
        addMouseWheelListener(mouseListener);
        addFocusListener(new GraphEditorFocusListener());
        addComponentListener(new Resizer());

        addKeyListener(keyListener);

        add(overlay, BorderLayout.CENTER);

        // FIXME: timers need to be stopped at some point
        Timer updateEditorPanelTimer = new Timer(CommonVisualSettings.getRedrawInterval(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (updateEditorPanelRequested) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateEditor();
                        }
                    });
                }
            }
        });

        Timer updatePropertyTimer = new Timer(CommonVisualSettings.getRedrawInterval(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (updatePropertyViewRequested) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updatePropertyView();
                        }
                    });
                }
            }
        });

        updateEditorPanelTimer.start();
        updatePropertyTimer.start();

        // This is a hack to prevent editor panel from loosing focus on Ctrl-UP key combination
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Event.CTRL_MASK), "doNothing");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Event.CTRL_MASK), "doNothing");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Event.CTRL_MASK), "doNothing");
        this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Event.CTRL_MASK), "doNothing");
        this.getActionMap().put("doNothing", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                //do nothing
            }
        });
    }

    private void reshape() {
        view.setShape(15, 15, getWidth() - 15, getHeight() - 15);
        ruler.setShape(0, 0, getWidth(), getHeight());
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        gridToggler.setSelected(CommonEditorSettings.getShowGrid());
        nameToggler.setSelected(CommonVisualSettings.getNameVisibility());
        labelToggler.setSelected(CommonVisualSettings.getLabelVisibility());
        rulerToggler.setSelected(CommonEditorSettings.getShowRulers());

        AffineTransform screenTransform = (AffineTransform) g2d.getTransform().clone();

        g2d.setBackground(CommonEditorSettings.getBackgroundColor());
        g2d.clearRect(0, 0, getWidth(), getHeight());

        if (CommonEditorSettings.getShowGrid()) {
            grid.draw(g2d);
        }
        g2d.setTransform(screenTransform);

        if (firstPaint) {
            reshape();
            firstPaint = false;
        }
        g2d.transform(view.getTransform());

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
//        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        getModel().draw(g2d, toolboxPanel.getTool().getDecorator(this));

        if (hasFocus()) {
            toolboxPanel.getTool().drawInUserSpace(this, g2d);
        }
        g2d.setTransform(screenTransform);

        if (CommonEditorSettings.getShowRulers()) {
            ruler.draw(g2d);
        }

        if (hasFocus()) {
            toolboxPanel.getTool().drawInScreenSpace(this, g2d);
            g2d.setTransform(screenTransform);

            g2d.setStroke(borderStroke);
            g2d.setColor(CommonVisualSettings.getBorderColor());
            g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        }

        paintChildren(g2d);
    }

    @Override
    /**
     * Redraw one pixel to force redrawing of the whole model. This is usually necessary
     * to recalculate bounding boxes of children components and correctly estimate the
     * bounding boxes of their parents.
     */
    public void forceRedraw() {
        super.paintImmediately(0, 0, 1, 1);
        repaint();
    }

    public VisualModel getModel() {
        return workspaceEntry.getModelEntry().getVisualModel();
    }

    public Viewport getViewport() {
        return view;
    }

    private Set<Point2D> calcConnectionSnaps(VisualConnection vc) {
        Set<Point2D> result = new HashSet<Point2D>();
        result.add(vc.getSecondCenter());
        result.add(vc.getFirstCenter());
        return result;
    }

    private Set<Point2D> getComponentSnaps(VisualComponent component) {
        Set<Point2D> result = new HashSet<Point2D>();
        AffineTransform localToRootTransform = TransformHelper.getTransformToRoot(component);
        Point2D pos = TransformHelper.transform(component, localToRootTransform).getCenter();
        result.add(pos);
        for (Connection connection: getModel().getConnections(component)) {
            if (connection instanceof VisualConnection) {
                VisualConnection vc = (VisualConnection) connection;
                if (vc.getConnectionType() == ConnectionType.POLYLINE) {
                    Polyline polyline = (Polyline) vc.getGraphic();
                    ControlPoint firstControlPoint = polyline.getFirstControlPoint();
                    if ((component == connection.getFirst()) && (firstControlPoint != null)) {
                        result.add(firstControlPoint.getPosition());
                    }
                    ControlPoint lastControlPoint = polyline.getLastControlPoint();
                    if ((component == connection.getSecond()) && (lastControlPoint != null)) {
                        result.add(lastControlPoint.getPosition());
                    }
                }
                result.addAll(calcConnectionSnaps(vc));
            }
        }
        return result;
    }

    private Set<Point2D> getControlPointSnaps(ControlPoint cp) {
        Set<Point2D> result = new HashSet<Point2D>();
        Node graphics = cp.getParent();
        if (graphics instanceof Polyline) {
            Polyline polyline = (Polyline) cp.getParent();
            result.add(cp.getPosition());
            result.add(polyline.getPrevAnchorPointLocation(cp));
            result.add(polyline.getNextAnchorPointLocation(cp));
        }
        Node parent = graphics.getParent();
        if (parent instanceof VisualConnection) {
            VisualConnection vc = (VisualConnection) parent;
            result.addAll(calcConnectionSnaps(vc));
        }
        return result;
    }

    @Override
    public Set<Point2D> getSnaps(VisualNode node) {
        Set<Point2D> result = new HashSet<Point2D>();
        if (node instanceof VisualComponent) {
            VisualComponent component = (VisualComponent) node;
            result.addAll(getComponentSnaps(component));
        } else if (node instanceof ControlPoint) {
            ControlPoint cp = (ControlPoint) node;
            result.addAll(getControlPointSnaps(cp));
        }
        return result;
    }

    @Override
    public Point2D snap(Point2D pos, Set<Point2D> snaps) {
        double x = grid.snapCoordinate(pos.getX());
        double y = grid.snapCoordinate(pos.getY());
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
        return workspaceEntry;
    }

    @Override
    public MainWindow getMainWindow() {
        return mainWindow;
    }

    private Properties propertiesWrapper(final ModelProperties mix) {
        return new Properties() {
            @Override
            public Collection<PropertyDescriptor> getDescriptors() {
                ArrayList<PropertyDescriptor> list = new ArrayList<PropertyDescriptor>();
                for (final PropertyDescriptor d : mix.getDescriptors()) {
                    list.add(new PropertyDescriptor() {
                        @Override
                        public void setValue(Object value) throws InvocationTargetException {
                            workspaceEntry.saveMemento();
                            d.setValue(value);
                        }

                        @Override
                        public boolean isWritable() {
                            return d.isWritable();
                        }

                        @Override
                        public Object getValue() throws InvocationTargetException {
                            return d.getValue();
                        }

                        @Override
                        public Class<?> getType() {
                            return d.getType();
                        }

                        @Override
                        public String getName() {
                            return d.getName();
                        }

                        @Override
                        public Map<? extends Object, String> getChoice() {
                            return d.getChoice();
                        }

                        @Override
                        public boolean isCombinable() {
                            return d.isCombinable();
                        }

                        @Override
                        public boolean isTemplatable() {
                            return d.isTemplatable();
                        }
                    });
                }
                return list;
            }
        };
    }

    private ModelProperties getModelProperties() {
        ModelProperties properties = new ModelProperties();
        // Properties of the visual model
        Properties modelProperties = getModel().getProperties(null);
        properties.addAll(modelProperties.getDescriptors());
        // Properties of the math model
        Properties mathModelProperties = getModel().getMathModel().getProperties(null);
        properties.addAll(mathModelProperties.getDescriptors());
        return properties;
    }

    private ModelProperties getNodeProperties(Node node) {
        ModelProperties properties = new ModelProperties();
        // Properties of the visual node
        Properties nodeProperties = getModel().getProperties(node);
        properties.addAll(nodeProperties.getDescriptors());
        if (node instanceof Properties) {
            properties.addAll(((Properties) node).getDescriptors());
        }
        // Properties of the math node
        if (node instanceof Dependent) {
            for (Node mathNode : ((Dependent) node).getMathReferences()) {
                Properties mathNodeProperties = getModel().getMathModel().getProperties(mathNode);
                properties.addAll(mathNodeProperties.getDescriptors());
                if (mathNode instanceof Properties) {
                    properties.addAll(((Properties) mathNode).getDescriptors());
                }
            }
        }
        return properties;
    }

    private ModelProperties getSelectionProperties(Collection<Node> nodes) {
        ModelProperties allProperties = new ModelProperties();
        for (Node node: nodes) {
            Properties nodeProperties = getNodeProperties(node);
            allProperties.addAll(nodeProperties.getDescriptors());
        }
        ModelProperties combinedProperties = new ModelProperties(allProperties.getDescriptors());
        return combinedProperties;
    }

    public void updatePropertyView() {
        ModelProperties properties;
        String titleSuffix = null;
        VisualNode templateNode = getModel().getTemplateNode();
        if (templateNode != null) {
            properties = getNodeProperties(templateNode);
            for (PropertyDescriptor pd: new LinkedList<>(properties.getDescriptors())) {
                if (!pd.isTemplatable()) {
                    properties.remove(pd);
                }
            }
            titleSuffix = TITLE_SUFFIX_TEMPLATE;
        } else {
            Collection<Node> selection = getModel().getSelection();
            if (selection.size() == 0) {
                properties = getModelProperties();
                titleSuffix = TITLE_SUFFIX_MODEL;
            } else    if (selection.size() == 1) {
                Node node = selection.iterator().next();
                properties = getNodeProperties(node);
                titleSuffix = TITLE_SUFFIX_SINGLE_ELEMENT;
            } else {
                properties = getSelectionProperties(selection);
                int nodeCount = selection.size();
                titleSuffix = nodeCount + " " + TITLE_SUFFIX_SELECTED_ELEMENTS;
            }
        }

        final PropertyEditorWindow propertyEditorWindow = mainWindow.getPropertyView();
        if (properties.getDescriptors().isEmpty()) {
            propertyEditorWindow.clearObject();
        } else {
            propertyEditorWindow.setObject(propertiesWrapper(properties));
        }

        String title = MainWindow.TITLE_PROPERTY_EDITOR;
        if (titleSuffix != null) {
            title += " [" + titleSuffix + "]";
        }
        mainWindow.setDockableTitle(mainWindow.getPropertyEditor(), title);
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
    public EditorOverlay getOverlay() {
        return overlay;
    }

    public ToolboxPanel getToolBox() {
        return toolboxPanel;
    }
    public void zoomIn() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getViewport().zoom(1);
                repaint();
            }
        });
    }

    public void zoomOut() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getViewport().zoom(-1);
                repaint();
            }
        });
    }

    public void zoomDefault() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getViewport().scaleDefault();
                repaint();
            }
        });
    }

    public void zoomFit() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Viewport viewport = getViewport();
                Rectangle2D viewportBox = viewport.getShape();
                VisualModel model = getModel();
                Collection<Touchable> nodes = Hierarchy.getChildrenOfType(model.getRoot(), Touchable.class);
                if (!model.getSelection().isEmpty()) {
                    nodes.retainAll(model.getSelection());
                }
                Rectangle2D modelBox = BoundingBoxHelper.mergeBoundingBoxes(nodes);
                if ((modelBox != null) && (viewportBox != null)) {
                    Point2D ratio = getVewportRatio(viewportBox);
                    double scaleX = ratio.getX() / modelBox.getWidth();
                    double scaleY = ratio.getY() / modelBox.getHeight();
                    double scale = 2.0 * Math.min(scaleX, scaleY);
                    viewport.scale(scale);
                    panCenter();
                }
            }

            private Point2D getVewportRatio(Rectangle2D viewportBox) {
                double ratioX = 1.0;
                double ratioY = 1.0;
                if ((viewportBox.getWidth() > VIEWPORT_MARGIN) && (viewportBox.getHeight() > VIEWPORT_MARGIN)) {
                    if (viewportBox.getWidth() > viewportBox.getHeight()) {
                        ratioX = (viewportBox.getWidth() - VIEWPORT_MARGIN) / viewportBox.getHeight();
                        ratioY = (viewportBox.getHeight() - VIEWPORT_MARGIN) / viewportBox.getHeight();
                    } else {
                        ratioX = (viewportBox.getWidth() - VIEWPORT_MARGIN) / viewportBox.getWidth();
                        ratioY = (viewportBox.getHeight() - VIEWPORT_MARGIN) / viewportBox.getWidth();
                    }
                }
                return new Point2D.Double(ratioX, ratioY);
            }
        });
    }

    public void panLeft() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getViewport().pan(20, 0);
                repaint();
            }
        });
    }

    public void panUp() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getViewport().pan(0, 20);
                repaint();
            }
        });
    }

    public void panRight() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getViewport().pan(-20, 0);
                repaint();
            }
        });
    }

    public void panDown() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                getViewport().pan(0, -20);
                repaint();
            }
        });
    }

    public void panCenter() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
                    viewport.pan(viewportCenterX - modelCenterInScreenSpace.x, viewportCenterY - modelCenterInScreenSpace.y);
                    repaint();
                }
            }
        });
    }

}
