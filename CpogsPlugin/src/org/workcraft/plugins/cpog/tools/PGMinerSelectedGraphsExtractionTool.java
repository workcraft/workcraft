package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.workcraft.exceptions.OperationCancelledException;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.tasks.PGMinerResultHandler;
import org.workcraft.plugins.cpog.tasks.PGMinerTask;
import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.workspace.WorkspaceEntry;

public class PGMinerSelectedGraphsExtractionTool implements Tool {

	public String getSection() {
		return "! Process Mining";
	}

	public String getDisplayName() {
		return "Extract concurrency";
	}

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		if (we.getModelEntry() == null) return false;
		if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
		return false;
	}

	public File getInputFile(WorkspaceEntry we) {
		File inputFile = null;
		try {
			VisualCPOG visualCpog = (VisualCPOG) we.getModelEntry().getVisualModel();
			String allGraphs = CpogParsingTool.getExpressionFromGraph(visualCpog);
			ArrayList<String> tempGraphs = new ArrayList<>();
			ArrayList<String> graphs = new ArrayList<>();
			String prefix = "input", suffix = ".tr";
			inputFile = File.createTempFile("input", ".tr");

			PrintStream expressions = new PrintStream(inputFile);

			int i = allGraphs.indexOf(" + ");
			while (i > -1) {
				allGraphs = allGraphs.substring(0, i) + "\n" + allGraphs.substring(i + 2);
				i = allGraphs.indexOf(" + ");
			}
			allGraphs = allGraphs.replaceAll(" -> ", " ");

			while (allGraphs.contains("\n")) {
				int index = allGraphs.indexOf("\n");
				String graph = (allGraphs.substring(0, index));
				allGraphs = allGraphs.substring(index + 1);
				tempGraphs.add(graph);
			}

			//tempGraphs.add(allGraphs);

			for (String graph : tempGraphs) {
				int index = graph.indexOf("= ");
				if (index >= 0) {
					graph = graph.substring(index + 2);
				}
				while (graph.startsWith(" ")) {
					graph = graph.substring(1);
				}
				while (graph.endsWith(" ")) {
					graph = graph.substring(0, graph.length() - 1);
				}
				graphs.add(graph);
			}

			for (String graph : graphs) {
				expressions.println(graph);
			}

			expressions.close();


			} catch (IOException exception) {
				exception.printStackTrace();
			} catch (ArrayIndexOutOfBoundsException e2) {
				JOptionPane.showMessageDialog(null,
						"Error: No graphs have been selected",
						"Error",
						JOptionPane.ERROR_MESSAGE);
						throw e2;
			}


		return inputFile;
	}

	public void run(WorkspaceEntry we) {

		try {

			File inputFile = getInputFile(we);
			PGMinerTask task = new PGMinerTask(inputFile, false);

			final Framework framework = Framework.getInstance();
			PGMinerResultHandler result = new PGMinerResultHandler((VisualCPOG) we.getModelEntry().getVisualModel(), we, true);
			framework.getTaskManager().queue(task, "PGMiner", result);
		} catch (ArrayIndexOutOfBoundsException e) {

		}

	}

}
