package org.workcraft.plugins.xmas.tools;

import org.workcraft.commands.Command;
import org.workcraft.dom.Node;
import org.workcraft.gui.tools.AbstractGraphEditorTool;
import org.workcraft.gui.tools.Decorator;
import org.workcraft.gui.tools.GraphEditor;
import org.workcraft.interop.ExternalProcess;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SyncComponent;
import org.workcraft.plugins.xmas.components.VisualQueueComponent;
import org.workcraft.plugins.xmas.components.VisualSyncComponent;
import org.workcraft.plugins.xmas.gui.SolutionsDialog1;
import org.workcraft.plugins.xmas.gui.SolutionsDialog2;
import org.workcraft.utils.*;
import org.workcraft.workspace.WorkspaceEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class XmasAnalysisTool extends AbstractGraphEditorTool implements Command {

    private static class Qslist {
        public final String name;
        public final int chk;

        Qslist(String s1, int n) {
            name = s1;
            chk = n;
        }
    }

    private int index = 1;
    private JFrame mainFrame = null;
    private static String level = "";
    private static String display = "";
    private static String highlight = "";
    private static final List<Qslist> qslist = new ArrayList<>();

    @Override
    public String getDisplayName() {
        return "Analysis";
    }

    @Override
    public Section getSection() {
        return new Section("Verification");
    }

    public void dispose() {
        mainFrame.setVisible(false);
    }

    private static List<String> processArg(String file, int index) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logError(e.getMessage());
        }
        String targ = "";
        String larg = "";
        String sarg = "";
        String aarg = "";
        while (sc.hasNextLine()) {
            Scanner line = new Scanner(sc.nextLine());
            Scanner nxt = new Scanner(line.next());
            String check = nxt.next();
            String str;
            if (check.startsWith("trace")) {
                nxt = new Scanner(line.next());
                targ = "-t";
                targ += nxt.next();
            } else if (check.startsWith("level")) {
                nxt = new Scanner(line.next());
                larg = "-v";
                str = nxt.next();
                level = str;
                if ("normal".equals(str)) {
                    //System.out.println("Read v1");
                    larg = "-v1";
                } else if ("advanced".equals(str)) {
                    //System.out.println("Read v2");
                    larg = "-v2";
                }
            } else if (check.startsWith("display")) {
                nxt = new Scanner(line.next());
                str = nxt.next();
                //System.out.println("strrr=" + str);
                display = str;
            } else if (check.startsWith("highlight")) {
                nxt = new Scanner(line.next());
                str = nxt.next();
                //System.out.println("strrr=" + str);
                highlight = str;
            } else if (check.startsWith("soln")) {
                nxt = new Scanner(line.next());
                str = nxt.next();
                //System.out.println("solnnnnnnnnnnnnnnnnn=" + str);
                sarg = "-s" + str;
            }
        }
        //System.out.println("aaaaaaaaaaaindex==============" + index);
        aarg = "-a" + index;
        //System.out.println("aaaaaaaaaaaaaaarggggg=" + aarg);
        ArrayList<String> args = new ArrayList<>();
        if (!targ.isEmpty()) args.add(targ);
        if (!larg.isEmpty()) args.add(larg);
        if (!sarg.isEmpty()) args.add(sarg);
        if (!aarg.isEmpty()) args.add(aarg);
        return args;
    }

    private static String processLoc(String file) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logError(e.getMessage());
            return null;
        }
        StringBuilder str = new StringBuilder();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            //System.out.println(sc.next());
            str.append(line);
            str.append('\n');
        }
        return str.toString();
    }

    private static void processQsl(String file) {
        qslist.clear();
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logError(e.getMessage());
            return;
        }
        while (sc.hasNextLine()) {
            Scanner line = new Scanner(sc.nextLine());
            Scanner nxt = new Scanner(line.next());
            String check = nxt.next();
            nxt = new Scanner(line.next());
            String str = nxt.next();
            int num = Integer.parseInt(str);
            //System.out.println("qsl " + check + ' ' + str + ' ' + num);
            qslist.add(new Qslist(check, num));
        }
    }

    private static String processEq(String file) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logError(e.getMessage());
            return null;
        }
        StringBuilder str = new StringBuilder();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            //System.out.println(sc.next());
            str.append(line);
            str.append('\n');
        }
        return str.toString();
    }

    public int checkType(String s) {
        if (s.contains("DEADLOCK FREE")) {
            return 0;
        } else if (s.contains("TRACE FOUND")) {
            return 1;
        } else if (s.contains("Local")) {
            return 2;
        }
        return -1;
    }

    public void initHighlight(Xmas xnet, VisualXmas vnet) {
        VisualQueueComponent vqc;
        VisualSyncComponent vsc;
        for (Node node : vnet.getNodes()) {
            if (node instanceof VisualQueueComponent) {
                vqc = (VisualQueueComponent) node;
                vqc.setForegroundColor(Color.BLACK);
            } else if (node instanceof VisualSyncComponent) {
                vsc = (VisualSyncComponent) node;
                vsc.setForegroundColor(Color.BLACK);
            }
        }
    }

    public void localHighlight(String s, Xmas xnet, VisualXmas vnet) {
        QueueComponent qc;
        SyncComponent sc;
        VisualQueueComponent vqc;
        VisualSyncComponent vsc;

        //System.out.println("s=" + s);
        for (String st : s.split(" |\n")) {
            if (st.startsWith("Q") || st.startsWith("S")) {
                System.out.println(st);
                for (Node node : vnet.getNodes()) {
                    if (node instanceof VisualQueueComponent) {
                        vqc = (VisualQueueComponent) node;
                        qc = vqc.getReferencedComponent();
                        //if (xnet.getName(qc).contains(st)) {
                        String rstr;
                        rstr = xnet.getName(qc);
                        rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                        if (rstr.equals(st)) {
                            vqc.setForegroundColor(Color.RED);
                        }
                    } else if (node instanceof VisualSyncComponent) {
                        vsc = (VisualSyncComponent) node;
                        sc = vsc.getReferencedComponent();
                        //if (xnet.getName(qc).contains(st)) {
                        String rstr;
                        rstr = xnet.getName(sc);
                        rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                        if (rstr.equals(st)) {
                            vsc.setForegroundColor(Color.RED);
                        }
                    }
                }
            }
        }
    }

    public void relHighlight(String s, Xmas xnet, VisualXmas vnet) {
        int typ = 0;
        String str = "";
        QueueComponent qc;
        SyncComponent sc;
        VisualQueueComponent vqc;
        VisualSyncComponent vsc;

        for (String st : s.split(" |;|\n")) {
            if (st.contains("->")) {
                //System.out.println("testst" + st);
                typ = 0;
                for (String st2 : st.split("->")) {
                    str = st2;
                    //System.out.println("str===" + str);
                    for (Node node : vnet.getNodes()) {
                        if (node instanceof VisualQueueComponent) {
                            vqc = (VisualQueueComponent) node;
                            qc = vqc.getReferencedComponent();
                            //System.out.println("x===" + xnet.getName(qc));
                            String rstr;
                            rstr = xnet.getName(qc);
                            rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                            if (rstr.equals(str) && typ == 0) {
                                vqc.setForegroundColor(Color.PINK);
                            }
                        } else if (node instanceof VisualSyncComponent) {
                            vsc = (VisualSyncComponent) node;
                            sc = vsc.getReferencedComponent();
                            //System.out.println("strrr===" + str + ' ' + xnet.getName(sc));
                            String rstr;
                            rstr = xnet.getName(sc);
                            rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                            if (rstr.equals(str) && typ == 0) {
                                vsc.setForegroundColor(Color.PINK);
                            }
                        }
                    }
                }
            } else if (st.contains("<-")) {
                //System.out.println("testst_" + st);
                typ = 1;
                for (String st2 : st.split("<-")) {
                    str = st2;
                    //System.out.println("str===" + str);
                    for (Node node : vnet.getNodes()) {
                        if (node instanceof VisualQueueComponent) {
                            vqc = (VisualQueueComponent) node;
                            qc = vqc.getReferencedComponent();
                            String rstr;
                            rstr = xnet.getName(qc);
                            rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                            if (rstr.equals(str) && typ == 1) {
                                vqc.setForegroundColor(Color.RED);
                            }
                        } else if (node instanceof VisualSyncComponent) {
                            vsc = (VisualSyncComponent) node;
                            sc = vsc.getReferencedComponent();
                            String rstr;
                            rstr = xnet.getName(sc);
                            rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                            if (rstr.equals(str) && typ == 1) {
                                vsc.setForegroundColor(Color.RED);
                            }
                        }
                    }
                }
            }
        }
    }

    public void activeHighlight(Xmas xnet, VisualXmas vnet) {
        QueueComponent qc;
        SyncComponent sc;
        VisualQueueComponent vqc;
        VisualSyncComponent vsc;

        for (Qslist ql : qslist) {
            if (ql.chk == 0) {
                for (Node node : vnet.getNodes()) {
                    if (node instanceof VisualQueueComponent) {
                        vqc = (VisualQueueComponent) node;
                        qc = vqc.getReferencedComponent();
                        String rstr;
                        rstr = xnet.getName(qc);
                        rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                        if (rstr.equals(ql.name)) {
                            vqc.setForegroundColor(Color.GREEN);
                        }
                    } else if (node instanceof VisualSyncComponent) {
                        vsc = (VisualSyncComponent) node;
                        sc = vsc.getReferencedComponent();
                        String rstr;
                        rstr = xnet.getName(sc);
                        rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                        if (rstr.equals(ql.name)) {
                            vsc.setForegroundColor(Color.GREEN);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xmas.class);
    }

    private static final List<JCheckBox> jcbn = new ArrayList<>();
    private JCheckBox jcblast;

    @SuppressWarnings("PMD.AssignmentInOperand")
    private void createPanel(List<JPanel> panellist, String file) {
        int no = 1;
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logError(e.getMessage());
        }
        JCheckBox jcb;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            Scanner lineSc = new Scanner(line);
            if (line.contains("SOLUTION")) {
                if (no > 1) {
                    panellist.get(panellist.size() - 1).add(jcb = new JCheckBox(""));
                    ItemListener itemListener = e -> {
                        if (e.getSource() instanceof JCheckBox sjcb) {
                            if (sjcb.isSelected()) index = jcbn.indexOf(sjcb) + 1;
                            if (sjcb.isSelected()) System.out.println("indexa==" + index);
                            if (jcblast != null) jcblast.setSelected(false);
                            jcblast = sjcb;
                        }
                    };
                    jcb.addItemListener(itemListener);
                    jcbn.add(jcb);
                }
                panellist.add(new JPanel());
                panellist.get(panellist.size() - 1).add(new JLabel(" Soln" + no + ": "));
                no++;
            } else if (line.contains("Qu")) {
                Scanner nxt = new Scanner(lineSc.next());
                String check = nxt.next();
                nxt = new Scanner(lineSc.next());
                panellist.get(panellist.size() - 1).add(new JLabel(check));
                panellist.get(panellist.size() - 1).add(new JTextField(nxt.next(), 1));
            }
            lineSc.close();
        }
        panellist.get(panellist.size() - 1).add(jcb = new JCheckBox(""));

        ItemListener itemListener = e -> {
            if (e.getSource() instanceof JCheckBox sjcb) {
                if (sjcb.isSelected()) index = jcbn.indexOf(sjcb) + 1;
                if (jcblast != null) jcblast.setSelected(false);
                jcblast = sjcb;
            }
        };
        jcb.addItemListener(itemListener);
        jcbn.add(jcb);
    }

    @Override
    public void run(WorkspaceEntry we) {
        System.out.println("Analysing Model");

        final VisualXmas vnet = WorkspaceUtils.getAs(we, VisualXmas.class);
        final Xmas xnet = WorkspaceUtils.getAs(we, Xmas.class);

        mainFrame = new JFrame("Analysis");
        JPanel panelmain = new JPanel();
        mainFrame.getContentPane().add(panelmain, BorderLayout.PAGE_START);
        panelmain.setLayout(new BoxLayout(panelmain, BoxLayout.PAGE_AXIS));
        List<JPanel> panellist = new ArrayList<>();

        JPanel panela = new JPanel();
        panela.setLayout(GuiUtils.createFlowLayout());
        panela.add(new JLabel(" UNIQUE SOLUTIONS "));
        panela.add(Box.createHorizontalGlue());
        panelmain.add(panela);

        jcbn.clear();
        File solnFile = XmasSettings.getTempVxmSolnFile();
        createPanel(panellist, solnFile.getAbsolutePath());
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

        mainFrame.pack();
        mainFrame.setVisible(true);

        cancelButton.addActionListener(event -> dispose());

        okButton.addActionListener(event -> {
            dispose();
            if (index != 0) {
                try {
                    File cpnFile = XmasSettings.getTempVxmCpnFile();
                    File inFile = XmasSettings.getTempVxmInFile();
                    FileUtils.copyFile(cpnFile, inFile);

                    ArrayList<String> vxmCommand = new ArrayList<>();
                    vxmCommand.add(XmasSettings.getTempVxmCommandFile().getAbsolutePath());
                    vxmCommand.addAll(processArg(XmasSettings.getTempVxmVsettingsFile().getAbsolutePath(), index));
                    ExternalProcess.printCommandLine(vxmCommand);
                    String[] cmdArray = vxmCommand.toArray(new String[0]);
                    Process vxmProcess = Runtime.getRuntime().exec(cmdArray, null, XmasSettings.getTempVxmDirectory());

                    String s;
                    StringBuilder str = new StringBuilder();
                    InputStreamReader inputStreamReader = new InputStreamReader(vxmProcess.getInputStream());
                    BufferedReader stdInput = new BufferedReader(inputStreamReader);
                    int n = 0;
                    int test = -1;
                    initHighlight(xnet, vnet);
                    while ((s = stdInput.readLine()) != null) {
                        //if (n == 1) test = checkType(s);
                        if (test == -1) test = checkType(s);
                        if (n > 0) {
                            str.append(s);
                            str.append('\n');
                        }
                        n++;
                        System.out.println(s);
                    }
                    if ("advanced".equals(level)) {
                        System.out.println("LEVEL IS ADVANCED ");
                        File qslFile = XmasSettings.getTempVxmQslFile();
                        processQsl(qslFile.getAbsolutePath());

                        File equFile = XmasSettings.getTempVxmEquFile();
                        str = new StringBuilder(processEq(equFile.getAbsolutePath())); //testing str assignment - fpb
                    } else if ("normal".equals(level) && (test == 2)) {
                        System.out.println("LEVEL IS NORMAL ");
                        File locFile = XmasSettings.getTempVxmLocFile();
                        str = new StringBuilder(processLoc(locFile.getAbsolutePath()));
                    }
                    if (test > 0) {
                        if ("popup".equals(display)) {
                            if (!"advanced".equals(level)) {
                                new SolutionsDialog1(test, str.toString());
                            } else {
                                new SolutionsDialog2(test, str.toString());
                            }
                        }
                        if (test == 2) {
                            if ("local".equals(highlight)) {
                                localHighlight(str.toString(), xnet, vnet);
                            } else if ("rel".equals(highlight)) {
                                relHighlight(str.toString(), xnet, vnet);
                                activeHighlight(xnet, vnet);
                            }
                        }
                    } else if (test == 0) {
                        if ("popup".equals(display)) {
                            DialogUtils.showInfo("The system is deadlock-free.");
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public Decorator getDecorator(GraphEditor editor) {
        return null;
    }

}
