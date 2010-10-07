package org.workcraft.testing.plugins.balsa;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.parsers.breeze.BreezeInstance;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.DefaultBreezeFactory;
import org.workcraft.parsers.breeze.EmptyValueList;
import org.workcraft.parsers.breeze.ParameterValueList;
import org.workcraft.parsers.breeze.PrimitivePart;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeHandshake;
import org.workcraft.plugins.balsa.io.BalsaExportConfig;
import org.workcraft.plugins.balsa.io.BalsaExportConfig.CompositionMode;
import org.workcraft.plugins.balsa.io.BalsaExportConfig.Protocol;
import org.workcraft.plugins.balsa.io.BalsaSystem;
import org.workcraft.plugins.balsa.io.ExtractControlSTGTask;
import org.workcraft.plugins.desij.DesiJOperation;
import org.workcraft.plugins.desij.DesiJSettings;
import org.workcraft.plugins.desij.tasks.DesiJResult;
import org.workcraft.plugins.desij.tasks.DesiJTask;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.tasks.DefaultTaskManager;
import org.workcraft.tasks.Result;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Import;

public class SeqMixTest {

	Framework f;
	//Exporter synthesiser;

	public SeqMixTest() throws PluginInstantiationException
	{
		f = new Framework();
		f.initPlugins();
//		synthesiser = new SynthesisWithMpsat(f);
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
		BreezeInstance<BreezeHandshake> callInstance = call.instantiate(factory,  new ParameterValueList.StringList("2"));

		final BalsaExportConfig config = new BalsaExportConfig(null, CompositionMode.IMPROVED_PCOMP, Protocol.FOUR_PHASE);
		new DefaultTaskManager().execute(new ExtractControlSTGTask(f, circuit, config), "extraction"); // "export_unconnected_mixer.g"
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


		final ExtractControlSTGTask stgExtractionTask = new ExtractControlSTGTask(f, circuit, new BalsaExportConfig(null, CompositionMode.PCOMP, Protocol.FOUR_PHASE));
		Export.exportToFile(new DotGExporter(), stgExtractionTask.getSTG(), "/home/dell/export_standard.g");
//		Export.exportToFile((Exporter)synthesiser, circuit, "/home/dell/export.eqn");
	}

	static class Generator implements DiamondGenerator
	{
		private final boolean haveConcurs;

		Generator(boolean haveConcurs)
		{
			this.haveConcurs = haveConcurs;
		}

		class GeneratorState
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

			GeneratorState() throws IOException
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
					if(haveConcurs && ((depth & 1) == 1))
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
		}

		@Override
		public String name() {
			return haveConcurs?"SeqMixParSync":"SeqMix";
		}

		@Override
		public BalsaCircuit build(int depth) throws Exception {
			GeneratorState state = new GeneratorState();
			state.build(depth, null, null);
			return state.circuit;
		}
	}

	@Test
	public void recursiveTest() throws Exception
	{
		new File("/home/dell/beautiful_table.txt").delete();
		recTest();
	}

	interface DiamondGenerator
	{
		BalsaCircuit build(int depth) throws Exception;
		String name();
	}

	private void recTest() throws Exception {

		DiamondGenerator[] generators = new DiamondGenerator[]
             {
				new ParArbMixDiamondGenerator(),
				new Generator(true),
				new Generator(false)
             };

		for(DiamondGenerator generator : generators)
		for(int inj = 0;inj < 2;inj++)
		for(int depth = 1;depth < 10;depth++)
		//int depth = 2;
			for(int o = 0;o < 2;o++)
				for(int s = 0;s < 2;s++)
				{
					boolean safenessPreserv = s==1;
					String runName = (inj==1?"inj":"ninj") + "\t" + generator.name() +"\t"+(o==0?"std":"opt") + "\t" + depth + "\t" + (safenessPreserv?"safe":"all");
					String linePattern =  runName+"\t%s\n";
					try
					{
					ExtractControlSTGTask.injectiveLabelling = inj == 1;
					DesiJSettings desiJSettings = new DesiJSettings(DesiJOperation.REMOVE_DUMMIES, null, 0, null, null, true, true, false,
							safenessPreserv, false, false,
							false, 0, false, false);

					BalsaCircuit circuit = generator.build(depth);

					String fileName = "/home/dell/stgs/"+ runName;
					File gFile = new File(fileName+".g");
					File contractedGFile = new File(fileName+".contracted.g");

					final BalsaExportConfig balsaConfig = new BalsaExportConfig(null, 1==(o&1) ? CompositionMode.IMPROVED_PCOMP : CompositionMode.PCOMP, Protocol.FOUR_PHASE);
					final ExtractControlSTGTask stgExtractionTask = new ExtractControlSTGTask(f, circuit, balsaConfig);
					Export.exportToFile(new DotGExporter(), stgExtractionTask.getSTG(), gFile);

					PrintStream defaultOut = System.out;
					File desiJOutFile = new File("/home/dell/desij_" + runName.replace('\t', '_') + ".out");
					PrintStream desiJOut = new PrintStream(desiJOutFile);
					STGModel model = (STGModel)Import.importFromFile(Import.chooseBestImporter(f.getPluginManager(), gFile),gFile);

					long ts = System.currentTimeMillis();

					System.setOut(desiJOut);
					Result<? extends DesiJResult> result = f.getTaskManager().execute(new DesiJTask(model, f, desiJSettings), "desij");

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
						FileUtils.appendAllText(new File("/home/dell/beautiful_table.txt"), String.format(linePattern, matcher.group(1)));
						System.out.println();
					}
					}
					catch(Throwable th)
					{
						th.printStackTrace();
						FileUtils.appendAllText(new File("/home/dell/beautiful_table.txt"), String.format(linePattern, "fail:"+th.getMessage()));
					}
				}
	}
}
