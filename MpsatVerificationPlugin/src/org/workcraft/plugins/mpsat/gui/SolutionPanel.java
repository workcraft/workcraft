package org.workcraft.plugins.mpsat.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.SimulationTool;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class SolutionPanel extends JPanel {

    public SolutionPanel(final WorkspaceEntry we, final Solution solution, final ActionListener closeAction) {
        double[][] sizes = new double[][] {
            {TableLayout.FILL, TableLayout.PREFERRED},
            {TableLayout.PREFERRED, TableLayout.FILL},
        };
        TableLayout layout = new TableLayout(sizes);
        int hGap = SizeHelper.getCompactLayoutHGap();
        int vGap = SizeHelper.getCompactLayoutVGap();
        layout.setHGap(hGap);
        layout.setVGap(vGap);
        setLayout(layout);

        JLabel commentLabel = new JLabel();
        if (solution.getComment() != null) {
            commentLabel.setText(solution.getComment());
        }
        JTextArea traceText = new JTextArea();
        traceText.setBorder(BorderFactory.createEmptyBorder(hGap, vGap, hGap, vGap));
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

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        JButton playButton = new JButton("Play");
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                GraphEditorPanel currentEditor = mainWindow.getCurrentEditor();
                if (currentEditor == null || currentEditor.getWorkspaceEntry() != we) {
                    final List<GraphEditorPanel> editors = mainWindow.getEditors(we);
                    if (editors.size() > 0) {
                        currentEditor = editors.get(0);
                        mainWindow.requestFocus(currentEditor);
                    } else {
                        currentEditor = mainWindow.createEditorWindow(we);
                    }
                }
                final ToolboxPanel toolbox = currentEditor.getToolBox();
                final SimulationTool tool = toolbox.getToolInstance(SimulationTool.class);
                toolbox.selectTool(tool);
                tool.setTrace(solution.getMainTrace(), solution.getBranchTrace(), currentEditor);
                String comment = solution.getComment();
                if ((comment != null) && !comment.isEmpty()) {
                    comment = comment.replaceAll("\\<.*?>", "");
                    LogUtils.logWarningLine(comment);
                }
                closeAction.actionPerformed(null);
            }
        });

        buttonsPanel.add(playButton);

        add(commentLabel, "0 0");
        add(scrollPane, "0 1");
        add(buttonsPanel, "1 1");
    }

}
