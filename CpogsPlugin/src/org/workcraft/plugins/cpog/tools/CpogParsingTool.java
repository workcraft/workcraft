package org.workcraft.plugins.cpog.tools;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.workcraft.dom.Connection;
import org.workcraft.dom.Container;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualGroup;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.VisualArc;
import org.workcraft.plugins.cpog.VisualCPOG;
import org.workcraft.plugins.cpog.VisualScenario;
import org.workcraft.plugins.cpog.VisualScenarioPage;
import org.workcraft.plugins.cpog.VisualVariable;
import org.workcraft.plugins.cpog.VisualVertex;
import org.workcraft.plugins.cpog.expressions.javacc.ParseException;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.util.Func;
import org.workcraft.workspace.WorkspaceEntry;

public class CpogParsingTool {

	 public CpogParsingTool(HashMap<String, Variable> variableMap, int xpos, HashMap<String, GraphReference> refMap)
	 {
		 this.variableMap = variableMap;
		 this.xpos = xpos;
		 this.refMap = refMap;
	 }

	private HashMap<String, Variable> variableMap;
	private int xpos;
	private HashMap<String, GraphReference> refMap;
	private ArrayList<String> usedReferences;

	 public BooleanFormula parseBool(String bool, final VisualCPOG visualCpog) throws ParseException
	 {
		Func<String, BooleanVariable> boolVars = new Func<String, BooleanVariable>()
		{
			public BooleanVariable eval(final String label)
			{
				if (variableMap.containsKey(label))
				{
                    if (!visualCpog.getVariables().contains(variableMap.get(label))) {
                        if (variableMap.get(label).getParent() != null) {
                            return variableMap.get(label);
                        } else  variableMap.remove(label);
                    } else {
                        variableMap.remove(label);
                    }
				}

					VisualVariable visVar = visualCpog.createVisualVariable();
					visVar.setLabel(label);
					visVar.setPosition(new Point2D.Double(xpos, -2));
					xpos++;
					variableMap.put(label, visVar.getMathVariable());

				return variableMap.get(label);
			}
		};

	    BooleanFormula boolForm;

	    try
	    {
	      boolForm = BooleanParser.parse(bool, boolVars);
	    } catch (org.workcraft.plugins.cpog.optimisation.javacc.ParseException e)
	    {
	      throw new ParseException("Boolean error in: " + bool);
	    }
	    return boolForm;
	  }

	 public void bfsLayout(Queue<Node> q, VisualCPOG visualCpog, double originalX, double originalY) {

         ArrayList<ArrayList<Node>> outer = new ArrayList<>();
         HashSet<VisualPage> pages = new HashSet<>();
         Node current = q.remove();
         ArrayList<Node> children = getChildren(visualCpog, current);

         outer.add(new ArrayList<Node>());
         outer.get(0).add(current);
         outer.add(new ArrayList<Node>());

         for (Node child : children) {
             q.add(child);
             outer.get(1).add(child);
         }

         findAllChildren(q, visualCpog, outer, pages);

         double x = positionNodes(originalX, originalY, outer);


     }


    public double positionNodes(double originalX, double originalY, ArrayList<ArrayList<Node>> outer) {
        Double centre = new Double(0, originalY);

        double x = originalX;
        double y = 0;

        Iterator<ArrayList<Node>> it = outer.iterator();

        while (it.hasNext()) {
            ArrayList<Node> inner = it.next();
            if (inner.size() > 1) {
                y = centre.getY() - (inner.size() / 2);
            } else {
                y = centre.getY();
            }
            for (Node n : inner) {
                if (n instanceof VisualVertex){
                    VisualVertex v = (VisualVertex) n;
                    if ((v.getParent() instanceof VisualPage) && (refMap.containsKey(((VisualPage) v.getParent()).getLabel()))) {
                        VisualPage p = (VisualPage) v.getParent();
                        Point2D.Double newPosition = new
                                Point2D.Double (refMap.get(p.getLabel()).getVertMap().get(v.getLabel()).getX(),
                                                refMap.get(p.getLabel()).getVertMap().get(v.getLabel()).getY());
                        v.setPosition(newPosition);
                    } else {
                        v.setPosition(new Double(x, y));
                    }
                }
                y += 1.5;
            }

            if (it.hasNext())
                x += 2.5;
        }
        y += 2.5;
        return x;
    }

