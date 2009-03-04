package org.workcraft.plugins.balsa.stgmodelstgbuilder;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.plugins.balsa.stgbuilder.ReadablePlace;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;

public class StgModelStgBuilder implements StgBuilder {


	private final STG model;

	public StgModelStgBuilder(STG model)
	{
		this.model = model;
	}

	public void addConnection(StgModelStgPlace source, StgModelStgTransition destination)
	{
		addConnection(source.getPetriPlace(), destination.getModelTransition());
	}

	public void addConnection(StgModelStgTransition source, StgModelStgPlace destination)
	{
		addConnection(source.getModelTransition(), destination.getPetriPlace());
	}

	public void addConnection(Component source, Component destination)
	{
		try {
			model.addConnection(new Connection(source, destination));
		} catch (InvalidConnectionException e) {
			e.printStackTrace();
			throw new RuntimeException("Invalid connection o_O");
		}
	}

	@Override
	public void addConnection(StgPlace place, StgTransition transition) {
		addConnection((StgModelStgPlace)place, (StgModelStgTransition)transition);
	}

	@Override
	public void addConnection(StgTransition transition, StgPlace place) {
		addConnection((StgModelStgTransition)transition, (StgModelStgPlace)place);
	}

	@Override
	public void addConnection(TransitionOutput transition, StgPlace place) {
		addConnection((StgModelStgTransition)transition, (StgModelStgPlace)place);
	}

	@Override
	public void addReadArc(ReadablePlace place, StgTransition transition) {
		StgModelStgTransition t = (StgModelStgTransition)transition;
		StgModelStgPlace p = (StgModelStgPlace)place;
		addConnection(p, t);
		addConnection(t, p);
	}

	@Override
	public StgPlace buildPlace() {
		return buildPlace(0);
	}

	@Override
	public StgTransition buildTransition() {
		SignalTransition transition = new SignalTransition();
		model.addComponent(transition);
		return new StgModelStgTransition(transition);
	}

	@Override
	public StgPlace buildPlace(int tokenCount) {
		Place place = new Place();
		place.setTokens(tokenCount);
		model.addComponent(place);
		return new StgModelStgPlace(place);
	}
}
