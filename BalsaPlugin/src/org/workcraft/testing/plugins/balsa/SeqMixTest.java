package org.workcraft.testing.plugins.balsa;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Model;
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
import org.workcraft.plugins.balsa.io.BalsaSystem;
import org.workcraft.plugins.balsa.io.BalsaToStgExporter;
import org.workcraft.plugins.balsa.io.BalsaToStgExporter_FourPhase;
import org.workcraft.plugins.balsa.io.SynthesisWithMpsat;
import org.workcraft.plugins.desij.DesiJOperation;
import org.workcraft.plugins.desij.DesiJSettings;
import org.workcraft.plugins.desij.tasks.DesiJResult;
import org.workcraft.plugins.desij.tasks.DesiJTask;
import org.workcraft.tasks.Result;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Import;

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
		//BreezeInstance<BreezeHandshake> loopInst = loop.instantiate(factory,  EmptyValueList.instance());
		//BreezeInstance<BreezeHandshake> seqInst = seq.instantiate(factory,  new ParameterValueList.StringList("3", "3"));
		//BreezeInstance<BreezeHandshake> concurInst = concur.instantiate(factory,  new ParameterValueList.StringList("2"));
		//BreezeInstance<BreezeHandshake> mix1 = call.instantiate(factory,  new ParameterValueList.StringList("2"));
		//BreezeInstance<BreezeHandshake> mix2 = call.instantiate(factory,  new ParameterValueList.StringList("2"));
		//BreezeInstance<BreezeHandshake> passivate1 = passivate.instantiate(factory,  new ParameterValueList.StringList("1"));
		//BreezeInstance<BreezeHandshake> passivate2 = passivate.instantiate(factory,  new ParameterValueList.StringList("1"));

		Export.exportToFile(new BalsaToStgExporter_FourPhase(), circuit, "/home/dell/export_unconnected_mixer.g");

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
		Export.exportToFile(exporter.withCompositionSettings(new BalsaToStgExporter.CompositionSettings(false, false)), circuit, "/home/dell/export_standard.g");
		Export.exportToFile((Exporter)synthesiser, circuit, "/home/dell/export.eqn");
	}

	static class Generator
	{
		BalsaCircuit circuit = new BalsaCircuit();
		DefaultBreezeFactory factory = new DefaultBreezeFactory(circuit);

		BreezeLibrary lib;
		PrimitivePart seq;
		PrimitivePart loop;
		PrimitivePart concur;
		PrimitivePart call;
		PrimitivePart sync;
		PrimitivePart passivate;

		Generator() throws IOException
		{
			circuit = new BalsaCircuit();
			factory = new DefaultBreezeFactory(circuit);

			lib = new BreezeLibrary(BalsaSystem.DEFAULT());
			seq = lib.getPrimitive("SequenceOptimised");
			loop = lib.getPrimitive("Loop");
			concur = lib.getPrimitive("Concur");
			call = lib.getPrimitive("Call");
			passivate = lib.getPrimitive("Passivator");
			sync = lib.getPrimitive("Synch");
		}

		void build(int depth, BreezeHandshake top, BreezeHandshake bottom) throws InvalidConnectionException
		{
			if(depth == 0)
			{
				if(top != null && bottom != null)
					circuit.connect(top, bottom);
			}
			else
			{

				BreezeInstance<BreezeHandshake> topPart;
				BreezeInstance<BreezeHandshake> bottomPart;
				if((depth & 1) == 1)
				{
					topPart = concur.instantiate(factory,  new ParameterValueList.StringList("2"));
					bottomPart = sync.instantiate(factory,  new ParameterValueList.StringList("2"));
				}
				else
				{
					topPart = seq.instantiate(factory,  new ParameterValueList.StringList("2", "2"));
					bottomPart = call.instantiate(factory,  new ParameterValueList.StringList("2"));
				}

				if(top != null)
					circuit.connect(top, topPart.ports().get(0));

				build(depth-1, topPart.ports().get(1), bottomPart.ports().get(0));
				build(depth-1, topPart.ports().get(2), bottomPart.ports().get(1));

				if(bottom != null)
					circuit.connect(bottom, bottomPart.ports().get(2));
			}
		}

		public BalsaCircuit build(int depth) throws InvalidConnectionException {
			build(depth, null, null);
			return circuit;
		}
	}

	@Test
	public void recursiveTest() throws Exception
	{
		new File("/home/dell/beautiful_table.txt").delete();
		recTest();
	}

	private void recTest() throws Exception {
		for(int depth = 1;depth < 10;depth++)
		//int depth = 2;
			for(int o = 0;o < 2;o++)
				for(int s = 0;s < 2;s++)
				{
					boolean safenessPreserv = s==1;
					DesiJSettings desiJSettings = new DesiJSettings(DesiJOperation.REMOVE_DUMMIES, null, 0, null, null, true, true, false,
							safenessPreserv, false, false,
							false, 0, false, false);

					BalsaCircuit circuit = new ParArbMixDiamondTest.Generator().build(depth);

					BalsaToStgExporter_FourPhase exporter = new BalsaToStgExporter_FourPhase();
					String fileName = "/home/dell/SeqMixParSync_"+(o==0?"std":"opt")+"_"+depth;
					File gFile = new File(fileName+".g");
					File contractedGFile = new File(fileName+".contracted.g");
					Export.exportToFile(exporter.withCompositionSettings(new BalsaToStgExporter.CompositionSettings(false, 1==(o&1))), circuit, gFile);

					PrintStream defaultOut = System.out;
					File desiJOutFile = new File("/home/dell/desij" + (o==0?"std":"opt") + "_" + depth + "_" + (safenessPreserv?"safe":"all") + ".out");
					PrintStream desiJOut = new PrintStream(desiJOutFile);
					Model model = Import.importFromFile(Import.chooseBestImporter(f.getPluginManager(), gFile),gFile);

					long ts = System.currentTimeMillis();

					System.setOut(desiJOut);
					Result<DesiJResult> result = f.getTaskManager().execute(new DesiJTask(model, f, desiJSettings), "desij");

					long dt = System.currentTimeMillis() - ts;

					File resultingFile = result.getReturnValue().getModifiedSpecResult();
					FileUtils.moveFile(resultingFile, contractedGFile);

					System.setOut(defaultOut);
					String log = FileUtils.readAllText(desiJOutFile);
					Pattern pattern = Pattern.compile(".* ([0-9]+) dummy transitions removed.*", Pattern.MULTILINE);
					Matcher matcher = pattern.matcher(log);
					if(!matcher.find())
					{
						throw new RuntimeException("no contraction information! the only information is: " + log);
					}
					else
					{
						FileUtils.appendAllText(new File("/home/dell/beautiful_table.txt"), (o==0?"std":"opt") + "\t" + depth + "\t" + (safenessPreserv?"safe":"all") + "\t" + matcher.group(1) + "\t" + dt + "\n");
						System.out.println();
					}

				}
	}
}
