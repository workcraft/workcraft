package org.workcraft.plugins.balsa.stgmodelstgbuilder;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.plugins.balsa.stgbuilder.ReadablePlace;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.StgPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgTransition;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.TransitionOutput;
import org.workcraft.plugins.petri.Place;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.SignalTransition;
import org.workcraft.plugins.stg.SignalTransition.Direction;
import org.workcraft.plugins.stg.SignalTransition.Type;

public class StgModelStgBuilder implements StgBuilder {

	private final STG model;
	HandshakeNameProvider nameProvider;

	public StgModelStgBuilder(STG model, HandshakeNameProvider nameProvider)
	{
		this.model = model;
		this.nameProvider = nameProvider;
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

	public void addConnection(StgPlace place, StgTransition transition) {
		addConnection((StgModelStgPlace)place, (StgModelStgTransition)transition);
	}

	public void addConnection(StgTransition transition, StgPlace place) {
		addConnection((StgModelStgTransition)transition, (StgModelStgPlace)place);
	}

	public void addConnection(TransitionOutput transition, StgPlace place) {
		addConnection((StgModelStgTransition)transition, (StgModelStgPlace)place);
	}

	public void addReadArc(ReadablePlace place, StgTransition transition) {
		StgModelStgTransition t = (StgModelStgTransition)transition;
		StgModelStgPlace p = (StgModelStgPlace)place;
		addConnection(p, t);
		addConnection(t, p);
	}

	public StgPlace buildPlace() {
		return buildPlace(0);
	}

	public StgModelStgTransition buildTransition() {
		SignalTransition transition = new SignalTransition();
		model.addComponent(transition);
		return new StgModelStgTransition(transition);
	}

	public StgPlace buildPlace(int tokenCount) {
		Place place = new Place();
		place.setTokens(tokenCount);
		model.addComponent(place);
		return new StgModelStgPlace(place);
	}

	public StgSignal buildSignal(SignalId id, boolean isOutput) {
		final StgModelStgTransition transitionP = buildTransition();
		final StgModelStgTransition transitionM = buildTransition();
		final String sname = nameProvider.getName(id.getOwner()) + ":" + id.getName();
		transitionP.getModelTransition().setSignalName(sname);
		transitionM.getModelTransition().setSignalName(sname);

		transitionP.getModelTransition().setDirection(Direction.PLUS);
		transitionM.getModelTransition().setDirection(Direction.MINUS);

		Type type = isOutput ? Type.OUTPUT : Type.INPUT;

		transitionP.getModelTransition().setType(type);
		transitionM.getModelTransition().setType(type);

		final StgSignal result = new StgSignal()
		{
			public StgTransition getMinus() {
				return transitionM;
			}
			public StgTransition getPlus() {
				return transitionP;
			}
		};

		if(exports.containsKey(id))
			throw new RuntimeException("Transitions with duplicate ids are not allowed!");
		exports.put(id, result);
		return result;
	}

	public Map<SignalId, StgSignal> getExports()
	{
		return exports;
	}

	HashMap<SignalId, StgSignal> exports = new HashMap<SignalId, StgSignal>();
}
