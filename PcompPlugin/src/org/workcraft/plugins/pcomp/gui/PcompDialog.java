package org.workcraft.plugins.pcomp.gui;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

import org.workcraft.Framework;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.trees.TreeWindow;
import org.workcraft.gui.workspace.Path;
import org.workcraft.gui.workspace.WorkspaceChooser;
import org.workcraft.plugins.pcomp.tasks.PcompTask.ConversionMode;
import org.workcraft.plugins.stg.StgWorkspaceFilter;

@SuppressWarnings("serial")
public class PcompDialog extends JDialog {
    protected boolean result;
    private WorkspaceChooser chooser;
    private Set<Path<String>> sourcePaths;
    private JCheckBox showInEditor;
    private JRadioButton leaveOutputs;
    private JRadioButton internalize;
    private JRadioButton dummify;
    private JCheckBox sharedOutputs;
    private JCheckBox improvedPcomp;

    public PcompDialog(Window owner) {
        super(owner, "Parallel composition", ModalityType.DOCUMENT_MODAL);
        final JPanel content = createContents();
        setContentPane(content);
    }

    public Set<Path<String>> getSourcePaths() {
        return sourcePaths;
    }

    public boolean showInEditor() {
        return showInEditor.isSelected();
    }

    public boolean isSharedOutputsChecked() {
        return sharedOutputs.isSelected();
    }

    public boolean isImprovedPcompChecked() {
        return improvedPcomp.isSelected();
    }

    public ConversionMode getMode() {
        if (leaveOutputs.isSelected()) {
            return ConversionMode.OUTPUT;
        }
        if (internalize.isSelected()) {
            return ConversionMode.INTERNAL;
        }
        if (dummify.isSelected()) {
            return ConversionMode.DUMMY;
        }
        throw new NotSupportedException("No button is selected. Cannot proceed.");
    }

    public boolean run() {
        setVisible(true);
        return result;
    }

    private JPanel createContents() {
        double[][] sizes = {
                {TableLayout.FILL, TableLayout.PREFERRED },
                {TableLayout.FILL, TableLayout.PREFERRED },
        };

        final JPanel content = new JPanel(new TableLayout(sizes));
        final Framework framework = Framework.getInstance();
        chooser = new WorkspaceChooser(framework.getWorkspace(), new StgWorkspaceFilter());
        chooser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Source STGs"),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        chooser.setCheckBoxMode(TreeWindow.CheckBoxMode.LEAF);
        content.add(chooser, "0 0 0 1");

        showInEditor = new JCheckBox();
        showInEditor.setText("Show result in editor");
        showInEditor.setSelected(true);

        JPanel outputOptions = new JPanel();
        outputOptions.setLayout(new BoxLayout(outputOptions, BoxLayout.Y_AXIS));
        outputOptions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Outputs"),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));

        leaveOutputs = new JRadioButton("Leave as outputs");
        internalize = new JRadioButton("Make internal");
        dummify = new JRadioButton("Make dummy");
        leaveOutputs.setSelected(true);

        ButtonGroup outputsGroup = new ButtonGroup();
        outputsGroup.add(leaveOutputs);
        outputsGroup.add(dummify);
        outputsGroup.add(internalize);

        outputOptions.add(leaveOutputs);
        outputOptions.add(internalize);
        outputOptions.add(dummify);

        sharedOutputs = new JCheckBox("Allow the STGs to share outputs");
        improvedPcomp = new JCheckBox("No computational interference");

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Options"),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        options.add(showInEditor, 0);
        options.add(outputOptions, 1);
        options.add(sharedOutputs, 2);
        options.add(improvedPcomp, 3);

        content.add(options, "1 0");
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAction();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelAction();
            }
        });

        buttonsPanel.add(runButton);
        buttonsPanel.add(cancelButton);

        content.add(buttonsPanel, "1 1");

        getRootPane().registerKeyboardAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        runAction();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        getRootPane().registerKeyboardAction(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancelAction();
                    }
                },
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        return content;
    }

    private void runAction() {
        result = true;
        sourcePaths = chooser.getCheckedNodes();
        setVisible(false);
    }

    private void cancelAction() {
        result = false;
        setVisible(false);
    }

}
