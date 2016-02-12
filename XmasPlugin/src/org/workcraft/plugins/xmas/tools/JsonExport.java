package org.workcraft.plugins.xmas.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;

import org.workcraft.Tool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.Positioning;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
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
import org.workcraft.util.Hierarchy;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class JsonExport implements Tool {

    //private final Framework framework;

    //VisualCircuit circuit;
    //private CheckCircuitTask checkTask;


    //ProgressMonitor<? super MpsatChainResult> monitor;

    /*public CircuitTestTool(Framework framework, Workspace ws) {
        this.framework = framework;
//        this.ws = ws;
    }*/


    private static final Positioning LEFT = null;

    public String getDisplayName() {
        return "Export to JSON file";
    }


    public String getSection() {
        return "Export";
    }


    public boolean isApplicableTo(WorkspaceEntry we) {
        return WorkspaceUtils.canHas(we, Xmas.class);
    }

    //public Collection<String> srcNodes;
    public Collection<VisualSourceComponent> srcNodes;

    public void syncReset() {
        File syncFile = XmasSettings.getTempVxmSyncFile();
        PrintWriter writerS = null;
        try {
            writerS = new PrintWriter(syncFile);
            writerS.println("empty");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (writerS != null) {
                writerS.close();
            }
        }
    }

    public void run(WorkspaceEntry we) {
        System.out.println("Running tests");
        VisualXmas vnet = (VisualXmas)we.getModelEntry().getVisualModel();
        //Circuit cnet = (Circuit)we.getModelEntry().getModel();
        //VisualCircuit vnet = (VisualCircuit)we.getModelEntry().getMathModel();

        Xmas cnet = (Xmas)we.getModelEntry().getMathModel();

        //srcNodes = cnet.getSourceComponent();
        int no=1;

        SourceComponent srcNode = null;
        SinkComponent snkNode = null;

        FunctionComponent funNode = null;
        QueueComponent quNode = null;

        ForkComponent frkNode = null;
        JoinComponent jnNode = null;

        SwitchComponent swNode = null;
        MergeComponent mrgNode = null;

        int syncr=0;
        for (Node node : vnet.getNodes()) {
            if(node instanceof VisualSyncComponent) {
                syncr=1;
            }
        }
        if(syncr==0) {
            syncReset();
        }
        for(VisualGroup vg: Hierarchy.getDescendantsOfType(vnet.getRoot(), VisualGroup.class)) {
            for(VisualComponent vp: vg.getComponents()) {
                if(vp instanceof VisualSourceComponent) {
                    VisualSourceComponent vsc=(VisualSourceComponent)vp;
                    SourceComponent sc=vsc.getReferencedSourceComponent();
                    sc.setGr(no);
                } else if(vp instanceof VisualSinkComponent) {
                    VisualSinkComponent vsc=(VisualSinkComponent)vp;
                    SinkComponent sc=vsc.getReferencedSinkComponent();
                    sc.setGr(no);
                } else if(vp instanceof VisualFunctionComponent) {
                    VisualFunctionComponent vsc=(VisualFunctionComponent)vp;
                    FunctionComponent sc=vsc.getReferencedFunctionComponent();
                    sc.setGr(no);
                } else if(vp instanceof VisualQueueComponent) {
                    VisualQueueComponent vsc=(VisualQueueComponent)vp;
                    QueueComponent sc=vsc.getReferencedQueueComponent();
                    sc.setGr(no);
                } else if(vp instanceof VisualForkComponent) {
                    VisualForkComponent vsc=(VisualForkComponent)vp;
                    ForkComponent sc=vsc.getReferencedForkComponent();
                    sc.setGr(no);
                } else if(vp instanceof VisualJoinComponent) {
                    VisualJoinComponent vsc=(VisualJoinComponent)vp;
                    JoinComponent sc=vsc.getReferencedJoinComponent();
                    sc.setGr(no);
                } else if(vp instanceof VisualSwitchComponent) {
                    VisualSwitchComponent vsc=(VisualSwitchComponent)vp;
                    SwitchComponent sc=vsc.getReferencedSwitchComponent();
                    sc.setGr(no);
                } else if(vp instanceof VisualMergeComponent) {
                    VisualMergeComponent vsc=(VisualMergeComponent)vp;
                    MergeComponent sc=vsc.getReferencedMergeComponent();
                    sc.setGr(no);
                }
            }
            no++;
        }

        //Set Component properties from VisualComponent
        VisualSourceComponent vsc;
        VisualFunctionComponent fsc;
        VisualQueueComponent vqc;

        for (SourceComponent node : cnet.getSourceComponents()) {
            //System.out.println("Num =" + cnet.getPostset(node).size());
            //System.out.println("Name =" + cnet.getName(node));
            //((SourceComponent)node).setVal('b');
            //System.out.println("Val =" + ((SourceComponent)node).getType());
            srcNode=node;
        }
        //System.out.println("Name_ =" + cnet.getName(srcNode));
        XmasContact contactNode = null;
        //Collection<Contact> contacts = srcNode.getContacts();
        Collection<XmasContact> contacts = srcNode.getOutputs();
        Collection<XmasContact> contacts2 = srcNode.getOutputs();
        for(XmasContact node : contacts) {
            //System.out.println("OutputContact =" + cnet.getName(node));
            contactNode=node;
        }
        /* //All contacts
        for (Contact node : cnet.getContact()) {
            System.out.println("Name =" + cnet.getName(node));
            contactNode=node;
        }*/
        //System.out.println("Name_ =" + cnet.getName(contactNode));
        for (Connection c : cnet.getConnections(contactNode)) {
            //System.out.println("OutputConnection =" + cnet.getName(c));
            //if(c.getFirst() instanceof Contact) System.out.println("Found First Contact");
            if(c.getSecond() instanceof XmasContact) {
                //System.out.println("Found Output Contact" + cnet.getName(c.getSecond()));
                Node cpNode = c.getSecond().getParent();
                //System.out.println("Found Output Component" + cnet.getName(cpNode));
            }
        }
        int numNodes=0;
        for (Node node : cnet.getNodes()) {
            //System.out.println("Name =" + cnet.getName(node));
            //cnet.getLabel(node);
            numNodes++;
        }
        //GEN JSON
        File file = XmasSettings.getTempVxmJsonFile();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(file);
            int countNodes=0;
            int numOutputs=0;
            System.out.println("Generate Json");
            System.out.println("{");
            writer.println("{");
            System.out.println("  \"VARS\": [],");
            writer.println("  \"VARS\": [],");
            System.out.println("  \"PACKET_TYPE\": {},");
            writer.println("  \"PACKET_TYPE\": {},");
            System.out.println("  \"COMPOSITE_OBJECTS\": [],");
            writer.println("  \"COMPOSITE_OBJECTS\": [],");
            System.out.println("  \"NETWORK\": [");
            writer.println("  \"NETWORK\": [");
            for (Node node : cnet.getNodes()) {
                countNodes++;
                System.out.println("    {");
                writer.println("    {");
                String rstr;
                rstr = cnet.getName(node);
                rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
                System.out.println("      \"id\": \"" + cnet.getName(node) + "\",");
                //writer.println("      \"id\": \"" + cnet.getName(node) + "\",");
                writer.println("      \"id\": \"" + rstr + "\",");
                if(cnet.getType(node).equals("fork")) {
                    System.out.println("      \"type\": \"" + "xfork" + "\",");
                    writer.println("      \"type\": \"" + "xfork" + "\",");
                } else if(cnet.getType(node).equals("switch")) {
                    System.out.println("      \"type\": \"" + "xswitch" + "\",");
                    writer.println("      \"type\": \"" + "xswitch" + "\",");
                } else {
                    System.out.println("      \"type\": \"" + cnet.getType(node) + "\",");
                    writer.println("      \"type\": \"" + cnet.getType(node) + "\",");
                }
                System.out.println("      \"outs\" : [");
                writer.println("      \"outs\" : [");
                if(node instanceof SourceComponent) {
                    srcNode=(SourceComponent)node;
                    contacts=srcNode.getOutputs();
                    numOutputs=1;
                } else if(node instanceof SinkComponent) {
                    snkNode=(SinkComponent)node;
                    contacts=snkNode.getOutputs();
                    numOutputs=1;
                } else if(node instanceof FunctionComponent) {
                    funNode=(FunctionComponent)node;
                    contacts=funNode.getOutputs();
                    numOutputs=1;
                } else if(node instanceof QueueComponent) {
                    quNode=(QueueComponent)node;
                    contacts=quNode.getOutputs();
                    numOutputs=1;
                } else if(node instanceof ForkComponent) {
                    frkNode=(ForkComponent)node;
                    contacts=frkNode.getOutputs();
                    numOutputs=2;
                } else if(node instanceof JoinComponent) {
                    jnNode=(JoinComponent)node;
                    contacts=jnNode.getOutputs();
                    numOutputs=1;
                } else if(node instanceof SwitchComponent) {
                    swNode=(SwitchComponent)node;
                    contacts=swNode.getOutputs();
                    numOutputs=2;
                } else if(node instanceof MergeComponent) {
                    mrgNode=(MergeComponent)node;
                    contacts=mrgNode.getOutputs();
                    numOutputs=1;
                }
                for(XmasContact contactNode2 : contacts) {
                    for (Connection c : cnet.getConnections(contactNode2)) {
                        if(c.getSecond() instanceof XmasContact) {
                            //System.out.println("Output Contact" + cnet.getName(c.getSecond()));
                            System.out.println("        {");
                            writer.println("        {");
                            Node cpNode = c.getSecond().getParent();
                            int contactCount=1;
                            int contactNo=1;
                            if(cpNode instanceof JoinComponent) {
                                JoinComponent jnNode2=(JoinComponent)cpNode;
                                contacts2=jnNode2.getInputs();
                                for(XmasContact jncntNode : contacts2) {
                                    if(jncntNode==c.getSecond()) {
                                        //System.out.println("  Found jn contact = " + contactNo);
                                        contactNo=contactCount;
                                    }
                                    contactCount++;
                                }
                            } else if(cpNode instanceof MergeComponent) {
                                MergeComponent mrgNode2=(MergeComponent)cpNode;
                                contacts2=mrgNode2.getInputs();
                                for(XmasContact mrgcntNode : contacts2) {
                                    if(mrgcntNode==c.getSecond()) {
                                        //System.out.println("  Found mrg contact = " + contactNo);
                                        contactNo=contactCount;
                                    }
                                    contactCount++;
                                }
                            }
                            rstr = cnet.getName(cpNode);
                            rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
                            System.out.println("          \"id\": " + "\"" + cnet.getName(cpNode) + "\",");
                            //writer.println("          \"id\": " + "\"" + cnet.getName(cpNode) + "\",");
                            writer.println("          \"id\": " + "\"" + rstr + "\",");
                            System.out.println("          \"in_port\": " + (contactNo-1));
                            writer.println("          \"in_port\": " + (contactNo-1));
                            if(numOutputs>1) {
                                System.out.println("         },");
                                writer.println("         },");
                                numOutputs=1;  //fpb
                            } else {
                                System.out.println("        }");
                                writer.println("        }");
                            }
                        }
                    }
                }
                if(node instanceof SourceComponent) {
                    System.out.println("      ],");
                    writer.println("      ],");
                } else if(node instanceof QueueComponent) {
                    System.out.println("      ],");
                    writer.println("      ],");
                } else {
                    System.out.println("      ],");
                    writer.println("      ],");
                }
                /*else {
                  System.out.println("      ]");
                  writer.println("      ]");
                  }*/
                if(node instanceof SourceComponent) {
                    //char srcchecktype = srcNode.getVal();
                    //System.out.println("type = " + srcchecktype);
                    //int srccheckmode = srcNode.getMode();
                    //System.out.println("mode = " + srccheckmode);
                    System.out.println("      \"fields\": [");
                    writer.println("      \"fields\": [");
                    System.out.println("        {");
                    writer.println("        {");
                    int sgr = srcNode.getGr();
                    System.out.println("          \"gr\": " + sgr + ",");
                    writer.println("          \"gr\": " + sgr + ",");
                    String srcgp = srcNode.getGp();
                    System.out.println("          \"gpf\": " + srcgp);
                    writer.println("          \"gpf\": " + srcgp);
                    System.out.println("        }");
                    writer.println("        }");
                    System.out.println("      ]");
                    writer.println("      ]");
                } else if(node instanceof SinkComponent) {
                    System.out.println("      \"fields\": [");
                    writer.println("      \"fields\": [");
                    System.out.println("        {");
                    writer.println("        {");
                    int sgr = snkNode.getGr();
                    System.out.println("          \"gr\": " + sgr);
                    writer.println("          \"gr\": " + sgr);
                    System.out.println("        }");
                    writer.println("        }");
                    System.out.println("      ]");
                    writer.println("      ]");
                } else if(node instanceof FunctionComponent) {
                    //char fncheckfn = funNode.getVal();
                    //System.out.println("val = " + fncheckfn);
                    System.out.println("      \"fields\": [");
                    writer.println("      \"fields\": [");
                    System.out.println("        {");
                    writer.println("        {");
                    //String fs = funNode.getFun();
                    //System.out.println("          \"fn\": " + fs);
                    //writer.println("          \"fn\": " + fs + ",");
                    int fgr = funNode.getGr();
                    System.out.println("          \"gr\": " + fgr);
                    writer.println("          \"gr\": " + fgr);
                    System.out.println("        }");
                    writer.println("        }");
                    System.out.println("      ]");
                    writer.println("      ]");
                } else if(node instanceof QueueComponent) {
                    System.out.println("      \"fields\": [");
                    writer.println("      \"fields\": [");
                    System.out.println("        {");
                    writer.println("        {");
                    int quchecksize = quNode.getCapacity();
                    //System.out.println("capacity = " + quchecksize + ",");
                    int qusize = quNode.getCapacity();
                    //int qusize = 2;    //hardwire
                    //int qusize = 3;    //hardwire
                    System.out.println("          \"size\": " + qusize + ",");
                    writer.println("          \"size\": " + qusize + ",");
                    int quinit = quNode.getInit();
                    System.out.println("          \"init\": " + quinit + ",");
                    writer.println("          \"init\": " + quinit + ",");
                    int qgr = quNode.getGr();
                    System.out.println("          \"gr\": " + qgr + ",");
                    writer.println("          \"gr\": " + qgr + ",");
                    String qugp = quNode.getGp();
                    System.out.println("          \"gpf\": " + qugp);
                    writer.println("          \"gpf\": " + qugp);
                    System.out.println("        }");
                    writer.println("        }");
                    System.out.println("      ]");
                    writer.println("      ]");
                } else if(node instanceof ForkComponent) {
                    System.out.println("      \"fields\": [");
                    writer.println("      \"fields\": [");
                    System.out.println("        {");
                    writer.println("        {");
                    int fgr = frkNode.getGr();
                    System.out.println("          \"gr\": " + fgr);
                    writer.println("          \"gr\": " + fgr);
                    System.out.println("        }");
                    writer.println("        }");
                    System.out.println("      ]");
                    writer.println("      ]");
                } else if(node instanceof JoinComponent) {
                    System.out.println("      \"fields\": [");
                    writer.println("      \"fields\": [");
                    System.out.println("        {");
                    writer.println("        {");
                    int jgr = jnNode.getGr();
                    System.out.println("          \"gr\": " + jgr);
                    writer.println("          \"gr\": " + jgr);
                    System.out.println("        }");
                    writer.println("        }");
                    System.out.println("      ]");
                    writer.println("      ]");
                } else if(node instanceof SwitchComponent) {
                    System.out.println("      \"fields\": [");
                    writer.println("      \"fields\": [");
                    System.out.println("        {");
                    writer.println("        {");
                    int sgr = swNode.getGr();
                    System.out.println("          \"gr\": " + sgr);
                    writer.println("          \"gr\": " + sgr);
                    System.out.println("        }");
                    writer.println("        }");
                    System.out.println("      ]");
                    writer.println("      ]");
                } else if(node instanceof MergeComponent) {
                    System.out.println("      \"fields\": [");
                    writer.println("      \"fields\": [");
                    System.out.println("        {");
                    writer.println("        {");
                    int mgr = mrgNode.getGr();
                    System.out.println("          \"gr\": " + mgr);
                    writer.println("          \"gr\": " + mgr);
                    System.out.println("        }");
                    writer.println("        }");
                    System.out.println("      ]");
                    writer.println("      ]");
                }
                if(countNodes<numNodes) {
                    System.out.println("    },");
                    writer.println("    },");
                } else {
                    System.out.println("    }");
                    writer.println("    }");
                }
            }
            System.out.println("  ]");
            writer.println("  ]");
            System.out.println("}");
            writer.println("}");
            System.out.println("Output written to JsonFile");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
