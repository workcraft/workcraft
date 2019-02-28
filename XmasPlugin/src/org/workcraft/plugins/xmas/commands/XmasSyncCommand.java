package org.workcraft.plugins.xmas.commands;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.workcraft.commands.Command;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.ForkComponent;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.JoinComponent;
import org.workcraft.plugins.xmas.components.MergeComponent;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SinkComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;
import org.workcraft.plugins.xmas.components.SyncComponent;
import org.workcraft.plugins.xmas.components.VisualForkComponent;
import org.workcraft.plugins.xmas.components.VisualFunctionComponent;
import org.workcraft.plugins.xmas.components.VisualJoinComponent;
import org.workcraft.plugins.xmas.components.VisualMergeComponent;
import org.workcraft.plugins.xmas.components.VisualQueueComponent;
import org.workcraft.plugins.xmas.components.VisualSinkComponent;
import org.workcraft.plugins.xmas.components.VisualSourceComponent;
import org.workcraft.plugins.xmas.components.VisualSwitchComponent;
import org.workcraft.plugins.xmas.components.VisualSyncComponent;
import org.workcraft.plugins.xmas.components.XmasContact;
import org.workcraft.utils.Hierarchy;
import org.workcraft.workspace.WorkspaceEntry;
import org.workcraft.utils.WorkspaceUtils;

public class XmasSyncCommand implements Command {

    @Override
    public String getDisplayName() {
        return "Configure Sync";
    }