    public void findAllChildren(Queue<Node> q, VisualCPOG visualCpog, ArrayList<ArrayList<Node>> outer, HashSet<VisualPage> pages) {
        Node current;
        ArrayList<Node> children;
        int index = 0;
        while (!q.isEmpty()) {
            current = q.remove();
            index = findVertex(outer, current);
            if ((current.getParent() instanceof VisualScenarioPage) | (current.getParent() instanceof VisualPage)) {
                VisualPage vp = (VisualPage) current.getParent();
                pages.add(vp);
            }
            children = getChildren(visualCpog, current);
            for (Node child : children) {
                q.add(child);
                addNode(child, index + 1, outer);
            }
        }
    }

    public static ArrayList<Node> getChildren(VisualCPOG visualCpog, Node node)
	 {
         ArrayList<Node> children = new ArrayList<>();
         HashSet<VisualArc> arcs = getAllArcs(visualCpog.getRoot(), visualCpog);


		 for (VisualArc arc : arcs)
		 {
			 if ((arc.getFirst().equals(node))) {
                 children.add(arc.getSecond());
			 }
		 }

		 return children;
	 }

    public static HashSet<Node> getParents(VisualCPOG visualCpog, Node node)
    {
        HashSet<Node> parents = new HashSet<>();
        HashSet<VisualArc> arcs = getAllArcs(visualCpog.getRoot(), visualCpog);

        for (VisualArc arc : arcs)
        {
            if ((arc.getSecond().equals(node))) {
                parents.add(arc.getFirst());
            }
        }

        return parents;
    }

	 public static String getExpressionFromGraph(VisualCPOG visualCpog)
	 {
		 Collection<Node> originalSelection;
		 ArrayList<VisualTransformableNode> groups = new ArrayList<>();
		 ArrayList<Node> vertices = new ArrayList<Node>();
		 ArrayList<String> expression = new ArrayList<String>();

         groups = getScenarios(visualCpog);
         originalSelection = copySelected(visualCpog);
		 //Add vertices from group
		 if (!groups.isEmpty()) {
			 for (VisualTransformableNode group : groups) {
				 expression.add(group.getLabel() + " =");
                 originalSelection.remove(group);

                 getAllGroupVertices(vertices, group);

                 HashSet<Node> roots = getRoots(visualCpog, vertices);

				 Iterator<Node> i = roots.iterator();
				 VisualVertex current;
				 Set<Connection> totalConnections;
				 ArrayList<Connection> connections = new ArrayList<Connection>();
				 HashSet<VisualVertex> visitedVertices = new HashSet<VisualVertex>();
				 HashSet<Connection> visitedConnections = new HashSet<Connection>();
				 ConcurrentLinkedQueue<Node> q = new ConcurrentLinkedQueue<Node>();
				 while(i.hasNext())
				 {
				   q.add(i.next());
				   while(!q.isEmpty()){
					   connections.clear();
					   current = (VisualVertex) q.remove();

					   totalConnections = visualCpog.getConnections(current);

					   describeArcs(expression, totalConnections, visitedVertices, visitedConnections, current, vertices, visualCpog, q);

					   if ((!q.isEmpty() || (i.hasNext())) && (expression.get(expression.size() - 1) != "+"))
					   {
						   expression.add("+");
					   }

					   if ((i.hasNext()) && !(expression.get(expression.size() - 1) == "+"))
					   {
						   expression.add("+");
					   }
				   }

				 }
				 while (expression.get(expression.size() - 1) == "+")
				 {
					 expression.remove(expression.size() - 1);
				 }
				 expression.add("\n");
			 }
		 }
		 if (!originalSelection.isEmpty())
		 {
			 vertices.clear();
			 for (Node n : originalSelection) {
                 if (n instanceof VisualVertex) {
                     vertices.add(n);
                 } else if ((n instanceof VisualScenarioPage) || (n instanceof VisualPage)) {
                     VisualPage p = (VisualPage) n;
                     for (Node child : p.getChildren()) {
                         if (child instanceof VisualVertex) {
                             vertices.add(child);
                         }
                     }
                 }
             }

			 HashSet<Node> roots = getRoots(visualCpog, vertices);

			 Iterator<Node> i = roots.iterator();
			 VisualVertex current;
			 Set<Connection> totalConnections;
			 ArrayList<Connection> connections = new ArrayList<Connection>();
			 HashSet<VisualVertex> visitedVertices = new HashSet<VisualVertex>();
			 HashSet<Connection> visitedConnections = new HashSet<Connection>();
			 ConcurrentLinkedQueue<Node> q = new ConcurrentLinkedQueue<Node>();

			 while(i.hasNext())
			 {

			   q.add(i.next());
			   while(!q.isEmpty()){
				   connections.clear();
				   current = (VisualVertex) q.remove();

				   totalConnections = visualCpog.getConnections(current);

                   describeArcs(expression, totalConnections, visitedVertices, visitedConnections, current, vertices, visualCpog, q);

				   if ((!q.isEmpty() || (i.hasNext())) && (expression.get(expression.size() - 1) != "+"))
				   {
					   expression.add("+");
				   }

				   if ((i.hasNext()) && !(expression.get(expression.size() - 1) == "+"))
				   {
					   expression.add("+");
				   }
			   }
			 }

		 }

		 String total = "";

         if (expression.get(expression.size() - 1).compareTo("+") == 0) {
             expression.remove(expression.size() - 1);
         }

		 for (String ex : expression)
		 {
			 	 if ((total.endsWith("\n")) || (total.equals(""))) {
			 		 total = total + ex;
			 	 } else
			 		 total = total + " " + ex;
		 }

         return total;

	 }


