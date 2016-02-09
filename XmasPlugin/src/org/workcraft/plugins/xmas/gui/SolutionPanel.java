package org.workcraft.plugins.xmas.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


@SuppressWarnings("serial")
public class SolutionPanel extends JPanel {
    private JPanel buttonsPanel;
    private JTextArea sText;

    public SolutionPanel(final String str, final ActionListener closeAction) {

        sText = new JTextArea();
        sText.setColumns(50);
        sText.setLineWrap(true);
        sText.setText(str);

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(sText);

        buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));

        JButton saveButton = new JButton("Save");

        JButton playButton = new JButton("Play trace");
        playButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*final WorkspaceEntry we = task.getWorkspaceEntry();
                final MainWindow mainWindow = task.getFramework().getMainWindow();
                GraphEditorPanel currentEditor = mainWindow.getCurrentEditor();
                if(currentEditor == null || currentEditor.getWorkspaceEntry() != we)
                {
                    final List<GraphEditorPanel> editors = mainWindow.getEditors(we);
                    if(editors.size()>0) {
                        currentEditor = editors.get(0);
                        mainWindow.requestFocus(currentEditor);
                    }
                    else {
                        currentEditor = mainWindow.createEditorWindow(we);
                    }
                }
                final ToolboxPanel toolbox = currentEditor.getToolBox();
                final PetriNetSimulationTool tool = toolbox.getToolInstance(PetriNetSimulationTool.class);
                tool.setTrace(t);
                toolbox.selectTool(tool);
                closeAction.actionPerformed(null);*/
            }
        });

        //buttonsPanel.add(saveButton);
        //buttonsPanel.add(playButton);


        add(scrollPane, "0 0");
        add(buttonsPanel, "1 0");
    }
}
