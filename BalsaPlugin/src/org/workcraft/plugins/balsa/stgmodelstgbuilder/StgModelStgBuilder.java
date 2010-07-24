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

package org.workcraft.plugins.balsa.stgmodelstgbuilder;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.stgbuilder.Event;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.DummyTransition;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class StgModelStgBuilder implements StgBuilder {

	private final STG model;
	NameProvider<? super Handshake> nameProvider;

	public StgModelStgBuilder(STG model, NameProvider<? super Handshake> nameProvider)
	{
		this.model = model;
		this.nameProvider = nameProvider;
	}

	public void addConnection(StgModelStgPlace source, StgModelStgTransition destination)
	{
		addConnection(source.getPetriPlace(), destination.getModelTransition().getTransition());
	}

	public void addConnection(StgModelStgTransition source, StgModelStgPlace destination)
	{
		addConnection(source.getModelTransition().getTransition(), destination.getPetriPlace());
	}

	public void addConnection(MathNode source, MathNode destination)
	{
		try {
			model.connect(source, destination);
		} catch (InvalidConnectionException e) {
			e.printStackTrace();
			throw new RuntimeException("Invalid connection o_O");
		}
	}

	public void connect(StgPlace place, Event transition) {
		addConnection((StgModelStgPlace)place, (StgModelStgTransition)transition);
	}

	public void connect(Event transition, StgPlace place) {
		addConnection((StgModelStgTransition)transition, (StgModelStgPlace)place);
	}

	public void addReadArc(StgPlace place, Event transition) {
		StgModelStgTransition t = (StgModelStgTransition)transition;
		StgModelStgPlace p = (StgModelStgPlace)place;
		addConnection(p, t);
		addConnection(t, p);
	}

	public StgPlace buildPlace() {
		return buildPlace(0);
	}

	public StgModelStgTransition buildTransition()
	{
		DummyTransition transition = new DummyTransition();
		model.add(transition);
		return new StgModelStgTransition(transition);
	}

	public StgModelStgTransition buildSignalTransition(Type type, String name, Direction direction)
	{
		SignalTransition transition = new SignalTransition();
		transition.setSignalType(type);
		transition.setDirection(direction);
		model.add(transition);
		model.setName(transition, name);
		return new StgModelStgTransition(transition);
	}

	private StgModelStgPlace buildStgPlace(int tokenCount) {
		Place place = new Place();
		place.setTokens(tokenCount);
		model.add(place);
		return new StgModelStgPlace(place);
	}

	public StgSignal buildSignal(SignalId id, boolean isOutput) {
		final Type type = isOutput ? Type.OUTPUT : Type.INPUT;
		final String sname = nameProvider.getName(id.getOwner()) + "_" + id.getName();
		final StgModelStgTransition transitionP = buildSignalTransition(type, sname, Direction.PLUS);
		final StgModelStgTransition transitionM = buildSignalTransition(type, sname, Direction.MINUS);


		final StgSignal result = new StgSignal()
		{
			public InputOutputEvent getMinus() {
				return transitionM;
			}
			public InputOutputEvent getPlus() {
				return transitionP;
			}
		};

		return result;
	}

	public void connect(Event t1, Event t2) {
		StgPlace place = this.buildPlace();
		connect(t1, place);
		connect(place, t2);
	}

	@Override
	public StgPlace buildPlace(int tokenCount) {
		return buildStgPlace(tokenCount);
	}
}