    @Override
    public String getSection() {
        return "Sync";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualXmas.class);
    }

    int cntSyncnodes = 0;
    JFrame mainFrame = null;
    JComboBox combob = null;

    public void dispose() {
        mainFrame.setVisible(false);
    }

    private static class Sync {
        String name1;
        String name2;
        String name3;
        String l1;
        String l2;
        String typ;
        int gr1;
        int gr2;
        String g1;
        String g2;

        Sync(String s1, String s2, String s3, String s4, int gr, int cno) {
            name1 = s1;
            name2 = s2;
            name3 = s3;
            if (s4.equals("o")) {  //new
                if (cno == 0) {
                    l1 = s4;
                } else if (cno == 1) {
                    l1 = "b";
                } else if (cno == 2) {
                    l1 = "a";
                }
                gr1 = gr;
            } else {
                if (cno == 0) {
                    l2 = s4;
                } else if (cno == 1) {
                    l2 = "b";
                } else if (cno == 2) {
                    l2 = "a";
                }
                gr2 = gr;
            }
        }
    }

    public List<String> slist = new ArrayList<>();
    static List<Sync> synclist = new ArrayList<>();

    private static int checksynclist(String str) {
        for (Sync s : synclist) {
            if (s.name1.equals(str)) {
                return 0;
            }
        }
        return 1;
    }

    private static void storeSname(String str, String str2, int gr, int cno) {
        for (Sync s : synclist) {
            if (s.name1.equals(str)) {
                s.name3 = str2;
                if (cno == 0) {
                    s.l2 = "i";
                } else if (cno == 1) {
                    s.l2 = "b";
                } else if (cno == 2) {
                    s.l2 = "a";
                }
                s.gr2 = gr;
            }
        }
    }

    private static void storeSname2(String str, String str2, int gr, int cno) {
        for (Sync s : synclist) {
            if (s.name1.equals(str)) {
                s.name2 = str2;
                if (cno == 0) {
                    s.l1 = "o";
                } else if (cno == 1) {
                    s.l1 = "b";
                } else if (cno == 2) {
                    s.l1 = "a";
                }
                s.gr1 = gr; //new
            }
        }
    }

    public void updatesynclist() {
        int no = 0;
        for (Sync s : synclist) {
            if (slist.get(no).equals("asynchronous")) {
                s.typ = "a";
            } else if (slist.get(no).equals("mesochronous")) {
                s.typ = "m";
            } else if (slist.get(no).equals("pausible")) {
                s.typ = "p";
            }
            s.g1 = slist1.get(no);
            s.g2 = slist2.get(no);
            no++;
        }
    }

    public void writesynclist() {
        File syncFile = XmasSettings.getTempVxmSyncFile();
        PrintWriter writerS = null;
        try {
            writerS = new PrintWriter(syncFile);
            for (Sync s : synclist) {
                String str;
                str = s.name1.replace("Sync", "Qs");
                str = s.name1.replace("sync", "Qs");
                String rstr2;
                rstr2 = s.name2;
                rstr2 = rstr2.replace(rstr2.charAt(0), Character.toUpperCase(rstr2.charAt(0)));
                String rstr3;
                rstr3 = s.name3;
                rstr3 = rstr3.replace(rstr3.charAt(0), Character.toUpperCase(rstr3.charAt(0)));
                System.out.println("//gensync2s " + str + " " + s.g1 + " " + s.g2 + " " + s.typ);
                writerS.println("//gensync2s " + str + " " + s.g1 + " " + s.g2 + " " + s.typ);
                System.out.println(rstr2 + " " + s.l1 + " " + rstr3 + " " + s.l2 + " " + "0");
                writerS.println(rstr2 + " " + s.l1 + " " + rstr3 + " " + s.l2 + " " + "0");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writerS != null) {
                writerS.close();
            }
        }
    }

    public void writeOutput() {
        //JPanel panelmain=mainFrame.getContentPane().get();

        slist.clear();
        for (Component con : mainFrame.getContentPane().getComponents()) {
            if (con instanceof JPanel) {
                JPanel jp = (JPanel) con;
                for (Component cn : jp.getComponents()) {
                    JPanel jp2 = (JPanel) cn;
                    for (Component cn2 : jp2.getComponents()) {
                        if (cn2 instanceof JComboBox) {
                            JComboBox cb = (JComboBox) cn2;
                            String str = cb.getSelectedItem().toString();
                            //System.out.println("Found " + str);
                            if (str.equals("asynchronous")) {
                                slist.add(new String("asynchronous"));
                            } else if (str.equals("mesochronous")) {
                                slist.add(new String("mesochronous"));
                            } else if (str.equals("pausible")) {
                                slist.add(new String("pausible"));
                            }
                        }
                    }
                }
            }
        }
    }

    public void storeFields() {
        slist1.clear();
        slist2.clear();
        for (Component con : mainFrame.getContentPane().getComponents()) {
            if (con instanceof JPanel) {
                JPanel jp = (JPanel) con;
                for (Component cn : jp.getComponents()) {
                    JPanel jp2 = (JPanel) cn;
                    int n = 1;
                    for (Component cn2 : jp2.getComponents()) {
                        if (cn2 instanceof JTextField) {
                            JTextField tf = (JTextField) cn2;
                            String str = tf.getText().toString();
                            if (n == 2) {
                                slist1.add(new String(str));
                            } else if (n == 3) {
                                slist2.add(new String(str));
                            }
                            n++;
                        }
                    }
                }
            }
        }
    }

    public void setFields() {
        for (Component con : mainFrame.getContentPane().getComponents()) {
            if (con instanceof JPanel) {
                JPanel jp = (JPanel) con;
                for (Component cn : jp.getComponents()) {
                    JPanel jp2 = (JPanel) cn;
                    int n = 1;
                    String sel = "";
                    for (Component cn2 : jp2.getComponents()) {
                        if (cn2 instanceof JComboBox) {
                            JComboBox cb = (JComboBox) cn2;
                            sel = (String) cb.getSelectedItem();
                        } else if (cn2 instanceof JTextField) {
                            JTextField tf = (JTextField) cn2;
                            if (sel.equals("mesochronous")) {
                                if (n == 2) {
                                    tf.setEnabled(false);
                                } else if (n == 3) {
                                    tf.setEnabled(false);
                                }
                            } else if (sel.equals("asynchronous")) {
                                if (n == 2) {
                                    tf.setEnabled(true);
                                } else if (n == 3) {
                                    tf.setEnabled(true);
                                }
                            } else if (sel.equals("pausible")) {
                                if (n == 2) {
                                    tf.setEnabled(true);
                                } else if (n == 3) {
                                    tf.setEnabled(true);
                                }
                            }
                            n++;
                        }
                    }
                }
            }
        }
    }

    int loaded = 0;
    VisualXmas vnet1;
    public List<Integer> grnums = new ArrayList<>();
    public List<Integer> grnums1 = new ArrayList<>();
    public List<Integer> grnums2 = new ArrayList<>();
    public List<String> slist1 = new ArrayList<>();
    public List<String> slist2 = new ArrayList<>();

    @Override
    public void run(WorkspaceEntry we) {
        System.out.println("Running tests");
        final VisualXmas vnet = WorkspaceUtils.getAs(we, VisualXmas.class);
        if (vnet != vnet1) {
            loaded = 0;
        }
        vnet1 = vnet;

        cntSyncnodes = 0;
        if (loaded == 0) {
            grnums = new ArrayList<Integer>();
            grnums1 = new ArrayList<Integer>();
            grnums2 = new ArrayList<Integer>();
            slist2 = new ArrayList<String>();
            slist2 = new ArrayList<String>();
        }
        setGroups(vnet);
        writeJson(vnet);

        String[] choices = {
            "asynchronous",
            "mesochronous",
            "pausible",
        };

        mainFrame = new JFrame("Configure Synchronisation");
        JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain, BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain, BoxLayout.PAGE_AXIS));

        System.out.println("loaded = " + loaded);
        List<JPanel> panellist = new ArrayList<>();
        for (int no = 0; no < cntSyncnodes; no = no + 1) {
            if (loaded == 0) slist1.add(new String("1"));
            if (loaded == 0) slist2.add(new String("1"));
            panellist.add(new JPanel());
            panellist.get(panellist.size() - 1).add(new JLabel(" Name" + no));
            panellist.get(panellist.size() - 1).add(new JTextField("Sync" + no));
            panellist.get(panellist.size() - 1).add(new JLabel(" Type "));
            panellist.get(panellist.size() - 1).add(combob = new JComboBox(choices));
            combob.addActionListener(event -> {
                JComboBox comboBox = (JComboBox) event.getSource();
                Object selected = comboBox.getSelectedItem();
                if (selected.toString().equals("mesochronous")) {
                    setFields();
                }
            });
            panellist.get(panellist.size() - 1).add(new JLabel(" ClkF1  "));
            panellist.get(panellist.size() - 1).add(new JTextField(slist1.get(no), 10));
            panellist.get(panellist.size() - 1).add(new JLabel(" ClkF2  "));
            panellist.get(panellist.size() - 1).add(new JTextField(slist2.get(no), 10));
        }
        loaded = 1;

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

        cancelButton.addActionListener(event -> dispose());

        okButton.addActionListener(event -> {
            int no = 1;
            dispose();
            writeOutput();
            storeFields();
            updatesynclist();
            writesynclist();
            String gp = "";

            no = 0;
            for (Sync s : synclist) {
                grnums1.set(no, s.gr1);
                grnums2.set(no, s.gr2);
                no++;
            }
            no = 0;
            for (Node node : vnet.getNodes()) {
                if (node instanceof VisualSyncComponent) {   //won't work for sync
                    VisualSyncComponent vsc1 = (VisualSyncComponent) node;
                    SyncComponent sc1 = vsc1.getReferencedSyncComponent();
                    System.out.println("Sync component " + "Sync" + no + " = " + slist.get(no));
                    System.out.println("group1 = " + grnums1.get(no) + " " + "group2 = " + grnums2.get(no));
                    System.out.println("Clk1 = " + slist1.get(no) + " " + "Clk2 = " + slist2.get(no));
                    String gp1 = slist1.get(no);
                    sc1.setGp1(gp1);
                    String gp2 = slist2.get(no);
                    sc1.setGp2(gp2);
                    String typ = slist.get(no);
                    sc1.setTyp(typ);
                    no++;  //shifted
                } else if (node instanceof VisualSourceComponent) {
                    VisualSourceComponent vsc2 = (VisualSourceComponent) node;
                    SourceComponent sc2 = vsc2.getReferencedSourceComponent();
                    int sno = sc2.getGr();
                    for (int i1 = 0; i1 < grnums1.size(); i1++) {
                        if (grnums1.get(i1) == sno) gp = slist1.get(i1);
                        if (grnums2.get(i1) == sno) gp = slist2.get(i1);
                    }
                    sc2.setGp(gp);
                } else if (node instanceof VisualQueueComponent) {
                    VisualQueueComponent vsc3 = (VisualQueueComponent) node;
                    QueueComponent sc3 = vsc3.getReferencedQueueComponent();
                    int qno = sc3.getGr();
                    for (int i2 = 0; i2 < grnums1.size(); i2++) {
                        if (grnums1.get(i2) == qno) gp = slist1.get(i2);
                        if (grnums2.get(i2) == qno) gp = slist2.get(i2);
                    }
                    sc3.setGp(gp);
                }
            }
        });
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void setGroups(final VisualXmas vnet) {
        int gno = 1;
        for (VisualGroup vg: Hierarchy.getDescendantsOfType(vnet.getRoot(), VisualGroup.class)) {
            for (VisualComponent vp: vg.getComponents()) {
                if (vp instanceof VisualSourceComponent) {
                    VisualSourceComponent vsc = (VisualSourceComponent) vp;
                    SourceComponent sc = vsc.getReferencedSourceComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualSinkComponent) {
                    VisualSinkComponent vsc = (VisualSinkComponent) vp;
                    SinkComponent sc = vsc.getReferencedSinkComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualFunctionComponent) {
                    VisualFunctionComponent vsc = (VisualFunctionComponent) vp;
                    FunctionComponent sc = vsc.getReferencedFunctionComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualQueueComponent) {
                    VisualQueueComponent vsc = (VisualQueueComponent) vp;
                    QueueComponent sc = vsc.getReferencedQueueComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualForkComponent) {
                    VisualForkComponent vsc = (VisualForkComponent) vp;
                    ForkComponent sc = vsc.getReferencedForkComponent();
                    sc.setGr(gno);
                    //System.out.println("Fork no = " + gno + " " + sc.getGr());
                } else if (vp instanceof VisualJoinComponent) {
                    VisualJoinComponent vsc = (VisualJoinComponent) vp;
                    JoinComponent sc = vsc.getReferencedJoinComponent();
                    sc.setGr(gno);
                    //System.out.println("Join no = " + gno + " " + sc.getGr());
                } else if (vp instanceof VisualSwitchComponent) {
                    VisualSwitchComponent vsc = (VisualSwitchComponent) vp;
                    SwitchComponent sc = vsc.getReferencedSwitchComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualMergeComponent) {
                    VisualMergeComponent vsc = (VisualMergeComponent) vp;
                    MergeComponent sc = vsc.getReferencedMergeComponent();
                    sc.setGr(gno);
                }
            }
            gno++;
        }
    }

    private void writeJson(final VisualXmas vnet) {
        //GEN JSON
        File jsonFile = XmasSettings.getTempVxmJsonFile();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(jsonFile);
            List<VisualComponent> vcomps = new ArrayList<>();
            List<VisualSyncComponent> vscomps = new ArrayList<>();

            for (Node node : vnet.getNodes()) {
                if (node instanceof VisualSyncComponent) {
                    cntSyncnodes++;
                    vscomps.add((VisualSyncComponent) node);
                    if (loaded == 0) grnums1.add(0);
                    if (loaded == 0) grnums2.add(0);
                }
            }

            //Finds all components inside groups
            int grnum = 1;
            for (VisualGroup vg: Hierarchy.getDescendantsOfType(vnet.getRoot(), VisualGroup.class)) {
                for (VisualComponent vp: vg.getComponents()) {
                    vcomps.add(vp);
                    if (loaded == 0) grnums.add(grnum);
                }
                grnum++;
            }

            synclist.clear();
            //Finds all sync connections + groups
            Collection<VisualConnection> lvc = ((VisualGroup) vnet.getRoot()).getConnections();
            for (VisualConnection vc: lvc) {
                VisualNode vc1 = vc.getFirst();
                VisualNode vc2 = vc.getSecond();
                Node vn1 = vc1.getParent();
                Node vn2 = vc2.getParent();

                if (vn2 instanceof VisualSyncComponent) {   //vn2
                    if (vn1 instanceof VisualFunctionComponent) {
                        writeJsonInFunction(vnet, (VisualFunctionComponent) vn1);
                    } else if (vn1 instanceof VisualQueueComponent) {
                        writeJsonInQueue(vnet, (VisualQueueComponent) vn1);
                    } else if (vn1 instanceof VisualSwitchComponent) {
                        writeJsonInSwitch(vnet, (VisualSwitchComponent) vn1);
                    } else if (vn1 instanceof VisualMergeComponent) {
                        writeJsonInMerge(vnet, (VisualMergeComponent) vn1);
                    } else if (vn1 instanceof VisualForkComponent) {
                        writeJsonInFork(vnet, (VisualForkComponent) vn1);
                    } else if (vn1 instanceof VisualJoinComponent) {
                        writeJsonInJoin(vnet, (VisualJoinComponent) vn1);
                    }
                } else if (vn1 instanceof VisualSyncComponent) {    //vn1
                    if (vn2 instanceof VisualFunctionComponent) {
                        writeJsonOutFunction(vnet, (VisualFunctionComponent) vn2);
                    } else if (vn2 instanceof VisualQueueComponent) {
                        writeJsonOutQueue(vnet, (VisualQueueComponent) vn2);
                    } else if (vn2 instanceof VisualSwitchComponent) {
                        writeJsonOutSwitch(vnet, (VisualSwitchComponent) vn2);
                    } else if (vn2 instanceof VisualMergeComponent) {
                        writeJsonOutMerge(vnet, (VisualMergeComponent) vn2);
                    } else if (vn2 instanceof VisualForkComponent) {
                        writeJsonOutFork(vnet, (VisualForkComponent) vn2);
                    } else if (vn2 instanceof VisualJoinComponent) {
                        writeJsonOutJoin(vnet, (VisualJoinComponent) vn2);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void writeJsonInFunction(final VisualXmas vnet, final VisualFunctionComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final FunctionComponent sc = vsc.getReferencedFunctionComponent();
        for (XmasContact contactNode2 : sc.getOutputs()) {
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getSecond().getParent();
                    if (checksynclist(cnet.getName(cpNode)) == 1) {
                        synclist.add(new Sync(cnet.getName(cpNode), cnet.getName(sc), "", "o", sc.getGr(), 0));
                    } else {
                        storeSname2(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                    }
                }
            }
        }
    }

    private void writeJsonInQueue(final VisualXmas vnet, final VisualQueueComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final QueueComponent sc = vsc.getReferencedQueueComponent();
        for (XmasContact contactNode2 : sc.getOutputs()) {
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getSecond().getParent();
                    if (checksynclist(cnet.getName(cpNode)) == 1) {
                        synclist.add(new Sync(cnet.getName(cpNode), cnet.getName(sc), "", "o", sc.getGr(), 0));
                    } else {
                        storeSname2(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                    }
                }
            }
        }
    }

    private void writeJsonInSwitch(final VisualXmas vnet, final VisualSwitchComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final SwitchComponent sc = vsc.getReferencedSwitchComponent();
        for (XmasContact contactNode2 : sc.getOutputs()) {
            int cno = 0;
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getSecond().getParent();
                    cno++;
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            synclist.add(new Sync(cnet.getName(cpNode), cnet.getName(sc), "", "o", sc.getGr(), cno));
                        } else {
                            storeSname2(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                        }
                    }
                }
            }
        }
    }

    private void writeJsonInMerge(final VisualXmas vnet, final VisualMergeComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final MergeComponent sc = vsc.getReferencedMergeComponent();
        for (XmasContact contactNode2 : sc.getOutputs()) {
            int cno = 0;
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getSecond().getParent();
                    cno++;
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            synclist.add(new Sync(cnet.getName(cpNode), cnet.getName(sc), "", "o", sc.getGr(), cno));
                        } else {
                            storeSname2(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                        }
                    }
                }
            }
        }
    }

    private void writeJsonInFork(final VisualXmas vnet, final VisualForkComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final ForkComponent sc = vsc.getReferencedForkComponent();
        for (XmasContact contactNode2: sc.getOutputs()) {
            int cno = 0;
            for (Connection c: cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getSecond().getParent();
                    cno++;
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            synclist.add(new Sync(cnet.getName(cpNode), cnet.getName(sc), "", "o", sc.getGr(), cno));
                        } else {
                            storeSname2(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                        }
                    }
                }
            }
        }
    }

    private void writeJsonInJoin(final VisualXmas vnet, final VisualJoinComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final JoinComponent sc = vsc.getReferencedJoinComponent();
        for (XmasContact contactNode2 : sc.getOutputs()) {
            int cno = 0;
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getSecond().getParent();
                    cno++;
                    if (checksynclist(cnet.getName(cpNode)) == 1) {
                        synclist.add(new Sync(cnet.getName(cpNode), cnet.getName(sc), "", "o", sc.getGr(), cno));
                    } else {
                        storeSname2(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                    }
                }
            }
        }
    }

    private void writeJsonOutFunction(final VisualXmas vnet, final VisualFunctionComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final FunctionComponent sc = vsc.getReferencedFunctionComponent();
        for (XmasContact contactNode2 : sc.getInputs()) {
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getFirst().getParent();
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            synclist.add(new Sync(cnet.getName(cpNode), "", cnet.getName(sc), "i", sc.getGr(), 0));
                        } else {
                            storeSname(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                        }
                    }
                }
            }
        }
    }

    private void writeJsonOutQueue(final VisualXmas vnet, final VisualQueueComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final QueueComponent sc = vsc.getReferencedQueueComponent();
        for (XmasContact contactNode2 : sc.getInputs()) {
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getFirst().getParent();
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            synclist.add(new Sync(cnet.getName(cpNode), "", cnet.getName(sc), "i", sc.getGr(), 0));
                        } else {
                            storeSname(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                        }
                    }
                }
            }
        }
    }

    private void writeJsonOutSwitch(final VisualXmas vnet, final VisualSwitchComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final SwitchComponent sc = vsc.getReferencedSwitchComponent();
        for (XmasContact contactNode2 : sc.getInputs()) {
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getFirst().getParent();
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            synclist.add(new Sync(cnet.getName(cpNode), "", cnet.getName(sc), "i", sc.getGr(), 0));
                        } else {
                            storeSname(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                        }
                    }
                }
            }
        }
    }

    private void writeJsonOutMerge(final VisualXmas vnet, final VisualMergeComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final MergeComponent sc = vsc.getReferencedMergeComponent();
        int cno = 0;
        int cno2 = 0;
        for (XmasContact contactNode2 : sc.getInputs()) {
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getFirst().getParent();
                    cno++;
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            if (cno == 1) {
                                cno2 = 2;
                            } else if (cno == 2) {
                                cno2 = 1;
                            }
                            synclist.add(new Sync(cnet.getName(cpNode), "", cnet.getName(sc), "i", sc.getGr(), cno2));
                        } else {
                            if (cno == 1) {
                                cno2 = 2;
                            } else if (cno == 2) {
                                cno2 = 1;
                            }
                            storeSname(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), cno2);
                        }
                    }
                }
            }
        }
    }

    private void writeJsonOutFork(final VisualXmas vnet, final VisualForkComponent vn) {
        final Xmas cnet = vnet.getMathModel();
        final ForkComponent sc = vn.getReferencedForkComponent();
        for (XmasContact contactNode2 : sc.getInputs()) {
            for (Connection c : cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getFirst().getParent();
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            synclist.add(new Sync(cnet.getName(cpNode), "", cnet.getName(sc), "i", sc.getGr(), 0));
                        } else {
                            storeSname(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), 0);
                        }
                    }
                }
            }
        }
    }

    private void writeJsonOutJoin(final VisualXmas vnet, final VisualJoinComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final JoinComponent sc = vsc.getReferencedJoinComponent();
        int cno = 0;
        int cno2 = 0;
        for (XmasContact contactNode2: sc.getInputs()) {
            for (Connection c: cnet.getConnections(contactNode2)) {
                if (c.getSecond() instanceof XmasContact) {
                    Node cpNode = c.getFirst().getParent();
                    cno++;
                    if (cnet.getName(cpNode).contains("Sync") || cnet.getName(cpNode).contains("sync")) {
                        if (checksynclist(cnet.getName(cpNode)) == 1) {
                            if (cno == 1) {
                                cno2 = 2;
                            } else if (cno == 2) {
                                cno2 = 1;
                            }
                            synclist.add(new Sync(cnet.getName(cpNode), "", cnet.getName(sc), "i", sc.getGr(), cno2));
                        } else {
                            if (cno == 1) {
                                cno2 = 2;
                            } else if (cno == 2) {
                                cno2 = 1;
                            }
                            storeSname(cnet.getName(cpNode), cnet.getName(sc), sc.getGr(), cno2);
                        }
                    }
                }
            }
        }
    }

}
