package org.workcraft.testing.plugins.balsa;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.workcraft.dom.Node;
import org.workcraft.framework.exceptions.InvalidConnectionException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.SerialisationException;
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
import org.workcraft.plugins.serialisation.BalsaToStgExporter_FourPhase;

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
	public void Test() throws IOException, ModelValidationException, SerialisationException
	{
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

		File file = new File("gcd.g");
		if(file.exists())
			file.delete();
		FileOutputStream stream = new FileOutputStream(file);

		new BalsaToStgExporter_FourPhase().export(circuit, stream);

		stream.close();
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
