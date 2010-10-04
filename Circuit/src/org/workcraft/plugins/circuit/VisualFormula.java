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

import java.awt.event.KeyEvent;
import java.awt.font.GlyphVector;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.gui.propertyeditor.PropertyDeclaration;
import java.awt.Graphics2D;

@DisplayName("Formula")
@Hotkey(KeyEvent.VK_F)
@SVGIcon("images/icons/svg/circuit-formula.svg")

public class VisualFormula extends VisualCircuitComponent {

	GlyphVector formulaGlyph=null;

	public VisualFormula(Formula component) {
		super(component);
		addPropertyDeclarations();
	}

	private void addPropertyDeclarations() {
		addPropertyDeclaration(new PropertyDeclaration(this, "Formula", "getFormula", "setFormula", String.class));
	}

	public String getFormula() {
		return ((Formula)getReferencedComponent()).getFormula();
	}

	public void setFormula(String formula) {
		((Formula)getReferencedComponent()).setFormula(formula);
	}

	@Override
	public void draw(Graphics2D g) {
		super.draw(g);

	}



}
