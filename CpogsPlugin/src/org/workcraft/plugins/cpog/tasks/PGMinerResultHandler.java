package org.workcraft.plugins.cpog.tasks;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.workcraft.Framework;
import org.workcraft.dom.VisualModelDescriptor;
import org.workcraft.dom.math.MathModel;
import org.workcraft.exceptions.VisualModelInstantiationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.cpog.CpogDescriptor;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.tools.CpogSelectionTool;
import org.workcraft.plugins.shared.tasks.ExternalProcessResult;
import org.workcraft.tasks.DummyProgressMonitor;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.util.FileUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class PGMinerResultHandler extends DummyProgressMonitor<ExternalProcessResult> {

	private VisualCPOG visualCpog;
	private WorkspaceEntry we;
	private boolean importAndExtract;

	public PGMinerResultHandler(VisualCPOG visualCpog, WorkspaceEntry we, boolean importAndExtract) {
		this.visualCpog = visualCpog;
		this.we = we;
		this.importAndExtract = importAndExtract;
	}

	public void finished(final Result<? extends ExternalProcessResult> result, String description) {

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					final Framework framework = Framework.getInstance();
					MainWindow mainWindow = framework.getMainWindow();
					if (result.getOutcome() == Outcome.FAILED) {
						JOptionPane.showMessageDialog(mainWindow, "PGMiner could not run", "Concurrency extraction failed", JOptionPane.ERROR_MESSAGE);
					} else {
					if (!importAndExtract) {

					CpogDescriptor cpogModel = new CpogDescriptor();
					MathModel mathModel = cpogModel.createMathModel();
					Path<String> path = we.getWorkspacePath();
					VisualModelDescriptor v = cpogModel.getVisualModelDescriptor();
					try {
						if (v == null) {
						throw new VisualModelInstantiationException("visual model is not defined for \"" + cpogModel.getDisplayName() + "\".");
						}
						visualCpog = (VisualCPOG) v.create(mathModel);
						final Workspace workspace = framework.getWorkspace();
						final Path<String> directory = workspace.getPath(we).getParent();
						final String name = FileUtils.getFileNameWithoutExtension(new File(path.getNode()));
						final ModelEntry me = new ModelEntry(cpogModel , visualCpog);
						workspace.add(directory, name, me, true, true);


					} catch (VisualModelInstantiationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
					byte[] output = result.getReturnValue().getOutputFile("output.1.cpog");
					String text = new String(output);
					String line = "";
					boolean finished = false;

					System.out.println("\nResulting Parameterised Graph Equations");
					while (!finished) {
						if (text.contains("\n")) {
							line = text.substring(0, text.indexOf("\n"));
							text = text.substring(text.indexOf("\n") + 1, text.length());
						}
						if (text.compareTo("") == 0) {
							finished = true;
						}
						line = line.replaceAll("\r", "");
						while (line.endsWith(" ")) {
							line = line.substring(0, line.length() - 1);
						}
						System.out.println(line);
						while (line.endsWith(" ")) {
							line = line.substring(0, line.length() - 1);
						}
						if (!(line.endsWith("="))) {
							final GraphEditorPanel editor = framework.getMainWindow().getCurrentEditor();
							final ToolboxPanel toolbox = editor.getToolBox();
							final CpogSelectionTool tool = toolbox.getToolInstance(CpogSelectionTool.class);
							tool.insertExpression(line, false, false, false, true);
							line = "";
						}
						else {
							JOptionPane.showMessageDialog(mainWindow, "PGMiner finished with no result", "No concurrency", JOptionPane.ERROR_MESSAGE);
						}
					}
					System.out.println();
				}
				}
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}

