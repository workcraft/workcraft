package org.workcraft.plugins.petri;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.edit.tools.AbstractTool;
import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class SimulationTool extends AbstractTool {

	private VisualPetriNet visualNet;
	private PetriNet net;

	private static Color enabledColor = new Color(1.0f, 0.5f, 0.0f);

	private void highlightEnabledTransitions() {
		/*
		for (Transition t : net.getTransitions())
			if (t.isEnabled())
				visualNet.getComponentByRefID(t.getID()).setColorisation(enabledColor);
			else
				visualNet.getComponentByRefID(t.getID()).clearColorisation();
		*/

		for (VisualComponent t : visualNet.getVisualComponents()) {
			if (t instanceof VisualTransition) {
				if (((VisualTransition)t).isEnabled())
					t.setColorisation(enabledColor);
				else
					t.clearColorisation();
			}
		}


	}

	@Override
	public void activated(GraphEditor editor)
	{
		visualNet = (VisualPetriNet)editor.getModel();
		net = (PetriNet)visualNet.getMathModel();

		highlightEnabledTransitions();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		VisualNode node = e.getModel().getRoot().hitComponent(e.getPosition());

		if (node instanceof VisualTransition) {
			VisualTransition vt = (VisualTransition)node;

			if (vt.isEnabled()) {
				vt.fire();
				highlightEnabledTransitions();
				e.getEditor().repaint();
			}

		}
	}


	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		g.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
		Rectangle2D r = g.getFont().getStringBounds("Simulation O_O;;", g.getFontRenderContext());
		g.setColor(Color.BLUE);
		g.drawString ("Simulation O_O;;", editor.getWidth()/2 - (int)r.getWidth()/2, editor.getHeight() - 20);
	}

	public String getIconPath() {
		return null;
	}

	public String getName() {
		return "Simulation";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_M;
	}
}