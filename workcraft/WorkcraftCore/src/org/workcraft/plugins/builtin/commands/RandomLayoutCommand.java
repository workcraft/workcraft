package org.workcraft.plugins.builtin.commands;

import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.plugins.builtin.settings.RandomLayoutSettings;

import java.awt.geom.Point2D;

public class RandomLayoutCommand extends AbstractLayoutCommand {

    @Override
    public String getDisplayName() {
        return "Random";
    }

    @Override
    public void layout(VisualModel model) {
        Point2D start = new Point2D.Double(RandomLayoutSettings.getStartX(), RandomLayoutSettings.getStartY());
        Point2D range = new Point2D.Double(RandomLayoutSettings.getRangeX(), RandomLayoutSettings.getRangeY());
        model.applyRandomLayout(start, range);
    }

}
