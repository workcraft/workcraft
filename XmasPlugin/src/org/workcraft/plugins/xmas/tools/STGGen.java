package org.workcraft.plugins.xmas.tools;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.exceptions.InvalidConnectionException;
import org.workcraft.gui.workspace.Path;
import org.workcraft.plugins.stg.StgDescriptor;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.components.SyncComponent;
import org.workcraft.plugins.xmas.components.VisualSyncComponent;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.ModelEntry;
import org.workcraft.workspace.Workspace;
import org.workcraft.workspace.WorkspaceEntry;

public class STGGen implements Tool {

    private List<JRadioButton> rlist = new ArrayList<JRadioButton>();

    private class RadioListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            JRadioButton button = (JRadioButton) e.getSource();

            // Set enabled based on button text (you can use whatever text you prefer)
            for (JRadioButton r : rlist) {
                if(r==button) {
                    r.setSelected(true);
                } else {
                    r.setSelected(false);
                }
            }
        }
    }

    @Override
    public String getSection() {
        return "Sync";
    }

    @Override
    public String getDisplayName() {
        return "Generate STG";
    }

    int cntSyncNodes=0;
    JFrame mainFrame = null;

    public void dispose() {
        mainFrame.setVisible(false);
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, Xmas.class);
    }

    @Override
    public void run(final WorkspaceEntry we) {
        System.out.println("Generating Circuit");

        VisualXmas vnet = (VisualXmas)we.getModelEntry().getVisualModel();
        List<SyncComponent> scomps = new ArrayList<SyncComponent>();
        for (Node node : vnet.getNodes()) {
            if(node instanceof VisualSyncComponent) {
                cntSyncNodes++;
                VisualSyncComponent vsc=(VisualSyncComponent)node;
                SyncComponent sc=vsc.getReferencedSyncComponent();
                scomps.add(sc);
            }
        }

        JFrame mainFrame = new JFrame("Configure Synchronisation");
        JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain,BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain,BoxLayout.PAGE_AXIS));
        List<JPanel> panellist = new ArrayList<JPanel>();
        JRadioButton jr;
        for(int no = 0; no < cntSyncNodes; no = no+1) {
            //if(loaded==0) slist1.add(new String("1"));
            //if(loaded==0) slist2.add(new String("1"));
            panellist.add(new JPanel());
            panellist.get(panellist.size()-1).add(new JLabel(" Name" + no));
            panellist.get(panellist.size()-1).add(new JTextField("Sync" + no));
            panellist.get(panellist.size()-1).add(new JLabel(" Type "));
            panellist.get(panellist.size()-1).add(jr = new JRadioButton(scomps.get(no).typ));
            if(no==0) jr.setSelected(true);
            rlist.add(jr);
            RadioListener listener = new RadioListener();
            jr.addActionListener(listener);
        }
        for (JPanel plist : panellist) {
            panelmain.add(plist);
        }

        JPanel panelb = new JPanel();
        panelb.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton okButton = new JButton("OK");
        panelb.add(Box.createHorizontalGlue());
        panelb.add(cancelButton);
        panelb.add(okButton);
        panelmain.add(panelb);

        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }

        });

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                int no=0, sel=0;

                for (JRadioButton r : rlist) {
                    if(r.isSelected()) {
                        if(r.getText().equals("asynchronous")) {
                            sel=0;
                        } else if(r.getText().equals("mesochronous")) {
                            sel=1;
                        } else if(r.getText().equals("pausible")) {
                            sel=2;
                        }
                        //sel=no;
                    }
                    no++;
                }
                STGConv generator = null;
                try {
                    generator = new STGConv(sel);
                } catch (InvalidConnectionException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                final Framework framework = Framework.getInstance();
                final Workspace workspace = framework.getWorkspace();
                final Path<String> directory = we.getWorkspacePath().getParent();
                String desiredName="STG";
                final ModelEntry me = new ModelEntry(new StgDescriptor(), generator.getSTG());
                workspace.add(directory, desiredName, me, false, true);
            }
        });

        mainFrame.pack();
        mainFrame.setVisible(true);

    }

}
