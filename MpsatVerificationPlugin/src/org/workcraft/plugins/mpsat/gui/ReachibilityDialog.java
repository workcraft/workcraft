package org.workcraft.plugins.mpsat.gui;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.mpsat.tasks.Solution;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

@SuppressWarnings("serial")
public class ReachibilityDialog extends JDialog {

    public ReachibilityDialog(Window owner, WorkspaceEntry we, String title, String message, List<Solution> solutions) {
        JPanel solutionsPanel = new JPanel(new GridLayout(solutions.size(), 1,
                SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));

        solutionsPanel.setBorder(SizeHelper.getEmptyBorder());
        for (Solution solution : solutions) {
            SolutionPanel panel = new SolutionPanel(we, solution, event -> setVisible(false));

            solutionsPanel.add(panel);
        }
        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(solutionsPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = GuiUtils.createDialogButton("Close");
        getRootPane().setDefaultButton(closeButton);

        closeButton.addActionListener(event -> actionClose());

        getRootPane().registerKeyboardAction(event -> actionClose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        buttonsPanel.add(closeButton);

        JPanel contents = new JPanel(new BorderLayout(SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));
        contents.add(new JLabel(message), BorderLayout.NORTH);
        contents.add(scrollPane, BorderLayout.CENTER);
        contents.add(buttonsPanel, BorderLayout.SOUTH);

        setTitle(title);
        setContentPane(contents);
        setMinimumSize(new Dimension(400, 200));
        setModal(true);
        pack();
        setLocationRelativeTo(owner);
    }

    private void actionClose() {
        this.setVisible(false);
    }

    public boolean reveal() {
        setVisible(true);
        return true;
    }

}
