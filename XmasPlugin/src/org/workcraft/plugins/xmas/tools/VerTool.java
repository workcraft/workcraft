package org.workcraft.plugins.xmas.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.workcraft.Framework;
import org.workcraft.Tool;
import org.workcraft.dom.Node;
import org.workcraft.gui.graph.GraphEditorPanel;
import org.workcraft.gui.graph.tools.AbstractTool;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.GraphEditor;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SyncComponent;
import org.workcraft.plugins.xmas.components.VisualQueueComponent;
import org.workcraft.plugins.xmas.components.VisualSyncComponent;
import org.workcraft.plugins.xmas.gui.SolutionsDialog1;
import org.workcraft.plugins.xmas.gui.SolutionsDialog2;
import org.workcraft.util.FileUtils;
import org.workcraft.util.LogUtils;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

public class VerTool extends AbstractTool implements Tool {

    @Override
    public String getSection() {
        return "Verification";
    }

    @Override
    public String getDisplayName() {
        return "Verification";
    }

    private static class Qslist {
        String name;
        int chk;

        Qslist(String s1, int n) {
            name = s1;
            chk = n;
        }
    }

    int cntSyncNodes = 0;
    JFrame mainFrame = null;
    static String level = "";
    static String display = "";
    static String highlight = "";
    static String soln = "";
    static List<Qslist> qslist = new ArrayList<>();

    public void dispose() {
        mainFrame.setVisible(false);
    }

