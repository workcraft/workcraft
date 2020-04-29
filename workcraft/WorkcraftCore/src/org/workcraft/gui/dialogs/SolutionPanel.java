package org.workcraft.gui.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;
import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.traces.Solution;
import org.workcraft.traces.Trace;
import org.workcraft.utils.GuiUtils;
import org.workcraft.utils.TraceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

@SuppressWarnings("serial")
public class SolutionPanel extends JPanel {

    private static final int MIN_COLUMN_COUNT = 20;
    private static final int MAX_COLUMN_COUNT = 60;

    public SolutionPanel(final WorkspaceEntry we, final Solution solution, final ActionListener closeAction) {
        setBorder(GuiUtils.getEmptyBorder());
        setLayout(GuiUtils.createTableLayout(
                new double[]{TableLayout.FILL, TableLayout.PREFERRED},
                new double[]{TableLayout.PREFERRED, TableLayout.FILL}));

        JLabel commentLabel = new JLabel();
        if (solution.getComment() != null) {
            commentLabel.setBorder(GuiUtils.getEmptyLeftRightBorder());
            commentLabel.setText(solution.getComment());
        }

        JTextArea traceText = new JTextArea(getRowCount(solution), getColumnCount(solution));
        traceText.setMargin(SizeHelper.getTextMargin());
        String solutionString = solution.toString();
        traceText.setText(solutionString);
        if (TraceUtils.EMPTY_TEXT.equals(solutionString)) {
            traceText.setEnabled(false);
        } else {
            traceText.setEditable(false);
        }

        JPanel tracePanel = new JPanel(new BorderLayout());
        tracePanel.setBorder(GuiUtils.getEmptyLeftRightBorder());
        tracePanel.add(new JScrollPane(traceText), BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        JButton playButton = new JButton("Play");
        playButton.addActionListener(event -> {
            TraceUtils.playSolution(we, solution);
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
