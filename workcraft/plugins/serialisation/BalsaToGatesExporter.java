package org.workcraft.plugins.serialisation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.workcraft.dom.Model;
import org.workcraft.framework.exceptions.ExportException;
import org.workcraft.framework.exceptions.ModelValidationException;
import org.workcraft.framework.interop.SynchronousExternalProcess;
import org.workcraft.framework.serialisation.Exporter;
import org.workcraft.plugins.balsa.BalsaCircuit;

public class BalsaToGatesExporter implements Exporter {
	@Override
	public void export(Model model, OutputStream out) throws IOException,
			ModelValidationException, ExportException {
		File tempDir = createTempDirectory();

		File original = new File(tempDir, "composition.g");
		exportOriginal(model, original);

		File unfolding = new File(tempDir, "composition.mci");

		makeUnfolding(original, unfolding);

		File csc_resolved_mci = new File(tempDir, "resolved.mci");

		resolveConflicts(unfolding, csc_resolved_mci);

		File synthesised = new File(tempDir, "RESULT");

		synthesise(csc_resolved_mci, synthesised);
	}

	private void synthesise(File cscResolvedMci, File synthesised) {
		// TODO Auto-generated method stub

	}

	private void resolveConflicts(File unfolding, File cscResolvedMci) throws IOException {
		File resolutionDir = createTempDirectory();

		String mpsatFullPath = new File(mpsatPath).getAbsolutePath();
		String[] split = mpsatArgsFormat.split(" ");
		String[] args = new String[split.length + 2];
		args[0] = mpsatFullPath;
		for(int i=0;i<split.length;i++)
			args[i+1] = split[i];
		args[split.length+1] = unfolding.getAbsolutePath();

		SynchronousExternalProcess process = new SynchronousExternalProcess(args, resolutionDir.getAbsolutePath());

		process.start(100000);
		System.out.print("MPSAT CSC resolution output: ");
		System.out.write(process.getOutputData());
		System.out.print("MPSAT CSC resolution errors: ");
		System.out.write(process.getErrorData());

		deleteDirectory(resolutionDir);
	}

	private void deleteDirectory(File dir) {
		File [] files = dir.listFiles();
		if(files != null)
			for(File file : files)
				deleteDirectory(file);

		dir.delete();
	}

	private void makeUnfolding(File original, File unfolding) throws IOException {
		String punfFullPath = new File(punfPath).getAbsolutePath();
		SynchronousExternalProcess process =
			new SynchronousExternalProcess(
					new String[]{
							punfFullPath,
							"-f="+original.getAbsolutePath(),
							"-m="+unfolding.getAbsolutePath()},
							".");
		process.start(100000);
		System.out.print("Unfolding output: ");
		System.out.write(process.getOutputData());
		System.out.print("Unfolding errors: ");
		System.out.write(process.getErrorData());
	}

	private void exportOriginal(Model model, File original)
			throws FileNotFoundException, IOException,
			ModelValidationException, ExportException {
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

	private File createTempDirectory() {
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

	private String punfPath = "../Util/punf";
	private String mpsatPath = "../Util/mpsat";
	private String mpsatArgsFormat = "-R -f -$1 -p0 -@ -cl";


	public String getDescription() {
		return "To Gates";
	}

	public String getExtenstion() {
		return ".gates";
	}

	public boolean isApplicableTo(Model model) {
		return model.getMathModel() instanceof BalsaCircuit;
	}

}
