package org.workcraft.plugins.mpsat.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.mpsat.tasks.MpsatChainTask;
import org.workcraft.plugins.stg.tools.Core;
import org.workcraft.plugins.stg.tools.EncodingConflictAnalyserTool;
import org.workcraft.util.ColorUtils;
import org.workcraft.util.ColorGenerator;
import org.workcraft.workspace.WorkspaceEntry;

import info.clearthought.layout.TableLayout;


@SuppressWarnings("serial")
public class EncodingConflictDialog extends JDialog {

	private final ColorGenerator coreColorGenerator = new ColorGenerator(ColorUtils.getHsbPalette(
					new float[]{0.45f, 0.15f, 0.70f, 0.25f, 0.05f, 0.80f, 0.55f, 0.20f, 075f, 0.50f},
					new float[]{0.30f},  new float[]{0.9f, 0.7f, 0.5f}));


	private JPanel contents;
	private JPanel solutionsPanel;
	private JPanel buttonsPanel;

	public EncodingConflictDialog(final MpsatChainTask task, String title, String message, final List<Solution> solutions) {

		double sizes[][] = {
				{ TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED }
		};

		solutionsPanel = new JPanel();
		solutionsPanel.setLayout(new BoxLayout(solutionsPanel, BoxLayout.Y_AXIS));
		for (Solution solution : solutions) {
			solutionsPanel.add(new SolutionPanel(task, solution, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					EncodingConflictDialog.this.setVisible(false);
				}
			}));
		}

		buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

		JButton analyseButton = new JButton("Analyse");
		analyseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EncodingConflictDialog.this.setVisible(false);
				final Framework framework = Framework.getInstance();
				final WorkspaceEntry we = task.getWorkspaceEntry();
				final MainWindow mainWindow = framework.getMainWindow();
				GraphEditorPanel currentEditor = mainWindow.getCurrentEditor();
				if(currentEditor == null || currentEditor.getWorkspaceEntry() != we) {
					final List<GraphEditorPanel> editors = mainWindow.getEditors(we);
					if(editors.size()>0) {
						currentEditor = editors.get(0);
						mainWindow.requestFocus(currentEditor);
					} else {
						currentEditor = mainWindow.createEditorWindow(we);
					}
				}

				final ToolboxPanel toolbox = currentEditor.getToolBox();
				final EncodingConflictAnalyserTool tool = toolbox.getToolInstance(EncodingConflictAnalyserTool.class);
				toolbox.selectTool(tool);
				ArrayList<Core> cores = convertSolutionsToCores(solutions);
				tool.setCores(cores);
			}
		});
		buttonsPanel.add(analyseButton);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EncodingConflictDialog.this.setVisible(false);
			}
		});
		buttonsPanel.add(closeButton);

		contents = new JPanel(new TableLayout(sizes));
		contents.add(new JLabel(message), "0 0");
		contents.add(solutionsPanel, "0 1");
		contents.add(buttonsPanel, "0 2");

		this.setTitle(title);
		this.setContentPane(contents);
		setMinimumSize(new Dimension(450, 200));
		setSize(new Dimension(500, 300));
		this.setModal(true);
		getRootPane().setDefaultButton(analyseButton);
	}

	private ArrayList<Core> convertSolutionsToCores(List<Solution> solutions) {
		ArrayList<Core> cores = new ArrayList<>();
		for (Solution solution: solutions) {
			Core core = new Core(solution.getCore());
			core.setColor(coreColorGenerator.updateColor());
			cores.add(core);
		}
		return cores;
	}

}
