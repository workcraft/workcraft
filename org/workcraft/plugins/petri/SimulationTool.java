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

package org.workcraft.plugins.petri;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.util.GUI;


public class SimulationTool extends AbstractTool {
	private VisualModel visualNet;
	private PetriNet net;
	private JPanel interfacePanel;

	private JButton autoPlayButton, stepButton, loadTraceButton, saveMarkingButton, loadMarkingButton;

	HashMap<Place, Integer> tokens = new HashMap<Place, Integer>();

	public SimulationTool() {
		super();
		createInterface();
	}

	private static Color enabledColor = new Color(1.0f, 0.5f, 0.0f);

	private void createInterface() {
		interfacePanel = new JPanel(new SimpleFlowLayout(5,5));

		autoPlayButton = new JButton("Play");
		stepButton = new JButton ("Step");
		loadTraceButton = new JButton ("Load trace");
		saveMarkingButton = new JButton ("Save marking");
		loadMarkingButton = new JButton ("Load marking");

		interfacePanel.add(autoPlayButton);
	}

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

		e.getEditor().repaint();
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		GUI.drawEditorMessage(editor, g, Color.BLACK, "Simulation O_O;;");
	}

	public String getLabel() {
		return "Simulation";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_M;
	}

	@Override
	public Icon getIcon() {
		try {
			return GUI.loadIconFromResource("images/play.png");
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}
}