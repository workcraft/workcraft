package org.workcraft.plugins.xmas.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;
import org.workcraft.util.LogUtils;

public class PNetExt {

    //private final Framework framework;

    //VisualCircuit circuit;
    //private CheckCircuitTask checkTask;

    private static boolean printoutput = true;

    private static class Source {

        String name1;
        String name2;

        Source(String s1, String s2) {
            name1 = s1;
            name2 = s2;
        }
    }

    private static class Switch {

        String name1;
        String name2;

        Switch(String s1, String s2) {
            name1 = s1;
            name2 = s2;
        }
    }

    private static class Merge {

        String name1;
        String name2;

        Merge(String s1, String s2) {
            name1 = s1;
            name2 = s2;
        }
    }

    private static class Fun {

        String name1;

        Fun(String s1) {
            name1 = s1;
        }
    }

    static List<Source> sourcelist = new ArrayList<>();
    static List<Switch> switchlist = new ArrayList<>();
    static List<Merge> mergelist = new ArrayList<>();
    static List<Fun> funlist = new ArrayList<>();

    private static void initlist() {
        funlist.clear();
        sourcelist.clear();
        switchlist.clear();
        mergelist.clear();
    }

    private static void readFile(String file, int syncflag) {
        String typ = null;
        String g1 = null;
        String g2 = null;
        ArrayList storeWordList = new ArrayList();
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) { //Catch exception if any
            LogUtils.logErrorLine(e.getMessage());
        }
        String name;
        int num;
        while (sc.hasNextLine()) {
            Scanner line = new Scanner(sc.nextLine());
            Scanner nxt = new Scanner(line.next());
            String check = nxt.next();
            if (check.startsWith("//gen")) {
                if (check.startsWith("//gensource")) {
                    nxt = new Scanner(line.next());
                    name = nxt.next();
                    sourcelist.add(new Source(name, name));
                    typ = "Source";
                } else if (check.startsWith("//genfunction")) {
                    nxt = new Scanner(line.next());
                    name = nxt.next();
                    funlist.add(new Fun(name));
                    typ = "Function";
                } else if (check.startsWith("//genmerge")) {
                    nxt = new Scanner(line.next());
                    name = nxt.next();
                    mergelist.add(new Merge(name, name));
                    typ = "Merge";
                } else if (check.startsWith("//genswitch")) {
                    nxt = new Scanner(line.next());
                    name = nxt.next();
                    switchlist.add(new Switch(name, name));
                    typ = "Switch";
                }
            }
        }
    }

    private static void writeNet(PrintWriter writer, Collection<SourceComponent> srcNodes, Collection<FunctionComponent> funNodes, Collection<SwitchComponent> swNodes) {
        writer.println("TS");
        int no = 0;
        for (SourceComponent srcNode : srcNodes) {
            String ls = "";
            switch (srcNode.getMode()) {
            case MODE_0:
                ls = "d";
                //writer.println("d");
                break;
            case MODE_1:
                ls = "t";
                //writer.println("t");
                break;
            case MODE_2:
                ls = "n";
                //writer.println("n");
                break;
            }
            writer.println(sourcelist.get(no).name1 + "\"" + srcNode.getType() + "\"" + ls);
            no++;
        }
        writer.println("FN");
        no = 0;
        for (FunctionComponent funNode : funNodes) {
            writer.println(funlist.get(no).name1 + "\"" + funNode.getType() + "\"");       //changed from below
            no++;
        }
        writer.println("ST");
        /*for (Switch sw : switchlist) {
        //writer.println(sw.name1 + "\"" + "t");
        writer.println(sw.name1 + "\"" + sw.getType());
        }*/
        no = 0;
        for (SwitchComponent swNode : swNodes) {
            writer.println(switchlist.get(no).name1 + "\"" + swNode.getType() + "\"" + swNode.getVal());
            no++;
        }
        writer.println("MT");
        for (Merge mrg : mergelist) {
            writer.println(mrg.name1 + "\"" + mrg.name2);
        }
        System.out.print("Output written to CPNFile");
    }

    public PNetExt(Collection<SourceComponent> srcNodes, Collection<FunctionComponent> funNodes, Collection<SwitchComponent> swNodes, int syncflag) {
        initlist();
        File pncFile = XmasSettings.getTempVxmPncFile();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(pncFile);
            File cpnFile = XmasSettings.getTempVxmCpnFile();
            readFile(cpnFile.getAbsolutePath(), syncflag);
            writeNet(writer, srcNodes, funNodes, swNodes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        System.out.println("");
    }

}
