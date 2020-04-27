package org.workcraft.gui.dialogs;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.traces.Solution;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class ReachibilityDialog extends JDialog {

    public ReachibilityDialog(Window owner, WorkspaceEntry we, String title,
            String message, Solution solution) {
        this(owner, we, title, message, Collections.singletonList(solution));
    }

    public ReachibilityDialog(Window owner, WorkspaceEntry we, String title,
            String message, List<Solution> solutions) {

        JLabel messageLabel = new JLabel(message);
        messageLabel.setBorder(SizeHelper.getGapBorder());

        JPanel solutionsPanel = new JPanel(new GridLayout(solutions.size(), 1,
                SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));

        solutionsPanel.setBorder(SizeHelper.getEmptyBorder());
        for (Solution solution : solutions) {
            SolutionPanel solutionPanel = new SolutionPanel(we, solution, event -> setVisible(false));
            solutionsPanel.add(solutionPanel);
        }

        JPanel scrollPanel = new JPanel(new BorderLayout());
        scrollPanel.setBorder(new EmptyBorder(0, SizeHelper.getLayoutHGap(), 0, SizeHelper.getLayoutHGap()));
        scrollPanel.add(new JScrollPane(solutionsPanel), BorderLayout.CENTER);

        JPanel buttonsPanel = GuiUtils.createDialogButtonsPanel();
        JButton closeButton = GuiUtils.createDialogButton("Close");
        getRootPane().setDefaultButton(closeButton);

        closeButton.addActionListener(event -> closeAction());

        getRootPane().registerKeyboardAction(event -> closeAction(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        buttonsPanel.add(closeButton);

        JPanel contentPanel = new JPanel(new BorderLayout(SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        contentPanel.add(messageLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPanel, BorderLayout.CENTER);
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setTitle(title);
        setContentPane(contentPanel);
        setMinimumSize(new Dimension(400, 233));
        setModal(true);
        pack();
        setLocationRelativeTo(owner);
    }

    private void closeAction() {
        this.setVisible(false);
    }

    public boolean reveal() {
        setVisible(true);
        return true;
    }

}
