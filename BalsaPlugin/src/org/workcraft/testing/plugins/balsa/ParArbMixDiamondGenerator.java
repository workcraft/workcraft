package org.workcraft.testing.plugins.balsa;

import java.io.IOException;

import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.parsers.breeze.BreezeInstance;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.DefaultBreezeFactory;
import org.workcraft.parsers.breeze.ParameterValueList;
import org.workcraft.parsers.breeze.PrimitivePart;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeHandshake;
import org.workcraft.plugins.balsa.io.BalsaSystem;
import org.workcraft.testing.plugins.balsa.SeqMixTest.DiamondGenerator;

public class ParArbMixDiamondGenerator implements DiamondGenerator {

	public static class State {

		BalsaCircuit circuit = new BalsaCircuit();

		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);

		BreezeLibrary lib;
		PrimitivePart seq;
		PrimitivePart loop;
		PrimitivePart concur;
		PrimitivePart call;
		PrimitivePart passivate;
		PrimitivePart arbiter;

		State() {
			try {
				lib = new BreezeLibrary(BalsaSystem.DEFAULT());
			} catch (IOException e) {
				e.printStackTrace();
			}
			seq = lib.getPrimitive("SequenceOptimised");
			loop = lib.getPrimitive("Loop");
			concur = lib.getPrimitive("Concur");
			call = lib.getPrimitive("Call");
			passivate = lib.getPrimitive("Passivator");
			arbiter = lib.getPrimitive("Arbiter");
		}

		public void build(int depth, BreezeHandshake top, BreezeHandshake bottom) throws InvalidConnectionException {
			if (depth == 0) {
				if (top != null && bottom != null)
					circuit.connect(top, bottom);
			} else {

				BreezeInstance<BreezeHandshake> concurInstance = concur.instantiate(factory, new ParameterValueList.StringList("2"));
				BreezeInstance<BreezeHandshake> arbiterInstance = arbiter.instantiate(factory, new ParameterValueList.StringList());
				BreezeInstance<BreezeHandshake> syncInstance = call.instantiate(factory, new ParameterValueList.StringList("2"));

				if (top != null)
					circuit.connect(top, concurInstance.ports().get(0));

				build(depth - 1, concurInstance.ports().get(1), arbiterInstance.ports().get(0));
				build(depth - 1, concurInstance.ports().get(2), arbiterInstance.ports().get(1));

				circuit.connect(arbiterInstance.ports().get(2), syncInstance.ports().get(0));
				circuit.connect(arbiterInstance.ports().get(3), syncInstance.ports().get(1));

				if (bottom != null)
					circuit.connect(bottom, syncInstance.ports().get(2));
			}
		}
	}

	public BalsaCircuit build(int depth) throws Exception {
		State state = new State();
		state.build(depth, null, null);
		return state.circuit;
	}

	@Override
	public String name() {
		return "ParArbMix";
	}
}
