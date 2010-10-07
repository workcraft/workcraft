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

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;

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
import static org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations.*;

import org.workcraft.plugins.cpog.optimisation.dnf.DnfGenerator;
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations;
import org.workcraft.plugins.cpog.optimisation.expressions.CleverBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker;
import org.workcraft.plugins.cpog.optimisation.expressions.Not;
import org.workcraft.plugins.cpog.optimisation.expressions.Or;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Func;



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

	private FormulaRenderingResult renderedFormula = null;
	public void resetRenderedFormula() {
		renderedFormula = null;
	}


	FormulaRenderingResult getRenderedFormula(FontRenderContext fcon) {
		if (renderedFormula == null) {
			updateCombinedFunction();
			renderedFormula = FormulaToGraphics.render(((FunctionContact)getReferencedContact()).getCombinedFunction(), fcon, font);
		}
		return renderedFormula;
	}

	FunctionContact function=null;

	public VisualFunctionContact(FunctionContact component) {
		super(component);
		function=component;
		addPropertyDeclarations();
	}

	public VisualFunctionContact(FunctionContact component, VisualContact.Direction dir, String label) {
		super(component);
		function=component;

		component.addObserver(this);
		setName(label);
		setDirection(dir);

		addPropertyDeclarations();

	}

	private BooleanFormula parseFormula(String resetFunction) {
		try {
			return BooleanParser.parse(resetFunction,
					new Func<String, BooleanFormula>() {
						@Override
						public BooleanFormula eval(String name) {
							return ((VisualFunctionComponent)getParent()).getOrCreateInput(name)
									.getReferencedContact();
						}
					});
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@NoAutoSerialisation
	public String getResetFunction() {
		return FormulaToString.toString(function.getResetFunction());
	}

	@NoAutoSerialisation
	public String getSetFunction() {
		return FormulaToString.toString(function.getSetFunction());
	}

	@NoAutoSerialisation
	public void setResetFunction(String resetFunction) {
		renderedFormula = null;
		function.setResetFunction(parseFormula(resetFunction));
		sendNotification(new PropertyChangedEvent(this, "resetFunction"));
	}

	@NoAutoSerialisation
	public void setSetFunction(String setFunction) {
		renderedFormula = null;
		function.setSetFunction(parseFormula(setFunction));

		sendNotification(new PropertyChangedEvent(this, "setFunction"));
	}

	public void updateCombinedFunction() {
		CleverBooleanWorker worker = new CleverBooleanWorker();
		BooleanOperations.worker = new DumbBooleanWorker();
		function.setCombinedFunction(
				DnfGenerator.generate(
				worker.or(function.getSetFunction(), worker.and(new FreeVariable(getName()), worker.not(function.getResetFunction())))
				)

		);
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration(this, "Set function", "getSetFunction", "setSetFunction", String.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Reset function", "getResetFunction", "setResetFunction", String.class));
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);

		if (getIOType()==IOType.OUTPUT) {

			FormulaRenderingResult result = getRenderedFormula(g.getFontRenderContext());

			Rectangle2D textBB = result.boundingBox;

			float textX = 0;
			float textY = (float)-textBB.getCenterY()-(float)0.5;

			AffineTransform transform = g.getTransform();
			AffineTransform at = new AffineTransform();

			switch (getDirection()) {
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

			result.draw(g, Coloriser.colorise(getColorisation(), getColorisation()));

			g.setTransform(transform);

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

}
