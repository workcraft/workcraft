/**
 *
 */
package org.workcraft.plugins.desij.tasks;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.dom.Model;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.desij.DesiJOperation;
import org.workcraft.plugins.desij.DesiJSettings;
import org.workcraft.plugins.stg.STGModel;
import org.workcraft.serialisation.Format;
import org.workcraft.tasks.ProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.util.Export;
import org.workcraft.util.Export.ExportTask;

/**
 * @author Dominic Wist
 *
 */
public class DesiJTask implements Task<DesiJResult> {

	private boolean userCancelled = false; // user cancelled the execution of desiJ
	private int returnCode = 0;

	private STGModel specModel;
	private File specificationFile; // specified in the last argument of desiJArgs

	private DesiJSettings desiJSettings = null; // is not always set
	private String[] desiJArgs; // parameters to call desiJMain(desijArgs);


	/*
	 * Constructors
	 */
	public DesiJTask(STGModel model, Framework framework, String[] desiJParameters) {

		this.specModel = model;
		desiJArgs = new String[desiJParameters.length+1];

		// copy content from desiJParameters to desiJArgs
		for (int i=0; i < desiJParameters.length; i++)
			desiJArgs[i] = desiJParameters[i];

		this.specificationFile = getSpecificationFile(model, framework);
		// and add the specification filename as last argument
		try {
			desiJArgs[desiJParameters.length] = this.specificationFile.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public DesiJTask(STGModel model, Framework framework, DesiJSettings settings) {
		this(model, framework, generateCommandLineParameters(settings));
		this.desiJSettings = settings;
	}


	/* (non-Javadoc)
	 * @see org.workcraft.tasks.Task#run(org.workcraft.tasks.ProgressMonitor)
	 *
	 */
	@Override
	public Result<? extends DesiJResult> run(ProgressMonitor<? super DesiJResult> monitor) {

		// create desiJ thread
		DesiJThread desiJThread = new DesiJThread(desiJArgs);

		// start desiJ thread
		desiJThread.start();

		// monitor the desiJ execution
		while (true) {
			if (monitor.isCancelRequested() && desiJThread.isAlive()) {
				desiJThread.killThread(); // hard cancellation --> could lead to deadlocks
				userCancelled = true;
			}
			if (!desiJThread.isAlive()) break;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				desiJThread.killThread();
				userCancelled = true;
				break;
			}
		}

		returnCode = desiJThread.getExitCode();

		if (userCancelled)
			return new Result<DesiJResult>(Outcome.CANCELLED); // maybe input or output files are not released

		// ---- build DesiJResult ----

		File[] componentFiles = getResultingComponents();
		File logFile = getLogFile();
		File modifiedSpecification = null;
		File equationsFile = null;

		if (desiJSettings != null) {
			if (desiJSettings.getOperation() != DesiJOperation.DECOMPOSITION)
				modifiedSpecification = getModifiedSpecification();
			if (desiJSettings.getPostSynthesisOption())
				equationsFile = getEquationsFile();
		}

		DesiJResult result = new DesiJResult(this.specModel, this.specificationFile,
				componentFiles, logFile, modifiedSpecification, equationsFile);

		if (returnCode < 2)
			return new Result<DesiJResult>(Outcome.FINISHED, result);
		else
			return new Result<DesiJResult>(Outcome.FAILED, result);
	}


	// ******************* private helper routines ***********************

	private File getEquationsFile() {
		try {
			return new File(this.specificationFile.getCanonicalPath() + ".equations");
			// current DesiJ naming convention for petrify and mpsat synthesis
		} catch (IOException e) {
			return null; // no file found!
		}
	}

	private File getLogFile() {
		return new File("desij.logfile"); // current DesiJ naming convention
	}

	/**
	 * Only called when "desiJSettings!=null"
	 * @return Spec without redundant places and/or dummies
	 */
	private File getModifiedSpecification() {

		if (desiJSettings.getOperation() != DesiJOperation.IMPLICIT_PLACE_DELETION &&
				desiJSettings.getOperation() != DesiJOperation.REMOVE_DUMMIES)
			return null;

		if (desiJSettings.getOperation() == DesiJOperation.IMPLICIT_PLACE_DELETION) {
			// get specification without redundant places
			try {
				return new File(this.specificationFile.getCanonicalPath() + ".red.g"); // current DesiJ naming convention
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		else if (desiJSettings.getOperation() == DesiJOperation.REMOVE_DUMMIES) {
			// get specification without dummy transitions
			return new File("STGwithoutDummies.g"); // naming convention, see generateCommandLineParameters()
		}

		return null; // should be not reachable, see first line of this method

	}

	private File[] getResultingComponents() {

		String canonicalSpecName;
		try {
			canonicalSpecName = this.specificationFile.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// get WorkingDiretory and SpecFile names
		int fileSeparatorIndex =
			canonicalSpecName.lastIndexOf(System.getProperty("file.separator"));
		final String workingDirName = canonicalSpecName.substring(0, fileSeparatorIndex);
		final String specFileName = canonicalSpecName.substring(fileSeparatorIndex+1);
		File workingDirectory = new File(workingDirName);

		// get all component filenames
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(specFileName + "__final_"); // current DesiJ naming convention
			}
		};
		String[] children = workingDirectory.list(filter); // based on File names

		// format the result
		if (children == null || children.length == 0) {
			// Either workingDirectory does not exist or is not a directory or
			// no children are returned, because no decomposition operation has been performed
			return null;
		}
		else {
			File[] result = new File[children.length];
			for (int i=0; i<children.length; i++) {
				result[i] = new File(workingDirectory, children[i]);
			}
			return result;
		}
	}


	private File getSpecificationFile(Model model, Framework framework) {
		Exporter stgExporter = Export.chooseBestExporter(framework.getPluginManager(), model, Format.STG);

		if (stgExporter == null)
			throw new RuntimeException ("Exporter not available: model class " + model.getClass().getName() + " to format STG.");

		File stgFile;

		try {
			stgFile = File.createTempFile("specification", stgExporter.getExtenstion());
			ExportTask exportTask = new ExportTask(stgExporter, model, stgFile.getCanonicalPath());
			framework.getTaskManager().execute(exportTask, "Exporting .g");

			return stgFile;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// **** private static methods to generate Parameter String Array in Constructor *******

	private static String[] generateCommandLineParameters(DesiJSettings settings) {
		ArrayList<String> parameters = new ArrayList<String>();

		String redPlaceParam = redundantPlaceDeletion(settings);
		String contrModeParam = contractionMode(settings);

		if (settings.getOperation() == DesiJOperation.DECOMPOSITION) {
			decoStrategy(settings, parameters);
			outputPartitioning(settings,parameters);
			if (redPlaceParam != null) parameters.add(redPlaceParam);
			if (contrModeParam != null) parameters.add(contrModeParam);
			synthesisOptions(settings, parameters);
		}
		else if (settings.getOperation() == DesiJOperation.REMOVE_DUMMIES) {
			parameters.add("operation=killdummies");
			parameters.add("outfile=STGwithoutDummies.g"); // result's filename
			if (redPlaceParam != null) parameters.add(redPlaceParam);
			if (contrModeParam != null) parameters.add(contrModeParam);

		}
		else if (settings.getOperation() == DesiJOperation.IMPLICIT_PLACE_DELETION) {
			parameters.add("operation=reddel");
			if (redPlaceParam != null) parameters.add(redPlaceParam);
		}

		parameters.trimToSize();
		String[] result = new String[parameters.size()];
		parameters.toArray(result);
		return result;
	}

	private static void outputPartitioning(DesiJSettings settings,
			ArrayList<String> result) {
		switch (settings.getPartitionMode()) {
		case FINEST: // default: Do nothing!
			//result.add("partition=finest");
			break;
		case BEST:
			result.add("partition=best");
			break;
		case CUSTOM:
			String customPartition = settings.getPartition();
			if (customPartition == null || customPartition.equals(""))
				break; // error handling...
			try {
				File partitionFile = File.createTempFile("partition", "txt");
				FileWriter outStream = new FileWriter(partitionFile, false);
				outStream.write(settings.getPartition());
				outStream.close();
				result.add("partition=file:" + partitionFile.getCanonicalPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			break;
		}
	}

	private static void synthesisOptions(DesiJSettings settings,
			ArrayList<String> result) {
		if (settings.getInternalCommunicationOption())
			result.add("-k");
		if (settings.getPostSynthesisOption()) {
			result.add("-y");
			switch (settings.getSynthesiser()) {
				case DesiJSettings.SYN_PETRIFY:
					result.add("syn-tool=petrify");
					break;
				case DesiJSettings.SYN_MPSAT:
					result.add("syn-tool=mpsat");
					break;
			}
		}
	}

	private static void decoStrategy(DesiJSettings settings, ArrayList<String> result) {
		if (settings.getCSCAwareOption()) {
			if (settings.getAggregationFactor() > 0) {
				result.add("version=csc-aware");
				result.add("-a"); // aggregation included
				result.add("mcs=" + settings.getAggregationFactor() + ".0"); // aggregation signal count
			}
			else {
				result.add("version=csc-aware");
			}
		}
		else
			switch (settings.getDecoStrategy()) {
				case TREE: // default
					if (settings.getAggregationFactor() > 0) {
						result.add("-a");
						result.add("mcs=" + settings.getAggregationFactor() + ".0");
					}
					break;
				case BASIC:
					result.add("version=basic");
					break;
				case LAZYMULTI:
					result.add("version=lazy-multi");
					break;
				case LAZYSINGLE:
					result.add("version=lazy-single");
					break;
			}
	}

	private static String redundantPlaceDeletion(DesiJSettings settings) {

		if (!settings.getLoopDuplicatePlaceHandling())
			return "-P"; // no removal of redundant places
		if (!settings.getShortcutPlaceHandling())
			return "-u"; // loop duplicate places are deleted anyway
		if (settings.getImplicitPlaceHandling())
			return "-X"; // -u and -P are set by default

		return null;
	}

	private static String contractionMode(DesiJSettings settings) {
		String result = null;

		if (!settings.getSafenessPreservingContractionOption())
			result = "-f";
		if (settings.getOutputDeterminacyOption()) {
			if (result == null)
				result = "-@";
			else
				result = result + "@";
		}
		if (settings.getRiskyOption()) {
			if (result == null)
				result = "-Y";
			else
				result = result + "Y";
		}

		return result;
	}

}
