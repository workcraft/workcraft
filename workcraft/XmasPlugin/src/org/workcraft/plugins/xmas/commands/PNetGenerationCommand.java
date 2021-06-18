package org.workcraft.plugins.xmas.commands;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.workcraft.commands.Command;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.SinkComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;
import org.workcraft.utils.LogUtils;
import org.workcraft.utils.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

public class PNetGenerationCommand implements Command {

    private static final int dl = 1;
    private static final boolean printoutput = true;

    private static class Ids {
        public final String a;
        public final String b;

        Ids(String s1, String s2) {
            a = s1;
            b = s2;
        }
    }

    private static class Info {
        public final String a;
        public final String b;
        public final String c;
        public final String d;

        Info(String s1, String s2, String s3, String s4) {
            a = s1;
            b = s2;
            c = s3;
            d = s4;
        }
    }

    private static final List<Info> lst = new ArrayList<>();
    private static final List<Ids> lst2 = new ArrayList<>();
    private static final List<Info> slsti = new ArrayList<>();
    private static final List<Info> slsto = new ArrayList<>();
    private static final List<Info> slsto2 = new ArrayList<>();

    public void initlist() {
        lst.clear();
        lst2.clear();
        slsti.clear();
        slsto.clear();
        slsto2.clear();
    }

