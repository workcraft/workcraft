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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.gui.Coloriser;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.observation.StateEvent;
import org.workcraft.observation.StateObserver;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.FreeVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaRenderingResult;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToGraphics;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.CleverBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Func;


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
		addPropertyDeclarations();
	}

	public VisualFunctionContact(FunctionContact component, VisualContact.Direction dir, String label) {
		super(component);
		function = component;

		component.addObserver(this);
		setName(label);
		setDirection(dir);

		addPropertyDeclarations();

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
		renderedResetFormula = null;
		getFunction().setResetFunction(resetFunction);

		sendNotification(new PropertyChangedEvent(this, "resetFunction"));
	}

	@NoAutoSerialisation
	public void setSetFunction(BooleanFormula setFunction) {
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

	private void addPropertyDeclarations() {
		//addPropertyDeclaration(new PropertyDeclaration(this, "Set function", "getSetFunction", "setSetFunction", String.class));
		//addPropertyDeclaration(new PropertyDeclaration(this, "Reset function", "getResetFunction", "setResetFunction", String.class));
	}


	private void drawFormula(Graphics2D g, float yOffset, Color color, FormulaRenderingResult result) {

		Rectangle2D textBB = result.boundingBox;

		float textX = 0;
		float textY = (float)-textBB.getCenterY()-(float)0.5-yOffset;

		AffineTransform transform = g.getTransform();
		AffineTransform at = new AffineTransform();
		Direction dir = getDirection();

		if (!(getParent() instanceof VisualFunctionComponent)) {
			dir = flipDirection(dir);
		}

		switch (dir) {
		case EAST:
			textX = (float)+0.5;
			break;
		case NORTH:
			at.quadrantRotate(-1);
			g.transform(at);
			textX = (float)+0.5;
			break;
		case WEST:
			textX = (float)-textBB.getWidth()-(float)0.5;
			break;
		case SOUTH:
			at.quadrantRotate(-1);
			g.transform(at);
			textX = (float)-textBB.getWidth()-(float)0.5;
			break;
		}

		g.translate(textX, textY);
		result.draw(g, color);

		g.setTransform(transform);

	}

	@Override
	public void draw(DrawRequest r) {
		super.draw(r);

		Graphics2D g = r.getGraphics();
		Color colorisation = r.getDecoration().getColorisation();

		if (getParent()!=null) {
			if ((getIOType()==IOType.INPUT)^(getParent() instanceof VisualComponent)) {
				FormulaRenderingResult setResult = getRenderedSetFormula(g.getFontRenderContext());
				FormulaRenderingResult resetResult = getRenderedResetFormula(g.getFontRenderContext());

				drawFormula(g, (resetResult==null?(float)0:(float)0.5), Coloriser.colorise(Color.BLACK, colorisation), setResult);
				if (resetResult!=null)
					drawFormula(g, (float)-0.2, Coloriser.colorise(Color.BLACK, colorisation), resetResult);
			}

		}

		/*
		HashMap<BooleanVariable, BooleanFormula> values = new HashMap<BooleanVariable, BooleanFormula>();

		for(VisualContact c : Hierarchy.filterNodesByType(getChildren(), VisualContact.class))
			values.put(c.getReferencedContact(), c.getDirection() == Direction.WEST ? One.instance() : Zero.instance());

		g.setColor(function.getSetFunction()
				.accept(new BooleanReplacer(values))
				.accept(new BooleanEvaluator()) ? Color.GREEN : Color.RED);

		g.drawOval(-1, -1, 2, 2);
		*/
	}


	@Override
	public void notify(StateEvent e) {
//		renderedFormula = null;
	}

	public FunctionContact getFunction() {
		return function;
	}

}
