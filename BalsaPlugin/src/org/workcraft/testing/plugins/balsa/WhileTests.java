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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.workcraft.Framework;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.exceptions.LayoutFailedException;
import org.workcraft.exceptions.ModelCheckingFailedException;
import org.workcraft.exceptions.ModelSaveFailedException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.parsers.breeze.BreezeLibrary;
import org.workcraft.parsers.breeze.EmptyParameterScope;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.BreezeComponent;
import org.workcraft.plugins.balsa.BreezeHandshake;
import org.workcraft.plugins.balsa.components.DynamicComponent;
import org.workcraft.plugins.balsa.handshakebuilder.Handshake;
import org.workcraft.plugins.balsa.handshakes.MainHandshakeMaker;
import org.workcraft.plugins.balsa.handshakestgbuilder.TwoSideStg;
import org.workcraft.plugins.balsa.io.BalsaExportConfig;
import org.workcraft.plugins.balsa.io.BalsaExportConfig.CompositionMode;
import org.workcraft.plugins.balsa.io.BalsaExportConfig.Protocol;
import org.workcraft.plugins.balsa.io.BalsaSystem;
import org.workcraft.plugins.balsa.io.ExtractControlSTGTask;
import org.workcraft.plugins.balsa.protocols.FourPhaseProtocol_NoDataPath;
import org.workcraft.plugins.balsa.stg.MainStgBuilder;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.NameProvider;
import org.workcraft.plugins.balsa.stgmodelstgbuilder.StgModelStgBuilder;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModelDescriptor;
import org.workcraft.plugins.stg.VisualSTG;
import org.workcraft.util.Export;
import org.workcraft.util.Import;
import org.workcraft.workspace.ModelEntry;


public class WhileTests {

	@Test
	public void Test1() throws ModelSaveFailedException, VisualModelInstantiationException, ModelCheckingFailedException, ModelValidationException, SerialisationException, IOException
	{
		final DynamicComponent wh = null;//new While();

		final STG stg = new STG();

		final Map<String, Handshake> handshakes = MainHandshakeMaker.getHandshakes(wh);

		StgModelStgBuilder stgBuilder = new StgModelStgBuilder(stg, new NameProvider<Handshake>()
		{
			HashMap<Handshake, String> names;

			{
				names = new HashMap<Handshake, String>();
				for(Entry<String, Handshake> entry : handshakes.entrySet())
				{
					names.put(entry.getValue(), entry.getKey());
				}
			}

			public String getName(Handshake handshake) {
				return names.get(handshake);
			}
		});
		FourPhaseProtocol_NoDataPath handshakeBuilder = new FourPhaseProtocol_NoDataPath();

		Map<String, TwoSideStg> hsStgs = MainStgBuilder.buildHandshakes(handshakes, handshakeBuilder, stgBuilder);
		MainStgBuilder.buildStg(wh, hsStgs, stgBuilder);

		//new DeadlockChecker().run(stg);

		new org.workcraft.Framework().save(new ModelEntry(new STGModelDescriptor(),  new VisualSTG(stg)), "while.stg.work");
	}

	@Test
	public void TestCombine() throws ModelSaveFailedException, VisualModelInstantiationException, InvalidConnectionException, ModelCheckingFailedException, IOException, LayoutFailedException, ModelValidationException, SerialisationException, DeserialisationException
	{
		BalsaCircuit balsa = new BalsaCircuit();

		BreezeComponent while1 = new BreezeComponent();
		while1.setUnderlyingComponent(createWhile());
		BreezeComponent while2 = new BreezeComponent();
		while2.setUnderlyingComponent(createWhile());

		balsa.add(while1);
		balsa.add(while2);

		BreezeHandshake wh1Out = while1.getHandshakeComponents().get(while1.getHandshakes().get("activateOut"));
		BreezeHandshake wh2In = while2.getHandshakeComponents().get(while2.getHandshakes().get("activate"));

		balsa.connect(wh1Out, wh2In);

		File stgFile = new File("while_while.g");

		Framework framework = new Framework();
		framework.initPlugins();

		final BalsaExportConfig balsaConfig = new BalsaExportConfig(null, CompositionMode.IMPROVED_PCOMP, Protocol.FOUR_PHASE);
		final ExtractControlSTGTask stgExtractionTask = new ExtractControlSTGTask(framework, balsa, balsaConfig);
		Export.exportToFile(new DotGExporter(), stgExtractionTask.getSTG(), stgFile);

		final STG stg = (STG) Import.importFromFile(new DotGImporter(), stgFile).getModel();

		VisualSTG visualStg = new VisualSTG(stg);

		framework.save(new ModelEntry(new STGModelDescriptor(), visualStg), "while_while.stg.work");
	}

	private DynamicComponent createWhile() {
		try {
			return new DynamicComponent(new BreezeLibrary(BalsaSystem.DEFAULT()).getPrimitive("While"), EmptyParameterScope.instance());
		} catch (IOException e) {
			throw new java.lang.RuntimeException(e);
		}
	}
}
