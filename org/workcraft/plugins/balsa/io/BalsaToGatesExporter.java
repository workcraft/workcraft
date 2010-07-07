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
import org.workcraft.FrameworkConsumer;
import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.balsa.io.BalsaExportConfig.DummyContractionMode;
import org.workcraft.plugins.interop.CSCResolver;
import org.workcraft.plugins.interop.DotGExporter;
import org.workcraft.plugins.interop.DotGImporter;
import org.workcraft.plugins.stg.STG;
import org.workcraft.plugins.verification.MpsatMode;
import org.workcraft.plugins.verification.MpsatSettings;
import org.workcraft.plugins.verification.MpsatSettings.SolutionMode;
import org.workcraft.plugins.verification.PetriNetToolsSettings;
import org.workcraft.plugins.verification.tasks.ExternalProcessResult;
import org.workcraft.plugins.verification.tasks.ExternalProcessTask;
import org.workcraft.plugins.verification.tasks.MpsatTask;
import org.workcraft.plugins.verification.tasks.PunfTask;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Import;
public abstract class BalsaToGatesExporter implements Exporter, FrameworkConsumer {

	private TaskManager taskManager;

	@Override
	public void acceptFramework(Framework framework) {
		this.taskManager = framework.getTaskManager();
	}

	@Override
	public void export(Model model, OutputStream out) throws IOException,
			ModelValidationException, SerialisationException {
		if(model instanceof STG)
			exportFromStg((STG)model, out);
		else
		{
			File original = File.createTempFile("composition", ".g");
			exportOriginal(model, original);

			File synthesised = File.createTempFile("result", ".eqn");

			synthesiseStg(taskManager, original, synthesised, getConfig());

			FileUtils.copyFileToStream(synthesised, out);
		}
	}

	abstract protected BalsaExportConfig getConfig();

	private void exportFromStg(STG model, OutputStream out) throws IOException, ModelValidationException, SerialisationException {
		File dotG = File.createTempFile("original", ".g");
		File eqn = File.createTempFile("result", ".eqn");
		Export.exportToFile(new DotGExporter(), model, dotG);

		synthesiseStg(taskManager, dotG, eqn, getConfig());

		FileUtils.copyFileToStream(eqn, out);
	}

	public static void synthesiseStg(TaskManager taskManager, File original, File synthesised, BalsaExportConfig config)
			throws IOException {

		File implicitRemoved = File.createTempFile("remImplicit", ".g");

		removeImplicitPlaces(original, implicitRemoved);

		FileUtils.copyFile(implicitRemoved, new File(original.getAbsolutePath()+".explicit.g"));//DEBUG

		File afterContraction;
		File contracted = null;

		if(config.dummyContractionMode() == DummyContractionMode.NONE)
			afterContraction = implicitRemoved;
		else
		{
			afterContraction = contracted = File.createTempFile("contracted", ".g");
			switch(config.dummyContractionMode())
			{
			case PETRIFY:
				contractDummies(taskManager, implicitRemoved, contracted);
			case DESIJ:
				contractDummiesDesiJ(implicitRemoved, contracted);
			default:
				throw new RuntimeException("Unsupported contraction");

			}
		}

		synthesise(taskManager, afterContraction, synthesised, config);
	}

	private static void removeImplicitPlaces(File original, File renamed2)
			throws IOException {
		try {
			Model stg = Import.importFromFile(new DotGImporter(), original);
			Export.exportToFile(new DotGExporter(), stg, renamed2);
		} catch (ModelValidationException e) {
			throw new RuntimeException(e);
		} catch (SerialisationException e) {
			throw new RuntimeException(e);
		} catch (DeserialisationException e) {
			throw new RuntimeException(e);
		}
	}

	private static void synthesise(TaskManager taskManager, File original, File synthesised, BalsaExportConfig config) throws IOException {
		switch(config.synthesisTool())
		{
		case MPSAT:
		{
			File tempDir = FileUtils.createTempDirectory("stgSynthesis");

			File unfolding = new File(tempDir, "composition.mci");

			makeUnfolding(taskManager, original, unfolding);

			File csc_resolved_mci = new File(tempDir, "resolved.mci");

			CSCResolver.resolveConflicts(taskManager, unfolding, csc_resolved_mci, null);

			mpsatMakeEqn(taskManager, csc_resolved_mci, synthesised);
			break;
		}
		case PETRIFY:
			petrifyMakeEqn(taskManager, original, synthesised);
			break;
			default:
		}
	}

