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
import org.workcraft.plugins.cpog.optimisation.FreeVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.CleverBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.serialisation.xml.NoAutoSerialisation;


@DisplayName("Input/output port")
@Hotkey(KeyEvent.VK_P)
@SVGIcon("images/icons/svg/circuit-port.svg")

public class VisualFunctionContact extends VisualContact implements StateObserver {

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

	public void resetRenderedFormula() {
		renderedSetFormula = null;
		renderedResetFormula = null;
	}

	FormulaRenderingResult getRenderedSetFormula(FontRenderContext fcon) {
		if (renderedSetFormula == null) {
			renderedSetFormula = FormulaToGraphics.render(((FunctionContact)getReferencedContact()).getSetFunction(), fcon, font);
		}
		return renderedSetFormula;
	}

	FormulaRenderingResult getRenderedResetFormula(FontRenderContext fcon) {
		if (((FunctionContact)getReferencedContact()).getResetFunction()==null) return null;

		if (renderedResetFormula == null) {
			renderedResetFormula = FormulaToGraphics.render(((FunctionContact)getReferencedContact()).getResetFunction(), fcon, font);
		}
		return renderedResetFormula;
	}

	private FunctionContact function=null;

	public VisualFunctionContact(FunctionContact component) {
		super(component);
		function = component;
	}

	public VisualFunctionContact(FunctionContact component, VisualContact.Direction dir, String label) {
		super(component);
		function = component;

		component.addObserver(this);
		setName(label);
		setDirection(dir);

	}

	@NoAutoSerialisation
	public String getResetFunction() {
		return FormulaToString.toString(getFunction().getResetFunction());
	}

	@NoAutoSerialisation
	public String getSetFunction() {
		return FormulaToString.toString(getFunction().getSetFunction());
	}

	@NoAutoSerialisation
	public void setResetFunction(BooleanFormula resetFunction) {
		if (getParent() instanceof VisualFunctionComponent) {
			VisualFunctionComponent p = (VisualFunctionComponent) getParent();
			p.resetRenderingResult();
		}

		renderedResetFormula = null;
		getFunction().setResetFunction(resetFunction);

		sendNotification(new PropertyChangedEvent(this, "resetFunction"));
	}

	@NoAutoSerialisation
	public void setSetFunction(BooleanFormula setFunction) {
		if (getParent() instanceof VisualFunctionComponent) {
			VisualFunctionComponent p = (VisualFunctionComponent) getParent();
			p.resetRenderingResult();
		}
		renderedSetFormula = null;
		getFunction().setSetFunction(setFunction);

		sendNotification(new PropertyChangedEvent(this, "setFunction"));
	}

	public void updateCombinedFunction() {
		CleverBooleanWorker worker = new CleverBooleanWorker();
		BooleanOperations.worker = new DumbBooleanWorker();
		if (getFunction().getSetFunction()!=null&&
			getFunction().getResetFunction()!=null)

			getFunction().setCombinedFunction(
					DnfGenerator.generate(
					worker.or(getFunction().getSetFunction(), worker.and(new FreeVariable(getName()), worker.not(getFunction().getResetFunction())))
					));

		if (getFunction().getSetFunction()!=null&&
			getFunction().getResetFunction()==null)

			getFunction().setCombinedFunction(
					DnfGenerator.generate(getFunction().getSetFunction()));
	}


	private void drawFormula(Graphics2D g, int arrowType, float xOffset, float yOffset, Color foreground, Color background, FormulaRenderingResult result) {

		Rectangle2D textBB = result.boundingBox;

		float textX = 0;
		float textY = (float)-textBB.getCenterY()-(float)0.5-yOffset;

		float arrX = 0;
		float arrY = (float)-textBB.getCenterY()-(float)0.5-yOffset;


		AffineTransform transform = g.getTransform();
		AffineTransform at = new AffineTransform();
		Direction dir = getDirection();

		if (!(getParent() instanceof VisualFunctionComponent)) {
			dir = flipDirection(dir);
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
//			g.draw(path);
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
//			g.draw(path);
			g.draw(line);
		}

		g.translate(textX, textY);


		result.draw(g, foreground);

		g.setTransform(transform);
	}


	@Override
	public void draw(DrawRequest r) {
		super.draw(r);
		Graphics2D g = r.getGraphics();
		Color foreground = Coloriser.colorise(Color.BLACK, r.getDecoration().getColorisation());
		Color background = Coloriser.colorise(Color.WHITE, r.getDecoration().getBackground());
		Node p = getParent();
		if (p!=null) {
			if ((getIOType()==IOType.INPUT)^(p instanceof VisualComponent)) {
				if (!(p instanceof VisualCircuitComponent)||
						((VisualCircuitComponent)p).getRenderType()==RenderType.BOX) {

					FormulaRenderingResult setResult = getRenderedSetFormula(g.getFontRenderContext());
					FormulaRenderingResult resetResult = getRenderedResetFormula(g.getFontRenderContext());
					float xOfs = (float)0.5;

					if (!CircuitSettings.getShowContacts()&&(p instanceof VisualComponent)) xOfs = (float)-0.5;

					if (resetResult!=null) {
						drawFormula(g, 1, xOfs, (float)-0.2, foreground, background, resetResult);
						drawFormula(g, 2, xOfs, (float)0.5, foreground, background, setResult);

					} else {
						drawFormula(g, 0, xOfs, (resetResult==null?(float)0:(float)0.5), foreground, background, setResult);
					}
				}
			}
		}

	}


	@Override
	public void notify(StateEvent e) {
//		renderedFormula = null;
	}

	public FunctionContact getFunction() {
		return function;
	}

}
