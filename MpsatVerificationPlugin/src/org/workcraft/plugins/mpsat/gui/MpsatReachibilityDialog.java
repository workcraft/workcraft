package org.workcraft.plugins.mpsat.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.workcraft.dom.visual.SizeHelper;
import org.workcraft.plugins.mpsat.tasks.MpsatSolution;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class MpsatReachibilityDialog extends JDialog {

    public MpsatReachibilityDialog(WorkspaceEntry we, String title, String message, List<MpsatSolution> solutions) {
        JPanel solutionsPanel = new JPanel(new GridLayout(solutions.size(), 1,
                SizeHelper.getLayoutHGap(), SizeHelper.getLayoutVGap()));

        solutionsPanel.setBorder(SizeHelper.getEmptyBorder());
        for (MpsatSolution solution : solutions) {
            MpsatSolutionPanel panel = new MpsatSolutionPanel(we, solution, event -> setVisible(false));

            solutionsPanel.add(panel);
        }
        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(solutionsPanel);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = GuiUtils.createDialogButton("Close");
        getRootPane().setDefaultButton(closeButton);

        closeButton.addActionListener(event -> close());

        getRootPane().registerKeyboardAction(event -> close(),
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
    }

    private void close() {
        this.setVisible(false);
    }

}
