package org.workcraft.testing.plugins.balsa;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.framework.Framework;
import org.workcraft.framework.exceptions.DocumentFormatException;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.PluginInstantiationException;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.util.Export;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.HandshakeComponent;
import org.workcraft.plugins.balsa.components.BinaryFunc;
import org.workcraft.plugins.balsa.components.BinaryOperator;
import org.workcraft.plugins.balsa.components.CallMux;
import org.workcraft.plugins.balsa.components.Case;
import org.workcraft.plugins.balsa.components.Component;
import org.workcraft.plugins.balsa.components.Concur;
import org.workcraft.plugins.balsa.components.Fetch;
import org.workcraft.plugins.balsa.components.SequenceOptimised;
import org.workcraft.plugins.balsa.components.Variable;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.serialisation.BalsaToGatesExporter;
import org.workcraft.plugins.serialisation.BalsaToStgExporter_FourPhase;
import org.workcraft.testing.plugins.balsa.TestGCD.ChunkSplitter.Result;
import org.workcraft.util.Hierarchy;

public class TestGCD {
	BalsaCircuit circuit;

	private BreezeComponent addComponent(Component component)
	{
		BreezeComponent comp = new BreezeComponent();
		comp.setUnderlyingComponent(component);
		circuit.add(comp);
		return comp;
	}

	@Test
	public void SynthesizeAll() throws IOException, DocumentFormatException, PluginInstantiationException
	{
		init();
		for(Chunk chunk : getAllChunks())
			synthesize(chunk);
	}

	private Collection<Chunk> getAllChunks() {
		Collection<BreezeComponent> allComponents = getAllComponents();

		Collection<Chunk> allChunks = new ArrayList<Chunk>();

		Chunk ch = new Chunk(new ArrayList<BreezeComponent>());
		Collection<Chunk> chunks = new ArrayList<Chunk>();
		chunks.add(ch);

		while(chunks.size()>0)
		{
			chunks = getChunks(allComponents, chunks);
			allChunks.addAll(chunks);
		}
		return allChunks;
	}

	private Collection<BreezeComponent> getAllComponents() {
		return Hierarchy.getDescendantsOfType(circuit.getRoot(), BreezeComponent.class);
	}

	static class Chunk
	{
		public Chunk(Collection<BreezeComponent> components)
		{
			this.components = new HashSet<BreezeComponent>();
			this.components.addAll(components);
		}
		private Set<BreezeComponent> components;
		public Set<BreezeComponent> getComponents()
		{
			return components;
		}

		@Override
		public boolean equals(Object obj) {
			if(obj == this)
				return true;
			if(!(obj instanceof Chunk))
				return false;
			Chunk ch =(Chunk)obj;

			if(this.components.size() != ch.components.size())
				return false;
			HashSet<BreezeComponent> comparingSet = new HashSet<BreezeComponent>(components);
			comparingSet.removeAll(ch.components);

			return comparingSet.size() == 0;
		};

		@Override
		public int hashCode() {
			Object [] comps = components.toArray();

			int [] hcs = new int[components.size()];
			for(int i=0;i<comps.length;i++)
				hcs[i] = comps[i].hashCode();

			Arrays.sort(hcs);
			return Arrays.hashCode(hcs);
		}
		public Chunk addComponent(BreezeComponent component)
		{
			HashSet<BreezeComponent> newSet = new HashSet<BreezeComponent>(components);
			newSet.add(component);
			return new Chunk(newSet);
		}

		public boolean isSubSetOf(Chunk superChunk) {
			HashSet<BreezeComponent> our = new HashSet<BreezeComponent>(components);
			our.removeAll(superChunk.components);
			return our.size() == 0;
		}

		public static Chunk subtract(Chunk toSplit, Chunk chunk) {
			HashSet<BreezeComponent> result = new HashSet<BreezeComponent>(toSplit.components);
			result.removeAll(chunk.components);
			return new Chunk(result);
		}
	}

	private Collection<Chunk> getChunks(Iterable<BreezeComponent> allComponents, Iterable<Chunk> smallerChunks)
	{
		HashSet<Chunk> result = new HashSet<Chunk>();

		for(BreezeComponent component : allComponents)
			for(Chunk chunk : smallerChunks)
				if(isConnected(chunk, component))
					result.add(chunk.addComponent(component));

		return result;
	}

	private boolean isConnected(Chunk chunk, BreezeComponent component) {
		return chunk.components.size() == 0 || !contains(chunk, component) && containsAny(chunk, getConnected(component));
	}

	private boolean containsAny(Chunk chunk, Iterable<BreezeComponent> connected) {
		for(BreezeComponent c : connected)
			if(contains(chunk, c))
				return true;
		return false;
	}

	private boolean contains(Chunk chunk, BreezeComponent c) {
		return chunk.components.contains(c);
	}

