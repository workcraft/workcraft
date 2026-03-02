package org.workcraft.plugins.circuit.tools;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.EditorCommonSettings;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.plugins.circuit.Circuit;
import org.workcraft.plugins.circuit.FunctionComponent;
import org.workcraft.plugins.circuit.VisualCircuit;
import org.workcraft.plugins.circuit.VisualFunctionComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.function.BiConsumer;

public class InstancePanel extends JPanel {
    private static final double MIN_SCALE_FACTOR = 10.0;
    private static final double MAX_SCALE_FACTOR = 30.0;

    private VisualFunctionComponent templateNode;
    private Instantiator instantiator;

    public record ComponentInCircuit(VisualFunctionComponent component, VisualCircuit circuit) {
    }

    public interface Instantiator extends BiConsumer<ComponentInCircuit, GraphEditorMouseEvent> {
    }

    private final VisualCircuit circuit = new VisualCircuit(new Circuit()) {
        @Override
        public void registerGraphEditorTools() {
            // Prevent registration of GraphEditorTools because it leads to a loop
            // between VisualCircuit and *ComponentGeneratorTool classes.
        }
    };

    private VisualFunctionComponent component = null;

    public void setInstantiator(Instantiator instantiator) {
        this.instantiator = instantiator;
        update();
    }

    public void setTemplateNode(VisualFunctionComponent templateNode) {
        this.templateNode = templateNode;
        repaint();
    }

    public void update() {
        if (component != null) {
            circuit.remove(component);
        }
        component = new VisualFunctionComponent(new FunctionComponent()) {
            @Override
            public boolean getNameVisibility() {
                return false;
            }

            @Override
            public boolean getLabelVisibility() {
                return true;
            }

            @Override
            public boolean getFanoutVisibility() {
                return false;
            }
        };
        circuit.getMathModel().add(component.getReferencedComponent());
        circuit.add(component);
        if (instantiator != null) {
            instantiator.accept(new ComponentInCircuit(component, circuit), null);
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        setBackground(EditorCommonSettings.getBackgroundColor());
        super.paintComponent(g);
        if (component != null) {
            // Copy template style, if template node is set
            if (templateNode != null) {
                component.copyStyle(templateNode);
            }

            // Cache component text to better estimate its bounding box
            component.cacheRenderedText(null);

            Graphics2D g2d = (Graphics2D) g;
            Rectangle2D bb = component.getBoundingBox();
            double scaleX = (getWidth() - 5 * SizeHelper.getLayoutHGap()) / bb.getWidth();
            double scaleY = (getHeight() - 5 * SizeHelper.getLayoutVGap()) / bb.getHeight();
            double scale = Math.min(scaleX, scaleY);
            if (scale < MIN_SCALE_FACTOR) {
                scale = MIN_SCALE_FACTOR;
            }
            if (scale > MAX_SCALE_FACTOR) {
                scale = MAX_SCALE_FACTOR;
            }
            g2d.translate(getWidth() / 2, getHeight() / 2);
            g2d.scale(scale, scale);
            g2d.translate(-bb.getCenterX(), -bb.getCenterY());
            // Pass decoration with non-null colorisation to make pivot point visible
            circuit.draw(g2d, node -> new BorederColorDecoration());
        }
    }

    private static final class BorederColorDecoration implements Decoration {
        @Override
        public Color getColorisation() {
            return VisualCommonSettings.getBorderColor();
        }
    }

}
