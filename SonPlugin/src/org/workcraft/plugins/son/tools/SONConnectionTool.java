package org.workcraft.plugins.son.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;

import org.workcraft.Framework;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.graph.tools.ConnectionTool;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.son.VisualSON;
import org.workcraft.plugins.son.connections.SONConnection.Semantics;
import org.workcraft.plugins.son.elements.VisualBlock;

public class SONConnectionTool extends ConnectionTool {

    private Semantics semantic = Semantics.PNLINE;

    @Override
    public void updateToolbar(JToolBar toolbar, final GraphEditor editor) {
        JRadioButton polyButton = new JRadioButton("Causal Connection");
        polyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                semantic = Semantics.PNLINE;
            }
        });
        toolbar.add(polyButton);

        JRadioButton bhvButton = new JRadioButton("Behavioural Abstraction");
        bhvButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                semantic = Semantics.BHVLINE;
            }
        });
        toolbar.add(bhvButton);

        polyButton.setSelected(true);
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
