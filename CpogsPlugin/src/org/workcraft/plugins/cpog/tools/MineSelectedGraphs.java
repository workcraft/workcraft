package org.workcraft.plugins.cpog.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JOptionPane;

import org.workcraft.plugins.cpog.CpogSettings;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.workspace.WorkspaceEntry;

public class MineSelectedGraphs extends PGMinerTool{

	public String getDisplayName() {
		return "Process mine selected graphs";
	}

	@Override
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
			return null;
			}
		return inputFile;
	}

	@Override
	public File getOutputFile(File inputFile) {

		String filePath = inputFile.getAbsolutePath();

		int index = filePath.lastIndexOf('/');
		String fileName = filePath.substring(index + 1).replace(".tr", "") + ".1.cpog";
		filePath = filePath.substring(0, index + 1);
		File outputFile = new File(filePath + fileName);

		return outputFile;
	}

}
