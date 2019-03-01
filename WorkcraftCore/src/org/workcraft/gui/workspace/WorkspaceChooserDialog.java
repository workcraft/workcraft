package org.workcraft.gui.workspace;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.workcraft.types.Func;
import org.workcraft.utils.GuiUtils;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

import info.clearthought.layout.TableLayout;

@SuppressWarnings("serial")
public class WorkspaceChooserDialog extends JDialog {
    private final Workspace workspace;
    private final Func<Path<String>, Boolean> filter;

    public WorkspaceChooserDialog(Window parent, String title, Workspace workspace, Func<Path<String>, Boolean> filter) {
        super(parent, title, ModalityType.APPLICATION_MODAL);
        this.workspace = workspace;
        this.filter = filter;

        this.setContentPane(createContents());
    }

    private Container createContents() {

        double[][] sizes = {
            {TableLayout.FILL},
            {TableLayout.FILL, TableLayout.PREFERRED},
        };

        JPanel contents = new JPanel(new TableLayout(sizes));
        WorkspaceChooser chooser = new WorkspaceChooser(workspace, filter);
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton runButton = GuiUtils.createDialogButton("OK");
        runButton.addActionListener(event -> setVisible(false));

        JButton cancelButton = GuiUtils.createDialogButton("Cancel");
        cancelButton.addActionListener(event -> setVisible(false));

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(runButton);

        contents.add(chooser, "0 0");
        contents.add(buttonsPanel, "0 1");

        return contents;
    }

    public List<WorkspaceEntry> choose() {
        LinkedList<WorkspaceEntry> result = new LinkedList<>();

        setVisible(true);

        return result;
    }
}
