package org.workcraft.plugins.mpsat.gui;

import info.clearthought.layout.TableLayout;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.mpsat.tasks.MpsatSolution;
import org.workcraft.plugins.mpsat.tasks.MpsatUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class MpsatSolutionPanel extends JPanel {

    private static final int MIN_COLUMN_COUNT = 20;
    private static final int MAX_COLUMN_COUNT = 60;

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

        JTextArea traceText = new JTextArea(getRowCount(solution), getColumnCount(solution));
        traceText.setMargin(SizeHelper.getTextMargin());
        String solutionString = solution.toString();
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

    public int getRowCount(MpsatSolution solution) {
        return solution.getBranchTrace() == null ? 1 : 2;
    }

    public int getColumnCount(MpsatSolution solution) {
        int result = 0;
        Trace mainTrace = solution.getMainTrace();
        if (mainTrace != null) {
            result = Math.max(result, mainTrace.toString().length());
        }
        Trace branchTrace = solution.getBranchTrace();
        if (branchTrace != null) {
            result = Math.max(result, branchTrace.toString().length());
        }
        if (result < MIN_COLUMN_COUNT) {
            result = MIN_COLUMN_COUNT;
        }
        if (result > MAX_COLUMN_COUNT) {
            result = MAX_COLUMN_COUNT;
        }
        return result;
    }

}