    public static void describeArcs(ArrayList<String> expression, Set<Connection> totalConnections, HashSet<VisualVertex> visitedVertices, HashSet<Connection> visitedConnections,
    			VisualVertex current, ArrayList<Node> vertices, VisualCPOG visualCpog, ConcurrentLinkedQueue<Node> q) {
    	ArrayList<Connection> connections = new ArrayList<>();
    	for (Connection c : totalConnections) {
    		if ((!visitedConnections.contains(c)) && (!c.getSecond().equals(current)) && (vertices.contains(c.getSecond()))) {
                connections.add(c);
            }
    	}
    	if (connections.size() == 1) {
    		VisualArc arc = (VisualArc) connections.get(0);
    		String insert = "";

    		if (!(FormulaToString(arc.getCondition()).equals("1"))) {
    			insert = "[" + FormulaToString(arc.getCondition()) + "](";
    		}

    		if (!(FormulaToString(current.getCondition()).equals("1")) || !(FormulaToString(current.getCondition()).compareTo(FormulaToString(arc.getCondition())) == 0)) {
    			insert = insert + "[" + FormulaToString(current.getCondition()) + "]";
    		}

    		insert = insert + current.getLabel() + " -> ";
    		VisualVertex child = (VisualVertex) arc.getSecond();

    		if (!(FormulaToString(child.getCondition()).equals("1")) || !(FormulaToString(child.getCondition()).equals(FormulaToString(arc.getCondition())))) {
    			insert = insert + "[" + FormulaToString(child.getCondition()) + "]";
    		}


    		insert = insert + child.getLabel();
    		visitedConnections.add(arc);

    		boolean finished = false;
    		while (!finished) {
    			if (getChildren(visualCpog, child).size() == 1) {
    				ArrayList<Node> nextVertices = getChildren(visualCpog, child);
    				VisualVertex nextVertex = (VisualVertex) nextVertices.get(0);
    				VisualArc nextArc = (VisualArc) visualCpog.getConnection(child, nextVertex);

    				if (FormulaToString(nextArc.getCondition()).equals(FormulaToString(arc.getCondition()))) {
    					insert = insert + " -> ";
    					if (!(FormulaToString(nextVertex.getCondition()).equals("1")) || !(FormulaToString(child.getCondition()).equals(FormulaToString(arc.getCondition())))) {
    		    			insert = insert + "[" + FormulaToString(child.getCondition()) + "]";
    		    		}
    					insert = insert + nextVertex.getLabel();
    					visitedConnections.add(nextArc);
    					child = nextVertex;

    				}
    			} else {
    				finished = true;
    		}
    		}

    		if (!(FormulaToString(arc.getCondition()).equals("1"))) {
    			insert = insert + ")";
    		}
    		expression.add(insert);
    	} else if (connections.size() > 1) {

    		while(!connections.isEmpty()) {
    			VisualArc arc  = (VisualArc) connections.get(0);
    			String insert = "";

    			if (!FormulaToString(arc.getCondition()).equals("1")) {
    				insert = "[" + FormulaToString(arc.getCondition()) + "](";
    			}


    			if (!(FormulaToString(current.getCondition()).equals("1")) || !(FormulaToString(current.getCondition()).equals(FormulaToString(arc.getCondition())))) {
        			insert = insert + "[" + FormulaToString(current.getCondition()) + "]";
        		}

        		insert = insert + current.getLabel() + " -> ";

    			ArrayList<VisualArc> toBeRemoved = new ArrayList<>();

    			for (Connection c : connections) {
    				VisualArc a = (VisualArc) c;

    				if (a.getCondition().equals(arc.getCondition())) {
    					toBeRemoved.add(a);
    				}
    			}

    			if (toBeRemoved.size() > 1) {
					insert = insert + "(";// + arc.getSecond().getLabel() + " + ";
				}

    			for (VisualArc a : toBeRemoved) {
    				insert = insert + ((VisualVertex)a.getSecond()).getLabel() + " + ";
    			}

    			while ((insert.endsWith(" ")) || (insert.endsWith("+"))) {
    				insert = insert.substring(0, insert.length() - 1);
    			}

    			if (toBeRemoved.size() > 1) {
    				insert = insert + ")";
    			}

    			visitedConnections.addAll(toBeRemoved);
    			connections.removeAll(toBeRemoved);

    			expression.add(insert);
    		}

    	} else {
    		String insert = "";
    		if (!(FormulaToString(current.getCondition()).equals("1"))) {
    			insert = "[" + FormulaToString(current.getCondition()) + "]";
    		}
    		insert = insert + current.getLabel();
    		expression.add(insert);
    	}
    }

