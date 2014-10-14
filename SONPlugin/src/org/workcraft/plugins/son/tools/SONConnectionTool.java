package org.workcraft.plugins.son.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.HitMan;
import org.workcraft.dom.visual.TransformHelper;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.VisualBlock;
import org.workcraft.util.GUI;

public class SONConnectionTool  extends AbstractTool implements ClipboardOwner{

	protected JPanel interfacePanel;

	private JRadioButton polyButton, asynButton, synButton, bhvButton;
	private ButtonGroup buttonGroup;

	private int conType = 1;

	private VisualNode mouseOverObject = null;
	private VisualNode first = null;

	private boolean mouseExitRequiredForSelfLoop = true;
	private boolean leftFirst = false;
	private Point2D lastMouseCoords;
	private String warningMessage = null;

	private static Color highlightColor = new Color(99, 130, 191).brighter();

	@Override
	public void activated(final GraphEditor editor) {
		super.activated(editor);
		lastMouseCoords = new Point2D.Double();
	}

	@Override
	public void createInterfacePanel(final GraphEditor editor) {
		super.createInterfacePanel(editor);

		interfacePanel = new JPanel();
		interfacePanel.setLayout(new BoxLayout(interfacePanel, BoxLayout.Y_AXIS));

		polyButton = new JRadioButton("Petri-Net Connection");
		polyButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				conType = 1;
		}});

		asynButton = new JRadioButton("A/Syn Communication");
		asynButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				conType = 2;
		}});

		synButton = new JRadioButton("Synchronous Communication");
		synButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				conType = 3;
		}});

		bhvButton = new JRadioButton("Behavioural Abstraction");
		bhvButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				conType = 4;
		}});

		polyButton.setSelected(true);

		buttonGroup = new ButtonGroup();
		this.buttonGroup.add(polyButton);
	//	this.buttonGroup.add(asynButton);
		//this.buttonGroup.add(synButton);
		this.buttonGroup.add(bhvButton);

		interfacePanel.add(polyButton);
		//interfacePanel.add(asynButton);
		//interfacePanel.add(synButton);
		interfacePanel.add(bhvButton);

	}

	@Override
	public JPanel getInterfacePanel() {
		return interfacePanel;
	}

	public Ellipse2D getBoundingCircle(Rectangle2D boundingRect) {

		double w_2 = boundingRect.getWidth()/2;
		double h_2 = boundingRect.getHeight()/2;
		double r = Math.sqrt(w_2 * w_2 + h_2 * h_2);

		return new Ellipse2D.Double(boundingRect.getCenterX() - r, boundingRect.getCenterY() - r, r*2, r*2);
	}

	@Override
	public void drawInUserSpace(GraphEditor editor, Graphics2D g) {
		g.setStroke(new BasicStroke((float)editor.getViewport().pixelSizeInUserSpace().getX()));

		if (first != null) {
			VisualGroup root = (VisualGroup)editor.getModel().getRoot();
			warningMessage = null;
			if (mouseOverObject != null) {
				try {
					VisualModel vNet = editor.getModel();
					if (vNet instanceof VisualSON)
					((VisualSON) vNet).validateConnection(first, mouseOverObject, getSONConnectionType());
					drawConnectingLine(g, root, Color.GREEN);
				} catch (InvalidConnectionException e) {
					warningMessage = e.getMessage();
					drawConnectingLine(g, root, Color.RED);
				}
			} else {
				drawConnectingLine(g, root, Color.BLUE);
			}
		}
	}

	private void drawConnectingLine(Graphics2D g, VisualGroup root, Color color) {
		g.setColor(color);

		Point2D center = TransformHelper.transform(first, TransformHelper.getTransformToAncestor(first, root)).getCenter();

		Line2D line = new Line2D.Double(center.getX(), center.getY(), lastMouseCoords.getX(), lastMouseCoords.getY());
		g.draw(line);
	}

	public String getLabel() {
		return "Connect";
	}

	@Override
	public void mouseMoved(GraphEditorMouseEvent e) {
		lastMouseCoords = e.getPosition();

		VisualNode newMouseOverObject = (VisualNode) HitMan.hitTestForConnection(e.getPosition(), e.getModel());

		mouseOverObject = newMouseOverObject;

		if (!leftFirst && mouseExitRequiredForSelfLoop) {
			if (mouseOverObject == first)
				mouseOverObject = null;
			else
				leftFirst = true;
		}

		e.getEditor().repaint();
	}

	@Override
	public void mousePressed(GraphEditorMouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			//prevent manually connecting from/to visual block. But not in program level (see connectToBlock).
			if (first == null && !(mouseOverObject instanceof VisualBlock)) {
				if (mouseOverObject != null) {
					first = mouseOverObject;
					leftFirst = false;
					mouseMoved(e);
				}
			} else if (mouseOverObject != null && !(mouseOverObject instanceof VisualBlock)) {
				try {
					VisualModel vNet = e.getModel();
					if (vNet instanceof VisualSON)
						((VisualSON) vNet).connect(first, mouseOverObject, getSONConnectionType());
					if ((e.getModifiers() & MouseEvent.CTRL_DOWN_MASK) != 0) {
						first = mouseOverObject;
						mouseOverObject = null;
					} else {
						first = null;
					}
				} catch (InvalidConnectionException e1) {
					Toolkit.getDefaultToolkit().beep();
				}

			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			first = null;
			mouseOverObject = null;
		}
		e.getEditor().repaint();
	}

	private Semantics getSONConnectionType(){
		if(this.conType == 1)
			return Semantics.PNLINE;
		if(this.conType == 2)
			return Semantics.ASYNLINE;
		if(this.conType == 3)
			return Semantics.SYNCLINE;
		if(this.conType == 4)
			return Semantics.BHVLINE;
		return null;
	}

	@Override
	public void drawInScreenSpace(GraphEditor editor, Graphics2D g) {
		String message;

		if (warningMessage != null)
			message = warningMessage;
		else
			if (first == null)
				message = "Click on the first component.";
			else
				message = "Click on the second component. Hold Ctrl to connect continuously.";

		GUI.drawEditorMessage(editor, g, warningMessage!=null ? Color.RED : Color.BLACK, message);
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_C;
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/connect.svg");
	}

	@Override
	public void deactivated(GraphEditor editor) {
		super.deactivated(editor);
		conType = 1;
		first = null;
		mouseOverObject = null;
	}

	@Override
	public Decorator getDecorator(final GraphEditor editor) {
		return new Decorator() {

			@Override
			public Decoration getDecoration(Node node) {
				if(node == mouseOverObject)
					return new Decoration(){

						@Override
						public Color getColorisation() {
							return highlightColor;
						}

						@Override
						public Color getBackground() {
							return null;
						}
				};
				return null;
			}

		};
	}


	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {

	}
}