	private static void petrifyMakeEqn(TaskManager taskManager, File original, File synthesised) throws IOException {



		ExternalProcessTask task = new ExternalProcessTask(Arrays.asList(
				new String[]{
						PetriNetToolsSettings.getPetrifyCommand(),
						"-hide",
						".dummy",
						"-eqn",
						synthesised.getAbsolutePath(),
						"-cg",
						original.getAbsolutePath()
				}), new File("."));

		Result<ExternalProcessResult> result = taskManager.execute(task, "PETRIFY synthesis");

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
	}


	private static void contractDummiesDesiJ(File original, File contracted) throws IOException
	{
		throw new RuntimeException("Not implemented");
	}

	private static void contractDummies(TaskManager taskManager, File original, File contracted) throws IOException
	{

		Result<ExternalProcessResult> result = taskManager.execute(
		new ExternalProcessTask(
				Arrays.asList(
				new String[]{
						PetriNetToolsSettings.getPetrifyCommand(),
						"-hide",
						".dummy",
						original.getAbsolutePath()
				}), new File(".")),
				"PETRIFY dummy contraction");

		if(result.getOutcome() == Outcome.CANCELLED)
			throw new RuntimeException("Cancelled");

		if(result.getOutcome() == Outcome.FAILED)
			throw new RuntimeException(result.getCause());

		FileOutputStream outStream = new FileOutputStream(contracted);
		ExternalProcessResult retVal = result.getReturnValue();
		outStream.write(retVal.getOutput());
		outStream.close();

		System.out.println("Petrify Dummy contraction errors: ");
		System.out.write(retVal.getErrors());System.out.println();System.out.println("----------------------------------------");
		if(retVal.getReturnCode() != 0)
			throw new RuntimeException("Dummy contraction failed! " + retVal.getErrors().toString());
	}

	private static void mpsatMakeEqn(TaskManager taskManager, File cscResolvedMci, File synthesised) throws IOException
	{
		MpsatSettings settings = new MpsatSettings(MpsatMode.COMPLEX_GATE_IMPLEMENTATION, 0, MpsatSettings.SOLVER_MINISAT, SolutionMode.FIRST, 1, null);

		Result<ExternalProcessResult> result = taskManager.execute(new MpsatTask(settings.getMpsatArguments(), cscResolvedMci.getAbsolutePath()), "MPSat Complex gate synthesis");

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
			FileUtils.writeAllBytes(result.getReturnValue().getOutput(), synthesised);
		}
	}

	public static void makeUnfolding(TaskManager taskManager, File original, File unfolding) throws IOException
	{
		PunfTask task = new PunfTask(original.getAbsolutePath(), unfolding.getAbsolutePath());

		Result<ExternalProcessResult> res = taskManager.execute(task, "Unfolding the Balsa circuit STG");

		System.out.println("Unfolding output: ");
		System.out.write(res.getReturnValue().getOutput());System.out.println();System.out.println("----------------------------------------");

		byte[] errors = res.getReturnValue().getErrors();

		if(errors.length > 0)
		{
			System.out.println("Unfolding errors stream: ");
			System.out.write(errors);System.out.println();System.out.println("----------------------------------------");
		}

		Outcome outcome = res.getOutcome();
		switch(outcome)
		{
		case CANCELLED:
			throw new RuntimeException("Unfolding operation cancelled by user");
		case FINISHED:
			return;
		case FAILED:
			throw new RuntimeException("Punf failed: " + new String(res.getReturnValue().getErrors()), res.getCause());
		}
	}

	private void exportOriginal(Model model, File original)
			throws FileNotFoundException, IOException,
			ModelValidationException, SerialisationException {
		FileOutputStream tempFileStream = new FileOutputStream(original);
		try
		{
			new BalsaToStgExporter_FourPhase().export(model, tempFileStream);
		}
		finally
		{
			tempFileStream.close();
		}
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
	public UUID getTargetFormat() {
		return Format.EQN;
	}
}
