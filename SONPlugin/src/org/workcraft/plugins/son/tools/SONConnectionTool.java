package org.workcraft.plugins.son.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;

public class SONConnectionTool extends ConnectionTool {

	protected JPanel interfacePanel;

	private JRadioButton polyButton, asynButton, synButton, bhvButton;
	private ButtonGroup buttonGroup;

	private Semantics semantic = Semantics.PNLINE;

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BoxLayout(interfacePanel, BoxLayout.Y_AXIS));

		polyButton = new JRadioButton("Petri-Net Connection");
		polyButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				semantic = Semantics.PNLINE;
		}});

		asynButton = new JRadioButton("A/Syn Communication");
		asynButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				semantic = Semantics.ASYNLINE;
		}});

		synButton = new JRadioButton("Synchronous Communication");
		synButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				semantic = Semantics.SYNCLINE;
		}});

		bhvButton = new JRadioButton("Behavioural Abstraction");
		bhvButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				semantic = Semantics.BHVLINE;
		}});

		polyButton.setSelected(true);

		buttonGroup = new ButtonGroup();
		this.buttonGroup.add(polyButton);
		this.buttonGroup.add(bhvButton);

		interfacePanel.add(polyButton);
		interfacePanel.add(bhvButton);
	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		VisualSON vson = (VisualSON)e.getModel();
		vson.forceConnectionSemantics(semantic);
		super.mousePressed(e);
	}

}
