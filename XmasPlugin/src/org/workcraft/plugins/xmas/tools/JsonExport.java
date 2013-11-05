package org.workcraft.plugins.xmas.tools;

import java.awt.Color;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;

import org.workcraft.Tool;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.plugins.xmas.VisualXmas;
import org.workcraft.plugins.xmas.Xmas;
import org.workcraft.plugins.xmas.XmasSettings;
import org.workcraft.plugins.xmas.components.XmasContact;
import org.workcraft.plugins.xmas.components.ForkComponent;
import org.workcraft.plugins.xmas.components.FunctionComponent;
import org.workcraft.plugins.xmas.components.JoinComponent;
import org.workcraft.plugins.xmas.components.MergeComponent;
import org.workcraft.plugins.xmas.components.QueueComponent;
import org.workcraft.plugins.xmas.components.SinkComponent;
import org.workcraft.plugins.xmas.components.SourceComponent;
import org.workcraft.plugins.xmas.components.SwitchComponent;
import org.workcraft.plugins.xmas.components.VisualSourceComponent;
import org.workcraft.util.WorkspaceUtils;
import org.workcraft.workspace.WorkspaceEntry;


public class JsonExport implements Tool {

	//private final Framework framework;

	//VisualCircuit circuit;
	//private CheckCircuitTask checkTask;


	//ProgressMonitor<? super MpsatChainResult> monitor;

	/*public CircuitTestTool(Framework framework, Workspace ws) {
		this.framework = framework;
//		this.ws = ws;
	}*/


	public String getDisplayName() {
		return "Export to JSON file";
	}


	public String getSection() {
		return "Export";
	}


	public boolean isApplicableTo(WorkspaceEntry we) {
		return WorkspaceUtils.canHas(we, Xmas.class);
	}


