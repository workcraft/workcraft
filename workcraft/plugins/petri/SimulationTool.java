package org.workcraft.plugins.petri;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import org.workcraft.dom.HierarchyNode;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.edit.tools.AbstractTool;
import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class SimulationTool extends AbstractTool {

	private VisualPetriNet visualNet;
	private PetriNet net;

	HashMap<Integer, Integer> tokens = new HashMap<Integer, Integer>();

	private static Color enabledColor = new Color(1.0f, 0.5f, 0.0f);

	private void highlightEnabledTransitions() {
		/*
		for (Transition t : net.getTransitions())
			if (t.isEnabled())
				visualNet.getComponentByRefID(t.getID()).setColorisation(enabledColor);
			else
				visualNet.getComponentByRefID(t.getID()).clearColorisation();
		*/

		for (HierarchyNode n : visualNet.getVisualComponents()) {
			if (n instanceof VisualTransition) {
				VisualTransition t = (VisualTransition)n;
				if (t.isEnabled())
					t.setColorisation(enabledColor);
				else
					t.clearColorisation();
			}
		}


	}

	@Override
	public void deactivated(GraphEditor editor)
	{

		for (Place p : net.getPlaces()) {
			p.setTokens(tokens.get(p.getID()));
		}
	}

	@Override
	public void activated(GraphEditor editor)
	{
		visualNet = (VisualPetriNet)editor.getModel();
		net = (PetriNet)visualNet.getMathModel();
		for (Place p : net.getPlaces()) {
			tokens.put(p.getID(), p.getTokens());
		}

		highlightEnabledTransitions();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		VisualNode node = HitMan.hitDeepestNodeOfType(e.getPosition(), e.getModel().getRoot(), VisualTransition.class);

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