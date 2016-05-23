package org.workcraft.plugins.son.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.VisualBlock;

public class SONConnectionTool extends ConnectionTool {

    protected JPanel interfacePanel;

    private Semantics semantic = Semantics.PNLINE;

    @Override
    public void createInterfacePanel(final GraphEditor editor) {
        super.createInterfacePanel(editor);

        interfacePanel = new JPanel();
        interfacePanel.setLayout(new BoxLayout(interfacePanel, BoxLayout.Y_AXIS));

        JRadioButton polyButton = new JRadioButton("Causal Connection");
        polyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                semantic = Semantics.PNLINE;
            }
        });

        JRadioButton asynButton = new JRadioButton("A/Syn Communication");
        asynButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                semantic = Semantics.ASYNLINE;
            }
        });

        JRadioButton synButton = new JRadioButton("Synchronous Communication");
        synButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                semantic = Semantics.SYNCLINE;
            }
        });

        JRadioButton bhvButton = new JRadioButton("Behavioural Abstraction");
        bhvButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                semantic = Semantics.BHVLINE;
            }
        });

        polyButton.setSelected(true);

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(polyButton);
        buttonGroup.add(bhvButton);

        interfacePanel.add(polyButton);
        interfacePanel.add(bhvButton);
    }

    @Override
    public JPanel getInterfacePanel() {
        return interfacePanel;
    }

    @Override
    public void mousePressed(GraphEditorMouseEvent e) {
        final Framework framework = Framework.getInstance();
        MainWindow mainWindow = framework.getMainWindow();
        VisualSON vson = (VisualSON) e.getModel();

        vson.forceConnectionSemantics(semantic);
        //forbid to connect with collapsed block (bound).
        if (currentNode instanceof VisualBlock) {
            JOptionPane.showMessageDialog(mainWindow,
                    "Connect with atomic block is not valid",
                    "Cannot create connection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        super.mousePressed(e);
    }

    @Override
    public void deactivated(GraphEditor editor) {
        semantic = Semantics.PNLINE;
        super.deactivated(editor);
    }

}
