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

import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StrictPetriBuilder;

public class StrictPetriBuilderImpl implements StrictPetriBuilder {

	private final StgBuilder builder;

	public StrictPetriBuilderImpl(StgBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void addReadArc(StgPlace place, InputOutputEvent transition) {
		builder.addReadArc(place, transition);
	}

	@Override
	public StgPlace buildPlace(int tokenCount) {
		return builder.buildPlace(tokenCount);
	}

	@Override
	public InputOutputEvent buildTransition() {
		return builder.buildTransition();
	}

	@Override
	public void connect(StgPlace place, OutputEvent transition) {
		builder.connect(place, transition);
	}

	@Override
	public void connect(InputEvent transition, StgPlace place) {
		builder.connect(transition, place);
	}

	@Override
	public void connect(InputEvent t1, OutputEvent t2) {
		builder.connect(t1, t2);
	}

}