	private Iterable<BreezeComponent> getConnected(BreezeComponent comp) {
		HashSet<BreezeComponent> result = new HashSet<BreezeComponent>();

		for(HandshakeComponent hs : comp.getHandshakeComponents().values()) {
			HandshakeComponent otherHs = circuit.getConnectedHandshake(hs);
			if(otherHs != null)
				result.add(otherHs.getOwner());
		}

		return result;
	}

	private int getId(BreezeComponent comp) {
		return circuit.getNodeID(comp);
	}

	class BcComparator implements Comparator<BreezeComponent> {
		@Override
		public int compare(BreezeComponent comp1, BreezeComponent comp2) {
			return getId(comp1) - getId(comp2);
		}

	}

	private String getChunkName(Chunk chunk)
	{
		ArrayList<BreezeComponent> list = new ArrayList<BreezeComponent>(chunk.getComponents());

		Collections.sort(list, new BcComparator());

		String result = null;
		for(BreezeComponent comp : list)
		{
			String name = componentNames.get(comp);
			if(result == null)
				result = name;
			else
				result+="-"+name;
		}
		return result;
	}

	private void synthesize(Chunk chunk) {
		String chunkName = getChunkName(chunk);
		System.out.println("synthesising " + chunkName);
		exportPartial(chunk.components.toArray(new BreezeComponent[0]), new File("../out/" + chunkName+".g"), getEqnFile(chunk));
	}


	public void printTable() throws NumberFormatException, IOException, DocumentFormatException, PluginInstantiationException
	{
		System.out.println("");
		System.out.println("Separate components: ");
		int totalCost = 0;
		for(Entry<Chunk, Integer> entry : readAllCosts().entrySet())
		{
			Chunk chunk = entry.getKey();
			if(chunk.getComponents().size() == 1) {
				Integer cost = entry.getValue();
				System.out.println(getChunkName(chunk) + "\t" + cost);
				totalCost += cost;
			}
		}
		System.out.println("Total cost of separate components: " + totalCost);
	}

	@Test
	public void FindBestSplit() throws IOException, ModelValidationException, SerialisationException, DocumentFormatException, PluginInstantiationException
	{
		init();

		Map<Chunk, Integer> costs = readAllCosts();
		ChunkSplitter splitter = new ChunkSplitter(costs);
		Chunk fullChunk = new Chunk(getAllComponents());
		Result bestSplit = splitter.getBestSplit(fullChunk);
		System.out.println("");
		System.out.println("Cost of best split is: " + bestSplit.cost);
		System.out.println("");
		System.out.println("The best split is: ");
		for(Chunk ch : bestSplit.chunks)
			System.out.println(getChunkName(ch) + "\t"+costs.get(ch));

		System.out.println("");

		printTable();

		/*
		File file = new File("gcd.g");
		if(file.exists())
			file.delete();
		FileOutputStream stream = new FileOutputStream(file);

		new BalsaToStgExporter_FourPhase().export(circuit, stream);

		exportPartial(new BreezeComponent[]{seq, concur, fetchA, fetchB}, "gcd_partial.g");

		stream.close();*/
	}

