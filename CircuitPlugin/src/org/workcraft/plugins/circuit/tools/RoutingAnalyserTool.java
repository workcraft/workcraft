package org.workcraft.plugins.circuit.tools;

import java.awt.event.KeyEvent;

import javax.swing.Icon;

import org.workcraft.util.GUI;

public class RoutingAnalyserTool extends CircuitSelectionTool {

    @Override
    public String getLabel() {
        return "Routing analyser";
    }

    @Override
    public int getHotKeyCode() {
        return KeyEvent.VK_R;
    }

    @Override
    public Icon getIcon() {
        return GUI.createIconFromSVG("images/circuit-tool-routing.svg");
    }

}