	public void run(WorkspaceEntry we) {
		System.out.println("Running tests");
		VisualXmas vnet = (VisualXmas)we.getModelEntry().getVisualModel();
		//Circuit cnet = (Circuit)we.getModelEntry().getModel();
		//VisualCircuit vnet = (VisualCircuit)we.getModelEntry().getMathModel();

		Xmas cnet = (Xmas)we.getModelEntry().getMathModel();

		SourceComponent src_node = null;
		SinkComponent snk_node = null;

		FunctionComponent fun_node = null;
		QueueComponent qu_node = null;

		ForkComponent frk_node = null;
		JoinComponent jn_node = null;

		SwitchComponent sw_node = null;
		MergeComponent mrg_node = null;

		//CommonVisualSettings.setForegroundColor(new Color(0, 0, 0, 64));
		VisualSourceComponent src_v;
		for (Node node : vnet.getNodes()) {
            if(node instanceof VisualSourceComponent) {
        		//System.out.println("Changing Colour ...............src_v");
            	src_v=(VisualSourceComponent)node;
            	src_v.setColorRed();
                src_v.setForegroundColor(new Color(255, 0, 0, 64));
            }
		}
		//for (VisualSourceComponent node : vnet.getVisualSourceComponent()) {

		//}

		for (SourceComponent node : cnet.getSourceComponent()) {
			//System.out.println("Name =" + node.getName());
			//System.out.println("Name =" + cnet.getPostset(node).size());
			//System.out.println("Num =" + cnet.getPostset(node).size());
			System.out.println("Name =" + cnet.getName(node));
			src_node=node;
		}
		//System.out.println("Name_ =" + cnet.getName(src_node));
		XmasContact contact_node = null;
		//Collection<Contact> contacts = src_node.getContacts();
		Collection<XmasContact> contacts = src_node.getOutputs();
		Collection<XmasContact> contacts_ = src_node.getOutputs();
		for(XmasContact node : contacts) {
			//System.out.println("OutputContact =" + cnet.getName(node));
			contact_node=node;
		}
		/* //All contacts
		for (Contact node : cnet.getContact()) {
			System.out.println("Name =" + cnet.getName(node));
			contact_node=node;
		}*/
		System.out.println("Name_ =" + cnet.getName(contact_node));
		for (Connection c : cnet.getConnections(contact_node)) {
			//System.out.println("OutputConnection =" + cnet.getName(c));
			//if((c.getFirst() instanceof Contact)) System.out.println("Found First Contact");
			if((c.getSecond() instanceof XmasContact)) {
				System.out.println("Found Output Contact" + cnet.getName(c.getSecond()));
				Node cp_node = c.getSecond().getParent();
				//System.out.println("Found Output Component" + cnet.getName(cp_node));
			}
		}
		int num_nodes=0;
		for (Node node : cnet.getNodes()) {
            //System.out.println("Name =" + cnet.getName(node));
			num_nodes++;
		}
		//GEN JSON
		File file = new File(XmasSettings.getJasonFileName());
	    PrintWriter writer = null;
		try
		{
	    writer = new PrintWriter(file);
		int cnt_nodes=0;
		int num_outputs=0;
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
			cnt_nodes++;
			System.out.println("    {");
			writer.println("    {");
            System.out.println("      \"id\": \"" + cnet.getName(node) + "\",");
            writer.println("      \"id\": \"" + cnet.getName(node) + "\",");
            if(cnet.getType(node).equals("fork")) {
            	System.out.println("      \"type\": \"" + "xfork" + "\",");
                writer.println("      \"type\": \"" + "xfork" + "\",");
            }
            else if(cnet.getType(node).equals("switch")) {
            	System.out.println("      \"type\": \"" + "xswitch" + "\",");
                writer.println("      \"type\": \"" + "xswitch" + "\",");
            }
            else {
            	System.out.println("      \"type\": \"" + cnet.getType(node) + "\",");
            	writer.println("      \"type\": \"" + cnet.getType(node) + "\",");
            }
            System.out.println("      \"outs\" : [");
            writer.println("      \"outs\" : [");
            if(node instanceof SourceComponent) {
            	src_node=(SourceComponent)node;
            	contacts=src_node.getOutputs();
        		num_outputs=1;
            }
            else if(node instanceof FunctionComponent) {
            	fun_node=(FunctionComponent)node;
            	contacts=fun_node.getOutputs();
        		num_outputs=1;
            }
            else if(node instanceof QueueComponent) {
            	qu_node=(QueueComponent)node;
            	contacts=qu_node.getOutputs();
        		num_outputs=1;
            }
            else if(node instanceof ForkComponent) {
            	frk_node=(ForkComponent)node;
            	contacts=frk_node.getOutputs();
        		num_outputs=2;
            }
            else if(node instanceof JoinComponent) {
            	jn_node=(JoinComponent)node;
            	contacts=jn_node.getOutputs();
        		num_outputs=1;
            }
            else if(node instanceof SwitchComponent) {
            	sw_node=(SwitchComponent)node;
            	contacts=sw_node.getOutputs();
        		num_outputs=2;
            }
            else if(node instanceof MergeComponent) {
            	mrg_node=(MergeComponent)node;
            	contacts=mrg_node.getOutputs();
        		num_outputs=1;
            }
            else if(node instanceof SinkComponent) {
            	snk_node=(SinkComponent)node;
            	contacts=snk_node.getOutputs();
            }
            for(XmasContact contact_node_ : contacts) {
            	for (Connection c : cnet.getConnections(contact_node_)) {
            		if((c.getSecond() instanceof XmasContact)) {
            			//System.out.println("Output Contact" + cnet.getName(c.getSecond()));
            			System.out.println("        {");
            			writer.println("        {");
            			Node cp_node = c.getSecond().getParent();
                		int contact_cnt=1;
                		int contact_no=1;
            			if(cp_node instanceof JoinComponent) {
            				jn_node=(JoinComponent)cp_node;
                        	contacts_=jn_node.getInputs();
                        	for(XmasContact jncnt_node : contacts_) {
                        		if(jncnt_node==c.getSecond()) {
                        			//System.out.println("  Found jn contact = " + contact_no);
                        			contact_no=contact_cnt;
                        		}
                    			contact_cnt++;
                        	}
            			}
            			else if(cp_node instanceof MergeComponent) {
            				mrg_node=(MergeComponent)cp_node;
                        	contacts_=mrg_node.getInputs();
                        	for(XmasContact mrgcnt_node : contacts_) {
                        		if(mrgcnt_node==c.getSecond()) {
                        			//System.out.println("  Found mrg contact = " + contact_no);
                        			contact_no=contact_cnt;
                        		}
                    			contact_cnt++;
                        	}
            			}
            			System.out.println("          \"id\": " + "\"" + cnet.getName(cp_node) + "\",");
            			writer.println("          \"id\": " + "\"" + cnet.getName(cp_node) + "\",");
            			System.out.println("          \"in_port\": " + (contact_no-1));
            			writer.println("          \"in_port\": " + (contact_no-1));
            			if(num_outputs>1) {
            				System.out.println("         },");
            				writer.println("         },");
            				num_outputs=1;  //fpb
            			}
            			else {
            				System.out.println("        }");
            				writer.println("        }");
            			}
            		}
            	}
            }
			if(node instanceof QueueComponent) {
			  System.out.println("      ],");
			  writer.println("      ],");
			}
			else {
			  System.out.println("      ]");
			  writer.println("      ]");
			}
			if(node instanceof QueueComponent) {
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
				int qusize = qu_node.getCapacity();
    			System.out.println("          \"size\": " + qusize);
    			writer.println("          \"size\": " + qusize);
				System.out.println("        }");
				writer.println("        }");
              	System.out.println("      ]");
              	writer.println("      ]");
			}
			if(cnt_nodes<num_nodes) {
			  System.out.println("    },");
			  writer.println("    },");
			}
			else {
		      System.out.println("    }");
		      writer.println("    }");
			}
		}
		System.out.println("  ]");
		writer.println("  ]");
		System.out.println("}");
		writer.println("}");
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
	            }
	    }
	}
}