package org.workcraft.plugins.wtg.tools;

import java.awt.Color;
import java.awt.Graphics2D;

import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.plugins.dtd.tools.DtdSignalGeneratorTool;
import org.workcraft.plugins.wtg.VisualWaveform;
import org.workcraft.utils.GuiUtils;

public class WtgSignalGeneratorTool extends DtdSignalGeneratorTool {

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        VisualModel model = e.getModel();
        if (model.getCurrentLevel() instanceof VisualWaveform) {
            super.mousePressed(e);
        }
    }

    @Override
    public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
        VisualModel model = editor.getModel();
        if (model.getCurrentLevel() instanceof VisualWaveform) {
            super.drawInScreenSpace(editor, g);
        } else {
            GuiUtils.drawEditorMessage(editor, g, Color.RED, "Signals can only be created inside waveforms.");
        }
    }

}
