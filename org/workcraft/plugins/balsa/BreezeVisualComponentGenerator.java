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

package org.workcraft.plugins.balsa;

import org.workcraft.dom.VisualComponentGenerator;
import org.workcraft.dom.math.MathNode;
import org.workcraft.dom.visual.VisualNode;

public class BreezeVisualComponentGenerator implements VisualComponentGenerator {

	@Override
	public VisualNode createComponent(MathNode component, Object... constructorParameters){
		if(constructorParameters.length == 0)
			return new VisualBreezeComponent((BreezeComponent)component);
		/*if(constructorParameters.length == 1)
			if(constructorParameters[0] instanceof Element)
				try {
					return new VisualBreezeComponent((BreezeComponent)component, (Element)constructorParameters[0]);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}*/
		throw new RuntimeException("Unsupported constructor parameters!");
	}
}
