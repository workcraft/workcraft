package org.workcraft.gui.workspace;

import info.clearthought.layout.TableLayout;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import org.workcraft.util.Func;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

@SuppressWarnings("serial")
public class WorkspaceChooserDialog extends JDialog {
    private WorkspaceChooser chooser;
    private JPanel buttonsPanel;
    private JButton cancelButton;
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
                {TableLayout.FILL },
                {TableLayout.FILL, TableLayout.PREFERRED },
        };

        JPanel contents = new JPanel(new TableLayout(sizes));

        chooser = new WorkspaceChooser(workspace, filter);

        buttonsPanel = new JPanel (new FlowLayout(FlowLayout.RIGHT));

        JButton runButton = new JButton ("OK");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //modalResult = 1;
                setVisible(false);
            }
        });

        cancelButton = new JButton ("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //modalResult = 0;
                setVisible(false);
            }
        });

        buttonsPanel.add(cancelButton);
        buttonsPanel.add(runButton);

        contents.add(chooser, "0 0");
        contents.add(buttonsPanel, "0 1");

        return contents;
    }

    public List<WorkspaceEntry> choose() {
        LinkedList<WorkspaceEntry> result = new LinkedList<WorkspaceEntry>();

        setVisible(true);

        return result;
    }
}
