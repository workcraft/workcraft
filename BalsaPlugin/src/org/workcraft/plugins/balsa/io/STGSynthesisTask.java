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

package org.workcraft.plugins.balsa.io;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.io.BalsaExportConfig.CompositionMode;
import org.workcraft.plugins.balsa.io.BalsaExportConfig.Protocol;
import org.workcraft.plugins.gates.GateLevelModel;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.shared.MpsatCscResolutionResultHandler;
import org.workcraft.plugins.shared.MpsatEqnParser;
import org.workcraft.plugins.shared.MpsatMode;
import org.workcraft.plugins.shared.MpsatSettings;
import org.workcraft.plugins.shared.MpsatSettings.SolutionMode;
import org.workcraft.plugins.shared.PetrifyUtilitySettings;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.plugins.shared.tasks.MpsatChainResult;
import org.workcraft.plugins.shared.tasks.MpsatChainTask;
import org.workcraft.plugins.shared.tasks.MpsatTask;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.plugins.verification.tasks.ExternalProcessTask;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Export;
import org.workcraft.util.Import;

public class STGSynthesisTask implements Task<SynthesisResult> {
	private final Framework framework;

	public STGSynthesisTask(Framework framework)
	{
		this.framework = framework;
	}

	public void export(Model model, OutputStream out) throws IOException,
			ModelValidationException, SerialisationException {
		if(model instanceof STG)
			exportFromStg((STG)model, out);
		else
		{
			BalsaCircuit circuit = (BalsaCircuit)model;
			STGModel stg = exportOriginal(circuit);

			exportFromStg(stg, out);
		}
	}

	private void exportFromStg(STGModel model, OutputStream out) throws IOException, ModelValidationException, SerialisationException {
		GateLevelModel gates = synthesise(framework, model, getConfig());
		Export.chooseBestExporter(framework.getPluginManager(), gates, Format.EQN);
		export(gates, out);
	}

	private BalsaExportConfig getConfig() {
		throw new org.workcraft.exceptions.NotImplementedException();
	}

	public static GateLevelModel synthesise(Framework framework, STGModel stg, BalsaExportConfig config) throws IOException {

		switch(config.getSynthesisSettings().getDummyContractionMode())
		{
		case PETRIFY:
			stg = contractDummies(framework.getTaskManager(), stg);
			break;
		case DESIJ:
			stg = contractDummiesDesiJ(stg);
			break;
		case NONE:
			break;
		default:
			throw new RuntimeException("Unsupported contraction");
		}

		switch(config.getSynthesisSettings().getSynthesisTool())
		{
		case MPSAT:
		{
			STGModel cscResolved = resolveCscWithMpsat(framework, stg);

			return mpsatMakeEqn(framework, cscResolved);
		}
		case PETRIFY:
			return petrifyMakeEqn(framework.getTaskManager(), stg);
		default:
			throw new RuntimeException("Unsupported synthesis tool");
		}

	}

	private static STGModel resolveCscWithMpsat(Framework framework, STGModel original) {
		MpsatSettings settings = new MpsatSettings(MpsatMode.RESOLVE_ENCODING_CONFLICTS, 4, MpsatSettings.SOLVER_MINISAT, SolutionMode.MINIMUM_COST, 1, null);
		MpsatChainTask mpsatTask = new MpsatChainTask(original, settings, framework);
		final Result<? extends MpsatChainResult> result = framework.getTaskManager().execute(mpsatTask, "CSC conflict resolution");
		return new MpsatCscResolutionResultHandler(mpsatTask, result).getResolvedStg();
	}

	private static GateLevelModel petrifyMakeEqn(TaskManager taskManager, STGModel stg) throws IOException {
		if(true)throw new NotImplementedException();
		//TODO: re-implement this using asynchronous tasks
		ExternalProcessTask task = new ExternalProcessTask(Arrays.asList(
				new String[]{
						PetrifyUtilitySettings.getPetrifyCommand(),
						"-hide",
						".dummy",
						"-eqn",
						//synthesised.getAbsolutePath(),
						"-cg",
						//original.getAbsolutePath()
				}), new File("."));

		Result<? extends ExternalProcessResult> result = taskManager.execute(task, "PETRIFY synthesis");

		switch(result.getOutcome())
		{
		case CANCELLED:
			throw new RuntimeException("Operation cancelled");
		case FAILED:
			throw new RuntimeException(result.getCause());
		}


		ExternalProcessResult retVal = result.getReturnValue();

		System.out.println("Petrify complex gate synthesis output: ");
		System.out.write(retVal.getOutput());System.out.println();System.out.println("----------------------------------------");

		System.out.println("Petrify complex gate synthesis errors: ");
		System.out.write(retVal.getErrors());System.out.println();System.out.println("----------------------------------------");

		if(retVal.getReturnCode() != 0)
			throw new RuntimeException("PETRIFY failed: " + new String(retVal.getErrors()));
		return null;
	}