    private static String FormulaToString(BooleanFormula condition) {
		return FormulaToString.toString(condition);
	}

	public static HashSet<Node> getRoots(VisualCPOG visualCpog, ArrayList<Node> vertices) {
        HashSet<Node> roots = new HashSet<Node>();
        Set<Connection> arcs;
        Iterator<Connection> it;
        Connection connection;
        boolean second = false;

        //get root(s)
        for (Node v : vertices)
        {
           arcs = visualCpog.getConnections(v);
           it = arcs.iterator();
           //The following covers root nodes, and nodes with no connections
           while (it.hasNext())
           {
               connection = it.next();
               if ((!connection.getFirst().equals(v)) && (vertices.contains(connection.getFirst())))
               {
                   second = true;
                   break;
               }
           }
           if (!second)
           {
               roots.add(v);
           }
           second = false;
        }
        return roots;
    }

    public static void getAllGroupVertices(ArrayList<Node> vertices, VisualTransformableNode group) {
        vertices.clear();
        for (VisualComponent v : group.getComponents()) {
            if (v instanceof VisualPage) {
                vertices.addAll(getPageVertices((VisualPage) v));
            } else vertices.add(v);
        }
    }


    public static void getGroups(VisualCPOG visualCpog, ArrayList<VisualTransformableNode> groups) {
        ArrayList<Node> prevSelection = copySelected(visualCpog);
        visualCpog.selectAll();

        for(Node n : visualCpog.getSelection()) {
            if ((n instanceof VisualPage) || (n instanceof VisualGroup)) {
                if (prevSelection.contains(n)) {
                    groups.add((VisualTransformableNode) n);
                }
            }
        }

        visualCpog.select(prevSelection);
    }

