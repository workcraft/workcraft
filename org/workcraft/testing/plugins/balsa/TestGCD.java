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
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.DocumentFormatException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.PluginInstantiationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.parsers.breeze.Netlist;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeConnection;
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
import org.workcraft.plugins.balsa.io.BalsaExportConfig;
import org.workcraft.plugins.balsa.io.BalsaToGatesExporter;
import org.workcraft.plugins.balsa.io.BalsaToStgExporter_FourPhase;
import org.workcraft.testing.plugins.balsa.TestGCD.ChunkSplitter.Result;
import org.workcraft.util.Export;
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

	Queue<Chunk> queue = new ArrayBlockingQueue<Chunk>(50000);

	//@Test
	public void SynthesizeAll() throws IOException, DocumentFormatException, PluginInstantiationException, InterruptedException
	{
		init();
		Collection<Chunk> allChunks = getAllChunks();
		Chunk [] chunks = allChunks.toArray(new Chunk[0]);

		for(Chunk ch : chunks)
		{
			if(!queue.add(ch))
				throw new RuntimeException("dermo");
		}

		System.out.println("Total count of all chunks: " + allChunks.size());

		launchThread();
		launchThread();
	}

	private void launchThread() {
		Thread th = new Thread(new Runnable()
		{
			@Override
			public void run() {
				while(true)
				{
					Chunk chunk = queue.poll();
					synthesize(chunk);
				}
			}
		});
		th.run();
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

	private String getId(BreezeComponent comp) {
		return circuit.getNodeReference(comp);
	}

	class BcComparator implements Comparator<BreezeComponent> {
		@Override
		public int compare(BreezeComponent comp1, BreezeComponent comp2) {
			return getId(comp1).compareTo(getId(comp2));
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
		exportPartial(chunk.components.toArray(new BreezeComponent[0]), new File(outDir, chunkName+".g"), getEqnFile(chunk));
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
	public void synthesiseSample() throws IOException, DocumentFormatException, PluginInstantiationException
	{
		init();

		synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{fetchA, muxA})));
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{fetchBmA, casE})));

		//synthesize(new Chunk(getAllComponents()));

		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{whilE})));
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{bfNotEquals})));
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{bfNotEquals, whilE})));
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{seq})));
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{whilE})));
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{bfNotEquals, whilE})));
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{fetchA, muxA})));
//		System.out.println("----- seq:  ");
//		synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{seq})));
//		System.out.println("----- concur:  ");
//		synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{concur})));
//		System.out.println("----- fetchA:  ");
//		synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{fetchA})));
//		System.out.println("----- fetchB:  ");
//		synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{fetchB})));
//		System.out.println("----- whilE:  ");
//		synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{whilE})));
//		System.out.println("----- seq+whilE:  ");
//		synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{seq, whilE})));
//		//System.out.println("----- concur+fetches:  ");
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{concur, fetchA, fetchB})));
		//synthesize(new Chunk(Arrays.asList(new BreezeComponent[]{seq, concur, fetchA, fetchB, whilE})));
	}

	//@Test
	public void FindBestSplit() throws IOException, ModelValidationException, SerialisationException, DocumentFormatException, PluginInstantiationException
	{
		init();

		printTable();

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
	}

	BreezeComponent seq;
	BreezeComponent bfGreater;
	BreezeComponent whilE;
	BreezeComponent casE;
	BreezeComponent concur;
	BreezeComponent fetchA;
	BreezeComponent fetchB;
	BreezeComponent fetchAmB;
	BreezeComponent fetchBmA;
	BreezeComponent fetchGT;
	BreezeComponent varB;
	BreezeComponent varA;
	BreezeComponent muxB;
	BreezeComponent muxA;
	BreezeComponent bfNotEquals;
	BreezeComponent bfAmB;
	BreezeComponent bfBmA;

	private void init() throws IOException, DocumentFormatException,
			PluginInstantiationException {
		Framework f = new Framework();
		f.getPluginManager().loadManifest();
		f.loadConfig("config/config.xml");

		circuit = new BalsaCircuit();

		seq = addComponent(new SequenceOptimised() { { setOutputCount(2); } });
		concur = addComponent(new Concur() { { setOutputCount(2); } });
		fetchA = addComponent(new Fetch() { { setWidth(8); } });
		fetchB = addComponent(new Fetch() { { setWidth(8); } });
		fetchAmB = addComponent(new Fetch() { { setWidth(8); } });
		fetchBmA = addComponent(new Fetch() { { setWidth(8); } });
		fetchGT = addComponent(new Fetch() { { setWidth(1); } });
		varA = addComponent(new Variable() { { setWidth(8); setName("A"); setReadPortCount(5); } });
		varB = addComponent(new Variable() { { setWidth(8); setName("B"); setReadPortCount(4); } });
		muxB = addComponent(new CallMux() { { setWidth(8); setInputCount(2); } });
		muxA = addComponent(new CallMux() { { setWidth(8); setInputCount(2); } });
		bfNotEquals = addComponent(new BinaryFunc() { { setInputAWidth(8); setInputBWidth(8); setOutputWidth(1); setOp(BinaryOperator.NOT_EQUALS); } });
		bfAmB = addComponent(new BinaryFunc() { { setInputAWidth(8); setInputBWidth(8); setOutputWidth(8); setOp(BinaryOperator.SUBTRACT); } });
		bfBmA = addComponent(new BinaryFunc() { { setInputAWidth(8); setInputBWidth(8); setOutputWidth(8); setOp(BinaryOperator.SUBTRACT); } });
		bfGreater = addComponent(new BinaryFunc() { { setInputAWidth(8); setInputBWidth(8); setOutputWidth(1); setOp(BinaryOperator.SUBTRACT); } }); // Should be GREATER_THAN. Used SUBTRACT to make it transfer data on the data-path.
		whilE = addComponent(new While());
		casE = addComponent(new Case() {{ setInputWidth(1); setOutputCount(2); setSpecification("хз"); }});

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

		connect(bfGreater, "out", fetchGT, "inp");
		connect(fetchGT, "out", casE, "inp");
		connect(casE, "activateOut0", fetchBmA, "activate");
		connect(casE, "activateOut1", fetchAmB, "activate");

		connect(bfAmB, "out", fetchAmB, "inp");
		connect(bfBmA, "out", fetchBmA, "inp");

		connect(fetchAmB, "out", muxA, "inp1");
		connect(fetchBmA, "out", muxB, "inp1");
	}

	Map<BreezeComponent, String> componentNames = new HashMap<BreezeComponent, String>();

	private void registerName(String name, BreezeComponent component) {
		componentNames.put(component, name);
	}

	static final File outDir = new File("../out");

	private Map<Chunk, Integer> readAllCosts() throws NumberFormatException, IOException {
		Map<Chunk, Integer> costs = new HashMap<Chunk, Integer>();
		Collection<Chunk> allChunks = getAllChunks();
		System.out.println("total chunks: " + allChunks.size());
		for(Chunk chunk : allChunks)
		{
			Integer petrifyCost = null;//readCost(getEqnFile(chunk), "# Estimated area = ");
			Integer mpsatCost = readCost(new File(outDir, "mpsat/" + getChunkName(chunk) + ".eqn"), "literals=");
			Integer cost = petrifyCost;
			if(cost == null)
				cost = mpsatCost;
			else
				if(mpsatCost != null && mpsatCost < cost)
					cost = mpsatCost;

			if(cost != null)
				costs.put(chunk, cost);
		}
		return costs;
	}

	private Integer readCost(File eqnFile, String literalsPrefix) throws NumberFormatException, IOException {
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

			while((line = reader.readLine()) != null)
				if(line.contains(literalsPrefix))
				{
					int index = line.indexOf(literalsPrefix) + literalsPrefix.length();
					int index2 = index;
					while(line.length()>index2 && Character.isDigit(line.charAt(index2)))
						index2++;
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
		return new File(outDir, getChunkName(chunk)+".eqn");
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

	private void exportPartial(final BreezeComponent[] components, File stgFile, File eqnFile)
	{
		BalsaToStgExporter_FourPhase exporter = new BalsaToStgExporter_FourPhase()
			{
				@Override protected Iterable<BreezeComponent> getComponentsToSave(Netlist<HandshakeComponent, BreezeComponent, BreezeConnection> balsa) {
					return Arrays.asList(components);
				}
			};

		try
		{
			Export.exportToFile(exporter, circuit, stgFile);

			try
			{
				BalsaToGatesExporter.synthesiseStg(stgFile, eqnFile, BalsaExportConfig.DEFAULT);
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
