package org.workcraft.testing.plugins.balsa;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

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

	private void synthesizeAllPossibilities()
	{
		Collection<BreezeComponent> allComponents = Hierarchy.getDescendantsOfType(circuit.getRoot(), BreezeComponent.class);

		Chunk ch = new Chunk(new ArrayList<BreezeComponent>());

		Collection<Chunk> chunks = new ArrayList<Chunk>();
		chunks.add(ch);

		while(chunks.size()>0)
		{
			System.out.println("Next size: " + chunks.size() + " different chunks");
			synthesiseAll(chunks);
			chunks = getChunks(allComponents, chunks);
		}
	}

	private void synthesiseAll(Iterable<Chunk> chunks) {
		for(Chunk chunk : chunks)
			synthesize(chunk);
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

		String result = "_";
		for(BreezeComponent comp : list)
			result+="c"+getId(comp)+"_";
		return result;
	}

	private void synthesize(Chunk chunk) {
		String chunkName = getChunkName(chunk);
		System.out.println("synthesising " + chunkName);
		exportPartial(chunk.components.toArray(new BreezeComponent[0]), chunkName+".g", chunkName+".eqn");
	}

	@Test
	public void Test() throws IOException, ModelValidationException, SerialisationException, DocumentFormatException, PluginInstantiationException
	{
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

		System.out.println("seq - " + getId(seq));
		System.out.println("concur - " + getId(concur));
		System.out.println("fetchA - " + getId(fetchA));
		System.out.println("fetchB - " + getId(fetchB));
		System.out.println("fetchAmB - " + getId(fetchAmB));
		System.out.println("fetchBmA - " + getId(fetchBmA));
		System.out.println("fetchGT - " + getId(fetchGT));
		System.out.println("varA - " + getId(varA));
		System.out.println("varB - " + getId(varB));
		System.out.println("muxB - " + getId(muxB));
		System.out.println("muxA - " + getId(muxA));
		System.out.println("bfNotEquals - " + getId(bfNotEquals));
		System.out.println("bfAmB - " + getId(bfAmB));
		System.out.println("bfBmA - " + getId(bfBmA));
		System.out.println("bfGreater - " + getId(bfGreater));
		System.out.println("whilE - " + getId(whilE));
		System.out.println("casE - " + getId(casE));

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

		synthesizeAllPossibilities();

		/*
		File file = new File("gcd.g");
		if(file.exists())
			file.delete();
		FileOutputStream stream = new FileOutputStream(file);

		new BalsaToStgExporter_FourPhase().export(circuit, stream);

		exportPartial(new BreezeComponent[]{seq, concur, fetchA, fetchB}, "gcd_partial.g");

		stream.close();*/
	}

	private void exportPartial(final BreezeComponent[] components, String stgPath, String eqnPath){

		BalsaToStgExporter_FourPhase exporter = new BalsaToStgExporter_FourPhase()
		{
		@Override
			protected Iterable<BreezeComponent> getComponentsToSave(
					BalsaCircuit balsa) {
				return Arrays.asList(components);
			}
		};
		try {
			Export.exportToFile(exporter, circuit, stgPath);

			try
			{

				BalsaToGatesExporter.synthesiseStg(new File(stgPath), new File(eqnPath));
			}
			catch(RuntimeException e)
			{
				FileWriter writer  = new FileWriter(eqnPath+".err");
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
