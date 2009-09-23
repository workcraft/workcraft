package org.workcraft.plugins.balsa.stgmodelstgbuilder;

import java.util.HashMap;
import java.util.Map;

import org.workcraft.dom.math.MathNode;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.plugins.balsa.stgbuilder.AnyPlace;
import org.workcraft.plugins.balsa.stgbuilder.InputEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputOutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.InputPlace;
import org.workcraft.plugins.balsa.stgbuilder.SignalId;
import org.workcraft.plugins.balsa.stgbuilder.StgBuilder;
import org.workcraft.plugins.balsa.stgbuilder.OutputPlace;
import org.workcraft.plugins.balsa.stgbuilder.StgSignal;
import org.workcraft.plugins.balsa.stgbuilder.OutputEvent;
import org.workcraft.plugins.balsa.stgbuilder.Event;
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

	public void addConnection(MathNode source, MathNode destination)
	{
		try {
			model.connect(source, destination);
		} catch (InvalidConnectionException e) {
			e.printStackTrace();
			throw new RuntimeException("Invalid connection o_O");
		}
	}

	private void connectInternal(AnyPlace place, Event transition) {
		addConnection((StgModelStgPlace)place, (StgModelStgTransition)transition);
	}

	public void connectInternal(Event transition, AnyPlace place) {
		addConnection((StgModelStgTransition)transition, (StgModelStgPlace)place);
	}

	public void connect(Event transition, OutputPlace place) {
		addConnection((StgModelStgTransition)transition, (StgModelStgPlace)place);
	}

	public void addReadArc(AnyPlace place, Event transition) {
		StgModelStgTransition t = (StgModelStgTransition)transition;
		StgModelStgPlace p = (StgModelStgPlace)place;
		addConnection(p, t);
		addConnection(t, p);
	}

	public OutputPlace buildPlace() {
		return buildPlace(0);
	}

	public StgModelStgTransition buildTransition() {
		SignalTransition transition = new SignalTransition();
		model.add(transition);
		return new StgModelStgTransition(transition);
	}

	private StgModelStgPlace buildAnyPlace(int tokenCount) {
		Place place = new Place();
		place.setTokens(tokenCount);
		model.add(place);
		return new StgModelStgPlace(place);
	}

	public StgSignal buildSignal(SignalId id, boolean isOutput) {
		final StgModelStgTransition transitionP = buildTransition();
		final StgModelStgTransition transitionM = buildTransition();
		final String sname = nameProvider.getName(id.getOwner()) + "_" + id.getName();
		transitionP.getModelTransition().setSignalName(sname);
		transitionM.getModelTransition().setSignalName(sname);

		transitionP.getModelTransition().setDirection(Direction.PLUS);
		transitionM.getModelTransition().setDirection(Direction.MINUS);

		Type type = isOutput ? Type.OUTPUT : Type.INPUT;

		transitionP.getModelTransition().setSignalType(type);
		transitionM.getModelTransition().setSignalType(type);

		final StgSignal result = new StgSignal()
		{
			public InputOutputEvent getMinus() {
				return transitionM;
			}
			public InputOutputEvent getPlus() {
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

	private void connectInternal(Event t1, Event t2) {
		OutputPlace place = this.buildPlace();
		connect(t1, place);
		connectInternal(place, t2);
	}

	@Override
	public void addReadArc(OutputPlace place, OutputEvent transition) {
		addReadArc((AnyPlace)place, transition);
	}

	@Override
	public void addReadArc(InputPlace place, InputEvent transition) {
		addReadArc((AnyPlace)place, transition);
	}

	@Override
	public InputPlace buildInputPlace() {
		return buildAnyPlace(0);
	}

	@Override
	public void connect(OutputPlace place, OutputEvent transition) {
		connectInternal(place, transition);
	}

	@Override
	public void connect(InputPlace place, InputEvent transition) {
		connectInternal(place, transition);
	}

	@Override
	public void connect(Event transition, InputPlace place) {
		connectInternal(transition, place);
	}

	@Override
	public void connect(Event t1, OutputEvent t2) {
		connectInternal(t1, t2);
	}

	@Override
	public void connect(Event t1, InputOutputEvent t2) {
		connectInternal(t1, t2);
	}

	@Override
	public void connect(Event t1, InputEvent t2) {
		connectInternal(t1, t2);
	}

	@Override
	public OutputPlace buildPlace(int tokenCount) {
		return buildAnyPlace(tokenCount);
	}
}