    public static ArrayList<VisualTransformableNode> getScenarios(VisualCPOG visualCpog) {
    	ArrayList<VisualTransformableNode> scenarios = new ArrayList<>();
    	TreeSet<String> nameList = new TreeSet<>();
    	ArrayList<Node> prevSelection = copySelected(visualCpog);
    	visualCpog.selectAll();

    	for(Node n : visualCpog.getSelection()) {
    		if ((n instanceof VisualScenarioPage) || (n instanceof VisualScenario)) {
    			if (prevSelection.contains(n)) {
    				scenarios.add((VisualTransformableNode) n);
    				VisualTransformableNode node = (VisualTransformableNode) n;
    				nameList.add(node.getLabel());
    			}
    		}
    	}


    	ArrayList<VisualTransformableNode> result = new ArrayList<>();

    	for (String name : nameList) {
    		boolean found = false;
    		int i = 0;
    		while (!found) {
    			if (scenarios.get(i).getLabel().equals(name)) {
    				result.add(scenarios.remove(i));
    				found = true;
    			}
    			i++;
    		}
    	}

    	visualCpog.select(prevSelection);
    	return result;
    }

    public int findVertex(ArrayList<ArrayList<Node>> outer, Node target)
	 {
		 int index = 0;
		 for (ArrayList<Node> inner : outer)
		 {
			 if (inner.contains(target))
			 {
				 return index;
			 }
			 index++;
		 }
		 return -1;
	 }

	 public void addNode(Node v, int index, ArrayList<ArrayList<Node>> outer)
	 {
		 int removalIndex = 0;

		 removalIndex = findVertex(outer, v);
		 if (removalIndex >= 0)
		 {
			 outer.get(removalIndex).remove(v);
		 }
		 if (outer.size() - 1 < index)
		 {
			 outer.add(new ArrayList<Node>());
		 }

		 outer.get(index).add(v);

	 }

	 public HashSet<VisualArc> findTransitives(VisualCPOG visualCpog, HashSet<Node> roots)
	 {
		 ConcurrentLinkedQueue<Node> q = new ConcurrentLinkedQueue<Node>();
		 HashSet<VisualArc> transitives = new HashSet<VisualArc>();
		 ArrayList<Node>  allChildren = new ArrayList<Node>();
         ArrayList<Node> children = new ArrayList<>();
		 Node current = null;
		 boolean transitiveFound = false;


		 for(Node root: roots)
		 {
			 q.add(root);
			 while(!q.isEmpty())
			 {
				current = (Node) q.remove();
				children = getChildren(visualCpog, current);
				for (Node child : children)
				{
					q.add(child);
				}
				for(Node target : children)
				{
					for (Node c : children)
					{
						if (!c.equals(target))
						{
							allChildren.add(c);
						}
					}

					while((!allChildren.isEmpty()) && (!transitiveFound))
					{
						if (allChildren.contains(target))
						{
							transitiveFound = true;
						} else
						{
							allChildren.addAll(getChildren(visualCpog, allChildren.remove(0)));
						}
					}
					if (transitiveFound)
					{
						transitives.add((VisualArc) visualCpog.getConnection(current, target));
						transitiveFound = false;
					}
					allChildren.clear();
				}
			 }
		 }


		 return transitives;
	 }

	 public void removeTransitives(VisualCPOG visualCpog,  HashSet<Node> roots, String text, boolean forceRemoval) {
		 HashSet<VisualArc> transitives = findTransitives(visualCpog, roots);
		 for (VisualArc t : transitives) {
			 if (!forceRemoval) {
				 String arc = ((VisualVertex)t.getFirst()).getLabel() + " -> " + ((VisualVertex)t.getSecond()).getLabel();
             	if (!(text.contains(" " + arc + " ")) && !(text.endsWith(" " + arc)) && !(text.contains("(" + arc + " ")) && !(text.contains("(" + arc + ")")) && !(text.contains(" " + arc + ")"))) {
            	 	visualCpog.remove(t);
             	}
			 } else
			 {
				 visualCpog.remove(t);
			 }
         }
         transitives.clear();
	 }

