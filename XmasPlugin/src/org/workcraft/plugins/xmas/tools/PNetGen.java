package org.workcraft.plugins.xmas.tools;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.workcraft.Tool;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class PNetGen implements Tool {

	//private final Framework framework;

	//VisualCircuit circuit;
	//private CheckCircuitTask checkTask;


	//ProgressMonitor<? super MpsatChainResult> monitor;

	/*public CircuitTestTool(Framework framework, Workspace ws) {
		this.framework = framework;
//		this.ws = ws;
	}*/

	private static boolean printoutput=true;

	private static class Ids {

		   String a;
		   String b;

		    public Ids(String s1,String s2) {
		      a = s1;
		      b = s2;
		    }
	}

	private static class Info {

		   String a;
		   String b;
		   String c;
		   String d;

		    public Info(String s1,String s2,String s3,String s4) {
		      a = s1;
		      b = s2;
		      c = s3;
		      d = s4;
		    }
	}

	static List<Info> lst = new ArrayList<Info>();
	static List<Ids> lst_ = new ArrayList<Ids>();

	public void initlist() {
		lst.clear();
		lst_.clear();
	}

	public String getDisplayName() {
		return "Generate Circuit Petri net";
	}

	public String getSection() {
		return "GenCPNet";
	}

	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, Xmas.class);
	}

	private static String searchlist(String id) {
	    String str = "";

	    for(int i=0;i<lst.size();i++) {
	      if(id.equals(lst.get(i).a)) str = lst.get(i).b;
	    }
	    return str;
	}

	private static String searchlist_(String id) {
	    String str = "";

	    for(int i=0;i<lst.size();i++) {
	      //System.out.print("id=" + id + "i" + i + "=" + lst.get(i).a);
	      //if(id.equals(lst.get(i).a)) System.out.print("found match");
	      if(id.equals(lst.get(i).a)) str = lst.get(i).c;
	    }
	    return str;
	}

	private static String searchlist__(String id) {
	    String str = "";

	    for(int i=0;i<lst_.size();i++) {
	      if(id.equals(lst_.get(i).a)) str = lst_.get(i).b;
	    }
	    return str;
	}

	private static String searchlist_first(String id) {
	    int tst=0;
	    String str = "";

	    int i=0;
	    while(i<lst.size() && tst==0) {
	      if(id.equals(lst.get(i).a)) {
	        if(lst.get(i).d.equals("0")) {
	          str = lst.get(i).b;
	          tst=1;
	        }
	      }
	      i++;
	    }
	    return str;
	}

	private static String searchlist_second(String id) {
	    int tst=0;
	    String str = "";

	    int i=0;
	    while(i<lst.size() && tst==0) {
	      if(id.equals(lst.get(i).a)) {
	        if(lst.get(i).d.equals("1")) {
	          str = lst.get(i).b;
	          tst=1;
	        }
	      }
	      i++;
	    }
	    return str;
	}

	private static String searchlist_first_(String id) {
	    int tst=0;
	    String str = "";

	    int i=0;
	    while(i<lst.size() && tst==0) {
	      if(id.equals(lst.get(i).a)) {
	        if(lst.get(i).d.equals("0")) {
	          str = lst.get(i).c;
	          tst=1;
	        }
	      }
	      i++;
	    }
	    return str;
	  }

	  private static String searchlist_second_(String id) {
	    int tst=0;
	    String str = "";

	    int i=0;
	    while(i<lst.size() && tst==0) {
	      if(id.equals(lst.get(i).a)) {
	        if(lst.get(i).d.equals("1")) {
	          str = lst.get(i).c;
	          tst=1;
	        }
	      }
	      i++;
	    }
	    return str;
	}

	private static void writeblock(String id, String label, PrintWriter writer) {
		if(printoutput) {
			System.out.println("p_"+id+label+"0  t_"+id+label+"plus ");
			System.out.println("t_"+id+label+"plus  p_"+id+label+"1");
			System.out.println("p_"+id+label+"1  t_"+id+label+"minus ");
			System.out.println("t_"+id+label+"minus  p_"+id+label+"0");
		}
	    writer.println("p_"+id+label+"0  t_"+id+label+"plus ");
	    writer.println("t_"+id+label+"plus  p_"+id+label+"1");
	    writer.println("p_"+id+label+"1  t_"+id+label+"minus ");
	    writer.println("t_"+id+label+"minus  p_"+id+label+"0");
	}

	private static void writebidir(String id, String label1, String label2, PrintWriter writer) {
		if(printoutput) {
			System.out.println("p_"+id+label1+"  t_"+id+label2);
			System.out.println("t_"+id+label2+"  p_"+id+label1);
		}
	    writer.println("p_"+id+label1+"  t_"+id+label2);
	    writer.println("t_"+id+label2+"  p_"+id+label1);
	}

	private static void writelink(String id, String id1, String label1, String label2, PrintWriter writer) {
		if(printoutput) {
			System.out.println("p_"+id1+label1+"  t_"+id+label2);
			System.out.println("t_"+id+label2+"  p_"+id1+label1);
		}
	    writer.println("p_"+id1+label1+"  t_"+id+label2);
	    writer.println("t_"+id+label2+"  p_"+id1+label1);
	}

	private static void writelinkp(String id, String id1, String label1, String label2, String idp, PrintWriter writer) {
	    if(idp.equals("0")) writelink(id, id1, "a"+label1, label2, writer);
	    else if(idp.equals("1")) writelink(id, id1, "b"+label1, label2, writer);
	    else writelink(id, id1, "i"+label1, label2, writer);
	}

	private static void writelinki(String id, String id1, String label1, String label2, String idi, PrintWriter writer) {
		if(idi.equals("a")) writelink(id, id1, "a"+label1, label2, writer);
		else if(idi.equals("b")) writelink(id, id1, "b"+label1, label2, writer);
		else writelink(id, id1, "o"+label1, label2, writer);
	}

	private static void writemarking(String id, String label, PrintWriter writer) {
		if(printoutput) {
			System.out.println("marking p_"+id+label+"0 1");
			System.out.println("marking p_"+id+label+"1 0");
		}
	    writer.println("marking p_"+id+label+"0 1");
	    writer.println("marking p_"+id+label+"1 0");
	}

	private static void writemarking_(String id, String label, PrintWriter writer) {
		if(printoutput) {
			System.out.println("marking p_"+id+label+"0 0");
			System.out.println("marking p_"+id+label+"1 1");
		}
	    writer.println("marking p_"+id+label+"0 0");
	    writer.println("marking p_"+id+label+"1 1");
	}

	private static void writepriority(String id, String label, int p, PrintWriter writer) {
		if(printoutput) {
			System.out.println("priority t_"+id+label+"plus "+p);
			System.out.println("priority t_"+id+label+"minus "+p);
		}
	    writer.println("priority t_"+id+label+"plus "+p);
	    writer.println("priority t_"+id+label+"minus "+p);
	}

	private static void writepriority_(String id, String label, int n, int p, PrintWriter writer) {
		if(printoutput) {
			System.out.println("priority t_"+id+label+"plus"+n+" "+p);
		}
	    writer.println("priority t_"+id+label+"plus"+n+" "+p);
	}

	private static void writepriority__(String id, String label, int n, int p, PrintWriter writer) {
		if(printoutput) {
			System.out.println("priority t_"+id+label+"minus"+n+" "+p);
		}
	    writer.println("priority t_"+id+label+"minus"+n+" "+p);
	}

	private static void gensource(String id, String id1, String idp, JsonNode y, PrintWriter writer) {
		if(printoutput) {
			System.out.println("//gensource " + id);
		}
	    writer.println("//gensource " + id);
	    writemarking(id, "_oracle", writer);
	    writepriority(id, "_oracle", 2, writer);
	    writeblock(id, "_oracle", writer);
	    writemarking(id, "o_irdy", writer);
	    writepriority(id, "o_irdy", 3, writer);
	    writeblock(id, "o_irdy", writer);
	    //writer.println("p_"+id+"o_irdy0  t_"+id+"o_irdyplus1");
	    //writer.println("t_"+id+"o_irdyplus1  p_"+id+"o_irdy1");
	    writebidir(id, "_oracle0", "o_irdyminus", writer);
	    writebidir(id, "_oracle1", "o_irdyplus", writer);
	    writelinkp(id, id1, "_trdy1", "o_irdyminus", idp, writer);
	    //if(idp.equals("0")) writelink(id, id1, "a_trdy1", "o_irdyminus", writer);
	    //else if(idp.equals("1")) writelink(id, id1, "b_trdy1", "o_irdyminus", writer);
	    //else writelink(id, id1, "i_trdy1", "o_irdyminus", writer);
	}

	private static void gensink(String id,PrintWriter writer) {
		if(printoutput) {
			System.out.println("//gensink " + id);
		}
	    writer.println("//gensink " + id);
	    writemarking(id, "_oracle", writer);
	    writepriority(id, "_oracle", 2, writer);
	    writeblock(id, "_oracle", writer);
	    writemarking(id, "i_trdy", writer);
	    writepriority(id, "i_trdy", 3, writer);
	    writeblock(id, "i_trdy", writer);
	    //writer.println("p_"+id+"i_trdy0  t_"+id+"i_trdyplus1");
	    //writer.println("t_"+id+"i_trdyplus1  p_"+id+"i_trdy1");
	    writebidir(id, "_oracle0", "i_trdyminus", writer);
	    writebidir(id, "_oracle1", "i_trdyplus", writer);
	    //writebidir(id, "i_irdy0", "i_trdyplus1", writer);
	    String id2 = searchlist(id);
	    String id3 = searchlist_(id);
	    //writelink(id, id2, "o_irdy0", "i_trdyplus1", writer);
	//printsearchlist();
	    if(id3.equals("a")) writelink(id, id2, "a_irdy1", "i_trdyminus", writer);
	    else if(id3.equals("b")) writelink(id, id2, "b_irdy1", "i_trdyminus", writer);
	    else writelink(id, id2, "o_irdy1", "i_trdyminus", writer);
	}

	private static void genfunction(String id, String id1, String idp, JsonNode y, PrintWriter writer) {
		if(printoutput) {
			System.out.println("//genfunction " + id);
		}
	    writer.println("//genfunction " + id);
	    writemarking(id, "o_irdy", writer);
	    writepriority(id, "o_irdy", 1, writer);
	    writeblock(id, "o_irdy", writer);
	    writemarking(id, "i_trdy", writer);
	    writepriority(id, "i_trdy", 1, writer);
	    writeblock(id, "i_trdy", writer);
	    String id2 = searchlist(id);
	    String id3 = searchlist_(id);
	    //writelink(id, id2, "o_irdy0", "o_irdyminus", writer);
	    if(id3.equals("a")) writelink(id, id2, "a_irdy0", "o_irdyminus", writer);
	    else if(id3.equals("b")) writelink(id, id2, "b_irdy0", "o_irdyminus", writer);
	    else writelink(id, id2, "o_irdy0", "o_irdyminus", writer);
	    //writelink(id, id2, "o_irdy1", "o_irdyplus", writer);
	    if(id3.equals("a")) writelink(id, id2, "a_irdy1", "o_irdyplus", writer);
	    else if(id3.equals("b")) writelink(id, id2, "b_irdy1", "o_irdyplus", writer);
	    else writelink(id, id2, "o_irdy1", "o_irdyplus", writer);

	    //writelink(id, id1, "i_trdy0", "i_trdyminus", writer);
	    writelinkp(id, id1, "_trdy0", "i_trdyminus", idp, writer);
	    //writelink(id, id1, "i_trdy1", "i_trdyplus", writer);
	    writelinkp(id, id1, "_trdy1", "i_trdyplus", idp, writer);
	}

	private static void genfork(String id, String id1, String id2, String idp1, String idp2, JsonNode y, PrintWriter writer) {
		if(printoutput) {
			System.out.println("//genfork " + id);
		}
	    writer.println("//genfork " + id);
	    writemarking(id, "a_irdy", writer);
	    writepriority(id, "a_irdy", 1, writer);
	    writeblock(id, "a_irdy", writer);
	    writepriority__(id, "a_irdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"a_irdy1  t_"+id+"a_irdyminus1");
			System.out.println("t_"+id+"a_irdyminus1  p_"+id+"a_irdy0");
		}
	    writer.println("p_"+id+"a_irdy1  t_"+id+"a_irdyminus1");
	    writer.println("t_"+id+"a_irdyminus1  p_"+id+"a_irdy0");
	    //writeblock(id, "a_trdy", writer);
	    writemarking(id, "b_irdy", writer);
	    writepriority(id, "b_irdy", 1, writer);
	    writeblock(id, "b_irdy", writer);
	    writepriority__(id, "b_irdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"b_irdy1  t_"+id+"b_irdyminus1");
			System.out.println("t_"+id+"b_irdyminus1  p_"+id+"b_irdy0");
		}
	    writer.println("p_"+id+"b_irdy1  t_"+id+"b_irdyminus1");
	    writer.println("t_"+id+"b_irdyminus1  p_"+id+"b_irdy0");
	    //writeblock(id, "b_trdy", writer);
	    writemarking(id, "i_trdy", writer);
	    writepriority(id, "i_trdy", 1, writer);
	    writeblock(id, "i_trdy", writer);
	    writepriority__(id, "i_trdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"i_trdy1  t_"+id+"i_trdyminus1");
			System.out.println("t_"+id+"i_trdyminus1  p_"+id+"i_trdy0");
		}
	    writer.println("p_"+id+"i_trdy1  t_"+id+"i_trdyminus1");
	    writer.println("t_"+id+"i_trdyminus1  p_"+id+"i_trdy0");
	    //writeblock(id, "i_irdy", writer);
	    String id2_ = searchlist(id);
	    String id3 = searchlist_(id);
	    //writelink(id, id2_, "o_irdy0", "a_irdyminus1", writer);
	    if(id3.equals("a")) writelink(id, id2_, "a_irdy0", "a_irdyminus1", writer);
	    else if(id3.equals("b")) writelink(id, id2_, "b_irdy0", "a_irdyminus1", writer);
	    else writelink(id, id2_, "o_irdy0", "a_irdyminus1", writer);
	    //writelink(id, id2_, "o_irdy1", "a_irdyplus", writer);
	    if(id3.equals("a")) writelink(id, id2_, "a_irdy1", "a_irdyplus", writer);
	    else if(id3.equals("b")) writelink(id, id2_, "b_irdy1", "a_irdyplus", writer);
	    else writelink(id, id2_, "o_irdy1", "a_irdyplus", writer);
	    //writelink(id, id2_, "o_irdy0", "b_irdyminus1", writer);
	    if(id3.equals("a")) writelink(id, id2_, "a_irdy0", "b_irdyminus1", writer);
	    else if(id3.equals("b")) writelink(id, id2_, "b_irdy0", "b_irdyminus1", writer);
	    else writelink(id, id2_, "o_irdy0", "b_irdyminus1", writer);
	    //writelink(id, id2_, "o_irdy1", "b_irdyplus", writer);
	    if(id3.equals("a")) writelink(id, id2_, "a_irdy1", "b_irdyplus", writer);
	    else if(id3.equals("b")) writelink(id, id2_, "b_irdy1", "b_irdyplus", writer);
	    else writelink(id, id2_, "o_irdy1", "b_irdyplus", writer);
	    writelinkp(id, id1, "_trdy0", "a_irdyminus", idp1, writer);
	    writelinkp(id, id1, "_trdy1", "a_irdyplus", idp1, writer);
	    writelinkp(id, id2, "_trdy0", "b_irdyminus", idp2, writer);
	    writelinkp(id, id2, "_trdy1", "b_irdyplus", idp2, writer);
	    writelinkp(id, id1, "_trdy1", "i_trdyplus", idp1, writer);
	    writelinkp(id, id2, "_trdy1", "i_trdyplus", idp2, writer);
	    writelinkp(id, id1, "_trdy0", "i_trdyminus", idp1, writer);
	    writelinkp(id, id2, "_trdy0", "i_trdyminus1", idp2, writer);
	}

	private static void genjoin(String id, String id1, String idp, JsonNode y, PrintWriter writer) {
		if(printoutput) {
			System.out.println("//genjoin " + id);
		}
	    writer.println("//genjoin " + id);
	    writemarking(id, "a_trdy", writer);
	    writepriority(id, "a_trdy", 1, writer);
	    writeblock(id, "a_trdy", writer);
	    writepriority__(id, "a_trdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"a_trdy1  t_"+id+"a_trdyminus1");
			System.out.println("t_"+id+"a_trdyminus1  p_"+id+"a_trdy0");
		}
	    writer.println("p_"+id+"a_trdy1  t_"+id+"a_trdyminus1");
	    writer.println("t_"+id+"a_trdyminus1  p_"+id+"a_trdy0");
	    //writeblock(id, "a_irdy", writer);
	    writemarking(id, "b_trdy", writer);
	    writepriority(id, "b_trdy", 1, writer);
	    writeblock(id, "b_trdy", writer);
	    writepriority__(id, "b_trdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"b_trdy1  t_"+id+"b_trdyminus1");
			System.out.println("t_"+id+"b_trdyminus1  p_"+id+"b_trdy0");
		}
	    writer.println("p_"+id+"b_trdy1  t_"+id+"b_trdyminus1");
	    writer.println("t_"+id+"b_trdyminus1  p_"+id+"b_trdy0");
	    //writeblock(id, "b_irdy", writer);
	    writemarking(id, "o_irdy", writer);
	    writepriority(id, "o_irdy", 1, writer);
	    writeblock(id, "o_irdy", writer);
	    writepriority__(id, "o_irdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"o_irdy1  t_"+id+"o_irdyminus1");
			System.out.println("t_"+id+"o_irdyminus1  p_"+id+"o_irdy0");
		}
	    writer.println("p_"+id+"o_irdy1  t_"+id+"o_irdyminus1");
	    writer.println("t_"+id+"o_irdyminus1  p_"+id+"o_irdy0");
	    //writeblock(id, "o_trdy", writer);
	    String id3 = searchlist_first(id);
	    String id4 = searchlist_second(id);
	    String id5 = searchlist_first_(id);
	    String id6 = searchlist_second_(id);
	    writelinki(id, id3, "_irdy0", "a_trdyminus1", id5, writer);
	    writelinki(id, id3, "_irdy1", "a_trdyplus", id5, writer);
	    writelinki(id, id4, "_irdy0", "b_trdyminus1", id6, writer);
	    writelinki(id, id4, "_irdy1", "b_trdyplus", id6, writer);
	    writelinki(id, id3, "_irdy1", "o_irdyplus", id5, writer);
	    writelinki(id, id4, "_irdy1", "o_irdyplus", id6, writer);
	    writelinki(id, id3, "_irdy0", "o_irdyminus", id5, writer);
	    writelinki(id, id4, "_irdy0", "o_irdyminus1", id6, writer);
	//printsearchlist();
	    writelinkp(id, id1, "_trdy0", "a_trdyminus", idp, writer);
	    writelinkp(id, id1, "_trdy1", "a_trdyplus", idp, writer);
	    writelinkp(id, id1, "_trdy0", "b_trdyminus", idp, writer);
	    writelinkp(id, id1, "_trdy1", "b_trdyplus", idp, writer);
	}

	private static void genswitch(String id, String id1, String id2, String idp1, String idp2, JsonNode y, PrintWriter writer) {
		if(printoutput) {
			System.out.println("//genswitch " + id);
		}
	    writer.println("//genswitch " + id);
	    writemarking(id, "_sw", writer);
	    writepriority(id, "_sw", 1, writer);
	    writeblock(id, "_sw", writer);
	    writemarking(id, "a_irdy", writer);
	    writepriority(id, "a_irdy", 1, writer);
	    writeblock(id, "a_irdy", writer);
	    writemarking(id, "b_irdy", writer);
	    writepriority(id, "b_irdy", 1, writer);
	    writeblock(id, "b_irdy", writer);
	    writemarking(id, "i_trdy", writer);
	    writepriority(id, "i_trdy", 1, writer);
	    writeblock(id, "i_trdy", writer);
	    writepriority_(id, "i_trdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"i_trdy0  t_"+id+"i_trdyplus1");
			System.out.println("t_"+id+"i_trdyplus1  p_"+id+"i_trdy1");
		}
	    writer.println("p_"+id+"i_trdy0  t_"+id+"i_trdyplus1");
	    writer.println("t_"+id+"i_trdyplus1  p_"+id+"i_trdy1");
	    writepriority__(id, "i_trdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"i_trdy1  t_"+id+"i_trdyminus1");
			System.out.println("t_"+id+"i_trdyminus1  p_"+id+"i_trdy0");
		}
	    writer.println("p_"+id+"i_trdy1  t_"+id+"i_trdyminus1");
	    writer.println("t_"+id+"i_trdyminus1  p_"+id+"i_trdy0");
	    writepriority__(id, "i_trdy", 2, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"i_trdy1  t_"+id+"i_trdyminus2");
			System.out.println("t_"+id+"i_trdyminus2  p_"+id+"i_trdy0");
		}
	    writer.println("p_"+id+"i_trdy1  t_"+id+"i_trdyminus2");
	    writer.println("t_"+id+"i_trdyminus2  p_"+id+"i_trdy0");
	    writepriority__(id, "i_trdy", 3, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"i_trdy1  t_"+id+"i_trdyminus3");
			System.out.println("t_"+id+"i_trdyminus3  p_"+id+"i_trdy0");
		}
	    writer.println("p_"+id+"i_trdy1  t_"+id+"i_trdyminus3");
	    writer.println("t_"+id+"i_trdyminus3  p_"+id+"i_trdy0");
	    writebidir(id, "_sw0", "b_irdyplus", writer);
	    writebidir(id, "_sw1", "a_irdyplus", writer);
	    writebidir(id, "a_irdy1", "i_trdyplus", writer);
	    writebidir(id, "b_irdy1", "i_trdyplus1", writer);
	    writebidir(id, "a_irdy0", "i_trdyminus3", writer);
	    writebidir(id, "a_irdy0", "i_trdyminus2", writer);
	    writebidir(id, "b_irdy0", "i_trdyminus3", writer);
	    writebidir(id, "b_irdy0", "i_trdyminus1", writer);
	    String id2_ = searchlist(id);
	    String id3 = searchlist_(id);
	    //writelink(id, id2_, "o_irdy0", "a_irdyminus", writer);
	    if(id3.equals("a")) writelink(id, id2_, "a_irdy0", "a_irdyminus", writer);
	    else if(id3.equals("b")) writelink(id, id2_, "b_irdy0", "a_irdyminus", writer);
	    else writelink(id, id2_, "o_irdy0", "a_irdyminus", writer);
	    //writelink(id, id2_, "o_irdy1", "a_irdyplus", writer);
	    if(id3.equals("a")) writelink(id, id2_, "a_irdy1", "a_irdyplus", writer);
	    else if(id3.equals("b")) writelink(id, id2_, "b_irdy1", "a_irdyplus", writer);
	    else writelink(id, id2_, "o_irdy1", "a_irdyplus", writer);
	    //writelink(id, id2_, "o_irdy0", "b_irdyminus", writer);
	    if(id3.equals("a")) writelink(id, id2_, "a_irdy0", "b_irdyminus", writer);
	    else if(id3.equals("b")) writelink(id, id2_, "b_irdy0", "b_irdyminus", writer);
	    else writelink(id, id2_, "o_irdy0", "b_irdyminus", writer);
	    //writelink(id, id2_, "o_irdy1", "b_irdyplus", writer);
	    if(id3.equals("a")) writelink(id, id2_, "a_irdy1", "b_irdyplus", writer);
	    else if(id3.equals("b")) writelink(id, id2_, "b_irdy1", "b_irdyplus", writer);
	    else writelink(id, id2_, "o_irdy1", "b_irdyplus", writer);
	    writelinkp(id, id1, "_trdy1", "i_trdyplus", idp1, writer);
	    writelinkp(id, id2, "_trdy1", "i_trdyplus1", idp2, writer);
	    writelinkp(id, id1, "_trdy0", "i_trdyminus", idp1, writer);
	    writelinkp(id, id1, "_trdy0", "i_trdyminus1", idp1, writer);
	    writelinkp(id, id2, "_trdy0", "i_trdyminus", idp2, writer);
	    writelinkp(id, id2, "_trdy0", "i_trdyminus2", idp2, writer);
	}

	private static void genmerge(String id, String id1, String idp, JsonNode y, PrintWriter writer) {
		if(printoutput) {
			System.out.println("//genmerge " + id);
		}
	    writer.println("//genmerge " + id);
	    writemarking(id, "_u", writer);
	    writepriority(id, "_u", 1, writer);
	    writeblock(id, "_u", writer);
	    writemarking(id, "a_trdy", writer);
	    writepriority(id, "a_trdy", 1, writer);
	    writeblock(id, "a_trdy", writer);
	    writepriority__(id, "a_trdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"a_trdy1  t_"+id+"a_trdyminus1");
			System.out.println("t_"+id+"a_trdyminus1  p_"+id+"a_trdy0");
		}
	    writer.println("p_"+id+"a_trdy1  t_"+id+"a_trdyminus1");
	    writer.println("t_"+id+"a_trdyminus1  p_"+id+"a_trdy0");
	    writepriority__(id, "a_trdy", 2, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"a_trdy1  t_"+id+"a_trdyminus2");
			System.out.println("t_"+id+"a_trdyminus2  p_"+id+"a_trdy0");
		}
	    writer.println("p_"+id+"a_trdy1  t_"+id+"a_trdyminus2");
	    writer.println("t_"+id+"a_trdyminus2  p_"+id+"a_trdy0");
	    writemarking(id, "b_trdy", writer);
	    writepriority(id, "b_trdy", 1, writer);
	    writeblock(id, "b_trdy", writer);
	    writepriority__(id, "b_trdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"b_trdy1  t_"+id+"b_trdyminus1");
			System.out.println("t_"+id+"b_trdyminus1  p_"+id+"b_trdy0");
		}
	    writer.println("p_"+id+"b_trdy1  t_"+id+"b_trdyminus1");
	    writer.println("t_"+id+"b_trdyminus1  p_"+id+"b_trdy0");
	    writepriority__(id, "b_trdy", 2, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"b_trdy1  t_"+id+"b_trdyminus2");
			System.out.println("t_"+id+"b_trdyminus2  p_"+id+"b_trdy0");
		}
	    writer.println("p_"+id+"b_trdy1  t_"+id+"b_trdyminus2");
	    writer.println("t_"+id+"b_trdyminus2  p_"+id+"b_trdy0");
	    writemarking(id, "o_irdy", writer);
	    writepriority(id, "o_irdy", 1, writer);
	    writeblock(id, "o_irdy", writer);
	    writepriority_(id, "o_irdy", 1, 1, writer);
		if(printoutput) {
			System.out.println("p_"+id+"o_irdy0  t_"+id+"o_irdyplus1");
			System.out.println("t_"+id+"o_irdyplus1  p_"+id+"o_irdy1");
		}
	    writer.println("p_"+id+"o_irdy0  t_"+id+"o_irdyplus1");
	    writer.println("t_"+id+"o_irdyplus1  p_"+id+"o_irdy1");
	    writebidir(id, "_u0", "a_trdyminus", writer);
	    writebidir(id, "_u1", "a_trdyplus", writer);
	    writebidir(id, "_u0", "b_trdyplus", writer);
	    writebidir(id, "_u1", "b_trdyminus", writer);
	    String id3 = searchlist_first(id);
	    String id4 = searchlist_second(id);
	    String id5 = searchlist_first_(id);
	    String id6 = searchlist_second_(id);
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

	private static void genqueue(String id, String id1, String idp, JsonNode y, PrintWriter writer) {
		if(printoutput) {
			System.out.println("//genqueue " + id);
		}
	    writer.println("//genqueue " + id);
	    writemarking(id, "o_irdy", writer);
	    writepriority(id, "o_irdy", 2, writer);
	    writeblock(id, "o_irdy", writer);
	    writemarking_(id, "i_trdy", writer);
	    writepriority(id, "i_trdy", 2, writer);
	    writeblock(id, "i_trdy", writer);
	    writemarking(id, "_q", writer);
	    writepriority(id, "_q", 2, writer);
	    writeblock(id, "_q", writer);
	    writebidir(id, "_q0", "o_irdyminus", writer);
	    writebidir(id, "_q0", "i_trdyplus", writer);
	    writebidir(id, "_q1", "o_irdyplus", writer);
	    writebidir(id, "_q1", "i_trdyminus", writer);
	    writebidir(id, "o_irdy1", "_qminus", writer);
	    writebidir(id, "i_trdy1", "_qplus", writer);
	    //writebidir(id, "i_irdy1", "_qplus", writer);
	    String id2 = searchlist(id);
	    String id3 = searchlist_(id);
	    //writelink(id, id2, "o_irdy1", "_qplus", writer);
	    if(id3.equals("a")) writelink(id, id2, "a_irdy1", "_qplus", writer);
	    else if(id3.equals("b")) writelink(id, id2, "b_irdy1", "_qplus", writer);
	    else writelink(id, id2, "o_irdy1", "_qplus", writer);
	    //writebidir(id, "o_trdy1", "_qminus", writer);
	    //writelink(id, id1, "i_trdy1", "_qminus", writer);
	    writelinkp(id, id1, "_trdy1", "_qminus", idp, writer);
	}

	private static void genqueue2p(int size, String id, String id1, String idp, JsonNode y, PrintWriter writer) {
	    int inc=0;
		if(printoutput) {
			System.out.println("//genqueue2p " + id);
		}
	    writer.println("//genqueue2p " + id);
	    writemarking(id, "o_irdy", writer);
	    writepriority(id, "o_irdy", 2, writer);
	    writeblock(id, "o_irdy", writer);
	    for(int i=1;i<size;i++) {
	      writepriority_(id, "o_irdy", i, 2, writer);
	      if(printoutput) {
	    	  System.out.println("p_"+id+"o_irdy0  t_"+id+"o_irdyplus"+i);
	    	  System.out.println("t_"+id+"o_irdyplus"+i+" p_"+id+"o_irdy1");
	      }
	      writer.println("p_"+id+"o_irdy0  t_"+id+"o_irdyplus"+i);
	      writer.println("t_"+id+"o_irdyplus"+i+" p_"+id+"o_irdy1");
	    }
	    writemarking_(id, "i_trdy", writer);
	    writepriority(id, "i_trdy", 2, writer);
	    writeblock(id, "i_trdy", writer);
	    for(int i=1;i<size;i++) {
	      writepriority_(id, "i_trdy", i, 2, writer);
	      if(printoutput) {
	    	  System.out.println("p_"+id+"i_trdy0  t_"+id+"i_trdyplus"+i);
	    	  System.out.println("t_"+id+"i_trdyplus"+i+" p_"+id+"i_trdy1");
	      }
	      writer.println("p_"+id+"i_trdy0  t_"+id+"i_trdyplus"+i);
	      writer.println("t_"+id+"i_trdyplus"+i+" p_"+id+"i_trdy1");
	    }
	    for(int i=1;i<=size;i++) {
	      writemarking(id, "_q"+i, writer);
	      writepriority(id, "_q"+i, 2, writer);
	      writeblock(id, "_q"+i, writer);
	    }
	    for(int i=1;i<=size;i++) {
	      if(i==1) writemarking_(id, "_hd"+i, writer);
	      else writemarking(id, "_hd"+i, writer);
	      writepriority(id, "_hd"+i, 1, writer);
	      writeblock(id, "_hd"+i, writer);
	    }
	    for(int i=1;i<=size;i++) {
	      if(i==size) writemarking(id, "_tl"+i, writer);
	      else writemarking(id, "_tl"+i, writer);
	      writepriority(id, "_tl"+i, 1, writer);
	      writeblock(id, "_tl"+i, writer);
	    }
	    for(int i=1;i<=size;i++) {
	      writebidir(id, "i_trdy1", "_q"+i+"plus", writer);
	      writebidir(id, "o_irdy1", "_q"+i+"minus", writer);
	    }
	    for(int i=1;i<=size;i++) {
	      writebidir(id, "_q"+i+"0", "o_irdyminus", writer);
	      writebidir(id, "_q"+i+"1", "i_trdyminus", writer);
	    }
	    writebidir(id, "_q1"+"0", "i_trdyplus", writer);
	    for(int i=2;i<=size;i++) {
	      writebidir(id, "_q"+i+"0", "i_trdyplus"+(i-1), writer);
	    }
	    writebidir(id, "_q1"+"1", "o_irdyplus", writer);
	    for(int i=2;i<=size;i++) {
	      writebidir(id, "_q"+i+"1", "o_irdyplus"+(i-1), writer);
	    }
	    for(int i=1;i<=size;i++) {
	        writebidir(id, "_q"+i+"0", "_hd"+i+"plus", writer);
	        writebidir(id, "_q"+i+"1", "_hd"+i+"minus", writer);
	        if(i==size) inc=1; else inc=i+1;
	        //writebidir(id, "_q"+i+"0", "_hd"+(inc)+"minus", writer);
	        writebidir(id, "_q"+i+"1", "_hd"+(inc)+"plus", writer);
	    }
	    for(int i=1;i<=size;i++) {
	        writebidir(id, "_q"+i+"0", "_tl"+i+"minus", writer);
	        writebidir(id, "_q"+i+"1", "_tl"+i+"plus", writer);
	        if(i==size) inc=1; else inc=i+1;
	        writebidir(id, "_q"+i+"0", "_tl"+(inc)+"plus", writer);
	        //writebidir(id, "_q"+i+"1", "_tl"+(inc)+"minus", writer);
	    }
	    for(int i=1;i<=size;i++) {
	        writebidir(id, "_hd"+i+"1", "_q"+i+"plus", writer);
	    }
	    for(int i=1;i<=size;i++) {
	        writebidir(id, "_tl"+i+"1", "_q"+i+"minus", writer);
	    }
	    String id2 = searchlist(id);
	    String id3 = searchlist_(id);
	    for(int i=1;i<=size;i++) {
	        if(id3.equals("a")) writelink(id, id2, "a_irdy1", "_q"+i+"plus", writer);
	        else if(id3.equals("b")) writelink(id, id2, "b_irdy1", "_q"+i+"plus", writer);
	        else writelink(id, id2, "o_irdy1", "_q"+i+"plus", writer);
	    }
	    for(int i=1;i<=size;i++) {
	      writelinkp(id, id1, "_trdy1", "_q"+i+"minus", idp, writer);
	    }
	}

	public static void init_parse(String args) throws Exception {
		JsonFactory f = new MappingJsonFactory();
	    JsonParser jp = f.createJsonParser(new File(args));

	    JsonToken current;

	    current = jp.nextToken();
	    if (current != JsonToken.START_OBJECT) {
	      System.out.println("Error: root should be object: quiting.");
	      return;
	    }

	    while (jp.nextToken() != JsonToken.END_OBJECT) {
	        String fieldName = jp.getCurrentName();
	        // move from field name to field value
	        current = jp.nextToken();

	        if (fieldName.equals("NETWORK")) {
	          if (current == JsonToken.START_ARRAY) {
	            // For each of the records in the array
	            while (jp.nextToken() != JsonToken.END_ARRAY) {
	              JsonNode node = jp.readValueAsTree();
	              String idName = node.get("id").getValueAsText();
	              String idName1 = "";
	              String idName2 = "";
	              String idNamep1 = "";
	              String idNamep2 = "";
	              String typeName = node.get("type").getValueAsText();
	              System.out.println("id: " + idName + "type: " + typeName);
	              lst_.add(new Ids(idName,typeName));
	              JsonNode y = node.get("outs");
	              if(y!=null) {
	               for(int i=0;y.has(i);i++) {
	                if(y.get(i).has("id")) {
	                  if(i==0) {
	                    idName1 = y.get(i).get("id").getValueAsText();
	                    idNamep1 = y.get(i).get("in_port").getValueAsText();
	                    if(typeName.equals("xfork")) lst.add(new Info(idName1,idName,"b",idNamep1));
	                    else if(typeName.equals("xswitch")) lst.add(new Info(idName1,idName,"a",idNamep1));
	                    else lst.add(new Info(idName1,idName,"",idNamep1));
	                  }
	                  else if(i==1) {
	                    idName2 = y.get(i).get("id").getValueAsText();
	                    idNamep2 = y.get(i).get("in_port").getValueAsText();
	                    if(typeName.equals("xfork")) lst.add(new Info(idName2,idName,"a",idNamep2));
	                    else if(typeName.equals("xswitch")) lst.add(new Info(idName2,idName,"b",idNamep2));
	                    else lst.add(new Info(idName2,idName,"",idNamep2));
	                  }
	                }
	               }
	              }
	            }
	          } else {
	            System.out.println("Error: records should be an array: skipping.");
	            jp.skipChildren();
	          }
	        } else {
	          System.out.println("Unprocessed property: " + fieldName);
	          jp.skipChildren();
	        }
	      }
	}

	public void run(WorkspaceEntry we) {
		System.out.println("");
		//VisualCircuit vnet = (VisualCircuit)we.getModelEntry().getVisualModel();
		//Circuit cnet = (Circuit)we.getModelEntry().getModel();
		//VisualCircuit vnet = (VisualCircuit)we.getModelEntry().getMathModel();
		Xmas cnet = (Xmas)we.getModelEntry().getMathModel();

		JsonFactory f = new MappingJsonFactory();
		File file = new File("/home/frank/git/CPNGenFile");
	    PrintWriter writer = null;
	    try
	    {
	    	initlist();
	    	init_parse("/home/frank/git/JsonExFile");
	    	writer = new PrintWriter(file);
	        JsonParser jp = f.createJsonParser(new File("/home/frank/git/JsonExFile"));
	        JsonToken current;

	        current = jp.nextToken();
	        if (current != JsonToken.START_OBJECT) {
	          System.out.println("Error: root should be object: quiting.");
	          return;
	        }

	        while (jp.nextToken() != JsonToken.END_OBJECT) {
	            String fieldName = jp.getCurrentName();
	            // move from field name to field value
	            current = jp.nextToken();
	            if (fieldName.equals("VARS")) {
	              if (current == JsonToken.START_ARRAY) {
	                while (jp.nextToken() != JsonToken.END_ARRAY) {
	                }
	              }
	            }
	            else if (fieldName.equals("PACKET_TYPE")) {
	              if (current == JsonToken.START_OBJECT) {
	                while (jp.nextToken() != JsonToken.END_OBJECT) {
	                }
	              }
	            }
	            else if (fieldName.equals("COMPOSITE_OBJECTS")) {
	              if (current == JsonToken.START_ARRAY) {
	                while (jp.nextToken() != JsonToken.END_ARRAY) {
	                }
	              }
	            }
	            else if (fieldName.equals("NETWORK")) {
	                if (current == JsonToken.START_ARRAY) {
	                  // For each of the records in the array
	                  while (jp.nextToken() != JsonToken.END_ARRAY) {
	                    // read the record into a tree model,
	                  JsonNode node = jp.readValueAsTree();
	                  String idName = node.get("id").getValueAsText();
	                  String idName1 = "";
	                  String idName2 = "";
	                  String idNamep1 = "";
	                  String idNamep2 = "";
	                  String fieldsize = "";
	                  String typeName = node.get("type").getValueAsText();

	                  JsonNode y = node.get("outs");
	                  if(y!=null) {
	                    for(int i=0;y.has(i);i++) {
	                      if(y.get(i).has("id")) {
	                        if(i==0) {
	                          idName1 = y.get(i).get("id").getValueAsText();
	                          /*if(typeName.equals("xfork")) lst.add(new Info(idName1,idName,"b"));
	                          else if(typeName.equals("xswitch")) lst.add(new Info(idName1,idName,"a"));
	                          else lst.add(new Info(idName1,idName,""));*/
	                        }
	                        else if(i==1) {
	                          idName2 = y.get(i).get("id").getValueAsText();
	                          /*if(typeName.equals("xfork")) lst.add(new Info(idName2,idName,"a"));
	                          else if(typeName.equals("xswitch")) lst.add(new Info(idName2,idName,"b"));
	                          else lst.add(new Info(idName2,idName,""));*/
	                        }
	                      }
	                      if(y.get(i).has("in_port")) {
	                        if(i==0) {
	                          String searchtyp="";
	                          searchtyp=searchlist__(idName1);
	                          if(searchtyp.equals("join")) {
	                            //idNamep1 = y.get(i).get("in_port").getValueAsText();
	                            if(y.get(i).get("in_port").getValueAsText().equals("0")) idNamep1="1";
	                            else idNamep1="0";
	                          }
	                          else if(searchtyp.equals("merge")) idNamep1 = y.get(i).get("in_port").getValueAsText();
	                        }
	                        else if(i==1) {
	                          String searchtyp1="";
	                          searchtyp1=searchlist__(idName2);
	                          if(searchtyp1.equals("join")) {
	                            //idNamep2 = y.get(i).get("in_port").getValueAsText();
	                            if(y.get(i).get("in_port").getValueAsText().equals("0")) idNamep2="1";
	                            else idNamep2="0";
	                          }
	                          else if(searchtyp1.equals("merge")) idNamep2 = y.get(i).get("in_port").getValueAsText();
	                        }
	                      }
	                    }
	                  }
	                  JsonNode y_ = node.get("fields");
	                  if(y_!=null) {
	                    for(int i=0;y_.has(i);i++) {
	                      if(y_.get(i).has("size")) {
	                        fieldsize = y_.get(i).get("size").getValueAsText();
	                      }
	                    }
	                  }

	                  if(typeName.equals("source")) gensource(idName,idName1,idNamep1,y,writer);
	                  if(typeName.equals("function")) genfunction(idName,idName1,idNamep1,y,writer);
	                  if(typeName.equals("xfork")) genfork(idName,idName1,idName2,idNamep1,idNamep2,y,writer);
	                  if(typeName.equals("join")) genjoin(idName,idName1,idNamep1,y,writer);
	                  if(typeName.equals("xswitch")) genswitch(idName,idName1,idName2,idNamep1,idNamep2,y,writer);
	                  if(typeName.equals("merge")) genmerge(idName,idName1,idNamep1,y,writer);
	                  if(typeName.equals("queue")) {
	                    if(fieldsize.equals("1")) genqueue(idName,idName1,idNamep1,y,writer);
	                    else genqueue2p(2,idName,idName1,idNamep1,y,writer);
	                  }
	                  if(typeName.equals("sink")) gensink(idName,writer);
	                  }
	                } else {
	                  System.out.println("Error: records should be an array: skipping.");
	                  jp.skipChildren();
	                }
	              } else {
	                System.out.println("Unprocessed property: " + fieldName);
	                jp.skipChildren();
	              }
	            }
	    }
	    catch (Exception e)
	    {
	            e.printStackTrace();
	    }
	    finally
	    {
	            if ( writer != null )
	            {
	                writer.close();
	        		System.out.println("CPN created");
	                PNetConv pnconv = new PNetConv();
	            }
	    }
		System.out.println("");
	}
}