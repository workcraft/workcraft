package org.workcraft.plugins.cpog.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Scanner;

import org.workcraft.Tool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.workspace.WorkspaceEntry;

public class PGMinerTool implements Tool {

	@Override
	public boolean isApplicableTo(WorkspaceEntry we) {
		if (we.getModelEntry() == null) return false;
		if (we.getModelEntry().getVisualModel() instanceof VisualCPOG) return true;
		return false;
	}

	@Override
	public String getSection() {
		return "Concurrency extraction";
	}

	@Override
	public String getDisplayName() {
		return "PG miner (level 1)";
	}

	@Override
	public void run(WorkspaceEntry we) {
		VisualCPOG visualCpog = (VisualCPOG) we.getModelEntry().getVisualModel();
		String allGraphs = CpogParsingTool.getExpressionFromGraph(visualCpog);
		ArrayList<String> tempGraphs = new ArrayList<>();
		ArrayList<String> graphs = new ArrayList<>();

		try {
			String prefix = "input", suffix = ".tr";
			File inputFile = File.createTempFile(prefix, ".tr");

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
				while (graph.endsWith(" ")) {
					graph = graph.substring(0, graph.length() - 1);
				}
				graphs.add(graph);
			}

			for (String graph : graphs) {
				expressions.println(graph);
			}

			expressions.close();

			Process process = new ProcessBuilder(CpogSettings.getPGMinerCommand(), inputFile.getAbsolutePath()).start();
			try {
				process.waitFor();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			String filePath = inputFile.getAbsolutePath();

			int index = filePath.lastIndexOf('/');
			String fileName = filePath.substring(index + 1).replace(suffix, "") + ".1.cpog";
			filePath = filePath.substring(0, index + 1);
			File outputFile = new File(filePath + fileName);
			CpogSelectionTool selectionTool = (CpogSelectionTool) visualCpog.getSelectionTool();

			boolean open = false;

			while (!open) {
				try {
					Scanner k = new Scanner(outputFile);
					open = true;
					while (k.hasNext()) {
						String line = k.nextLine();
						System.out.println(line);
						selectionTool.insertExpression(line, false, false, true);
					}
					k.close();
				} catch (IOException ex) {
					open = false;
				}
			}

		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}


}
