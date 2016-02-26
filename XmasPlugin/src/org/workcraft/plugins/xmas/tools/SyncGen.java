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

import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.components.SyncComponent;
import org.workcraft.plugins.xmas.components.VisualSyncComponent;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class SyncGen implements Tool {

    //private final Framework framework;

    //VisualCircuit circuit;
    //private CheckCircuitTask checkTask;

    //ProgressMonitor<? super MpsatChainResult> monitor;

    private static boolean printoutput = true;

    public String getDisplayName() {
        return "Generate Sync";
    }

    public String getSection() {
        return "Sync";
    }

    int cntSyncNodes = 0;
    JFrame mainFrame = null;

    public List<JRadioButton> rlist = new ArrayList<>();

    private class RadioListener implements ActionListener {

        private JTextField textField;

        public void actionPerformed(ActionEvent e) {
            JRadioButton button = (JRadioButton) e.getSource();

            // Set enabled based on button text (you can use whatever text you prefer)
            for (JRadioButton r : rlist) {
                if (r == button) {
                    r.setSelected(true);
                } else {
                    r.setSelected(false);
                }
            }
        }
    }

    public void createD(char c, char ck) {
        System.out.println("d" + c + "_1 " + " = " + "not " + "d" + c);
        System.out.println("d" + c + "_2 " + " = " + "not (" + "d" + c + "_1 & cd" + ck + ")");
        System.out.println("d" + c + "_3 " + " = " + "not (" + "d" + c + " & cd" + ck + ")");
        System.out.println("d" + c + "_q " + " = " + "not (" + "d" + c + "_2 & d" + c + "_q_)");
        System.out.println("d" + c + "_q_ " + " = " + "not (" + "d" + c + "_3 & d" + c + "_q)");
    }

    public void createSynca() {
        System.out.println("asynchronous wrapper");
        createD('a', 't');
        createD('b', 't');
        createD('c', 't');
        createD('d', 't');
        System.out.println("da1" + " = " + "db_q");
        System.out.println("dc1" + " = " + "dd_q");
        System.out.println("n1" + " = " + "WR | da_q");
        System.out.println("a1" + " = " + "not n1 & dc_q");
        createD('e', 'r');
        createD('f', 'r');
        createD('g', 'r');
        createD('h', 'r');
        System.out.println("de1" + " = " + "df_q");
        System.out.println("dg1" + " = " + "dh_q");
        System.out.println("n2" + " = " + "RD | de_q");
        System.out.println("a2" + " = " + "not n2 & dg_q");
    }

    public void createSyncm() {
        System.out.println("mesochronous wrapper");
        System.out.println("s1" + " = " + "not gclk");
        System.out.println("s2" + " = " + "not s1");
        System.out.println("t1" + " = " + "not gclk");
        System.out.println("t2" + " = " + "not t1");
        System.out.println("d1" + "_1 " + " = " + "not " + "d1");
        System.out.println("d1" + "_2 " + " = " + "not (" + "d1" + "_1 & cd1" + ")");
        System.out.println("d1" + "_3 " + " = " + "not (" + "d1" + " & cd1" + ")");
        System.out.println("d1" + "_q " + " = " + "not (" + "d1" + "_2 & d1" + "_q_)");
        System.out.println("d1" + "_q_ " + " = " + "not (" + "d1" + "_3 & d1" + "_q)");
        System.out.println("d2" + "_1 " + " = " + "not " + "d2");
        System.out.println("d2" + "_2 " + " = " + "not (" + "d2" + "_1 & cd2" + ")");
        System.out.println("d2" + "_3 " + " = " + "not (" + "d2" + " & cd2" + ")");
        System.out.println("d2" + "_q " + " = " + "not (" + "d2" + "_2 & d2" + "_q_)");
        System.out.println("d2" + "_q_ " + " = " + "not (" + "d2" + "_3 & d2" + "_q)");
        System.out.println("txtinit" + " = " + "s2 & reset");
        System.out.println("rxtinit" + " = " + "t2 & d2q & reset");
        System.out.println("t1" + " = " + "not gclk");
        System.out.println("t2" + " = " + "not t1");
        System.out.println("s3" + " = " + "not (txtinit & s2)");
        System.out.println("WR" + " = " + "not s3");
        System.out.println("a1" + " = " + "rxtinit & clk2");
    }

    public void createSyncp() {
        System.out.println("pausible wrapper");
        System.out.println("d1" + "_1 " + " = " + "not " + "d1");
        System.out.println("d1" + "_2 " + " = " + "not (" + "d1" + "_1 & cd1" + ")");
        System.out.println("d1" + "_3 " + " = " + "not (" + "d1" + " & cd1" + ")");
        System.out.println("d1" + "_q " + " = " + "not (" + "d1" + "_2 & d1" + "_q_)");
        System.out.println("d1" + "_q_ " + " = " + "not (" + "d1" + "_3 & d1" + "_q)");
        System.out.println("ia1" + " = " + "d1q");
        System.out.println("aa1" + " = " + "d1q & cdt");
        System.out.println("oa1" + " = " + "ia1 | cdt | WA");
        System.out.println("ia2" + " = " + "not cdt");
        System.out.println("ia3" + " = " + "not ia2");
        System.out.println("ia4" + " = " + "not ia3");
        System.out.println("naa1" + " = " + "not (oa1 & ia4)");
        System.out.println("cdt" + " = " + "not naa1");
        System.out.println("d2" + "_1 " + " = " + "not " + "d2");
        System.out.println("d2" + "_2 " + " = " + "not (" + "d2" + "_1 & cd2" + ")");
        System.out.println("d2" + "_3 " + " = " + "not (" + "d2" + " & cd2" + ")");
        System.out.println("d2" + "_q " + " = " + "not (" + "d2" + "_2 & d2" + "_q_)");
        System.out.println("d2" + "_q_ " + " = " + "not (" + "d2" + "_3 & d2" + "_q)");
        System.out.println("ib1" + " = " + "d2q");
        System.out.println("ab1" + " = " + "d2q & cdr");
        System.out.println("ob1" + " = " + "ib1 | cdr | WA");
        System.out.println("ib2" + " = " + "not cdr");
        System.out.println("ib3" + " = " + "not ib2");
        System.out.println("ib4" + " = " + "not ib3");
        System.out.println("nab1" + " = " + "not (ob1 & ib4)");
        System.out.println("cdr" + " = " + "not nab1");

    }

    public void selectSync(int sel) {
        if (sel == 0) {
            createSynca();
        } else if (sel == 1) {
            createSyncm();
        } else if (sel == 2) {
            createSyncp();
        }
    }

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, Xmas.class);
    }

    public void run(WorkspaceEntry we) {

        //VisualXmas vnet = (VisualXmas) we.getModelEntry().getVisualModel();
        //Xmas cnet = (Xmas) we.getModelEntry().getMathModel();

        VisualXmas vnet = (VisualXmas) we.getModelEntry().getVisualModel();

        System.out.println("");
        //System.out.println("Generating circuit");

        List<SyncComponent> scomps = new ArrayList<>();
        for (Node node : vnet.getNodes()) {
            if (node instanceof VisualSyncComponent) {
                cntSyncNodes++;
                VisualSyncComponent vsc = (VisualSyncComponent) node;
                SyncComponent sc = vsc.getReferencedSyncComponent();
                scomps.add(sc);
            }
        }
        JFrame mainFrame = new JFrame("Configure Synchronisation");
        JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain, BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain, BoxLayout.PAGE_AXIS));
        List<JPanel> panellist = new ArrayList<>();
        JRadioButton jr;
        for (int no = 0; no < cntSyncNodes; no = no + 1) {
            //if (loaded == 0) slist1.add(new String("1"));
            //if (loaded == 0) slist2.add(new String("1"));
            panellist.add(new JPanel());
            panellist.get(panellist.size() - 1).add(new JLabel(" Name" + no));
            panellist.get(panellist.size() - 1).add(new JTextField("Sync" + no));
            panellist.get(panellist.size() - 1).add(new JLabel(" Type "));
            panellist.get(panellist.size() - 1).add(jr = new JRadioButton(scomps.get(no).typ));
            if (no == 0) jr.setSelected(true);
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
                //dispose();
            }

        });

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                int no = 0, sel = 0;

                for (JRadioButton r : rlist) {
                    if (r.isSelected()) {
                        System.out.println("SELECTED=" + r.getText());
                        if (r.getText().equals("asynchronous")) {
                            sel = 0;
                        } else if (r.getText().equals("mesochronous")) {
                            sel = 1;
                        } else if (r.getText().equals("pausible")) {
                            sel = 2;
                        }
                        //sel=no;
                        selectSync(sel);
                    }
                    no++;
                }
                //try {
                //generator = new STGConv(sel);
                //} catch (InvalidConnectionException e1) {
                // TODO Auto-generated catch block
                //e1.printStackTrace();
                //}

            }
        });

        mainFrame.pack();
        mainFrame.setVisible(false);

    }

}
