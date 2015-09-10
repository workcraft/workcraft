package org.workcraft.plugins.petri.tools;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.LinkedList;

import javax.swing.Icon;

import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.plugins.petri.VisualPlace;
import org.workcraft.plugins.petri.VisualPlaceShadow;
import org.workcraft.plugins.petri.VisualReadArc;
import org.workcraft.plugins.petri.VisualTransition;
import org.workcraft.util.GUI;

public class ReadArcConnectionTool extends ConnectionTool {

	public ReadArcConnectionTool() {
		super(true, false);
	}

	@Override
	public boolean isConnectable(Node node) {
		return ((node instanceof VisualPlace) || (node instanceof VisualPlaceShadow) || (node instanceof VisualTransition));
	}

	@Override
	public VisualConnection createDefaultTemplateNode() {
		return new VisualReadArc();
	}

	@Override
	public Icon getIcon() {
		return GUI.createIconFromSVG("images/icons/svg/tool-readarc.svg");
	}

	@Override
	public String getLabel() {
		return "Read-arc";
	}

	@Override
	public int getHotKeyCode() {
		return KeyEvent.VK_R;
	}

	@Override
	public String getSecondHintMessage() {
		return (super.getSecondHintMessage() + " Hold Shift to create a place shadow.");
	}

	@Override
	public VisualConnection finishConnection(GraphEditorMouseEvent e) {
		VisualConnection connection = super.finishConnection(e);
		if ((e.getModifiers() & MouseEvent.SHIFT_DOWN_MASK) != 0) {
			VisualModel model = e.getEditor().getModel();
			if (connection instanceof VisualReadArc) {
				VisualReadArc readArc = (VisualReadArc)connection;
				VisualComponent first = readArc.getFirst();
				VisualComponent second = readArc.getSecond();
				if ((first instanceof VisualPlace) && (second instanceof VisualTransition)) {
					VisualPlace place = (VisualPlace)first;
					VisualTransition transition = (VisualTransition)second;
					VisualPlaceShadow placeShadow = new VisualPlaceShadow(place.getReferencedPlace());
					place.add(placeShadow);
					Point2D splitPoint = readArc.getSplitPoint();
					placeShadow.setRootSpacePosition(splitPoint);
					placeShadow.setNamePositioning(getBestNamePositioning(placeShadow, transition));
					LinkedList<Point2D> locationsInRootSpace = ConnectionHelper.getSuffixControlPoints(readArc, splitPoint);
					model.remove(readArc);
					try {
						connection = model.connectUndirected(placeShadow, transition);
						ConnectionHelper.addControlPoints(connection, locationsInRootSpace);
					} catch (InvalidConnectionException exeption) {
						Toolkit.getDefaultToolkit().beep();
					}
				}
			}
		}
		return connection;
	}

	private Positioning getBestNamePositioning(VisualPlaceShadow terminal, VisualTransition transition) {
		double dx = (terminal.getRootSpaceX() - transition.getRootSpaceX());
		double dy = (terminal.getRootSpaceY() - transition.getRootSpaceY());
		double dx2 = dx * dx;
		double dy2 = dy * dy;
		double dk = 2.0;
		boolean left = (dx < 0) && (dk * dx2 > dy2);
		boolean right = (dx > 0) && (dk * dx2 > dy2);
		boolean top = (dy < 0) && (dk * dy2 > dx2);
		boolean bottom = (dy > 0) && (dk * dy2 > dx2);
		Positioning positioning = Positioning.CENTER;
		if (left) {
			if (top) {
				positioning = Positioning.TOP_LEFT;
			} else if (bottom) {
				positioning = Positioning.BOTTOM_LEFT;
			} else {
				positioning = Positioning.LEFT;
			}
		} else if (right) {
			if (top) {
				positioning = Positioning.TOP_RIGHT;
			} else if (bottom) {
				positioning = Positioning.BOTTOM_RIGHT;
			} else {
				positioning = Positioning.RIGHT;
			}
		} else if (top) {
			positioning = Positioning.TOP;
		} else if (bottom) {
			positioning = Positioning.BOTTOM;
		} else {
			positioning = Positioning.CENTER;
		}
		return positioning;
	}

}