	private void init() throws IOException, DocumentFormatException,
			PluginInstantiationException {
		Framework f = new Framework();
		f.getPluginManager().loadManifest();
		f.loadConfig("config/config.xml");

		circuit = new BalsaCircuit();

		BreezeComponent seq = addComponent(new SequenceOptimised() { { setOutputCount(2); } });
		BreezeComponent concur = addComponent(new Concur() { { setOutputCount(2); } });
		BreezeComponent fetchA = addComponent(new Fetch() { { setWidth(8); } });
		BreezeComponent fetchB = addComponent(new Fetch() { { setWidth(8); } });
		BreezeComponent fetchAmB = addComponent(new Fetch() { { setWidth(8); } });
		BreezeComponent fetchBmA = addComponent(new Fetch() { { setWidth(8); } });
		BreezeComponent fetchGT = addComponent(new Fetch() { { setWidth(1); } });
		BreezeComponent varA = addComponent(new Variable() { { setWidth(8); setName("A"); setReadPortCount(5); } });
		BreezeComponent varB = addComponent(new Variable() { { setWidth(8); setName("B"); setReadPortCount(4); } });
		BreezeComponent muxB = addComponent(new CallMux() { { setWidth(8); setInputCount(2); } });
		BreezeComponent muxA = addComponent(new CallMux() { { setWidth(8); setInputCount(2); } });
		BreezeComponent bfNotEquals = addComponent(new BinaryFunc() { { setInputAWidth(8); setInputBWidth(8); setOutputWidth(1); setOp(BinaryOperator.NOT_EQUALS); } });
		BreezeComponent bfAmB = addComponent(new BinaryFunc() { { setInputAWidth(8); setInputBWidth(8); setOutputWidth(8); setOp(BinaryOperator.SUBTRACT); } });
		BreezeComponent bfBmA = addComponent(new BinaryFunc() { { setInputAWidth(8); setInputBWidth(8); setOutputWidth(8); setOp(BinaryOperator.SUBTRACT); } });
		BreezeComponent bfGreater = addComponent(new BinaryFunc() { { setInputAWidth(8); setInputBWidth(8); setOutputWidth(1); setOp(BinaryOperator.GREATER_THAN); } });
		BreezeComponent whilE = addComponent(new While());
		BreezeComponent casE = addComponent(new Case() {{ setInputWidth(1); setOutputCount(2); setSpecification("хз"); }});

		registerName("seq", seq);
		registerName("concur", concur);
		registerName("fetchA", fetchA);
		registerName("fetchB", fetchB);
		registerName("fetchAmB", fetchAmB);
		registerName("fetchBmA", fetchBmA);
		registerName("fetchGT", fetchGT);
		registerName("varA", varA);
		registerName("varB", varB);
		registerName("muxB", muxB);
		registerName("muxA", muxA);
		registerName("bfNotEquals", bfNotEquals);
		registerName("bfAmB", bfAmB);
		registerName("bfBmA", bfBmA);
		registerName("bfGreater", bfGreater);
		registerName("whilE", whilE);
		registerName("casE", casE);

		connect(seq, "activateOut0", concur, "activate");
		connect(seq, "activateOut1", whilE, "activate");
		connect(concur, "activateOut0", fetchA, "activate");
		connect(concur, "activateOut1", fetchB, "activate");
		connect(fetchA, "out", muxA, "inp0");
		connect(fetchB, "out", muxB, "inp0");
		connect(muxA, "out", varA, "write");
		connect(muxB, "out", varB, "write");

		connect(bfNotEquals, "inpA", varA, "read0");
		connect(bfNotEquals, "inpB", varB, "read0");
		connect(bfAmB, "inpA", varA, "read1");
		connect(bfAmB, "inpB", varB, "read1");
		connect(bfBmA, "inpA", varB, "read2");
		connect(bfBmA, "inpB", varA, "read2");
		connect(bfGreater, "inpA", varA, "read3");
		connect(bfGreater, "inpB", varB, "read3");

		connect(whilE, "guard", bfNotEquals, "out");
		connect(whilE, "activateOut", fetchGT, "activate");

		connect(fetchGT, "out", casE, "inp");
		connect(casE, "activateOut0", fetchBmA, "activate");
		connect(casE, "activateOut1", fetchAmB, "activate");

		connect(fetchAmB, "out", muxA, "inp1");
		connect(fetchBmA, "out", muxB, "inp1");
	}

	Map<BreezeComponent, String> componentNames = new HashMap<BreezeComponent, String>();

	private void registerName(String name, BreezeComponent component) {
		componentNames.put(component, name);
	}

	private Map<Chunk, Integer> readAllCosts() throws NumberFormatException, IOException {
		Map<Chunk, Integer> costs = new HashMap<Chunk, Integer>();
		Collection<Chunk> allChunks = getAllChunks();
		System.out.println("total chunks: " + allChunks.size());
		for(Chunk chunk : allChunks)
		{
			Integer cost = readCost(chunk);
			if(cost != null)
				costs.put(chunk, cost);
		}
		return costs;
	}

	private Integer readCost(Chunk chunk) throws NumberFormatException, IOException {
		File eqnFile = getEqnFile(chunk);
		BufferedReader reader;
		try
		{
			reader = new BufferedReader(new FileReader(eqnFile));
		}
		catch(FileNotFoundException ex)
		{
			return null;
		}
		try
		{
			String line;
			String literalsPrefix = "literals=";
			while((line = reader.readLine()) != null)
				if(line.contains(literalsPrefix))
				{
					int index = line.indexOf(literalsPrefix) + literalsPrefix.length();
					int index2 = line.indexOf(")", index);
					String literalsString = line.substring(index, index2);
					return new Integer(literalsString);
				}
		}
		finally
		{
			reader.close();
		}
		return null;
	}

	private File getEqnFile(Chunk chunk) {
		return new File("../out/" + getChunkName(chunk)+".eqn");
	}

	static int cc = 0;

	class ChunkSplitter
	{
		public ChunkSplitter(Map<Chunk, Integer> availableFullCosts)
		{
			optimal = new HashMap<Chunk, Result>();

			for(Chunk chunk : availableFullCosts.keySet())
				optimal.put(chunk, new Result(availableFullCosts.get(chunk), Arrays.asList(new Chunk[]{chunk})));

			boolean needChechOptimality = true;

			full = optimal;

			while(needChechOptimality)
			{
				needChechOptimality = false;
				for(Chunk chunk : availableFullCosts.keySet())
				{
					if(chunk.getComponents().size() > 1)
						if(findBestSplit(chunk).betterThan(getBestSplit(chunk)))
						{
							System.out.println("Everything is stupid! " + getChunkName(chunk) + " full has more literals than its parts.");
							optimal.remove(chunk);
							needChechOptimality = true;
						}
				}
			}

			full = new HashMap<Chunk, Result>(optimal);
		}


