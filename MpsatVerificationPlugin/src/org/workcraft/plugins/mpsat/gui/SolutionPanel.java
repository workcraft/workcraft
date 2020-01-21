package org.workcraft.plugins.mpsat.gui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.gui.tools.Trace;
import org.workcraft.plugins.mpsat.tasks.Solution;
import org.workcraft.plugins.mpsat.utils.MpsatUtils;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class SolutionPanel extends JPanel {

    private static final int MIN_COLUMN_COUNT = 20;
    private static final int MAX_COLUMN_COUNT = 60;

    public SolutionPanel(final WorkspaceEntry we, final Solution solution, final ActionListener closeAction) {
        setBorder(SizeHelper.getEmptyBorder());
        setLayout(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL, TableLayout.PREFERRED},
                new double[]{TableLayout.PREFERRED, TableLayout.FILL}));

        JLabel commentLabel = new JLabel();
        if (solution.getComment() != null) {
            commentLabel.setBorder(SizeHelper.getEmptyBorder(false, true));
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

        JPanel tracePanel = new JPanel(new BorderLayout());
        tracePanel.setBorder(SizeHelper.getEmptyBorder(false, true));
        tracePanel.add(new JScrollPane(traceText), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        JButton playButton = new JButton("Play");
        playButton.addActionListener(event -> {
            MpsatUtils.playSolution(we, solution);
            closeAction.actionPerformed(null);
        });

        buttonsPanel.add(playButton);

        add(commentLabel, new TableLayoutConstraints(0, 0));
        add(tracePanel, new TableLayoutConstraints(0, 1));
        add(buttonsPanel, new TableLayoutConstraints(1, 1));
    }

    private int getRowCount(Solution solution) {
        return solution.getBranchTrace() == null ? 1 : 2;
    }

    private int getColumnCount(Solution solution) {
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