	 public String replaceReferences(String text)
	 {
		 usedReferences = new ArrayList<>();
		 boolean added;
		 for (String k : refMap.keySet())
			{
			 added = false;
				if (text.contains(" " + k + " ")){
					if (k.startsWith("[")) {
						text = text.replaceAll(" " + k + " ", " (" + refMap.get(k).getNormalForm() + ") ");
						added = true;
					} else {
						text = text.replaceAll(" " + k + " ", " (" + refMap.get(k).getNormalForm() + ") ");
						added = true;
					}
				} if (text.contains("]" + k + " ")) {
						text = text.replaceAll("]" + k + " ", "](" + refMap.get(k).getNormalForm() + ") ");
						added = true;
				} if (text.contains("(" + k + ")")) {
						text = text.replaceAll("\\(" + k + "\\)", "\\(" + refMap.get(k).getNormalForm() + "\\)");
						added = true;
				} if (text.contains("(" + k + " ")) {
						text = text.replaceAll("\\(" + k + " ", "\\(\\(" + refMap.get(k).getNormalForm() + "\\) ");
						added = true;
				} if (text.contains(" " + k + ")")) {
						text = text.replaceAll(" " + k + "\\)", " \\(" + refMap.get(k).getNormalForm() + "\\)\\)");
						added = true;
				} if (text.endsWith(" " + k)) {
						text = text.replace(" " + k, " (" + refMap.get(k).getNormalForm() + ")");
						added = true;
				} if (text.endsWith("]" + k)) {
						text = text.replace("]" + k, "](" + refMap.get(k).getNormalForm() + ")");
						added = true;
				} if (text.endsWith(" " + k + ")")) {
						text = text.replace(" " + k + "\\)", " (" + refMap.get(k).getNormalForm() + "\\)\\)");
						added = true;
				}

				if (added) {
					usedReferences.add(k);
				}
			}
		 return text;
	 }

