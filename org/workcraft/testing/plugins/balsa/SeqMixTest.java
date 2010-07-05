package org.workcraft.testing.plugins.balsa;

import java.io.IOException;

import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.parsers.breeze.BreezeInstance;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.DefaultBreezeFactory;
import org.workcraft.parsers.breeze.EmptyValueList;
import org.workcraft.parsers.breeze.ParameterValueList;
import org.workcraft.parsers.breeze.PrimitivePart;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeHandshake;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.io.BalsaSystem;
import org.workcraft.plugins.balsa.io.BalsaToStgExporter_FourPhase;
import org.workcraft.plugins.balsa.io.SynthesisWithMpsat;
import org.workcraft.plugins.balsa.io.SynthesisWithPetrify;
import org.workcraft.util.Export;

public class SeqMixTest {

	Framework f;
	Exporter synthesiser;

	public SeqMixTest() throws PluginInstantiationException
	{
		f = new Framework();
		f.initPlugins();
		synthesiser = f.getPluginManager().getSingleton(SynthesisWithMpsat.class);
	}

	@Test
	public void testIndividual() throws IOException, InvalidConnectionException, ModelValidationException, SerialisationException
	{
		BalsaCircuit circuit = new BalsaCircuit();
		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);

		BreezeLibrary lib = new BreezeLibrary(BalsaSystem.DEFAULT());
		PrimitivePart seq = lib.getPrimitive("SequenceOptimised");
		PrimitivePart loop = lib.getPrimitive("Loop");
		PrimitivePart concur = lib.getPrimitive("Concur");
		PrimitivePart call = lib.getPrimitive("Call");
		PrimitivePart passivate = lib.getPrimitive("Passivator");
		//ParameterValueList params = new ParameterValueList.StringList();
		BreezeInstance<BreezeHandshake> loopInst = loop.instantiate(factory,  EmptyValueList.instance());
		BreezeInstance<BreezeHandshake> seqInst = seq.instantiate(factory,  new ParameterValueList.StringList("3", "3"));
		BreezeInstance<BreezeHandshake> concurInst = concur.instantiate(factory,  new ParameterValueList.StringList("2"));
		BreezeInstance<BreezeHandshake> mix1 = call.instantiate(factory,  new ParameterValueList.StringList("2"));
		BreezeInstance<BreezeHandshake> mix2 = call.instantiate(factory,  new ParameterValueList.StringList("2"));
		BreezeInstance<BreezeHandshake> passivate1 = passivate.instantiate(factory,  new ParameterValueList.StringList("1"));
		BreezeInstance<BreezeHandshake> passivate2 = passivate.instantiate(factory,  new ParameterValueList.StringList("1"));

		Export.exportToFile(new BalsaToStgExporter_FourPhase(), circuit, "/home/dell/export_unconnected.g");

		Export.exportToFile(synthesiser, circuit, "/home/dell/export_unconnected.eqn");
	}

	@Test
	public void test() throws InvalidConnectionException, IOException, ModelValidationException, SerialisationException, PluginInstantiationException
	{
		BalsaCircuit circuit = new BalsaCircuit();
		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);

		BreezeLibrary lib = new BreezeLibrary(BalsaSystem.DEFAULT());
		PrimitivePart seq = lib.getPrimitive("SequenceOptimised");
		PrimitivePart loop = lib.getPrimitive("Loop");
		PrimitivePart concur = lib.getPrimitive("Concur");
		PrimitivePart call = lib.getPrimitive("Call");
		PrimitivePart passivate = lib.getPrimitive("Passivator");
		//ParameterValueList params = new ParameterValueList.StringList();
		BreezeInstance<BreezeHandshake> loopInst = loop.instantiate(factory,  EmptyValueList.instance());
		BreezeInstance<BreezeHandshake> seqInst = seq.instantiate(factory,  new ParameterValueList.StringList("3", "3"));
		BreezeInstance<BreezeHandshake> concurInst = concur.instantiate(factory,  new ParameterValueList.StringList("2"));
		BreezeInstance<BreezeHandshake> mix1 = call.instantiate(factory,  new ParameterValueList.StringList("2"));
		BreezeInstance<BreezeHandshake> mix2 = call.instantiate(factory,  new ParameterValueList.StringList("2"));
		BreezeInstance<BreezeHandshake> passivate1 = passivate.instantiate(factory,  new ParameterValueList.StringList("1"));
		BreezeInstance<BreezeHandshake> passivate2 = passivate.instantiate(factory,  new ParameterValueList.StringList("1"));

		circuit.connect(loopInst.ports().get(1), seqInst.ports().get(0));
		circuit.connect(seqInst.ports().get(2), concurInst.ports().get(0));
		circuit.connect(seqInst.ports().get(1), mix1.ports().get(0));
		circuit.connect(seqInst.ports().get(3), mix2.ports().get(0));
		circuit.connect(concurInst.ports().get(1), mix1.ports().get(1));
		circuit.connect(concurInst.ports().get(2), mix2.ports().get(1));
		circuit.connect(mix1.ports().get(2), passivate1.ports().get(0));
		circuit.connect(mix2.ports().get(2), passivate2.ports().get(0));

		BalsaToStgExporter_FourPhase exporter = new BalsaToStgExporter_FourPhase();
		exporter.getSettings().eventBasedInternal = false;
		exporter.getSettings().improvedPcomp = true;
		Export.exportToFile(exporter, circuit, "/home/dell/export.g");

		Export.exportToFile((Exporter)synthesiser, circuit, "/home/dell/export.eqn");
	}
}
