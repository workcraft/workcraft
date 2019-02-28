package org.workcraft.plugins.wtg;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.utils.Coloriser;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.plugins.builtin.settings.CommonVisualSettings;
import org.workcraft.plugins.wtg.decorations.WaveformDecoration;
import org.workcraft.utils.Hierarchy;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

@Hotkey(KeyEvent.VK_W)
@DisplayName("Waveform")
@SVGIcon("images/wtg-node-waveform.svg")
public class VisualWaveform extends VisualPage {

    public static final String PROPERTY_GUARD_POSITIONING = "Guard positioning";
    public static final String PROPERTY_GUARD_COLOR = "Guard color";

    public VisualWaveform(Waveform waveform) {
        super(waveform);
        renamePropertyDeclarationByName(PROPERTY_LABEL, Waveform.PROPERTY_GUARD);
        renamePropertyDeclarationByName(PROPERTY_LABEL_COLOR, PROPERTY_GUARD_COLOR);
        renamePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING, PROPERTY_GUARD_POSITIONING);
    }

    public Waveform getReferencedWaveform() {
        return (Waveform) getReferencedComponent();
    }

    @Override
    public String getLabel() {
        return getReferencedWaveform().getGuard().toString();
    }

    @Override
    public void setLabel(String label) {
        Guard guard = Guard.createFromString(label);
        getReferencedWaveform().setGuard(guard);
        super.setLabel(label);
    }

    @Override
    public void draw(DrawRequest r) {
        Decoration d = r.getDecoration();
        if (d instanceof WaveformDecoration) {
            WaveformDecoration wd = (WaveformDecoration) d;
            setIsExcited(wd.isActive());
        }
        // This is to update the rendered text for names (and labels) of group children,
        // which is necessary to calculate the bounding box before children have been drawn
        for (VisualComponent component: Hierarchy.getChildrenOfType(this, VisualComponent.class)) {
            component.cacheRenderedText(r);
        }

        if (getParent() != null) {
            drawOutline(r);
            drawPivot(r);
            drawNameInLocalSpace(r);
            drawLabelInLocalSpace(r);
        }
    }

    @Override
    public void drawOutline(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        Rectangle2D bb = getInternalBoundingBoxInLocalSpace();
        if (bb != null) {
            Decoration d = r.getDecoration();
            if (getIsCollapsed() && !isCurrentLevelInside()) {
                g.setColor(Coloriser.colorise(getFillColor(), d.getColorisation()));
                g.fill(bb);
                g.setStroke(new BasicStroke((float) CommonVisualSettings.getStrokeWidth()));
            } else {
                float[] pattern = {0.05f, 0.05f};
                BasicStroke stroke = new BasicStroke(0.05f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f);
                if (d instanceof WaveformDecoration) {
                    WaveformDecoration wd = (WaveformDecoration) d;
                    if (wd.isActive()) {
                        stroke = new BasicStroke(0.05f);
                    }
                }
                g.setStroke(stroke);
            }
            g.setColor(Coloriser.colorise(getForegroundColor(), d.getColorisation()));
            g.draw(bb);
        }
    }

}
