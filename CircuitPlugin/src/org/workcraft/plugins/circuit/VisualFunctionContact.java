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

package org.workcraft.plugins.circuit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.gui.Coloriser;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.renderers.ComponentRenderingResult.RenderType;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Hierarchy;


@DisplayName("Input/output port")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/circuit-port.svg")
public class VisualFunctionContact extends VisualContact implements StateObserver {

	private double size = 0.3;
	private static Font font;
	static {
		try {
			font = Font.createFont(Font.TYPE1_FONT, ClassLoader.getSystemResourceAsStream("fonts/eurm10.pfb")).deriveFont(0.5f);
		} catch (FontFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private FormulaRenderingResult renderedSetFormula = null;
	private FormulaRenderingResult renderedResetFormula = null;

	public VisualFunctionContact(FunctionContact contact) {
		super(contact);
	}

	public FunctionContact getReferencedFunctionContact() {
		return (FunctionContact)getReferencedComponent();
	}

	@NoAutoSerialisation
	public BooleanFormula getSetFunction() {
		return getReferencedFunctionContact().getSetFunction();
	}

	@NoAutoSerialisation
	public void setSetFunction(BooleanFormula setFunction) {
		if (getParent() instanceof VisualFunctionComponent) {
			VisualFunctionComponent p = (VisualFunctionComponent) getParent();
			p.resetRenderingResult();
		}
		renderedSetFormula = null;
		getReferencedFunctionContact().setSetFunction(setFunction);
	}

	@NoAutoSerialisation
	public BooleanFormula getResetFunction() {
		return getReferencedFunctionContact().getResetFunction();
	}

	@NoAutoSerialisation
	public void setResetFunction(BooleanFormula resetFunction) {
		if (getParent() instanceof VisualFunctionComponent) {
			VisualFunctionComponent p = (VisualFunctionComponent) getParent();
			p.resetRenderingResult();
		}
		renderedResetFormula = null;
		getReferencedFunctionContact().setResetFunction(resetFunction);
	}

	public void resetRenderedFormula() {
		renderedSetFormula = null;
		renderedResetFormula = null;
	}

	FormulaRenderingResult getRenderedSetFormula(FontRenderContext fcon) {
		if (((FunctionContact)getReferencedContact()).getSetFunction() == null) {
			return null;
		} else if (renderedSetFormula == null) {
			renderedSetFormula = FormulaToGraphics.render(((FunctionContact)getReferencedContact()).getSetFunction(), fcon, font);
		}
		return renderedSetFormula;
	}

	FormulaRenderingResult getRenderedResetFormula(FontRenderContext fcon) {
		if (((FunctionContact)getReferencedContact()).getResetFunction() == null) {
			return null;
		} else if (renderedResetFormula == null) {
			renderedResetFormula = FormulaToGraphics.render(((FunctionContact)getReferencedContact()).getResetFunction(), fcon, font);
		}
		return renderedResetFormula;
	}

	private void drawFormula(Graphics2D g, int arrowType, float xOffset, float yOffset, Color foreground, Color background, FormulaRenderingResult result) {
		if (result == null) return;

		Rectangle2D textBB = result.boundingBox;
		float textX = 0;
		float textY = (float)-textBB.getCenterY()-(float)0.5-yOffset;
		float arrX = 0;
		float arrY = (float)-textBB.getCenterY()-(float)0.5-yOffset;

		AffineTransform transform = g.getTransform();
		AffineTransform at = new AffineTransform();
		Direction dir = getDirection();

		if (!(getParent() instanceof VisualFunctionComponent)) {
			dir = Direction.flipDirection(dir);
		}

		switch (dir) {
		case EAST:
			textX = (float)+xOffset;
			arrX = (float)+(xOffset-0.15);
			break;
		case NORTH:
			at.quadrantRotate(-1);
			g.transform(at);
			textX = (float)+xOffset;
			arrX = (float)+(xOffset-0.15);
			break;
		case WEST:
			textX = (float)-textBB.getWidth()-xOffset;
			arrX = (float)-(xOffset-0.15);
			break;
		case SOUTH:
			at.quadrantRotate(-1);
			g.transform(at);
			textX = (float)-textBB.getWidth()-xOffset;
			arrX = (float)-(xOffset-0.15);
			break;
		}


		if (arrowType==2) {
			Line2D line = new Line2D.Double(arrX, arrY, arrX, arrY-0.225);

			Path2D path = new Path2D.Double();
			path.moveTo(arrX-0.05, arrY-0.225);
			path.lineTo(arrX+0.05, arrY-0.225);
			path.lineTo(arrX, arrY-0.375);
			path.closePath();

			g.setStroke(new BasicStroke((float)0.02));
			g.setColor(foreground);
			g.fill(path);
			g.draw(line);
		} else if (arrowType==1) {
			Line2D line = new Line2D.Double(arrX, arrY-0.15, arrX, arrY-0.375);

			Path2D path = new Path2D.Double();
			path.moveTo(arrX-0.05, arrY-0.15);
			path.lineTo(arrX+0.05, arrY-0.15);
			path.lineTo(arrX, arrY);
			path.closePath();

			g.setStroke(new BasicStroke((float)0.02));
			g.setColor(foreground);
			g.fill(path);
			g.draw(line);
		}
		g.translate(textX, textY);
		g.setColor(foreground);
		result.draw(g);
		g.setTransform(transform);
	}

	@Override
	public void draw(DrawRequest r) {
		Graphics2D g = r.getGraphics();
		Color foreground = Coloriser.colorise(getForegroundColor(), r.getDecoration().getColorisation());
		Color background = Coloriser.colorise(getFillColor(), r.getDecoration().getBackground());
		Node parent = getParent();
		if (parent != null) {
			if ((getIOType() == IOType.INPUT) ^ (parent instanceof VisualComponent)) {
				if ( !(parent instanceof VisualCircuitComponent) || (((VisualCircuitComponent)parent).getRenderType() == RenderType.BOX) ) {
					FormulaRenderingResult setResult = getRenderedSetFormula(g.getFontRenderContext());
					FormulaRenderingResult resetResult = getRenderedResetFormula(g.getFontRenderContext());

					float xOfs = (float)size;
					if (!CircuitSettings.getShowContacts() && (parent instanceof VisualComponent)) {
						xOfs = (float)-size;
					}
					if (resetResult!=null) {
						drawFormula(g, 1, xOfs, (float)-size/2, foreground, background, resetResult);
						drawFormula(g, 2, xOfs, (float)size, foreground, background, setResult);
					} else {
						drawFormula(g, 0, xOfs, (resetResult==null?(float)0:(float)size), foreground, background, setResult);
					}
				}
			}
		}
		super.draw(r);
	}

	@Override
	public void notify(StateEvent e) {
		if (e instanceof PropertyChangedEvent) {
			PropertyChangedEvent pc = (PropertyChangedEvent)e;
			if (pc.getPropertyName().equals("setFunction")
			 || pc.getPropertyName().equals("resetFunction")) {
				resetRenderedFormula();
			}
			if (pc.getPropertyName().equals("name")) {
				Node root = Hierarchy.getRoot(this);
				for (VisualFunctionContact c : Hierarchy.getDescendantsOfType(root, VisualFunctionContact.class)) {
					c.resetRenderedFormula();
				}
			}
		}
		super.notify(e);
	}

}
