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
//		this.ws = ws;
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

	//public Collection<String> src_nodes;
	public Collection<VisualSourceComponent> src_nodes;

	public void sync_reset() {
		File syncFile = new File(XmasSettings.getVxmDirectory(), "sync");
	    PrintWriter writer_s = null;
	    try {
			writer_s = new PrintWriter(syncFile);
			writer_s.println("empty");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    finally
	    {
	            if ( writer_s != null )
	            {
	                writer_s.close();
	            }
	    }
	}

	public void run(WorkspaceEntry we) {
		System.out.println("Running tests");
		VisualXmas vnet = (VisualXmas)we.getModelEntry().getVisualModel();
		//Circuit cnet = (Circuit)we.getModelEntry().getModel();
		//VisualCircuit vnet = (VisualCircuit)we.getModelEntry().getMathModel();

		Xmas cnet = (Xmas)we.getModelEntry().getMathModel();

		//src_nodes = cnet.getSourceComponent();
    	int no=1;

		SourceComponent src_node = null;
		SinkComponent snk_node = null;

		FunctionComponent fun_node = null;
		QueueComponent qu_node = null;

		ForkComponent frk_node = null;
		JoinComponent jn_node = null;

		SwitchComponent sw_node = null;
		MergeComponent mrg_node = null;

		int syncr=0;
		for (Node node : vnet.getNodes()) {
            if(node instanceof VisualSyncComponent) {
            	syncr=1;
            }
		}
		if(syncr==0) {
        	sync_reset();
		}
		for(VisualGroup vg: Hierarchy.getDescendantsOfType(vnet.getRoot(), VisualGroup.class)) {
        	for(VisualComponent vp: vg.getComponents()) {
				if(vp instanceof VisualSourceComponent) {
					VisualSourceComponent vsc=(VisualSourceComponent)vp;
					SourceComponent sc=vsc.getReferencedSourceComponent();
				    sc.setGr(no);
				}
				else if(vp instanceof VisualSinkComponent) {
					VisualSinkComponent vsc=(VisualSinkComponent)vp;
					SinkComponent sc=vsc.getReferencedSinkComponent();
				    sc.setGr(no);
				}
				else if(vp instanceof VisualFunctionComponent) {
					VisualFunctionComponent vsc=(VisualFunctionComponent)vp;
					FunctionComponent sc=vsc.getReferencedFunctionComponent();
				    sc.setGr(no);
				}
				else if(vp instanceof VisualQueueComponent) {
					VisualQueueComponent vsc=(VisualQueueComponent)vp;
					QueueComponent sc=vsc.getReferencedQueueComponent();
				    sc.setGr(no);
				}
				else if(vp instanceof VisualForkComponent) {
					VisualForkComponent vsc=(VisualForkComponent)vp;
					ForkComponent sc=vsc.getReferencedForkComponent();
				    sc.setGr(no);
				}
				else if(vp instanceof VisualJoinComponent) {
					VisualJoinComponent vsc=(VisualJoinComponent)vp;
					JoinComponent sc=vsc.getReferencedJoinComponent();
				    sc.setGr(no);
				}
				else if(vp instanceof VisualSwitchComponent) {
					VisualSwitchComponent vsc=(VisualSwitchComponent)vp;
					SwitchComponent sc=vsc.getReferencedSwitchComponent();
				    sc.setGr(no);
				}
				else if(vp instanceof VisualMergeComponent) {
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
		//System.out.println("Name_ =" + cnet.getName(contact_node));
		for (Connection c : cnet.getConnections(contact_node)) {
			//System.out.println("OutputConnection =" + cnet.getName(c));
			//if((c.getFirst() instanceof Contact)) System.out.println("Found First Contact");
			if((c.getSecond() instanceof XmasContact)) {
				//System.out.println("Found Output Contact" + cnet.getName(c.getSecond()));
				Node cp_node = c.getSecond().getParent();
				//System.out.println("Found Output Component" + cnet.getName(cp_node));
			}
		}
		int num_nodes=0;
		for (Node node : cnet.getNodes()) {
            //System.out.println("Name =" + cnet.getName(node));
			//cnet.getLabel(node);
			num_nodes++;
		}
		//GEN JSON
		File file = new File(XmasSettings.getVxmDirectory(), "JsonFile");
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
			String rstr;
			rstr = cnet.getName(node);
			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
            System.out.println("      \"id\": \"" + cnet.getName(node) + "\",");
            //writer.println("      \"id\": \"" + cnet.getName(node) + "\",");
            writer.println("      \"id\": \"" + rstr + "\",");
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
            else if(node instanceof SinkComponent) {
            	snk_node=(SinkComponent)node;
            	contacts=snk_node.getOutputs();
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
            				JoinComponent jn_node_=(JoinComponent)cp_node;
                        	contacts_=jn_node_.getInputs();
                        	for(XmasContact jncnt_node : contacts_) {
                        		if(jncnt_node==c.getSecond()) {
                        			//System.out.println("  Found jn contact = " + contact_no);
                        			contact_no=contact_cnt;
                        		}
                    			contact_cnt++;
                        	}
            			}
            			else if(cp_node instanceof MergeComponent) {
            				MergeComponent mrg_node_=(MergeComponent)cp_node;
                        	contacts_=mrg_node_.getInputs();
                        	for(XmasContact mrgcnt_node : contacts_) {
                        		if(mrgcnt_node==c.getSecond()) {
                        			//System.out.println("  Found mrg contact = " + contact_no);
                        			contact_no=contact_cnt;
                        		}
                    			contact_cnt++;
                        	}
            			}
            			rstr = cnet.getName(cp_node);
            			rstr = rstr.replace(rstr.charAt(0),Character.toUpperCase(rstr.charAt(0)));
            			System.out.println("          \"id\": " + "\"" + cnet.getName(cp_node) + "\",");
            			//writer.println("          \"id\": " + "\"" + cnet.getName(cp_node) + "\",");
            			writer.println("          \"id\": " + "\"" + rstr + "\",");
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
			if(node instanceof SourceComponent) {
				  System.out.println("      ],");
				  writer.println("      ],");
				}
			else if(node instanceof QueueComponent) {
			  System.out.println("      ],");
			  writer.println("      ],");
			}
			else {
				  System.out.println("      ],");
				  writer.println("      ],");
				}
			/*else {
			  System.out.println("      ]");
			  writer.println("      ]");
			}*/
			if(node instanceof SourceComponent) {
				//char srcchecktype = src_node.getVal();
				//System.out.println("type = " + srcchecktype);
				//int srccheckmode = src_node.getMode();
				//System.out.println("mode = " + srccheckmode);
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
				int sgr = src_node.getGr();
				System.out.println("          \"gr\": " + sgr + ",");
    			writer.println("          \"gr\": " + sgr + ",");
    			String srcgp = src_node.getGp();
				System.out.println("          \"gpf\": " + srcgp);
    			writer.println("          \"gpf\": " + srcgp);
				System.out.println("        }");
				writer.println("        }");
              	System.out.println("      ]");
              	writer.println("      ]");
			}
			else if(node instanceof SinkComponent) {
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
				int sgr = snk_node.getGr();
				System.out.println("          \"gr\": " + sgr);
    			writer.println("          \"gr\": " + sgr);
				System.out.println("        }");
				writer.println("        }");
              	System.out.println("      ]");
              	writer.println("      ]");
			}
			else if(node instanceof FunctionComponent) {
				//char fncheckfn = fun_node.getVal();
				//System.out.println("val = " + fncheckfn);
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
    			//String fs = fun_node.getFun();
				//System.out.println("          \"fn\": " + fs);
    			//writer.println("          \"fn\": " + fs + ",");
				int fgr = fun_node.getGr();
				System.out.println("          \"gr\": " + fgr);
    			writer.println("          \"gr\": " + fgr);
				System.out.println("        }");
				writer.println("        }");
              	System.out.println("      ]");
              	writer.println("      ]");
			}
			else if(node instanceof QueueComponent) {
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
				int quchecksize = qu_node.getCapacity();
				//System.out.println("capacity = " + quchecksize + ",");
				//int qusize = qu_node.getCapacity();
				//int qusize = 2;    //hardwire
				int qusize = 2;    //hardwire
				//int qusize = 3;    //hardwire
    			System.out.println("          \"size\": " + qusize + ",");
    			writer.println("          \"size\": " + qusize + ",");
				int quinit = qu_node.getInit();
				System.out.println("          \"init\": " + quinit + ",");
    			writer.println("          \"init\": " + quinit + ",");
    			int qgr = qu_node.getGr();
				System.out.println("          \"gr\": " + qgr + ",");
    			writer.println("          \"gr\": " + qgr + ",");
    			String qugp = qu_node.getGp();
				System.out.println("          \"gpf\": " + qugp);
    			writer.println("          \"gpf\": " + qugp);
				System.out.println("        }");
				writer.println("        }");
              	System.out.println("      ]");
              	writer.println("      ]");
			}
			else if(node instanceof ForkComponent) {
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
				int fgr = frk_node.getGr();
				System.out.println("          \"gr\": " + fgr);
    			writer.println("          \"gr\": " + fgr);
				System.out.println("        }");
				writer.println("        }");
              	System.out.println("      ]");
              	writer.println("      ]");
			}
			else if(node instanceof JoinComponent) {
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
				int jgr = jn_node.getGr();
				System.out.println("          \"gr\": " + jgr);
    			writer.println("          \"gr\": " + jgr);
				System.out.println("        }");
				writer.println("        }");
              	System.out.println("      ]");
              	writer.println("      ]");
			}
			else if(node instanceof SwitchComponent) {
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
				int sgr = sw_node.getGr();
				System.out.println("          \"gr\": " + sgr);
    			writer.println("          \"gr\": " + sgr);
				System.out.println("        }");
				writer.println("        }");
              	System.out.println("      ]");
              	writer.println("      ]");
			}
			else if(node instanceof MergeComponent) {
				System.out.println("      \"fields\": [");
				writer.println("      \"fields\": [");
				System.out.println("        {");
				writer.println("        {");
				int mgr = mrg_node.getGr();
				System.out.println("          \"gr\": " + mgr);
    			writer.println("          \"gr\": " + mgr);
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
		System.out.println("Output written to JsonFile");
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