		Map<Chunk, Result> optimal;
		Map<Chunk, Result> full;

		public Result getBestSplit(Chunk toSplit) {
			Result result = optimal.get(toSplit);
			if(result == null)
			{
				result = findBestSplit(toSplit);
				optimal.put(toSplit, result);
			}
			return result;
		}

		class Result
		{
			public Result(int cost, List<Chunk> chunks)
			{
				if(chunks == null)
					throw new RuntimeException("Null o_O");
				this.cost = cost;
				this.chunks = chunks;
			}
			public final int cost;
			public final List<Chunk> chunks;

			public final boolean betterThan(Result other)
			{
				return other == null || other.cost > cost;
			}
		}


		private Result findBestSplit(Chunk toSplit) {
			Result best = null;

			for(Chunk chunk : new ArrayList<Chunk>(full.keySet()))
				if(chunk.isSubSetOf(toSplit))
				{
					Chunk remainder = Chunk.subtract(toSplit, chunk);
					if(remainder.getComponents().size() > 0)
					{
						List<Chunk> remainderParts = getConnectedParts(remainder);
						Result preResult = optimal.get(chunk);

						int cost = preResult.cost;
						List<Chunk> parts = new ArrayList<Chunk>(preResult.chunks);

						for(Chunk ch : remainderParts)
						{
							Result remRes = getBestSplit(ch);
							cost += remRes.cost;
							parts.addAll(remRes.chunks);
						}

						Result result = new Result(cost, parts);
						if(result.betterThan(best))
							best = result;
					}
				}

			if(best == null)
				throw new RuntimeException("can't find any split for " + getChunkName(toSplit));

			cc++;
			System.out.print("+");
			if(cc%100 == 0)
				System.out.println( " (size: " + optimal.size() +")" );

			return best;
		}

		private List<Chunk> getConnectedParts(Chunk remainder) {
			List<Chunk> result = new ArrayList<Chunk>();
			while(remainder.getComponents().size() != 0)
			{
				Chunk connectedChunk = getConnectedChunk(remainder);
				remainder = Chunk.subtract(remainder, connectedChunk);
				result.add(connectedChunk);
			}
			return result;
		}

		private Chunk getConnectedChunk(Chunk remainder) {
			HashSet<BreezeComponent> result = new HashSet<BreezeComponent>();
			HashSet<BreezeComponent> available = new HashSet<BreezeComponent>(remainder.getComponents());

			Queue<BreezeComponent> q = new ArrayDeque<BreezeComponent>();

			BreezeComponent first = remainder.getComponents().iterator().next();
			available.remove(first);
			q.add(first);

			while(!q.isEmpty())
			{
				BreezeComponent comp = q.poll();

				result.add(comp);

				for(BreezeComponent connected : getConnected(comp))
					if(available.contains(connected))
					{
						q.add(connected);
						available.remove(connected);
					}
			}

			return new Chunk(result);
		}
	}

	private void exportPartial(final BreezeComponent[] components, File stgFile, File eqnFile){

		BalsaToStgExporter_FourPhase exporter = new BalsaToStgExporter_FourPhase()
		{
		@Override
			protected Iterable<BreezeComponent> getComponentsToSave(
					BalsaCircuit balsa) {
				return Arrays.asList(components);
			}
		};
		try {
			Export.exportToFile(exporter, circuit, stgFile);

			try
			{

				BalsaToGatesExporter.synthesiseStg(stgFile, eqnFile);
			}
			catch(RuntimeException e)
			{
				FileWriter writer  = new FileWriter(eqnFile.getAbsolutePath()+".err");
				if (e.getMessage() != null)
					writer.write(e.getMessage());
				e.printStackTrace(new PrintWriter(writer));
				writer.close();
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void connect(BreezeComponent comp1, String hc1,
			BreezeComponent comp2, String hc2) {
		try {
			circuit.connect(getHc(comp1, hc1), getHc(comp2, hc2));
		} catch (InvalidConnectionException e) {
			throw new RuntimeException(e);
		}
	}

	private Node getHc(BreezeComponent comp, String hc) {
		HandshakeComponent hcc = comp.getHandshakeComponentByName(hc);
		assertTrue("Handshake "+ hc +" not found in component " + comp.getUnderlyingComponent().getClass().toString(), hcc != null);

		return hcc;
	}
}