    private static List<String> processArg(String file) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logErrorLine(e.getMessage());
        }
        String targ = "";
        String larg = "";
        String sarg = "";
        while (sc.hasNextLine()) {
            Scanner line = new Scanner(sc.nextLine());
            Scanner nxt = new Scanner(line.next());
            String check = nxt.next();
            String str;
            if (check.startsWith("trace")) {
                nxt = new Scanner(line.next());
                targ = "-t";
                targ = targ + nxt.next();
            } else if (check.startsWith("level")) {
                nxt = new Scanner(line.next());
                larg = "-v";
                str = nxt.next();
                level = str;
                if (str.equals("normal")) {
                    //System.out.println("Read v1");
                    larg = "-v1";
                } else if (str.equals("advanced")) {
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
                soln = str;
                sarg = "-s" + str;
            }
        }
        ArrayList<String> args = new ArrayList<>();
        if (!targ.isEmpty()) args.add(targ);
        if (!larg.isEmpty()) args.add(larg);
        if (!sarg.isEmpty()) args.add(sarg);
        return args;
    }

    private static String processLoc(String file) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logErrorLine(e.getMessage());
        }
        String str = "";
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            //System.out.println(sc.next());
            str = str + line + '\n';
        }
        return str;
    }

    private static void processQsl(String file) {
        qslist.clear();
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logErrorLine(e.getMessage());
        }
        while (sc.hasNextLine()) {
            Scanner line = new Scanner(sc.nextLine());
            Scanner nxt = new Scanner(line.next());
            String check = nxt.next();
            nxt = new Scanner(line.next());
            String str = nxt.next();
            int num = Integer.parseInt(str);
            //System.out.println("qsl " + check + " " + str + " " + num);
            qslist.add(new Qslist(check, num));
        }
    }

    private static String processEq(String file) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logErrorLine(e.getMessage());
        }
        String str = "";
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            //System.out.println(sc.next());
            str = str + line + '\n';
        }
        return str;
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
                vqc.setForegroundColor(Color.black);
            } else if (node instanceof VisualSyncComponent) {
                vsc = (VisualSyncComponent) node;
                vsc.setForegroundColor(Color.black);
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
                        qc = vqc.getReferencedQueueComponent();
                        //if (xnet.getName(qc).contains(st)) {
                        String rstr;
                        rstr = xnet.getName(qc);
                        rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                        if (rstr.equals(st)) {
                            vqc.setForegroundColor(Color.red);
                        }
                    } else if (node instanceof VisualSyncComponent) {
                        vsc = (VisualSyncComponent) node;
                        sc = vsc.getReferencedSyncComponent();
                        //if (xnet.getName(qc).contains(st)) {
                        String rstr;
                        rstr = xnet.getName(sc);
                        rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                        if (rstr.equals(st)) {
                            vsc.setForegroundColor(Color.red);
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
            //if (st.startsWith("Q")) {
            if (st.contains("->")) {
                //System.out.println("testst" + st);
                typ = 0;
                for (String st2 : st.split("->")) {
                    str = st2;
                    //System.out.println("str===" + str);
                    for (Node node : vnet.getNodes()) {
                        if (node instanceof VisualQueueComponent) {
                            vqc = (VisualQueueComponent) node;
                            qc = vqc.getReferencedQueueComponent();
                            //System.out.println("x===" + xnet.getName(qc));
                            String rstr;
                            rstr = xnet.getName(qc);
                            rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                            if (rstr.equals(str) && typ == 0) {
                                vqc.setForegroundColor(Color.pink);
                            }
                        } else if (node instanceof VisualSyncComponent) {
                            vsc = (VisualSyncComponent) node;
                            sc = vsc.getReferencedSyncComponent();
                            //System.out.println("strrr===" + str + ' ' + xnet.getName(sc));
                            String rstr;
                            rstr = xnet.getName(sc);
                            rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                            if (rstr.equals(str) && typ == 0) {
                                vsc.setForegroundColor(Color.pink);
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
                            qc = vqc.getReferencedQueueComponent();
                            String rstr;
                            rstr = xnet.getName(qc);
                            rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                            if (rstr.equals(str) && typ == 1) {
                                vqc.setForegroundColor(Color.red);
                            }
                        } else if (node instanceof VisualSyncComponent) {
                            vsc = (VisualSyncComponent) node;
                            sc = vsc.getReferencedSyncComponent();
                            String rstr;
                            rstr = xnet.getName(sc);
                            rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                            if (rstr.equals(str) && typ == 1) {
                                vsc.setForegroundColor(Color.red);
                            }
                        }
                    }
                }
            }

            //}
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
                        qc = vqc.getReferencedQueueComponent();
                        String rstr;
                        rstr = xnet.getName(qc);
                        rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                        if (rstr.equals(ql.name)) {
                            vqc.setForegroundColor(Color.green);
                        }
                    } else if (node instanceof VisualSyncComponent) {
                        vsc = (VisualSyncComponent) node;
                        sc = vsc.getReferencedSyncComponent();
                        String rstr;
                        rstr = xnet.getName(sc);
                        rstr = rstr.replace(rstr.charAt(0), Character.toUpperCase(rstr.charAt(0)));
                        if (rstr.equals(ql.name)) {
                            vsc.setForegroundColor(Color.green);
                        }
                    }
                }
            }
        }
    }

    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, Xmas.class);
    }

    GraphEditorPanel editor1;
    Graphics2D g;

    public void run(final WorkspaceEntry we) {
        System.out.println("Verifying Model");

        Xmas xnet = (Xmas) we.getModelEntry().getMathModel();
        VisualXmas vnet = (VisualXmas) we.getModelEntry().getVisualModel();

        try {
            File cpnFile = XmasSettings.getTempVxmCpnFile();
            File inFile = XmasSettings.getTempVxmInFile();
            FileUtils.copyFile(cpnFile, inFile);

            ArrayList<String> vxmCommand = new ArrayList<>();
            vxmCommand.add(XmasSettings.getTempVxmCommandFile().getAbsolutePath());
            vxmCommand.addAll(processArg(XmasSettings.getTempVxmVsettingsFile().getAbsolutePath()));
            ExternalProcessTask.printCommandLine(vxmCommand);
            Process vxmProcess = Runtime.getRuntime().exec(vxmCommand.toArray(new String[vxmCommand.size()]));

            String s, str = "";
            InputStreamReader inputStreamReader = new InputStreamReader(vxmProcess.getInputStream());
            BufferedReader stdInput = new BufferedReader(inputStreamReader);
            int n = 0;
            int test = -1;
            initHighlight(xnet, vnet);
            while ((s = stdInput.readLine()) != null) {
                if (test == -1) test = checkType(s);
                if (n > 0) str = str + s + '\n';
                n++;
                System.out.println(s);
            }
            if (level.equals("advanced")) {
                System.out.println("LEVEL IS ADVANCED ");
                File qslFile = XmasSettings.getTempVxmQslFile();
                processQsl(qslFile.getAbsolutePath());

                File equFile = XmasSettings.getTempVxmEquFile();
                str = processEq(equFile.getAbsolutePath());
            } else if (level.equals("normal") && test == 2) {
                System.out.println("LEVEL IS NORMAL ");
                File locFile = XmasSettings.getTempVxmLocFile();
                str = processLoc(locFile.getAbsolutePath());
            }
            if (test > 0) {
                if (display.equals("popup")) {
                    if (!level.equals("advanced")) {
                        new SolutionsDialog1(test, str);
                    } else {
                        new SolutionsDialog2(test, str);
                    }
                }
                if (test == 2) {
                    if (highlight.equals("local")) {
                        localHighlight(str, xnet, vnet);
                    } else if (highlight.equals("rel")) {
                        relHighlight(str, xnet, vnet);
                        //System.out.println("str = " + str);
                        activeHighlight(xnet, vnet);
                    }
                }
            } else if (test == 0) {
                if (display.equals("popup")) {
                    String message = "The system is deadlock-free.";
                    JOptionPane.showMessageDialog(null, message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //final SolutionsDialog solutionsDialog = new SolutionsDialog("hello", null);
        //GUI.centerAndSizeToParent(solutionsDialog, we.getFramework().getMainWindow());
        //solutionsDialog.setVisible(true);
        final Framework framework = Framework.getInstance();
        for (GraphEditorPanel e : framework.getMainWindow().getEditors(we)) {
            editor1 = e;
            g = (Graphics2D) e.getGraphics();
            for (int i = 0; i < 25; i++) {
                //GUI.drawEditorMessage(editor1, g, Color.RED, "hello");
            }
        }
    }

    @Override
    public String getLabel() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Decorator getDecorator(GraphEditor editor) {
        // TODO Auto-generated method stub
        return null;
    }

}
