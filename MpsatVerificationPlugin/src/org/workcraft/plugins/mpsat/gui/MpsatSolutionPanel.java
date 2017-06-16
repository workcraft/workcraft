package org.workcraft.plugins.mpsat.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.Framework;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.SimulationTool;
import org.workcraft.util.LogUtils;
import org.workcraft.workspace.WorkspaceEntry;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class MpsatSolutionPanel extends JPanel {

    private static final int MIN_COLUMN_COUNT = 25;
    private static final int MAX_COLUMN_COUNT = 80;

    public  MpsatSolutionPanel(final WorkspaceEntry we, final MpsatSolution solution, final ActionListener closeAction) {
        double[][] sizes = new double[][] {
            {TableLayout.FILL, TableLayout.PREFERRED},
            {TableLayout.PREFERRED, TableLayout.FILL},
        };
        TableLayout layout = new TableLayout(sizes);
        layout.setHGap(SizeHelper.getLayoutHGap());
        layout.setVGap(SizeHelper.getLayoutVGap());
        setLayout(layout);

        JLabel commentLabel = new JLabel();
        if (solution.getComment() != null) {
            commentLabel.setText(solution.getComment());
        }
        String solutionString = solution.toString();
        int columnCount = solutionString.length();
        if (columnCount < MIN_COLUMN_COUNT) {
            columnCount = MIN_COLUMN_COUNT;
        }
        if (columnCount > MAX_COLUMN_COUNT) {
            columnCount = MAX_COLUMN_COUNT;
        }
        JTextArea traceText = new JTextArea(1, columnCount);
        traceText.setMargin(SizeHelper.getTextMargin());
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
                GraphEditorPanel editor = mainWindow.getEditor(we);
                final Toolbox toolbox = editor.getToolBox();
                final SimulationTool tool = toolbox.getToolInstance(SimulationTool.class);
                toolbox.selectTool(tool);
                tool.setTrace(solution.getMainTrace(), solution.getBranchTrace(), editor);
                String comment = solution.getComment();
                if ((comment != null) && !comment.isEmpty()) {
                    comment = comment.replaceAll("\\<.*?>", "");
                    LogUtils.logWarning(comment);
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