    private static void processFile(String file) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File(file));
        } catch (FileNotFoundException e) {
            LogUtils.logError(e.getMessage());
        }
        String qs = "";
        String cn = "";
        String dn = "";
        int tst = 0;
        while (sc.hasNextLine() && tst == 0) {
            Scanner line = new Scanner(sc.nextLine());
            Scanner nxt = new Scanner(line.next());
            String check = nxt.next();
            if (check.startsWith("empty")) {
                tst = 1;
            } else if (check.startsWith("//gensync")) {
                nxt = new Scanner(line.next());
                String str = nxt.next();
                qs = str.replace("Qs", "Sync");
            } else {
                nxt = new Scanner(line.next());
                nxt.next();
                nxt = new Scanner(line.next());
                cn = nxt.next();
                nxt = new Scanner(line.next());
                dn = nxt.next();
                String d = "0";
                if ("b".equals(dn)) {
                    d = "1";  //Mrg conn
                }
                lst.add(new Info(cn, qs, "", d));
            }
        }
    }

    @Override
    public String getDisplayName() {
        return "Generate Circuit Petri Net";
    }

    @Override
    public String getSection() {
        return "GenCPNet";
    }

    @Override
    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.isApplicable(we, Xmas.class);
    }

    private static String searchList(String id) {
        String str = "";

        for (Info info : lst) {
            if (info.a.equals(id)) {
                str = info.b;
            }
        }
        return str;
    }

    private static String searchList2(String id) {
        String str = "";

        for (Info info : lst) {
            if (info.a.equals(id)) {
                str = info.c;
            }
        }
        return str;
    }

    private static String searchList3(String id) {
        String str = "";

        for (Ids ids : lst2) {
            if (ids.a.equals(id)) {
                str = ids.b;
            }
        }
        return str;
    }

    private static String searchListFirst(String id) {
        int tst = 0;
        String str = "";

        int i = 0;
        while (i < lst.size() && tst == 0) {
            if (lst.get(i).a.equals(id)) {
                if ("0".equals(lst.get(i).d)) {
                    str = lst.get(i).b;
                    tst = 1;
                }
            }
            i++;
        }
        return str;
    }

    private static String searchListSecond(String id) {
        int tst = 0;
        String str = "";

        int i = 0;
        while (i < lst.size() && tst == 0) {
            if (lst.get(i).a.equals(id)) {
                if ("1".equals(lst.get(i).d)) {
                    str = lst.get(i).b;
                    tst = 1;
                }
            }
            i++;
        }
        return str;
    }

    private static String searchListFirst2(String id) {
        int tst = 0;
        String str = "";

        int i = 0;
        while (i < lst.size() && tst == 0) {
            if (lst.get(i).a.equals(id)) {
                if ("0".equals(lst.get(i).d)) {
                    str = lst.get(i).c;
                    tst = 1;
                }
            }
            i++;
        }
        return str;
    }

    private static String searchListSecond2(String id) {
        int tst = 0;
        String str = "";

        int i = 0;
        while (i < lst.size() && tst == 0) {
            if (lst.get(i).a.equals(id)) {
                if ("1".equals(lst.get(i).d)) {
                    str = lst.get(i).c;
                    tst = 1;
                }
            }
            i++;
        }
        return str;
    }

    private static void writeblock(String id, String label, PrintWriter writer) {
        if (printoutput) {
            System.out.println("p_" + id + label + "0  t_" + id + label + "plus ");
            System.out.println("t_" + id + label + "plus  p_" + id + label + "1");
            System.out.println("p_" + id + label + "1  t_" + id + label + "minus ");
            System.out.println("t_" + id + label + "minus  p_" + id + label + "0");
        }
        writer.println("p_" + id + label + "0  t_" + id + label + "plus ");
        writer.println("t_" + id + label + "plus  p_" + id + label + "1");
        writer.println("p_" + id + label + "1  t_" + id + label + "minus ");
        writer.println("t_" + id + label + "minus  p_" + id + label + "0");
    }

    private static void writebidir(String id, String label1, String label2, PrintWriter writer) {
        if (printoutput) {
            System.out.println("p_" + id + label1 + "  t_" + id + label2);
            System.out.println("t_" + id + label2 + "  p_" + id + label1);
        }
        writer.println("p_" + id + label1 + "  t_" + id + label2);
        writer.println("t_" + id + label2 + "  p_" + id + label1);
    }

    private static void writelink(String id, String id1, String label1, String label2, PrintWriter writer) {
        if (id1.contains("Sync")) {
            String s = id1.replace("Sync", "");
            id1 = "Qs" + s;
        }
        if (printoutput) {
            System.out.println("p_" + id1 + label1 + "  t_" + id + label2);
            System.out.println("t_" + id + label2 + "  p_" + id1 + label1);
        }
        writer.println("p_" + id1 + label1 + "  t_" + id + label2);
        writer.println("t_" + id + label2 + "  p_" + id1 + label1);
    }

    private static void writelinkp(String id, String id1, String label1, String label2, String idp, PrintWriter writer) {
        if (id1.contains("Sync")) {
            String s = id1.replace("Sync", "");
            id1 = "Qs" + s;
        }
        if ("0".equals(idp)) {
            writelink(id, id1, "a" + label1, label2, writer);
        } else if ("1".equals(idp)) {
            writelink(id, id1, "b" + label1, label2, writer);
        } else {
            writelink(id, id1, "i" + label1, label2, writer);
        }
    }

    private static void writelinki(String id, String id1, String label1, String label2, String idi, PrintWriter writer) {
        if (id1.contains("Sync")) {
            String s = id1.replace("Sync", "");
            id1 = "Qs" + s;
        }
        if ("a".equals(idi)) {
            writelink(id, id1, "a" + label1, label2, writer);
        } else if ("b".equals(idi)) {
            writelink(id, id1, "b" + label1, label2, writer);
        } else {
            writelink(id, id1, "o" + label1, label2, writer);
        }
    }

    private static void writeMarking(String id, String label, PrintWriter writer) {
        if (printoutput) {
            System.out.println("marking p_" + id + label + "0 1");
            System.out.println("marking p_" + id + label + "1 0");
        }
        writer.println("marking p_" + id + label + "0 1");
        writer.println("marking p_" + id + label + "1 0");
    }

    private static void writeMarking2(String id, String label, PrintWriter writer) {
        if (printoutput) {
            System.out.println("marking p_" + id + label + "0 0");
            System.out.println("marking p_" + id + label + "1 1");
        }
        writer.println("marking p_" + id + label + "0 0");
        writer.println("marking p_" + id + label + "1 1");
    }

    private static void genSource(String id, String id1, String idp, int fieldgr, PrintWriter writer) {
        if (printoutput) {
            System.out.println("//gensource " + id);
        }
        writer.println("//gensource " + id);
        if (fieldgr > 0) {
            System.out.println("//gr " + fieldgr);
            writer.println("//gr " + fieldgr);
        }
        writeMarking(id, "_oracle", writer);
        writeblock(id, "_oracle", writer);
        writeMarking(id, "o_irdy", writer);
        writeblock(id, "o_irdy", writer);
        writebidir(id, "_oracle0", "o_irdyminus", writer);
        writebidir(id, "_oracle1", "o_irdyplus", writer);
        writelinkp(id, id1, "_trdy1", "o_irdyminus", idp, writer);
    }

    private static void genSink(String id, int fieldgr, PrintWriter writer) {
        if (printoutput) {
            System.out.println("//gensink " + id);
        }
        writer.println("//gensink " + id);
        if (fieldgr > 0) {
            System.out.println("//gr " + fieldgr);
            writer.println("//gr " + fieldgr);
        }
        writeMarking(id, "_oracle", writer);
        writeblock(id, "_oracle", writer);
        writeMarking(id, "i_trdy", writer);
        writeblock(id, "i_trdy", writer);
        writebidir(id, "_oracle0", "i_trdyminus", writer);
        writebidir(id, "_oracle1", "i_trdyplus", writer);
        String id2 = searchList(id);
        String id3 = searchList2(id);
        if ("a".equals(id3)) {
            writelink(id, id2, "a_irdy1", "i_trdyminus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2, "b_irdy1", "i_trdyminus", writer);
        } else {
            writelink(id, id2, "o_irdy1", "i_trdyminus", writer);
        }
    }

    private static void genFunction(String id, String id1, String idp, int fieldgr, PrintWriter writer) {
        if (printoutput) {
            System.out.println("//genfunction " + id);
        }
        writer.println("//genfunction " + id);
        if (fieldgr > 0) {
            System.out.println("//gr " + fieldgr);
            writer.println("//gr " + fieldgr);
        }
        writeMarking(id, "o_irdy", writer);
        writeblock(id, "o_irdy", writer);
        writeMarking(id, "i_trdy", writer);
        writeblock(id, "i_trdy", writer);
        String id2 = searchList(id);
        String id3 = searchList2(id);
        if ("a".equals(id3)) {
            writelink(id, id2, "a_irdy0", "o_irdyminus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2, "b_irdy0", "o_irdyminus", writer);
        } else {
            writelink(id, id2, "o_irdy0", "o_irdyminus", writer);
        }
        if ("a".equals(id3)) {
            writelink(id, id2, "a_irdy1", "o_irdyplus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2, "b_irdy1", "o_irdyplus", writer);
        } else {
            writelink(id, id2, "o_irdy1", "o_irdyplus", writer);
        }
        //writer.println("Function checking id = " + ' ' + id + "id2 = " + ' ' + id2);
        writelinkp(id, id1, "_trdy0", "i_trdyminus", idp, writer);
        writelinkp(id, id1, "_trdy1", "i_trdyplus", idp, writer);

    }

    private static void genFork(String id, String id1, String id2, String idp1, String idp2, int fieldgr, PrintWriter writer) {
        if (printoutput) {
            System.out.println("//genfork " + id);
        }
        writer.println("//genfork " + id);
        if (fieldgr > 0) {
            System.out.println("//gr " + fieldgr);
            writer.println("//gr " + fieldgr);
        }
        writeMarking(id, "a_irdy", writer);
        writeblock(id, "a_irdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "a_irdy1  t_" + id + "a_irdyminus1");
            System.out.println("t_" + id + "a_irdyminus1  p_" + id + "a_irdy0");
        }
        writer.println("p_" + id + "a_irdy1  t_" + id + "a_irdyminus1");
        writer.println("t_" + id + "a_irdyminus1  p_" + id + "a_irdy0");
        writeMarking(id, "b_irdy", writer);
        writeblock(id, "b_irdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "b_irdy1  t_" + id + "b_irdyminus1");
            System.out.println("t_" + id + "b_irdyminus1  p_" + id + "b_irdy0");
        }
        writer.println("p_" + id + "b_irdy1  t_" + id + "b_irdyminus1");
        writer.println("t_" + id + "b_irdyminus1  p_" + id + "b_irdy0");
        writeMarking(id, "i_trdy", writer);
        writeblock(id, "i_trdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "i_trdy1  t_" + id + "i_trdyminus1");
            System.out.println("t_" + id + "i_trdyminus1  p_" + id + "i_trdy0");
        }
        writer.println("p_" + id + "i_trdy1  t_" + id + "i_trdyminus1");
        writer.println("t_" + id + "i_trdyminus1  p_" + id + "i_trdy0");
        String id2b = searchList(id);
        String id3 = searchList2(id);
        if ("a".equals(id3)) {
            writelink(id, id2b, "a_irdy0", "a_irdyminus1", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2b, "b_irdy0", "a_irdyminus1", writer);
        } else {
            writelink(id, id2b, "o_irdy0", "a_irdyminus1", writer);
        }
        if ("a".equals(id3)) {
            writelink(id, id2b, "a_irdy1", "a_irdyplus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2b, "b_irdy1", "a_irdyplus", writer);
        } else {
            writelink(id, id2b, "o_irdy1", "a_irdyplus", writer);
        }
        if ("a".equals(id3)) {
            writelink(id, id2b, "a_irdy0", "b_irdyminus1", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2b, "b_irdy0", "b_irdyminus1", writer);
        } else {
            writelink(id, id2b, "o_irdy0", "b_irdyminus1", writer);
        }
        if ("a".equals(id3)) {
            writelink(id, id2b, "a_irdy1", "b_irdyplus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2b, "b_irdy1", "b_irdyplus", writer);
        } else {
            writelink(id, id2b, "o_irdy1", "b_irdyplus", writer);
        }
        writelinkp(id, id1, "_trdy0", "a_irdyminus", idp1, writer);
        writelinkp(id, id1, "_trdy1", "a_irdyplus", idp1, writer);
        writelinkp(id, id2, "_trdy0", "b_irdyminus", idp2, writer);
        writelinkp(id, id2, "_trdy1", "b_irdyplus", idp2, writer);
        writelinkp(id, id1, "_trdy1", "i_trdyplus", idp1, writer);
        writelinkp(id, id2, "_trdy1", "i_trdyplus", idp2, writer);
        writelinkp(id, id1, "_trdy0", "i_trdyminus", idp1, writer);
        writelinkp(id, id2, "_trdy0", "i_trdyminus1", idp2, writer);

    }

    private static void genJoin(String id, String id1, String idp, int fieldgr, PrintWriter writer) {
        if (printoutput) {
            System.out.println("//genjoin " + id);
        }
        writer.println("//genjoin " + id);
        if (fieldgr > 0) {
            System.out.println("//gr " + fieldgr);
            writer.println("//gr " + fieldgr);
        }
        writeMarking(id, "a_trdy", writer);
        writeblock(id, "a_trdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "a_trdy1  t_" + id + "a_trdyminus1");
            System.out.println("t_" + id + "a_trdyminus1  p_" + id + "a_trdy0");
        }
        writer.println("p_" + id + "a_trdy1  t_" + id + "a_trdyminus1");
        writer.println("t_" + id + "a_trdyminus1  p_" + id + "a_trdy0");
        writeMarking(id, "b_trdy", writer);
        writeblock(id, "b_trdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "b_trdy1  t_" + id + "b_trdyminus1");
            System.out.println("t_" + id + "b_trdyminus1  p_" + id + "b_trdy0");
        }
        writer.println("p_" + id + "b_trdy1  t_" + id + "b_trdyminus1");
        writer.println("t_" + id + "b_trdyminus1  p_" + id + "b_trdy0");
        writeMarking(id, "o_irdy", writer);
        writeblock(id, "o_irdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "o_irdy1  t_" + id + "o_irdyminus1");
            System.out.println("t_" + id + "o_irdyminus1  p_" + id + "o_irdy0");
        }
        writer.println("p_" + id + "o_irdy1  t_" + id + "o_irdyminus1");
        writer.println("t_" + id + "o_irdyminus1  p_" + id + "o_irdy0");
        String id3 = searchListFirst(id);
        String id4 = searchListSecond(id);
        String id5 = searchListFirst2(id);
        String id6 = searchListSecond2(id);
        writelinki(id, id3, "_irdy0", "a_trdyminus1", id5, writer);
        writelinki(id, id3, "_irdy1", "a_trdyplus", id5, writer);
        writelinki(id, id4, "_irdy0", "b_trdyminus1", id6, writer);
        writelinki(id, id4, "_irdy1", "b_trdyplus", id6, writer);
        writelinki(id, id3, "_irdy1", "o_irdyplus", id5, writer);
        writelinki(id, id4, "_irdy1", "o_irdyplus", id6, writer);
        writelinki(id, id3, "_irdy0", "o_irdyminus", id5, writer);
        writelinki(id, id4, "_irdy0", "o_irdyminus1", id6, writer);
        writelinkp(id, id1, "_trdy0", "a_trdyminus", idp, writer);
        writelinkp(id, id1, "_trdy1", "a_trdyplus", idp, writer);
        writelinkp(id, id1, "_trdy0", "b_trdyminus", idp, writer);
        writelinkp(id, id1, "_trdy1", "b_trdyplus", idp, writer);

    }

    private static void genSwitch(String id, String id1, String id2, String idp1, String idp2, int fieldgr, PrintWriter writer) {
        if (printoutput) {
            System.out.println("//genswitch " + id);
        }
        writer.println("//genswitch " + id);
        if (fieldgr > 0) {
            System.out.println("//gr " + fieldgr);
            writer.println("//gr " + fieldgr);
        }
        writeMarking(id, "_sw", writer);
        writeblock(id, "_sw", writer);
        writeMarking(id, "a_irdy", writer);
        writeblock(id, "a_irdy", writer);
        writer.println("p_" + id + "a_irdy1  t_" + id + "a_irdyminus1");
        writer.println("t_" + id + "a_irdyminus1  p_" + id + "a_irdy0");
        writeMarking(id, "b_irdy", writer);
        writeblock(id, "b_irdy", writer);
        writer.println("p_" + id + "b_irdy1  t_" + id + "b_irdyminus1");
        writer.println("t_" + id + "b_irdyminus1  p_" + id + "b_irdy0");
        writeMarking(id, "i_trdy", writer);
        writeblock(id, "i_trdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "i_trdy0  t_" + id + "i_trdyplus1");
            System.out.println("t_" + id + "i_trdyplus1  p_" + id + "i_trdy1");
        }
        writer.println("p_" + id + "i_trdy0  t_" + id + "i_trdyplus1");
        writer.println("t_" + id + "i_trdyplus1  p_" + id + "i_trdy1");
        if (printoutput) {
            System.out.println("p_" + id + "i_trdy1  t_" + id + "i_trdyminus1");
            System.out.println("t_" + id + "i_trdyminus1  p_" + id + "i_trdy0");
        }
        writer.println("p_" + id + "i_trdy1  t_" + id + "i_trdyminus1");
        writer.println("t_" + id + "i_trdyminus1  p_" + id + "i_trdy0");
        if (printoutput) {
            System.out.println("p_" + id + "i_trdy1  t_" + id + "i_trdyminus2");
            System.out.println("t_" + id + "i_trdyminus2  p_" + id + "i_trdy0");
        }
        writer.println("p_" + id + "i_trdy1  t_" + id + "i_trdyminus2");
        writer.println("t_" + id + "i_trdyminus2  p_" + id + "i_trdy0");
        if (printoutput) {
            System.out.println("p_" + id + "i_trdy1  t_" + id + "i_trdyminus3");
            System.out.println("t_" + id + "i_trdyminus3  p_" + id + "i_trdy0");
        }
        writer.println("p_" + id + "i_trdy1  t_" + id + "i_trdyminus3");
        writer.println("t_" + id + "i_trdyminus3  p_" + id + "i_trdy0");
        writebidir(id, "_sw0", "b_irdyplus", writer);
        writebidir(id, "_sw1", "b_irdyminus1", writer);
        writebidir(id, "_sw1", "a_irdyplus", writer);
        writebidir(id, "_sw0", "a_irdyminus1", writer);
        writebidir(id, "a_irdy1", "i_trdyplus", writer);
        writebidir(id, "b_irdy1", "i_trdyplus1", writer);
        writebidir(id, "a_irdy0", "i_trdyminus3", writer);
        writebidir(id, "a_irdy0", "i_trdyminus2", writer);
        writebidir(id, "b_irdy0", "i_trdyminus3", writer);
        writebidir(id, "b_irdy0", "i_trdyminus1", writer);
        String id2b = searchList(id);
        String id3 = searchList2(id);
        if ("a".equals(id3)) {
            writelink(id, id2b, "a_irdy0", "a_irdyminus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2b, "b_irdy0", "a_irdyminus", writer);
        } else {
            writelink(id, id2b, "o_irdy0", "a_irdyminus", writer);
        }
        if ("a".equals(id3)) {
            writelink(id, id2b, "a_irdy1", "a_irdyplus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2b, "b_irdy1", "a_irdyplus", writer);
        } else {
            writelink(id, id2b, "o_irdy1", "a_irdyplus", writer);
        }
        if ("a".equals(id3)) {
            writelink(id, id2b, "a_irdy0", "b_irdyminus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2b, "b_irdy0", "b_irdyminus", writer);
        } else {
            writelink(id, id2b, "o_irdy0", "b_irdyminus", writer);
        }
        if ("a".equals(id3)) {
            writelink(id, id2b, "a_irdy1", "b_irdyplus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2b, "b_irdy1", "b_irdyplus", writer);
        } else {
            writelink(id, id2b, "o_irdy1", "b_irdyplus", writer);
        }
        writelinkp(id, id1, "_trdy1", "i_trdyplus", idp1, writer);
        writelinkp(id, id2, "_trdy1", "i_trdyplus1", idp2, writer);
        writelinkp(id, id1, "_trdy0", "i_trdyminus", idp1, writer);
        writelinkp(id, id1, "_trdy0", "i_trdyminus1", idp1, writer);
        writelinkp(id, id2, "_trdy0", "i_trdyminus", idp2, writer);
        writelinkp(id, id2, "_trdy0", "i_trdyminus2", idp2, writer);

    }

    private static void genMerge(String id, String id1, String idp, int fieldgr, PrintWriter writer) {
        if (printoutput) {
            System.out.println("//genmerge " + id);
        }
        writer.println("//genmerge " + id);
        if (fieldgr > 0) {
            System.out.println("//gr " + fieldgr);
            writer.println("//gr " + fieldgr);
        }
        writeMarking(id, "_u", writer);
        writeblock(id, "_u", writer);
        writeMarking(id, "a_trdy", writer);
        writeblock(id, "a_trdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "a_trdy1  t_" + id + "a_trdyminus1");
            System.out.println("t_" + id + "a_trdyminus1  p_" + id + "a_trdy0");
        }
        writer.println("p_" + id + "a_trdy1  t_" + id + "a_trdyminus1");
        writer.println("t_" + id + "a_trdyminus1  p_" + id + "a_trdy0");
        if (printoutput) {
            System.out.println("p_" + id + "a_trdy1  t_" + id + "a_trdyminus2");
            System.out.println("t_" + id + "a_trdyminus2  p_" + id + "a_trdy0");
        }
        writer.println("p_" + id + "a_trdy1  t_" + id + "a_trdyminus2");
        writer.println("t_" + id + "a_trdyminus2  p_" + id + "a_trdy0");
        writeMarking(id, "b_trdy", writer);
        writeblock(id, "b_trdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "b_trdy1  t_" + id + "b_trdyminus1");
            System.out.println("t_" + id + "b_trdyminus1  p_" + id + "b_trdy0");
        }
        writer.println("p_" + id + "b_trdy1  t_" + id + "b_trdyminus1");
        writer.println("t_" + id + "b_trdyminus1  p_" + id + "b_trdy0");
        if (printoutput) {
            System.out.println("p_" + id + "b_trdy1  t_" + id + "b_trdyminus2");
            System.out.println("t_" + id + "b_trdyminus2  p_" + id + "b_trdy0");
        }
        writer.println("p_" + id + "b_trdy1  t_" + id + "b_trdyminus2");
        writer.println("t_" + id + "b_trdyminus2  p_" + id + "b_trdy0");
        writeMarking(id, "o_irdy", writer);
        writeblock(id, "o_irdy", writer);
        if (printoutput) {
            System.out.println("p_" + id + "o_irdy0  t_" + id + "o_irdyplus1");
            System.out.println("t_" + id + "o_irdyplus1  p_" + id + "o_irdy1");
        }
        writer.println("p_" + id + "o_irdy0  t_" + id + "o_irdyplus1");
        writer.println("t_" + id + "o_irdyplus1  p_" + id + "o_irdy1");
        writebidir(id, "_u0", "a_trdyminus", writer);
        writebidir(id, "_u1", "a_trdyplus", writer);
        writebidir(id, "_u0", "b_trdyplus", writer);
        writebidir(id, "_u1", "b_trdyminus", writer);
        String id3 = searchListFirst(id);
        String id4 = searchListSecond(id);
        String id5 = searchListFirst2(id);
        String id6 = searchListSecond2(id);
        writelinki(id, id3, "_irdy0", "_uminus", id5, writer);
        writelinki(id, id3, "_irdy1", "_uplus", id5, writer);
        writelinki(id, id4, "_irdy0", "_uplus", id6, writer);
        writelinki(id, id4, "_irdy1", "_uminus", id6, writer);
        writelinki(id, id3, "_irdy0", "a_trdyminus2", id5, writer);
        writelinki(id, id3, "_irdy1", "a_trdyplus", id5, writer);
        writelinki(id, id4, "_irdy0", "b_trdyminus2", id6, writer);
        writelinki(id, id4, "_irdy1", "b_trdyplus", id6, writer);
        writelinki(id, id3, "_irdy0", "o_irdyminus", id5, writer);
        writelinki(id, id3, "_irdy1", "o_irdyplus", id5, writer);
        writelinki(id, id4, "_irdy0", "o_irdyminus", id6, writer);
        writelinki(id, id4, "_irdy1", "o_irdyplus1", id6, writer);
        writelinkp(id, id1, "_trdy1", "a_trdyplus", idp, writer);
        writelinkp(id, id1, "_trdy1", "b_trdyplus", idp, writer);
        writelinkp(id, id1, "_trdy0", "a_trdyminus1", idp, writer);
        writelinkp(id, id1, "_trdy0", "b_trdyminus1", idp, writer);

    }

    private static void genQueue(String id, String id1, String idp, int init, String gpf, int fieldgr, PrintWriter writer) {
        if (printoutput) {
            System.out.println("//genqueue " + id + " " + gpf);
        }
        writer.println("//genqueue " + id + " " + gpf);
        if (fieldgr > 0) {
            System.out.println("//g " + fieldgr);
            writer.println("//g " + fieldgr);
        }
        if (init == 0) writeMarking(id, "o_irdy", writer);
        else writeMarking2(id, "o_irdy", writer);
        writeblock(id, "o_irdy", writer);
        if (init == 0) writeMarking2(id, "i_trdy", writer);
        else writeMarking(id, "i_trdy", writer);
        writeblock(id, "i_trdy", writer);
        if (init == 0) writeMarking(id, "_q", writer);
        else writeMarking2(id, "_q", writer);
        writeblock(id, "_q", writer);
        writebidir(id, "_q0", "o_irdyminus", writer);
        writebidir(id, "_q0", "i_trdyplus", writer);
        writebidir(id, "_q1", "o_irdyplus", writer);
        writebidir(id, "_q1", "i_trdyminus", writer);
        writebidir(id, "o_irdy1", "_qminus", writer);
        writebidir(id, "i_trdy1", "_qplus", writer);
        String id2 = searchList(id);
        String id3 = searchList2(id);
        if ("a".equals(id3)) {
            writelink(id, id2, "a_irdy1", "_qplus", writer);
        } else if ("b".equals(id3)) {
            writelink(id, id2, "b_irdy1", "_qplus", writer);
        } else {
            writelink(id, id2, "o_irdy1", "_qplus", writer);
        }
        writelinkp(id, id1, "_trdy1", "_qminus", idp, writer);
        if (dl == 1) {
            if (!id2.contains("Src") && !id2.contains("Qu") && !id2.contains("Sync")) writelink(id, id2, "_dl1", "_qplus", writer);
        }
    }

    private static void genQueue2p(int size, String id, String id1, String idp, int init, String gpf, int fieldgr, PrintWriter writer) {
        int inc = 0;
        if (printoutput) {
            if (size == 2) System.out.println("//genqueue2p " + id + " " + gpf);
            if (size == 3) System.out.println("//genqueue3p " + id + " " + gpf);
            if (size == 4) System.out.println("//genqueue4p " + id + " " + gpf);
            if (size == 5) System.out.println("//genqueue5p " + id + " " + gpf);
        }
        if (size == 2) writer.println("//genqueue2p " + id + " " + gpf);
        if (size == 3) writer.println("//genqueue3p " + id + " " + gpf);
        if (size == 4) writer.println("//genqueue4p " + id + " " + gpf);
        if (size == 5) writer.println("//genqueue5p " + id + " " + gpf);
        if (fieldgr > 0) {
            System.out.println("//g " + fieldgr);
            writer.println("//g " + fieldgr);
        }
        if (init > 0) writeMarking2(id, "o_irdy", writer);
        else writeMarking(id, "o_irdy", writer);
        writeblock(id, "o_irdy", writer);
        for (int i = 1; i < size; i++) {
            if (printoutput) {
                System.out.println("p_" + id + "o_irdy0  t_" + id + "o_irdyplus" + i);
                System.out.println("t_" + id + "o_irdyplus" + i + " p_" + id + "o_irdy1");
            }
            writer.println("p_" + id + "o_irdy0  t_" + id + "o_irdyplus" + i);
            writer.println("t_" + id + "o_irdyplus" + i + " p_" + id + "o_irdy1");
        }
        if (init >= size) writeMarking(id, "i_trdy", writer);
        else writeMarking2(id, "i_trdy", writer);
        writeblock(id, "i_trdy", writer);
        for (int i = 1; i < size; i++) {
            if (printoutput) {
                System.out.println("p_" + id + "i_trdy0  t_" + id + "i_trdyplus" + i);
                System.out.println("t_" + id + "i_trdyplus" + i + " p_" + id + "i_trdy1");
            }
            writer.println("p_" + id + "i_trdy0  t_" + id + "i_trdyplus" + i);
            writer.println("t_" + id + "i_trdyplus" + i + " p_" + id + "i_trdy1");
        }
        for (int i = 1; i <= size; i++) {
            if (init >= i) writeMarking2(id, "_q" + i, writer);
            else writeMarking(id, "_q" + i, writer);
            writeblock(id, "_q" + i, writer);
        }
        for (int i = 1; i <= size; i++) {
            if (i == init + 1) writeMarking2(id, "_hd" + i, writer);
            else writeMarking(id, "_hd" + i, writer);
            writeblock(id, "_hd" + i, writer);
        }
        for (int i = 1; i <= size; i++) {
            if (i == 1) {
                if (init <= 0) writeMarking(id, "_tl" + i, writer);
                else writeMarking2(id, "_tl" + i, writer);
            } else writeMarking(id, "_tl" + i, writer);
            writeblock(id, "_tl" + i, writer);
        }
        for (int i = 1; i <= size; i++) {
            writebidir(id, "i_trdy1", "_q" + i + "plus", writer);
            writebidir(id, "o_irdy1", "_q" + i + "minus", writer);
        }
        for (int i = 1; i <= size; i++) {
            writebidir(id, "_q" + i + "0", "o_irdyminus", writer);
            writebidir(id, "_q" + i + "1", "i_trdyminus", writer);
        }
        writebidir(id, "_q1" + "0", "i_trdyplus", writer);
        for (int i = 2; i <= size; i++) {
            writebidir(id, "_q" + i + "0", "i_trdyplus" + (i - 1), writer);
        }
        writebidir(id, "_q1" + "1", "o_irdyplus", writer);
        for (int i = 2; i <= size; i++) {
            writebidir(id, "_q" + i + "1", "o_irdyplus" + (i - 1), writer);
        }
        for (int i = 1; i <= size; i++) {
            writebidir(id, "_q" + i + "0", "_hd" + i + "plus", writer);
            writebidir(id, "_q" + i + "1", "_hd" + i + "minus", writer);
            if (i == size) inc = 1;
            else inc = i + 1;
            writebidir(id, "_q" + i + "1", "_hd" + inc + "plus", writer);
        }
        for (int i = 1; i <= size; i++) {
            writebidir(id, "_q" + i + "0", "_tl" + i + "minus", writer);
            writebidir(id, "_q" + i + "1", "_tl" + i + "plus", writer);
            if (i == size) inc = 1;
            else inc = i + 1;
            writebidir(id, "_q" + i + "0", "_tl" + inc + "plus", writer);
        }
        for (int i = 1; i <= size; i++) {
            writebidir(id, "_hd" + i + "1", "_q" + i + "plus", writer);
        }
        for (int i = 1; i <= size; i++) {
            writebidir(id, "_tl" + i + "1", "_q" + i + "minus", writer);
        }
        String id2 = searchList(id);
        String id3 = searchList2(id);
        for (int i = 1; i <= size; i++) {
            if ("a".equals(id3)) {
                writelink(id, id2, "a_irdy1", "_q" + i + "plus", writer);
            } else if ("b".equals(id3)) {
                writelink(id, id2, "b_irdy1", "_q" + i + "plus", writer);
            } else {
                writelink(id, id2, "o_irdy1", "_q" + i + "plus", writer);
            }
        }
        for (int i = 1; i <= size; i++) {
            writelinkp(id, id1, "_trdy1", "_q" + i + "minus", idp, writer);
        }
        if (dl == 1) {
            for (int i = 1; i <= size; i++) {
                if (!id2.contains("Src") && !id2.contains("Qu") && !id2.contains("Sync")) writelink(id, id2, "_dl1", "_q" + i + "plus", writer);
            }
        }
    }

    public static void initParse(String args) throws IOException {
        JsonFactory f = new MappingJsonFactory();
        JsonParser jp = f.createJsonParser(new File(args));

        JsonToken current;

        current = jp.nextToken();
        if (current != JsonToken.START_OBJECT) {
            LogUtils.logError("Root should be object: quiting.");
            return;
        }

        while (jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            // move from field name to field value
            current = jp.nextToken();

            if ("NETWORK".equals(fieldName)) {
                if (current == JsonToken.START_ARRAY) {
                    // For each of the records in the array
                    System.out.println("Generate CPNs");
                    while (jp.nextToken() != JsonToken.END_ARRAY) {
                        JsonNode node = jp.readValueAsTree();
                        String idName = node.get("id").toString();
                        String idName1 = "";
                        String idName2 = "";
                        String idNamep = "";
                        String idNamep1 = "";
                        String idNamep2 = "";
                        String typeName = node.get("type").toString();
                        //System.out.println("id: " + idName + "type: " + typeName);
                        lst2.add(new Ids(idName, typeName));
                        JsonNode y = node.get("outs");
                        if (y != null) {
                            for (int i = 0; y.has(i); i++) {
                                if (y.get(i).has("id")) {
                                    if (i == 0) {
                                        idName1 = y.get(i).get("id").toString();
                                        idNamep1 = y.get(i).get("in_port").toString();
                                        if ("xfork".equals(typeName)) {
                                            lst.add(new Info(idName1, idName, "b", idNamep1));
                                        } else if ("xswitch".equals(typeName)) {
                                            lst.add(new Info(idName1, idName, "a", idNamep1));
                                        } else {
                                            lst.add(new Info(idName1, idName, "", idNamep1));
                                        }
                                        if (idName1.contains("Sync")) {
                                            //System.out.println("id: " + idName + "sync: " + idName1);
                                            slsti.add(new Info(idName, idName1, "", idNamep1));   //swapped order slsti slsto
                                        }
                                        //add o based on order of i or reverse?
                                        if (idName.contains("Sync")) {
                                            slsto2.add(new Info(idName1, idName, "", idNamep1));
                                        }
                                    } else if (i == 1) {
                                        idName2 = y.get(i).get("id").toString();
                                        idNamep2 = y.get(i).get("in_port").toString();
                                        if ("xfork".equals(typeName)) {
                                            lst.add(new Info(idName2, idName, "a", idNamep2));
                                        } else if ("xswitch".equals(typeName)) {
                                            lst.add(new Info(idName2, idName, "b", idNamep2));
                                        } else {
                                            lst.add(new Info(idName2, idName, "", idNamep2));
                                        }
                                        if (idName2.contains("Sync")) slsti.add(new Info(idName, idName2, "", idNamep2));
                                        if (idName.contains("Sync")) {
                                            slsto2.add(new Info(idName2, idName, "", idNamep2));
                                        }
                                    } else {
                                        idName1 = y.get(i).get("id").toString();
                                        idNamep = y.get(i).get("in_port").toString();
                                        if (idName.contains("Sync")) {
                                            slsto2.add(new Info(idName, idName1, "", idNamep));
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LogUtils.logError("Records should be an array: skipping.");
                    jp.skipChildren();
                }
            } else {
                //System.out.println("Unprocessed property: " + fieldName);
                jp.skipChildren();
            }
        }
    }

    //public Collection<VisualSourceComponent> srcNodes;
    public Collection<SourceComponent> srcNodes;
    public Collection<FunctionComponent> funNodes;
    public Collection<SwitchComponent> swNodes;
    public Collection<SinkComponent> snkNodes;

    @Override
    public void run(WorkspaceEntry we) {
        System.out.println();
        final Xmas cnet = WorkspaceUtils.getAs(we, Xmas.class);
        srcNodes = cnet.getSourceComponents();
        //funNodes = Hierarchy.getDescendantsOfType(vnet.getRoot(), VisualFunctionComponent.class);
        funNodes = cnet.getFunctionComponents();
        swNodes = cnet.getSwitchComponents();
        snkNodes = cnet.getSinkComponents();

        JsonFactory f = new MappingJsonFactory();
        File cpnFile = XmasSettings.getTempVxmCpnFile();
        PrintWriter writer = null;
        try {

            initlist();
            File syncFile = XmasSettings.getTempVxmSyncFile();
            processFile(syncFile.getAbsolutePath());

            File jsonFile = XmasSettings.getTempVxmJsonFile();
            initParse(jsonFile.getAbsolutePath());
            //createSlsto();

            writer = new PrintWriter(cpnFile);
            JsonParser jp = f.createJsonParser(jsonFile);
            JsonToken current;

            current = jp.nextToken();
            if (current != JsonToken.START_OBJECT) {
                LogUtils.logError("Root should be object: quiting.");
                return;
            }

            while (jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jp.getCurrentName();
                // move from field name to field value
                current = jp.nextToken();
                if ("VARS".equals(fieldName)) {
                    if (current == JsonToken.START_ARRAY) {
                        while (jp.nextToken() != JsonToken.END_ARRAY) {
                        }
                    }
                } else if ("PACKET_TYPE".equals(fieldName)) {
                    if (current == JsonToken.START_OBJECT) {
                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                        }
                    }
                } else if ("COMPOSITE_OBJECTS".equals(fieldName)) {
                    if (current == JsonToken.START_ARRAY) {
                        while (jp.nextToken() != JsonToken.END_ARRAY) {
                        }
                    }
                } else if ("NETWORK".equals(fieldName)) {
                    if (current == JsonToken.START_ARRAY) {
                        // For each of the records in the array
                        while (jp.nextToken() != JsonToken.END_ARRAY) {
                            // read the record into a tree model,
                            JsonNode node = jp.readValueAsTree();
                            String idName = node.get("id").toString();
                            String idName1 = "";
                            String idName2 = "";
                            String idNamep1 = "";
                            String idNamep2 = "";
                            String fieldsize = "";
                            String fieldgpf = "";
                            int fieldinit = 0;
                            int fieldgr = 0;
                            String typeName = node.get("type").toString();

                            JsonNode y = node.get("outs");
                            if (y != null) {
                                for (int i = 0; y.has(i); i++) {
                                    if (y.get(i).has("id")) {
                                        if (i == 0) {
                                            idName1 = y.get(i).get("id").toString();
                                            /*if ("xfork".equals(typeName)) lst.add(new Info(idName1, idName, "b"));
                                              else if ("xswitch".equals(typeName)) lst.add(new Info(idName1, idName, "a"));
                                              else lst.add(new Info(idName1, idName, "")); */
                                        } else if (i == 1) {
                                            idName2 = y.get(i).get("id").toString();
                                            /*if ("xfork".equals(typeName)) lst.add(new Info(idName2, idName, "a"));
                                              else if ("xswitch".equals(typeName)) lst.add(new Info(idName2, idName, "b"));
                                              else lst.add(new Info(idName2, idName, "")); */
                                        }
                                    }
                                    if (y.get(i).has("in_port")) {
                                        if (i == 0) {
                                            String searchtyp = "";
                                            searchtyp = searchList3(idName1);
                                            if ("join".equals(searchtyp)) {
                                                //idNamep1 = y.get(i).get("in_port").toString();
                                                if ("0".equals(y.get(i).get("in_port").toString())) idNamep1 = "1";
                                                else idNamep1 = "0";
                                            } else if ("merge".equals(searchtyp)) {
                                                idNamep1 = y.get(i).get("in_port").toString();
                                            }
                                        } else if (i == 1) {
                                            String searchtyp1 = "";
                                            searchtyp1 = searchList3(idName2);
                                            if ("join".equals(searchtyp1)) {
                                                //idNamep2 = y.get(i).get("in_port").toString();
                                                if ("0".equals(y.get(i).get("in_port").toString())) idNamep2 = "1";
                                                else idNamep2 = "0";
                                            } else if ("merge".equals(searchtyp1)) {
                                                idNamep2 = y.get(i).get("in_port").toString();
                                            }
                                        }
                                    }
                                }
                            }
                            JsonNode y2 = node.get("fields");
                            if (y2 != null) {
                                for (int i = 0; y2.has(i); i++) {
                                    if (y2.get(i).has("size")) {
                                        fieldsize = y2.get(i).get("size").toString();
                                    }
                                    if (y2.get(i).has("init")) {
                                        fieldinit = y2.get(i).get("init").asInt();
                                    }
                                    if (y2.get(i).has("gr")) {
                                        fieldgr = y2.get(i).get("gr").asInt();
                                    }
                                    if (y2.get(i).has("gpf")) {
                                        fieldgpf = y2.get(i).get("gpf").toString();
                                    }
                                    if (y2.get(i).has("gpf1")) {
                                        fieldgpf = y2.get(i).get("gpf1").toString();
                                    }
                                }
                            }

                            if ("source".equals(typeName)) genSource(idName, idName1, idNamep1, fieldgr, writer);
                            if ("function".equals(typeName)) genFunction(idName, idName1, idNamep1, fieldgr, writer);
                            if ("xfork".equals(typeName)) genFork(idName, idName1, idName2, idNamep1, idNamep2, fieldgr, writer);
                            if ("join".equals(typeName)) genJoin(idName, idName1, idNamep1, fieldgr, writer);
                            if ("xswitch".equals(typeName)) genSwitch(idName, idName1, idName2, idNamep1, idNamep2, fieldgr, writer);
                            if ("merge".equals(typeName)) genMerge(idName, idName1, idNamep1, fieldgr, writer);
                            if ("queue".equals(typeName)) {
                                int size = Integer.parseInt(fieldsize);
                                //if ("1".equals(fieldsize)) genqueue(idName, idName1, idNamep1, fieldinit, writer);
                                //else genqueue2p(2, idName, idName1, idNamep1, writer);
                                if (size <= 1) {
                                    genQueue(idName, idName1, idNamep1, fieldinit, fieldgpf, fieldgr, writer);
                                } else {
                                    genQueue2p(size, idName, idName1, idNamep1, fieldinit, fieldgpf, fieldgr, writer);
                                }
                            }
                            if ("sink".equals(typeName)) genSink(idName, fieldgr, writer);
                        }
                    } else {
                        LogUtils.logError("Records should be an array: skipping.");
                        jp.skipChildren();
                    }
                } else {
                    LogUtils.logWarning("Unprocessed property: " + fieldName);
                    jp.skipChildren();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
                System.out.println("Control CPNs created");
                new PNetExt(srcNodes, funNodes, swNodes);
            }
        }
        System.out.println();
    }

}
