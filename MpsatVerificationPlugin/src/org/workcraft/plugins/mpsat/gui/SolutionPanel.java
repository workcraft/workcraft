package org.workcraft.plugins.mpsat.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.plugins.petri.tools.PetriNetSimulationTool;
import org.workcraft.workspace.WorkspaceEntry;

import info.clearthought.layout.TableLayout;


@SuppressWarnings("serial")
public class SolutionPanel extends JPanel {
    private JPanel buttonsPanel;
    private JTextArea traceText;

    public SolutionPanel(final WorkspaceEntry we, final Solution solution, final ActionListener closeAction) {
        super(new TableLayout(new double[][]
                {{TableLayout.FILL, TableLayout.PREFERRED },
                {TableLayout.FILL}, }
        ));

        traceText = new JTextArea();
        String solutionString = solution.toString();
        if (solutionString.isEmpty()) {
            traceText.setText("[empty trace]");
            traceText.setEnabled(false);
        } else {
            traceText.setText(solutionString);
            traceText.setEditable(false);
        }

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(traceText);

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Framework framework = Framework.getInstance();
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
                final PetriNetSimulationTool tool = toolbox.getToolInstance(PetriNetSimulationTool.class);
                toolbox.selectTool(tool);
                tool.setTrace(solution.getMainTrace(), solution.getBranchTrace(), currentEditor);
                closeAction.actionPerformed(null);
            }
        });

        buttonsPanel.add(playButton);

        add(scrollPane, "0 0");
        add(buttonsPanel, "1 0");
    }

}
