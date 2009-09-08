package org.workcraft.plugins.petri;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;

import javax.swing.Icon;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.edit.tools.AbstractTool;
import org.workcraft.gui.edit.tools.GraphEditor;
import org.workcraft.gui.events.GraphEditorMouseEvent;

public class SimulationTool extends AbstractTool {

	private VisualModel visualNet;
	private PetriNet net;

	HashMap<Place, Integer> tokens = new HashMap<Place, Integer>();

	private static Color enabledColor = new Color(1.0f, 0.5f, 0.0f);

	private void highlightEnabledTransitions(Container root) {
		for (Node n : root.getChildren())
		{
			if (n instanceof VisualTransition)
			{
				VisualTransition vt = (VisualTransition)n;
				if (net.isEnabled(vt.getTransition()))
						vt.setColorisation(enabledColor);
				else
					vt.clearColorisation();
			}

			if (n instanceof Container)
				highlightEnabledTransitions((Container)n);
		}
	}

	@Override
	public void deactivated(GraphEditor editor)
	{
		for (Place p : net.getPlaces()) {
			p.setTokens(tokens.get(p));
		}
	}

	@Override
	public void activated(GraphEditor editor)
	{
		visualNet = editor.getModel();
		net = (PetriNet)visualNet.getMathModel();

		for (Place p : net.getPlaces()) {
			tokens.put(p, p.getTokens());
		}

		highlightEnabledTransitions(visualNet.getRoot());
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Node node = HitMan.hitTestForSelection(e.getPosition(), e.getModel());

		if (node instanceof VisualTransition) {
			VisualTransition vt = (VisualTransition)node;
			net.fire(vt.getTransition());
			highlightEnabledTransitions(visualNet.getRoot());
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

	public String getLabel() {
		return "Simulation";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_M;
	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}
}