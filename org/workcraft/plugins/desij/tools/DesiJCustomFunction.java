package org.workcraft.plugins.desij.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.annotations.DisplayName;
import org.workcraft.dom.Model;
import org.workcraft.plugins.stg.STG;
import org.workcraft.util.GUI;

import org.workcraft.plugins.desij.DecompositionResultHandler;
import org.workcraft.plugins.desij.DesiJPresetManager;
import org.workcraft.plugins.desij.DesiJSettings;
import org.workcraft.plugins.desij.gui.DesiJConfigurationDialog;
import org.workcraft.plugins.desij.tasks.DesiJTask;
import org.workcraft.plugins.desij.DesiJOperation;

@DisplayName("DesiJ - customise function")
public class DesiJCustomFunction implements Tool {

	@Override
	public String getSection() {
		return "Decomposition";
	}

	@Override
	public boolean isApplicableTo(Model model) {
		if (model instanceof STG)
			return true;
		else
			return false;
	}

	@Override
	public void run(Model model, Framework framework) {
		DesiJPresetManager pmgr = new DesiJPresetManager();
		DesiJConfigurationDialog dialog = new DesiJConfigurationDialog(framework.getMainWindow(), pmgr);
		GUI.centerFrameToParent(dialog, framework.getMainWindow());
		dialog.setVisible(true);
		if (dialog.getModalResult() == 1)
		{
			framework.getTaskManager().queue(new DesiJTask(model, framework, generateCommandLineParameters(dialog.getSettings())),
					"DesiJ Execution", new DecompositionResultHandler(framework));
		}
	}

	// ************ private methods to generate Parameter String Array ***********

	private String[] generateCommandLineParameters(DesiJSettings settings) {
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

	private void outputPartitioning(DesiJSettings settings,
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

	private void synthesisOptions(DesiJSettings settings,
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

	private void decoStrategy(DesiJSettings settings, ArrayList<String> result) {
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

	private String redundantPlaceDeletion(DesiJSettings settings) {

		if (!settings.getLoopDuplicatePlaceHandling())
			return "-P"; // no removal of redundant places
		if (!settings.getShortcutPlaceHandling())
			return "-u"; // loop duplicate places are deleted anyway
		if (settings.getImplicitPlaceHandling())
			return "-X"; // -u and -P are set by default

		return null;
	}

	private String contractionMode(DesiJSettings settings) {
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
