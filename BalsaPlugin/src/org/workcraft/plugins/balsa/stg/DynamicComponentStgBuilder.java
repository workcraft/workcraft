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
package org.workcraft.plugins.balsa.stg;

import java.util.Map;

import org.workcraft.plugins.balsa.HandshakeComponentLayout;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakestgbuilder.StgInterface;
import org.workcraft.plugins.balsa.stg.implementations.StgBuilderSelector;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class DynamicComponentStgBuilder extends ComponentStgBuilder<DynamicComponent> {

	@Override
	public void buildStg(DynamicComponent component,
			Map<String, StgInterface> handshakes, StrictPetriBuilder builder) {
		getBuilder(component).buildStg(component, handshakes, builder);
	}
	private ComponentStgBuilder<DynamicComponent> getBuilder(DynamicComponent component) {
		return StgBuilderSelector.create(component.declaration().getName());
	}
	@Override
	public void buildEnvironment(DynamicComponent component, Map<String, StgInterface> handshakes, StrictPetriBuilder builder)
	{
		getBuilder(component).buildEnvironmentConstraint(component, handshakes, builder);
	}
	@Override
	public HandshakeComponentLayout getLayout(DynamicComponent component, Map<String, Handshake> handshakes) {
		return getBuilder(component).getLayout(component, handshakes);
	}
}
