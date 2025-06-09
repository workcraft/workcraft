package org.workcraft.plugins.xmas.commands;

import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;
import org.workcraft.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class PNetExt {

    private static class Source {
        public final String name1;
        public final String name2;

        Source(String s1, String s2) {
            name1 = s1;
            name2 = s2;
        }
    }

    private static class Switch {
        public final String name1;
        public final String name2;

        Switch(String s1, String s2) {
            name1 = s1;
            name2 = s2;
        }
    }

    private static class Merge {
        public final String name1;
        public final String name2;

        Merge(String s1, String s2) {
            name1 = s1;
            name2 = s2;
        }
    }

    private static class Fun {
        public final String name1;

        Fun(String s1) {
            name1 = s1;
        }
    }

    private static final List<Source> sourcelist = new ArrayList<>();
    private static final List<Fun> funlist = new ArrayList<>();
    private static final List<Switch> switchlist = new ArrayList<>();
    private static final List<Merge> mergelist = new ArrayList<>();
    private static final List<Source> sinklist = new ArrayList<>();

    private static void initlist() {
        sourcelist.clear();
        funlist.clear();
        switchlist.clear();
        mergelist.clear();
        sinklist.clear();
    }

    private static void readFile(String file) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) { //Catch exception if any
            LogUtils.logError(e.getMessage());
        }
        String name;
        while (sc.hasNextLine()) {
            Scanner lineScanner = new Scanner(sc.nextLine());
            String check = getToken(lineScanner);
            if (check.startsWith("//gen")) {
                if (check.startsWith("//gensource")) {
                    name = getToken(lineScanner);
                    sourcelist.add(new Source(name, name));
                } else if (check.startsWith("//genfunction")) {
                    name = getToken(lineScanner);
                    funlist.add(new Fun(name));
                } else if (check.startsWith("//genmerge")) {
                    name = getToken(lineScanner);
                    mergelist.add(new Merge(name, name));
                } else if (check.startsWith("//genswitch")) {
                    name = getToken(lineScanner);
                    switchlist.add(new Switch(name, name));
                } else if (check.startsWith("//gensink")) {
                    name = getToken(lineScanner);
                    sinklist.add(new Source(name, name));
                }
            }
            lineScanner.close();
        }
    }

    private static String getToken(Scanner line) {
        Scanner scanner = new Scanner(line.next());
        String name = scanner.next();
        scanner.close();
        return name;
    }

    private static void writeNet(PrintWriter writer, Collection<SourceComponent> srcNodes, Collection<FunctionComponent> funNodes, Collection<SwitchComponent> swNodes) {
        writer.println("TS");
        int no = 0;
        for (SourceComponent srcNode : srcNodes) {
            String ls = switch (srcNode.getMode()) {
                case MODE_0 -> "d";
                case MODE_1 -> "t";
                case MODE_2 -> "n";
            };
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

    public PNetExt(Collection<SourceComponent> srcNodes, Collection<FunctionComponent> funNodes, Collection<SwitchComponent> swNodes) {

        initlist();
        File pncFile = XmasSettings.getTempVxmPncFile();
        try (PrintWriter writer = new PrintWriter(pncFile)) {
            File cpnFile = XmasSettings.getTempVxmCpnFile();
            readFile(cpnFile.getAbsolutePath());
            writeNet(writer, srcNodes, funNodes, swNodes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println();
    }

}
