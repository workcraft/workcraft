package org.workcraft.plugins.xmas.commands;

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
import org.workcraft.plugins.xmas.components.*;
import org.workcraft.utils.Hierarchy;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("rawtypes")
public class SynchronisationCommand implements Command {

    @Override
    public String getDisplayName() {
        return "Configure Sync";
    }

    @Override
    public Section getSection() {
        return new Section("Sync");
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, VisualXmas.class);
    }

    private int cntSyncnodes = 0;
    private JFrame mainFrame = null;

    public void dispose() {
        mainFrame.setVisible(false);
    }

    private static class Sync {
        public final String name1;
        public String name2;
        public String name3;
        public String l1;
        public String l2;
        public String typ;
        public int gr1;
        public int gr2;
        public String g1;
        public String g2;

        Sync(String s1, String s2, String s3, String s4, int gr, int cno) {
            name1 = s1;
            name2 = s2;
            name3 = s3;
            if ("o".equals(s4)) {  //new
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

    public final List<String> slist = new ArrayList<>();
    private static final List<Sync> synclist = new ArrayList<>();

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
            if ("asynchronous".equals(slist.get(no))) {
                s.typ = "a";
            } else if ("mesochronous".equals(slist.get(no))) {
                s.typ = "m";
            } else if ("pausible".equals(slist.get(no))) {
                s.typ = "p";
            }
            s.g1 = slist1.get(no);
            s.g2 = slist2.get(no);
            no++;
        }
    }

    public void writesynclist() {
        File syncFile = XmasSettings.getTempVxmSyncFile();
        try (PrintWriter writerS = new PrintWriter(syncFile)) {
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
                System.out.println("//gensync2s " + str + ' ' + s.g1 + ' ' + s.g2 + ' ' + s.typ);
                writerS.println("//gensync2s " + str + ' ' + s.g1 + ' ' + s.g2 + ' ' + s.typ);
                System.out.println(rstr2 + ' ' + s.l1 + ' ' + rstr3 + ' ' + s.l2 + ' ' + '0');
                writerS.println(rstr2 + ' ' + s.l1 + ' ' + rstr3 + ' ' + s.l2 + ' ' + '0');
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void writeOutput() {
        //JPanel panelmain=mainFrame.getContentPane().get();

        slist.clear();
        for (Component con : mainFrame.getContentPane().getComponents()) {
            if (con instanceof JPanel jp) {
                for (Component cn : jp.getComponents()) {
                    JPanel jp2 = (JPanel) cn;
                    for (Component cn2 : jp2.getComponents()) {
                        if (cn2 instanceof JComboBox cb) {
                            String str = cb.getSelectedItem().toString();
                            //System.out.println("Found " + str);
                            if ("asynchronous".equals(str)) {
                                slist.add("asynchronous");
                            } else if ("mesochronous".equals(str)) {
                                slist.add("mesochronous");
                            } else if ("pausible".equals(str)) {
                                slist.add("pausible");
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
            if (con instanceof JPanel jp) {
                for (Component cn : jp.getComponents()) {
                    JPanel jp2 = (JPanel) cn;
                    int n = 1;
                    for (Component cn2 : jp2.getComponents()) {
                        if (cn2 instanceof JTextField tf) {
                            String str = tf.getText();
                            if (n == 2) {
                                slist1.add(str);
                            } else if (n == 3) {
                                slist2.add(str);
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
            if (con instanceof JPanel jp) {
                for (Component cn : jp.getComponents()) {
                    JPanel jp2 = (JPanel) cn;
                    int n = 1;
                    String sel = "";
                    for (Component cn2 : jp2.getComponents()) {
                        if (cn2 instanceof JComboBox cb) {
                            sel = (String) cb.getSelectedItem();
                        } else if (cn2 instanceof JTextField tf) {
                            if ("mesochronous".equals(sel)) {
                                if (n == 2) {
                                    tf.setEnabled(false);
                                } else if (n == 3) {
                                    tf.setEnabled(false);
                                }
                            } else if ("asynchronous".equals(sel)) {
                                if (n == 2) {
                                    tf.setEnabled(true);
                                } else if (n == 3) {
                                    tf.setEnabled(true);
                                }
                            } else if ("pausible".equals(sel)) {
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

    private int loaded = 0;
    private VisualXmas vnet1;
    public List<Integer> grnums = new ArrayList<>();
    public List<Integer> grnums1 = new ArrayList<>();
    public List<Integer> grnums2 = new ArrayList<>();
    public List<String> slist1 = new ArrayList<>();
    public List<String> slist2 = new ArrayList<>();

    @SuppressWarnings("PMD.AssignmentInOperand")
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
            grnums = new ArrayList<>();
            grnums1 = new ArrayList<>();
            grnums2 = new ArrayList<>();
            slist2 = new ArrayList<>();
            slist2 = new ArrayList<>();
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
        for (int no = 0; no < cntSyncnodes; no++) {
            if (loaded == 0) slist1.add("1");
            if (loaded == 0) slist2.add("1");
            panellist.add(new JPanel());
            panellist.get(panellist.size() - 1).add(new JLabel(" Name" + no));
            panellist.get(panellist.size() - 1).add(new JTextField("Sync" + no));
            panellist.get(panellist.size() - 1).add(new JLabel(" Type "));
            JComboBox combob = null;
            panellist.get(panellist.size() - 1).add(combob = new JComboBox<>(choices));
            combob.addActionListener(event -> {
                JComboBox comboBox = (JComboBox) event.getSource();
                Object selected = comboBox.getSelectedItem();
                if ("mesochronous".equals(selected.toString())) {
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
                if (node instanceof VisualSyncComponent vsc1) {   //won't work for sync
                    SyncComponent sc1 = vsc1.getReferencedComponent();
                    System.out.println("Sync component Sync" + no + " = " + slist.get(no));
                    System.out.println("group1 = " + grnums1.get(no) + " group2 = " + grnums2.get(no));
                    System.out.println("Clk1 = " + slist1.get(no) + " Clk2 = " + slist2.get(no));
                    String gp1 = slist1.get(no);
                    sc1.setGp1(gp1);
                    String gp2 = slist2.get(no);
                    sc1.setGp2(gp2);
                    String typ = slist.get(no);
                    sc1.setTyp(typ);
                    no++;  //shifted
                } else if (node instanceof VisualSourceComponent vsc2) {
                    SourceComponent sc2 = vsc2.getReferencedComponent();
                    int sno = sc2.getGr();
                    for (int i1 = 0; i1 < grnums1.size(); i1++) {
                        if (grnums1.get(i1) == sno) gp = slist1.get(i1);
                        if (grnums2.get(i1) == sno) gp = slist2.get(i1);
                    }
                    sc2.setGp(gp);
                } else if (node instanceof VisualQueueComponent vsc3) {
                    QueueComponent sc3 = vsc3.getReferencedComponent();
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
                if (vp instanceof VisualSourceComponent vsc) {
                    SourceComponent sc = vsc.getReferencedComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualSinkComponent vsc) {
                    SinkComponent sc = vsc.getReferencedComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualFunctionComponent vsc) {
                    FunctionComponent sc = vsc.getReferencedComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualQueueComponent vsc) {
                    QueueComponent sc = vsc.getReferencedComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualForkComponent vsc) {
                    ForkComponent sc = vsc.getReferencedComponent();
                    sc.setGr(gno);
                    //System.out.println("Fork no = " + gno + ' ' + sc.getGr());
                } else if (vp instanceof VisualJoinComponent vsc) {
                    JoinComponent sc = vsc.getReferencedComponent();
                    sc.setGr(gno);
                    //System.out.println("Join no = " + gno + ' ' + sc.getGr());
                } else if (vp instanceof VisualSwitchComponent vsc) {
                    SwitchComponent sc = vsc.getReferencedComponent();
                    sc.setGr(gno);
                } else if (vp instanceof VisualMergeComponent vsc) {
                    MergeComponent sc = vsc.getReferencedComponent();
                    sc.setGr(gno);
                }
            }
            gno++;
        }
    }

    private void writeJson(final VisualXmas vnet) {
        //GEN JSON
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
        for (VisualGroup vg : Hierarchy.getDescendantsOfType(vnet.getRoot(), VisualGroup.class)) {
            for (VisualComponent vp : vg.getComponents()) {
                vcomps.add(vp);
                if (loaded == 0) grnums.add(grnum);
            }
            grnum++;
        }

        synclist.clear();
        //Finds all sync connections + groups
        Collection<VisualConnection> lvc = ((VisualGroup) vnet.getRoot()).getConnections();
        for (VisualConnection vc : lvc) {
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
    }

    private void writeJsonInFunction(final VisualXmas vnet, final VisualFunctionComponent vsc) {
        final Xmas cnet = vnet.getMathModel();
        final FunctionComponent sc = vsc.getReferencedComponent();
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
        final QueueComponent sc = vsc.getReferencedComponent();
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
        final SwitchComponent sc = vsc.getReferencedComponent();
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
        final MergeComponent sc = vsc.getReferencedComponent();
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
        final ForkComponent sc = vsc.getReferencedComponent();
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
        final JoinComponent sc = vsc.getReferencedComponent();
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
        final FunctionComponent sc = vsc.getReferencedComponent();
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
        final QueueComponent sc = vsc.getReferencedComponent();
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
        final SwitchComponent sc = vsc.getReferencedComponent();
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
        final MergeComponent sc = vsc.getReferencedComponent();
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
        final ForkComponent sc = vn.getReferencedComponent();
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
        final JoinComponent sc = vsc.getReferencedComponent();
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
