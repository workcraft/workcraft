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
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;
import java.util.HashMap;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import org.workcraft.plugins.circuit.Contact.IOType;
import org.workcraft.plugins.circuit.VisualContact.Direction;
import org.workcraft.plugins.cpog.optimisation.BooleanEvaluator;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.expressions.One;
import org.workcraft.plugins.cpog.optimisation.expressions.Zero;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.plugins.cpog.optimisation.javacc.ParseException;
import org.workcraft.serialisation.xml.NoAutoSerialisation;
import org.workcraft.util.Func;
import org.workcraft.util.Hierarchy;

@DisplayName("Function")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/icons/svg/circuit-formula.svg")

public class VisualFunction extends VisualCircuitComponent {

	GlyphVector formulaGlyph=null;
	Function function=null;

	public VisualFunction(Function component) {
		super(component);
		function=component;
		addPropertyDeclarations();
	}

	public VisualContact getOrCreateInput(String arg) {

		for(VisualContact c : Hierarchy.filterNodesByType(getChildren(), VisualContact.class)) {
			if(c.getIOType() == IOType.INPUT && c.getName().equals(arg))
				return c;
		}

		return addInput(arg, Direction.WEST);
	}

	private BooleanFormula parseFormula(String resetFunction) {
		try {
			return BooleanParser.parse(resetFunction,
					new Func<String, BooleanFormula>() {
						@Override
						public BooleanFormula eval(String name) {
							return getOrCreateInput(name)
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
		function.setResetFunction(parseFormula(resetFunction));
	}

	@NoAutoSerialisation
	public void setSetFunction(String setFunction) {
		function.setSetFunction(parseFormula(setFunction));
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration(this, "Set function", "getSetFunction", "setSetFunction", String.class));
		addPropertyDeclaration(new PropertyDeclaration(this, "Reset function", "getResetFunction", "setResetFunction", String.class));
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);
		HashMap<BooleanVariable, BooleanFormula> values = new HashMap<BooleanVariable, BooleanFormula>();

		for(VisualContact c : Hierarchy.filterNodesByType(getChildren(), VisualContact.class))
			values.put(c.getReferencedContact(), c.getDirection() == Direction.WEST ? One.instance() : Zero.instance());
		g.setColor(function.getSetFunction()
				.accept(new BooleanReplacer(values))
				.accept(new BooleanEvaluator()) ? Color.GREEN : Color.RED);

		g.drawOval(-1, -1, 2, 2);
	}



}