	private static STGModel contractDummiesDesiJ(STGModel stg) throws IOException
	{
		throw new RuntimeException("Not implemented");
	}

	private static STGModel contractDummies(TaskManager taskManager, STGModel stg) throws IOException
	{
		if(true)throw new NotImplementedException();
		// TODO: re-implement this using asynchronous tasks
		Result<? extends ExternalProcessResult> result = taskManager.execute(
		new ExternalProcessTask(
				Arrays.asList(
				new String[]{
						PetrifyUtilitySettings.getPetrifyCommand(),
						"-hide",
						".dummy",
						//original.getAbsolutePath()
				}), new File(".")),
				"PETRIFY dummy contraction");

		if(result.getOutcome() == Outcome.CANCELLED)
			throw new RuntimeException("Cancelled");

		if(result.getOutcome() == Outcome.FAILED)
			throw new RuntimeException(result.getCause());

		FileOutputStream outStream = null;//new FileOutputStream(contracted);
		ExternalProcessResult retVal = result.getReturnValue();
		outStream.write(retVal.getOutput());
		outStream.close();

		System.out.println("Petrify Dummy contraction errors: ");
		System.out.write(retVal.getErrors());System.out.println();System.out.println("----------------------------------------");
		if(retVal.getReturnCode() != 0)
			throw new RuntimeException("Dummy contraction failed! " + retVal.getErrors().toString());
		return null;
	}

	private static GateLevelModel mpsatMakeEqn(Framework framework, STGModel stg) throws IOException
	{
		if(true)throw new NotImplementedException();
		//TODO: re-implement using MpsatChainTask
		MpsatSettings settings = new MpsatSettings(MpsatMode.COMPLEX_GATE_IMPLEMENTATION, 0, MpsatSettings.SOLVER_MINISAT, SolutionMode.FIRST, 1, null);

		new MpsatChainTask(stg, settings, framework);
		Result<? extends ExternalProcessResult> result = framework.getTaskManager().execute(new MpsatTask(settings.getMpsatArguments(), null), "MPSat Complex gate synthesis");

		System.out.println("MPSat complex gate synthesis output: ");
		System.out.write(result.getReturnValue().getOutput());System.out.println();System.out.println("----------------------------------------");
		byte[] errors = result.getReturnValue().getErrors();

		if(errors.length != 0)
		{
			System.out.println("MPSAT complex gate synthesis error stream: ");
			System.out.write(errors);System.out.println();System.out.println("----------------------------------------");
		}

		switch(result.getOutcome())
		{
		case CANCELLED:
			throw new RuntimeException("Complex gate synthesis cancelled by user.");
		case FAILED:
			throw new RuntimeException("Complex gate synthesis by MPSat failed: " + new String(errors), result.getCause());
		case FINISHED:
			return new MpsatEqnParser().parse(new String(result.getReturnValue().getOutput()));
		default:
			throw new RuntimeException("Unsupported outcome");
		}
	}

	private STGModel exportOriginal(BalsaCircuit balsaModel)
			throws FileNotFoundException, IOException,
			ModelValidationException, SerialisationException {

		final BalsaExportConfig balsaConfig = getConfig();
		final ExtractControlSTGTask stgExtractionTask = new ExtractControlSTGTask(framework, balsaModel, balsaConfig);
		return stgExtractionTask.getSTG();
	}

	public String getExtenstion() {
		return ".eqn";
	}

	public int getCompatibility(Model model) {
		if (model instanceof BalsaCircuit || model instanceof STG)
			return Exporter.BEST_COMPATIBILITY;
		else
			return Exporter.NOT_COMPATIBLE;
	}

	@Override
	public Result<? extends SynthesisResult> run(ProgressMonitor<? super SynthesisResult> monitor) {
		throw new org.workcraft.exceptions.NotImplementedException();
	}
}
