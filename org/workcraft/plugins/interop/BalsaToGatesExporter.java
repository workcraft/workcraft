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

package org.workcraft.plugins.interop;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.exceptions.DeserialisationException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.interop.SynchronousExternalProcess;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.interop.BalsaExportConfig.DummyContractionMode;
import org.workcraft.plugins.layout.PetriNetToolsSettings;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.Import;
public class BalsaToGatesExporter implements Exporter {

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

			synthesiseStg(original, synthesised, BalsaExportConfig.DEFAULT);

			FileUtils.copyFileToStream(synthesised, out);
		}
	}

	private void exportFromStg(STG model, OutputStream out) throws IOException, ModelValidationException, SerialisationException {
		File dotG = File.createTempFile("original", ".g");
		File eqn = File.createTempFile("result", ".eqn");
		Export.exportToFile(new DotGExporter(), model, dotG);

		synthesiseStg(dotG, eqn, BalsaExportConfig.DEFAULT);

		FileUtils.copyFileToStream(eqn, out);
	}

	public static void synthesiseStg(File original, File synthesised, BalsaExportConfig config)
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
				contractDummies(implicitRemoved, contracted);
			case DESIJ:
				contractDummiesDesiJ(implicitRemoved, contracted);
			default:
				throw new RuntimeException("Unsupported contraction");

			}
		}

		synthesise(afterContraction, synthesised, config);
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

	private static void synthesise(File original, File synthesised, BalsaExportConfig config) throws IOException {
		switch(config.synthesisTool())
		{
		case MPSAT:
		{
			File tempDir = FileUtils.createTempDirectory("stgSynthesis");

			File unfolding = new File(tempDir, "composition.mci");

			makeUnfolding(original, unfolding);

			File csc_resolved_mci = new File(tempDir, "resolved.mci");

			CSCResolver.resolveConflicts(unfolding, csc_resolved_mci, null);

			mpsatMakeEqn(csc_resolved_mci, synthesised);
			break;
		}
		case PETRIFY:
			petrifyMakeEqn(original, synthesised);
			break;
			default:
		}
	}

	private static void petrifyMakeEqn(File original, File synthesised) throws IOException {
		SynchronousExternalProcess process = new SynchronousExternalProcess(
				new String[]{
						PetriNetToolsSettings.getPetrifyCommand(),
						"-hide",
						".dummy",
						"-eqn",
						synthesised.getAbsolutePath(),
						"-cg",
						original.getAbsolutePath()
				}, ".");

		process.start(500000);

		System.out.println("Petrify complex gate synthesis output: ");
		System.out.write(process.getOutputData());System.out.println();System.out.println("----------------------------------------");

		System.out.println("Petrify complex gate synthesis errors: ");
		System.out.write(process.getErrorData());System.out.println();System.out.println("----------------------------------------");
	}


	private static void contractDummiesDesiJ(File original, File contracted) throws IOException
	{
		throw new RuntimeException("Not implemented");
	}

	private static void contractDummies(File original, File contracted) throws IOException
	{
		SynchronousExternalProcess process = new SynchronousExternalProcess(
				new String[]{
						PetriNetToolsSettings.getPetrifyCommand(),
						"-hide",
						".dummy",
						original.getAbsolutePath()
				}, ".");

		process.start(200000);

		FileOutputStream outStream = new FileOutputStream(contracted);
		outStream.write(process.getOutputData());
		outStream.close();

		System.out.println("Petrify Dummy contraction errors: ");
		System.out.write(process.getErrorData());System.out.println();System.out.println("----------------------------------------");
		if(process.getReturnCode() != 0)
			throw new RuntimeException("Dummy contraction failed! " + process.getErrorData().toString());
	}

	private static void mpsatMakeEqn(File cscResolvedMci, File synthesised) throws IOException
	{
		SynchronousExternalProcess process = new SynchronousExternalProcess(
				new String[]{
						PetriNetToolsSettings.getMpsatCommand(),
						"-E",
						cscResolvedMci.getAbsolutePath()
				}, ".");

		process.start(300000);

		FileOutputStream outStream = new FileOutputStream(synthesised);
		outStream.write(process.getOutputData());
		outStream.close();

		System.out.println("MPSAT complex gate synthesis errors: ");
		System.out.write(process.getErrorData());System.out.println();System.out.println("----------------------------------------");
		if(process.getReturnCode() != 0)
			throw new RuntimeException("MPSAT complex gate synthesis failed! " + process.getErrorData().toString());
	}

	public static void makeUnfolding(File original, File unfolding) throws IOException {

		SynchronousExternalProcess process =
			new SynchronousExternalProcess(
					new String[]{
							PetriNetToolsSettings.getPunfCommand(),
							"-f="+original.getAbsolutePath(),
							"-m="+unfolding.getAbsolutePath()},
							".");
		process.start(100000);
		System.out.println("Unfolding output: ");
		System.out.write(process.getOutputData());System.out.println();System.out.println("----------------------------------------");
		System.out.println("Unfolding errors: ");
		System.out.write(process.getErrorData());System.out.println();System.out.println("----------------------------------------");
		if(process.getReturnCode() != 0)
			throw new RuntimeException("PUNF Failed: " + new String(process.getErrorData()));
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

	public String getDescription() {
		return "To Gates";
	}

	public String getExtenstion() {
		return ".eqn";
	}

	public boolean isApplicableTo(Model model) {
		return model instanceof BalsaCircuit || model instanceof STG;
	}

}
