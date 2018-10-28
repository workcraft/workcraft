package org.workcraft.plugins.mpsat.gui;

import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.graph.tools.Trace;
import org.workcraft.plugins.mpsat.tasks.MpsatSolution;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
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
        int rowCount = solution.getBranchTrace() == null ? 1 : 2;
        JTextArea traceText = new JTextArea(rowCount, columnCount);
        traceText.setMargin(SizeHelper.getTextMargin());
        if (solutionString.isEmpty()) {
            traceText.setText(Trace.EMPTY_TRACE_TEXT);
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
        playButton.addActionListener(event -> {
            MpsatUtils.playSolution(we, solution);
            closeAction.actionPerformed(null);
        });

        buttonsPanel.add(playButton);

        add(commentLabel, "0 0");
        add(scrollPane, "0 1");
        add(buttonsPanel, "1 1");
    }

}
