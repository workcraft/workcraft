package org.workcraft.testing.plugins.balsa.components;

import org.junit.Test;
import org.workcraft.framework.ModelSaveFailedException;
import org.workcraft.framework.exceptions.VisualModelInstantiationException;
import org.workcraft.plugins.balsa.components.FourPhaseProtocol;
import org.workcraft.plugins.balsa.components.While;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.VisualSTG;


public class WhileTests {

	@Test
	public void Test1() throws ModelSaveFailedException, VisualModelInstantiationException
	{
		STG stg = new STG();
		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg);
		FourPhaseProtocol handshakeBuilder = new FourPhaseProtocol(stgBuilder);
		new While().buildStg(handshakeBuilder);
		new org.workcraft.framework.Framework().save(new VisualSTG(stg), "while.stg.work");
	}
}
