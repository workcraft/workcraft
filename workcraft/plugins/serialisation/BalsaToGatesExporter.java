package org.workcraft.plugins.serialisation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.exceptions.SerialisationException;
import org.workcraft.framework.interop.SynchronousExternalProcess;
import org.workcraft.framework.serialisation.Exporter;
import org.workcraft.framework.util.Export;
import org.workcraft.framework.util.Import;
import org.workcraft.plugins.balsa.BalsaCircuit;
import org.workcraft.plugins.layout.PetriNetToolsSettings;
import org.workcraft.util.DummyRenamer;

public class BalsaToGatesExporter implements Exporter {
	private static String mpsatArgsFormat = "-R -f -$1 -p0 -@ -cl";


	@Override
	public void export(Model model, OutputStream out) throws IOException,
			ModelValidationException, SerialisationException {
		File tempDir = createTempDirectory();

		File original = new File(tempDir, "composition.g");
		exportOriginal(model, original);

		File synthesised = new File(tempDir, "RESULT");

		synthesiseStg(original, synthesised);

		FileUtils.copyFileToStream(synthesised, out);
	}

	public static void synthesiseStg(File original, File synthesised)
			throws IOException {
		File tempDir = createTempDirectory();

		File unfolding = new File(tempDir, "composition.mci");
		File renamed = new File(tempDir, "renamed.g");
		File renamed2 = new File(tempDir, "renamed2.g");

		DummyRenamer.rename(original, renamed);

		try {
			Model stg = Import.importFromFile(new DotGImporter(), renamed);
			Export.exportToFile(new DotGExporter(), stg, renamed2);
		} catch (ModelValidationException e) {
			throw new RuntimeException(e);
		} catch (SerialisationException e) {
			throw new RuntimeException(e);
		} catch (DeserialisationException e) {
			throw new RuntimeException(e);
		}

		FileUtils.copyFile(renamed, new File(original.getAbsolutePath()+".ren"));
		FileUtils.copyFile(renamed2, new File(original.getAbsolutePath()+".ren2"));

		File contracted = new File(tempDir, "contracted.g");

		contractDummies(renamed2, contracted);

		makeUnfolding(contracted, unfolding);

		File csc_resolved_mci = new File(tempDir, "resolved.mci");

		resolveConflicts(unfolding, csc_resolved_mci);

		synthesise(csc_resolved_mci, synthesised);
	}

	private static void contractDummies(File original, File contracted) throws IOException {

		SynchronousExternalProcess process = new SynchronousExternalProcess(
				new String[]{
						"petrify",
						"-hide",
						".dummy",
						original.getAbsolutePath()
				}, ".");

		process.start(100000);

		FileOutputStream outStream = new FileOutputStream(contracted);
		outStream.write(process.getOutputData());
		outStream.close();

		System.out.println("Petrify Dummy contraction errors: ");
		System.out.write(process.getErrorData());System.out.println();System.out.println("----------------------------------------");
	}

	private static void synthesise(File cscResolvedMci, File synthesised) throws IOException
	{
		SynchronousExternalProcess process = new SynchronousExternalProcess(
				new String[]{
						PetriNetToolsSettings.getMpsatCommand(),
						"-E",
						cscResolvedMci.getAbsolutePath()
				}, ".");

		process.start(100000);

		FileOutputStream outStream = new FileOutputStream(synthesised);
		outStream.write(process.getOutputData());
		outStream.close();

		System.out.println("MPSAT complex gate synthesis errors: ");
		System.out.write(process.getErrorData());System.out.println();System.out.println("----------------------------------------");
	}

	private static void resolveConflicts(File unfolding, File cscResolvedMci) throws IOException {
		File resolutionDir = createTempDirectory();

		String[] split = mpsatArgsFormat.split(" ");
		String[] args = new String[split.length + 2];
		args[0] = PetriNetToolsSettings.getMpsatCommand();
		for(int i=0;i<split.length;i++)
			args[i+1] = split[i];
		args[split.length+1] = unfolding.getAbsolutePath();

		SynchronousExternalProcess process = new SynchronousExternalProcess(args, resolutionDir.getAbsolutePath());

		process.start(100000);
		System.out.println("MPSAT CSC resolution output: ");
		System.out.write(process.getOutputData());System.out.println();System.out.println("----------------------------------------");
		System.out.println("MPSAT CSC resolution errors: ");
		System.out.write(process.getErrorData());System.out.println();System.out.println("----------------------------------------");

		if(process.getReturnCode() != 0)
			throw new RuntimeException("MPSAT SCS resolution failed: " + new String(process.getErrorData()));

		FileUtils.copyFile(new File(resolutionDir, "mpsat.mci"), cscResolvedMci);

		deleteDirectory(resolutionDir);
	}

	private static void deleteDirectory(File dir) {
		File [] files = dir.listFiles();
		if(files != null)
			for(File file : files)
				deleteDirectory(file);

		dir.delete();
	}

	private static void makeUnfolding(File original, File unfolding) throws IOException {

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

	private static File createTempDirectory() {
		File tempDir;
		try {
			tempDir = File.createTempFile("balsaExport", "");
		} catch (IOException e) {
			throw new RuntimeException("can't create a temp file");
		}
		tempDir.delete();
		if(!tempDir.mkdir())
			throw new RuntimeException("can't create a temp directory");
		return tempDir;
	}

	public String getDescription() {
		return "To Gates";
	}

	public String getExtenstion() {
		return ".gates";
	}

	public boolean isApplicableTo(Model model) {
		return model instanceof BalsaCircuit;
	}

}
