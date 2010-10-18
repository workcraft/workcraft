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

package org.workcraft.plugins.petri.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.workcraft.Trace;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.gui.SimpleFlowLayout;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.petri.PetriNetModel;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.petri.Transition;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.Func;
import org.workcraft.util.GUI;


public class SimulationTool extends AbstractTool {
	private VisualModel visualNet;
	private PetriNetModel net;
	private JPanel interfacePanel;

	private JButton resetButton, autoPlayButton, stopButton, stepButton, loadTraceButton, saveMarkingButton, loadMarkingButton;
	private JSlider speedSlider;

	final double DEFAULT_SIMULATION_DELAY = 0.3;
	final double EDGE_SPEED_MULTIPLIER = 10;

	Map<Place, Integer> initialMarking;
	Map<Place, Integer> savedMarking = null;
	int savedStep = 0;

	public SimulationTool() {
		super();
		createInterface();
	}

	private static Color enabledColor = new Color(1.0f, 0.5f, 0.0f);

	private Trace trace;
	private int currentStep = 0;
	private Timer timer = null;

	private void applyMarking(Map<Place, Integer> marking)
	{
		for (Place p : net.getPlaces()) {
			p.setTokens(marking.get(p));
		}
	}

	private void update()
	{
		if(timer != null && (trace == null || currentStep == trace.size()))
		{
			timer.stop();
			timer = null;
		}

		if(timer != null)
			timer.setDelay(getAnimationDelay());

		resetButton.setEnabled(trace != null && currentStep > 0);
		autoPlayButton.setEnabled(trace != null && currentStep < trace.size());
		stopButton.setEnabled(timer!=null);
		stepButton.setEnabled(trace != null && currentStep < trace.size());
		loadTraceButton.setEnabled(true);
		saveMarkingButton.setEnabled(true);
		loadMarkingButton.setEnabled(savedMarking != null);

		highlightEnabledTransitions(visualNet.getRoot());
	}

	private void step() {
		String transitionId = trace.get(currentStep);

		final Node transition = net.getNodeByReference(transitionId);

		net.fire((Transition)transition);

		currentStep++;
		update();
	}

	private void reset() {
		applyMarking(initialMarking);
		currentStep = 0;
		if(timer!=null)
		{
			timer.stop();
			timer = null;
		}
		update();
	}

	private int getAnimationDelay()
	{
		return (int)(1000.0 * DEFAULT_SIMULATION_DELAY * Math.pow(EDGE_SPEED_MULTIPLIER, -speedSlider.getValue() / 1000.0));
	}

	private void createInterface() {
		interfacePanel = new JPanel(new SimpleFlowLayout(5,5));

		resetButton = new JButton ("Reset");
		speedSlider = new JSlider(-1000, 1000, 0);
		autoPlayButton = GUI.createIconButton(GUI.createIconFromSVG("images/icons/svg/start.svg"), "Automatic simulation");
		stopButton = new JButton ("Stop");
		stepButton = new JButton ("Step");
		loadTraceButton = new JButton ("Load trace");
		saveMarkingButton = new JButton ("Save marking");
		loadMarkingButton = new JButton ("Load marking");

		speedSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if(timer != null)
				{
					timer.stop();
					timer.setInitialDelay(getAnimationDelay());
					timer.setDelay(getAnimationDelay());
					timer.start();
				}
				update();
			}
		});

		resetButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		autoPlayButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				timer = new Timer(getAnimationDelay(), new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e) {
						step();
					}
				});
				timer.start();
				update();
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				timer.stop();
				timer = null;
				update();
			}
		});

		stepButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				step();
			}
		});

		saveMarkingButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				savedMarking = readMarking();
				savedStep = currentStep;

				update();
			}
		});

		loadMarkingButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				applyMarking(savedMarking);
				currentStep = savedStep;

				update();
			}
		});

		interfacePanel.add(resetButton);
		interfacePanel.add(speedSlider);
		interfacePanel.add(autoPlayButton);
		interfacePanel.add(stopButton);
		interfacePanel.add(stepButton);
		interfacePanel.add(loadTraceButton);
		interfacePanel.add(saveMarkingButton);
		interfacePanel.add(loadMarkingButton);
	}

	private void highlightEnabledTransitions(Container root) {
		for (Node n : root.getChildren())
		{
			if (n instanceof VisualTransition)
			{
				VisualTransition vt = (VisualTransition)n;
				if (net.isEnabled(vt.getReferencedTransition()))
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
		reset();
	}

	@Override
	public void activated(GraphEditor editor)
	{
		visualNet = editor.getModel();
		net = (PetriNetModel)visualNet.getMathModel();

		initialMarking = readMarking();

		update();
	}

	private Map<Place, Integer> readMarking() {
		HashMap<Place, Integer> result = new HashMap<Place, Integer>();
		for (Place p : net.getPlaces()) {
			result.put(p, p.getTokens());
		}
		return result;
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		Node node = HitMan.hitDeepest(e.getPosition(), e.getModel().getRoot(), new Func<Node, Boolean>()
				{
					@Override
					public Boolean eval(Node node) {
						return
							   node instanceof VisualTransition
							&& net.isEnabled(((VisualTransition)node).getReferencedTransition());
					}
				});

		if (node instanceof VisualTransition) {
			VisualTransition vt = (VisualTransition)node;
			net.fire(vt.getReferencedTransition());

		}

		update();

		e.getEditor().repaint();
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		GUI.drawEditorMessage(editor, g, Color.BLACK, "Simulation: click on the highlighted transitions to fire them");
	}

	public String getLabel() {
		return "Simulation";
	}

	public int getHotKeyCode() {
		return KeyEvent.VK_M;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/start-green.svg");
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	public void setTrace(Trace t) {
		this.trace = t;
	}
}