	 public void setArcConditions(HashSet<ArcCondition> arcConditionList, VisualCPOG visualCpog, HashMap<String, VisualVertex> vertexMap)
	 {
		 int index;
		 for (ArcCondition a : arcConditionList)
			{
			 if (a.getBoolForm().compareTo("") != 0) {
				index = 0;
					ArrayList<String> vertexList = a.getVertexList();
					Iterator<String> it = vertexList.iterator();
					String first, second;
					VisualArc arc;

					while(it.hasNext())
					{
						first = it.next();
						for (int c = index + 1; c < vertexList.size(); c++)
						{
							second = vertexList.get(c);

							ArrayList<String> verts1 = new ArrayList<String>();
							ArrayList<String> verts2 = new ArrayList<String>();
							int ind = 0;
							if (first.contains("("))
							{
								first = first.replace("(", "");
								first = first.replace(")", "");
								while(first.contains("+"))
								{
									ind = first.indexOf("+");
									verts1.add(first.substring(0, ind));
									first = first.substring(ind+1);
								}
									verts1.add(first);
							}
							verts1.add(first);
							if (second.contains("("))
							{
								second = second.replace("(", "");
								second = second.replace(")", "");
								while(second.contains("+"))
								{
									ind = second.indexOf("+");
									verts2.add(second.substring(0, ind));
									second = second.substring(ind+1);
								}
							}
							verts2.add(second);

							for (String vert1 : verts1)
							{
								for (String vert2 : verts2)
								{
									arc = (VisualArc) visualCpog.getConnection(vertexMap.get(vert1), vertexMap.get(vert2));
									ArrayList<VisualArc> dupArcs = new ArrayList<VisualArc>();
									if (arc != null)
									{
										for (Connection con : visualCpog.getConnections(vertexMap.get(vert1))) {
											if (con.getSecond().equals(vertexMap.get(vert2))) {
												dupArcs.add((VisualArc) con);
											}
										}
                                        boolean conditionFound = false;
                                        ArrayList<VisualArc> toBeRemoved = new ArrayList<>();
										if (dupArcs.size() > 1) {
											for (VisualArc va : dupArcs) {
												if (FormulaToString.toString(va.getCondition()).compareTo("1") != 0) {
													toBeRemoved.add(va);
                                                    conditionFound = true;
												}
											}
                                            for (VisualArc va : toBeRemoved) { dupArcs.remove(va); }

                                            if (!(conditionFound) && (dupArcs.size() > 1)) {
                                                for (int i = 1; i < dupArcs.size(); i++) {
                                                    visualCpog.remove(dupArcs.get(i));
                                                }
                                            } else {
                                                for (int i = 0; i < dupArcs.size(); i++) {
                                                    visualCpog.remove(dupArcs.get(i));
                                                }
                                            }
										}

										try {
											if (FormulaToString.toString(arc.getCondition()).compareTo("1") == 0)
											{
												arc.setCondition(parseBool(a.getBoolForm(), visualCpog));
											} else
											{
												arc.setCondition(parseBool(FormulaToString.toString(arc.getCondition()) + "|" + a.getBoolForm(), visualCpog));
											}
										} catch (ParseException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}

						}
						index++;
					}
			 }
				}
	 }

	 public Point2D.Double getLowestVertex(VisualCPOG visualCpog)
	 {
		 Collection<VisualVertex> vertices =  visualCpog.getVertices(visualCpog.getCurrentLevel());
		 vertices.removeAll(visualCpog.getSelection());

         ArrayList<Node> prevSelection = new ArrayList<Node>();
         for (Node n : visualCpog.getSelection()) prevSelection.add(n);

         ArrayList<VisualScenarioPage> pages = new ArrayList<VisualScenarioPage>();
         visualCpog.selectAll();
         for (Node n : visualCpog.getSelection()) {
             if (n instanceof VisualScenarioPage) {
                 pages.add((VisualScenarioPage) n);
             }
         }

         visualCpog.select(prevSelection);

         pages.removeAll(visualCpog.getSelection());

		 Point2D.Double centre, startPoint = null;

		 for(VisualVertex vertex : vertices) {
			 centre = (Double) vertex.getCenter();
			 if (startPoint == null) {
				 startPoint = new Point2D.Double (centre.getX(), centre.getY());
			 } else {
				 if (centre.getY() > startPoint.getY()) {
				 	startPoint.setLocation(startPoint.getX(), centre.getY());
			 	}
				 if (centre.getX() < startPoint.getX()){
					 startPoint.setLocation(centre.getX(), startPoint.getY());
				 }
			 }
		 }
		 for(VisualScenarioPage page : pages) {
			 Rectangle2D.Double rect = (java.awt.geom.Rectangle2D.Double) page.getBoundingBox();
			 Point2D.Double bl = new Point2D.Double(0, rect.getCenterY() + (rect.getHeight()/2));

			 if (startPoint == null) {
				 startPoint = new Point2D.Double(bl.getX(), bl.getY());
			 } else {
				 if (bl.getY() > startPoint.getY()) {
					 startPoint.setLocation(startPoint.getX(), bl.getY());
				 }
			 }
		 }
		 if (startPoint == null) {
			 startPoint = new Point2D.Double(0,0);
		 } else
		 {
			 startPoint.setLocation(startPoint.getX(), startPoint.getY() + 2);
		 }

		 return startPoint;

	 }


	 public ArrayList<String> getUsedReferences()
	 {
		 return usedReferences;
	 }

     public static ArrayList<VisualComponent> getPageVertices(VisualPage p) {
         ArrayList<VisualComponent> result = new ArrayList<VisualComponent>();

         for (VisualComponent c : p.getComponents()) {
             if (c instanceof VisualPage) {
                result.addAll(getPageVertices((VisualPage) c));
             } else {
                 result.add(c);
             }
         }
         return result;
     }


    public static ArrayList<Node> copySelected(VisualCPOG visualCpog) {
        ArrayList<Node> result = new ArrayList<Node>();
        for (Node n : visualCpog.getSelection()) {
            result.add(n);
        }

        return result;
    }

    public static HashSet<VisualArc> getAllArcs(Container root, VisualCPOG visualCpog) {
        HashSet<VisualArc> result = new HashSet<>();
        for (Node node : root.getChildren()) {
            if ((node instanceof VisualPage) || (node instanceof VisualScenarioPage)) {
                result.addAll(getAllArcs((VisualPage) node, visualCpog));
            } else if (node instanceof VisualScenario) {
            	result.addAll(getAllArcs((VisualScenario) node, visualCpog));
        	}else if (node instanceof VisualArc) {
                result.add((VisualArc) node);
            }
        }

        return result;
    }

	public static boolean hasEnoughScenarios(WorkspaceEntry we) {
		VisualCPOG cpog = (VisualCPOG)(we.getModelEntry().getVisualModel());
		return (getScenarios(cpog).size() > 1);
	}

/*    public void combineConditions(String old, String newCond, final VisualCPOG visualCpog) throws ParseException {
        if (!old.contains(newCond)) {
            parseBool()
        }

    }*/


}
