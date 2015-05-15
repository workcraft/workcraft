package org.workcraft.plugins.cpog.tools;

import org.workcraft.dom.Connection;
import org.workcraft.dom.visual.VisualPage;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.VisualComponent;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.plugins.cpog.*;
import org.workcraft.plugins.cpog.expressions.javacc.ParseException;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.BooleanVariable;
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.FormulaToString;
import org.workcraft.plugins.cpog.optimisation.javacc.BooleanParser;
import org.workcraft.util.Func;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CpogParsingTool {

	 public CpogParsingTool(HashMap<String, Variable> variableMap, int xpos, double maxX, double maxY, HashMap<String, String> refMap, HashMap<String, HashMap<String, VisualVertex>> refVertMap)
	 {
		 this.variableMap = variableMap;
		 this.xpos = xpos;
		 this.maxX = maxX;
		 this.maxY = maxY;
		 this.refMap = refMap;
		 this.refVertMap = refVertMap;
	 }

	private HashMap<String, Variable> variableMap;
	private int xpos;
	private double maxX;
	private double maxY;
	private HashMap<String, String> refMap;
	private HashMap<String, HashMap<String, VisualVertex>> refVertMap;
	private ArrayList<String> usedReferences;

	 public BooleanFormula parseBool(String bool, final VisualCPOG visualCpog) throws ParseException
	 {

		Func<String, BooleanVariable> boolVars = new Func<String, BooleanVariable>()
		{
			public BooleanVariable eval(final String label)
			{
				if (variableMap.containsKey(label))
				{
					return variableMap.get(label);
				} else
				{

					VisualVariable visVar = visualCpog.createVisualVariable();
					visVar.setLabel(label);
					visVar.setPosition(new Point2D.Double(xpos, -2));
					xpos++;
					variableMap.put(label, visVar.getMathVariable());
				}

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

         ArrayList<ArrayList<Node>> outer = new ArrayList<ArrayList<Node>>();
         HashSet<VisualScenarioPage> pages = new HashSet<VisualScenarioPage>();
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


         positionPages(pages, x);

     }

    public void positionPages(HashSet<VisualScenarioPage> pages, double x) {
        for (VisualScenarioPage page : pages) {
            Double pagePos = new Double();
            double pageLeft = x;
            double pageRight = -x;
            //Leftmost vertex
            for (VisualComponent c : page.getComponents()) {
                if (c.getPosition().getX() < pageLeft) {
                    pageLeft = c.getPosition().getX();
                }
            }
            //Rightmost vertex
            for (VisualComponent c : page.getComponents()) {
                if (c.getPosition().getX() > pageRight) {
                    pageRight = c.getPosition().getX();
                }
            }

            double pageCentre = ((pageLeft + pageRight) / 2);

            pagePos.setLocation(pageCentre, page.getPosition().getY());

            //page.setPosition(pagePos);
            for (VisualComponent c : page.getComponents()) {
                c.setX(c.getPosition().getX() - pageCentre);
            }
            page.setX(pageCentre);

        }
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
                    v.setPosition(new Double(x, y));
                } else if (n instanceof VisualScenarioPage) {
                    VisualScenarioPage p = (VisualScenarioPage) n;
                    p.setPosition(new Double(x, y));
                }
                y += 1.5;
            }

            if (it.hasNext())
                x += 2.5;
        }
        y += 2.5;
        return x;
    }

    public void findAllChildren(Queue<Node> q, VisualCPOG visualCpog, ArrayList<ArrayList<Node>> outer, HashSet<VisualScenarioPage> pages) {
        Node current;
        ArrayList<Node> children;
        int index = 0;
        while (!q.isEmpty()) {
            current = (Node) q.remove();
            index = findVertex(outer, current);
            if (current.getParent() instanceof VisualScenarioPage) {
                VisualScenarioPage vp = (VisualScenarioPage) current.getParent();
                pages.add(vp);
            }
            children = getChildren(visualCpog, current);
            for (Node child : children) {
                q.add(child);
                addNode(child, index + 1, outer);
            }
        }
    }

    public ArrayList<Node> getChildren(VisualCPOG visualCpog, Node node)
	 {
		 ArrayList<Node> children = new ArrayList<Node>();
		 Connection connection;

		 Iterator<Connection> i = visualCpog.getConnections(node).iterator();
		 while (i.hasNext())
		 {
			 connection = i.next();
			 if (!(connection.getSecond().equals(node)))
			 {
                 children.add(connection.getSecond());
			 }
		 }

		 return children;
	 }

	 public String getExpressionFromGraph(VisualCPOG visualCpog)
	 {
		 Collection<Node> originalSelection;
		 ArrayList<VisualPage> groups = new ArrayList<VisualPage>();
		 ArrayList<Node> vertices = new ArrayList<Node>();
		 ArrayList<String> expression = new ArrayList<String>();

         getPages(visualCpog, groups);
         originalSelection = copySelected(visualCpog);
		 //Add vertices from group
		 if (!groups.isEmpty()) {
			 for (VisualPage group : groups) {
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

                       getExpressionConditions(expression, current, totalConnections, connections, visitedVertices, visitedConnections, vertices);

					   if (connections.size() > 1)
					   {
						   expression.add("(");
					   }

                       getConnectionExpressions(expression, connections, visitedVertices, visitedConnections, q);

					   if (connections.size() > 1)
					   {
						   expression.add(")");
					   }

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

             /*if ((exclude != null) && (vertices.contains(exclude))) {
                 vertices.remove(exclude);
             }*/

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

                   getExpressionConditions(expression, current, totalConnections, connections, visitedVertices, visitedConnections, vertices);

				   if (connections.size() > 1)
				   {
					   expression.add("(");
				   }

                   getConnectionExpressions(expression, connections, visitedVertices, visitedConnections, q);

				   if (connections.size() > 1)
				   {
					   expression.add(")");
				   }

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
				 total = total + " " + ex;
		 }
         return total;

	 }

    public void getConnectionExpressions(ArrayList<String> expression, ArrayList<Connection> connections, HashSet<VisualVertex> visitedVertices,
                                         HashSet<Connection> visitedConnections, ConcurrentLinkedQueue<Node> q) {
        Connection connection;Iterator<Connection> conIt = connections.iterator();
        VisualVertex child;
        while(conIt.hasNext())
        {
            connection = conIt.next();
            child = (VisualVertex) connection.getSecond();
            if (FormulaToString.toString(child.getCondition()).compareTo("1") == 0)
            {
                expression.add(child.getLabel());
            } else
            {
                expression.add("[" + FormulaToString.toString(child.getCondition()) + "]" + child.getLabel());
            }


            visitedConnections.add(connection);
            visitedVertices.add(child);
            q.add(child);

            if (conIt.hasNext())
            {
                expression.add("+");
            }

        }
    }

    public void getExpressionConditions(ArrayList<String> expression, VisualVertex current, Set<Connection> totalConnections, ArrayList<Connection> connections,
                                        HashSet<VisualVertex> visitedVertices, HashSet<Connection> visitedConnections, ArrayList<Node> vertices) {
        for (Connection c : totalConnections)
        {
            if ((!visitedConnections.contains(c)) && (!c.getSecond().equals(current)) && (vertices.contains(c.getSecond())))
            {
                connections.add(c);
            }
        }
        if (connections.size() > 0)
        {
            if (FormulaToString.toString(current.getCondition()).compareTo("1") == 0)
            {
                expression.add(current.getLabel() + " ->");
            } else
            {
                expression.add("[" + FormulaToString.toString(current.getCondition()) + "]" + current.getLabel() + " ->");
            }
        } else if (!visitedVertices.contains(current))
        {
            if (FormulaToString.toString(current.getCondition()).compareTo("1") == 0)
            {
                expression.add(current.getLabel());
            } else
            {
                expression.add("[" + FormulaToString.toString(current.getCondition()) + "]" + current.getLabel());
            }

        }
    }

    public HashSet<Node> getRoots(VisualCPOG visualCpog, ArrayList<Node> vertices) {
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

    public void getAllGroupVertices(ArrayList<Node> vertices, VisualPage group) {
        vertices.clear();
        for (VisualComponent v : group.getComponents()) {
            if (v instanceof VisualPage) {
                vertices.addAll(getPageVertices((VisualPage) v));
            } else vertices.add(v);
        }
    }


    public static void getPages(VisualCPOG visualCpog, ArrayList<VisualPage> groups) {
        ArrayList<Node> prevSelection = copySelected(visualCpog);
        visualCpog.selectAll();

        for(Node n : visualCpog.getSelection()) {
            if (n instanceof VisualPage) {
                if (prevSelection.contains(n)) {
                    groups.add((VisualPage) n);
                    //prevSelection.remove(n);
                }
            }
        }

        visualCpog.select(prevSelection);
    }

    public static void getScenarios(VisualCPOG visualCpog, ArrayList<VisualTransformableNode> groups) {
        ArrayList<Node> prevSelection = copySelected(visualCpog);
        visualCpog.selectAll();

        for(Node n : visualCpog.getSelection()) {
            if ((n instanceof VisualScenarioPage) || (n instanceof VisualScenario)) {
                if (prevSelection.contains(n)) {
                    groups.add((VisualTransformableNode) n);
                }
            }
        }

        visualCpog.select(prevSelection);
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
		 ArrayList<Node> children, allChildren = new ArrayList<Node>();
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

	 public void removeTransitives(VisualCPOG visualCpog,  HashSet<Node> roots) {
		 HashSet<VisualArc> transitives = findTransitives(visualCpog, roots);
		 for (VisualArc t : transitives) {
			 visualCpog.remove(t);
			 }
	 }

	 public String replaceReferences(String text)
	 {
		 usedReferences = new ArrayList<String>();
		 boolean added;
		 for (String k : refMap.keySet())
			{
			 added = false;
				if (text.contains(" " + k + " ")){
					if (k.startsWith("[")) {
						text = text.replaceAll(" " + k + " ", " (" + refMap.get(k) + ") ");
						added = true;
					} else {
						text = text.replaceAll(" " + k + " ", " (" + refMap.get(k) + ") ");
						added = true;
					}
				} if (text.contains("]" + k + " ")) {
						text = text.replaceAll("]" + k + " ", "](" + refMap.get(k) + ") ");
						added = true;
				} if (text.contains("(" + k + ")")) {
						text = text.replaceAll("\\(" + k + "\\)", "\\(" + refMap.get(k) + "\\)");
						added = true;
				} if (text.contains("(" + k + " ")) {
						text = text.replaceAll("\\(" + k + " ", "\\(\\(" + refMap.get(k) + "\\) ");
						added = true;
				} if (text.contains(" " + k + ")")) {
						text = text.replaceAll(" " + k + "\\)", " \\(" + refMap.get(k) + "\\)\\)");
						added = true;
				} if (text.endsWith(" " + k)) {
						text = text.replace(" " + k, " (" + refMap.get(k) + ")");
						added = true;
				} if (text.endsWith("]" + k)) {
						text = text.replace("]" + k, "](" + refMap.get(k) + ")");
						added = true;
				} if (text.endsWith(" " + k + ")")) {
						text = text.replace(" " + k + "\\)", " (" + refMap.get(k) + "\\)\\)");
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
										if (dupArcs.size() > 1)
										{
											for (VisualArc va : dupArcs) {
												if (FormulaToString.toString(va.getCondition()).compareTo("1") != 0)
												{
													dupArcs.remove(va);
												} else
												{
													visualCpog.remove(va);
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

	 public void addToReferenceList(String gn, VisualCPOG visualCpog, String text)
	 {
			gn = gn.replace("{", "");
			gn = gn.replace("}", "");

			refMap.put(gn, text);
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
				 if (centre.getY() < startPoint.getY()) {
				 	startPoint.setLocation(startPoint.getX(), centre.getY());
			 	}
				 if (centre.getX() < startPoint.getX()){
					 startPoint.setLocation(centre.getX(), startPoint.getY());
				 }
			 }
		 }
		 for(VisualScenarioPage page : pages) {
			 Rectangle2D.Double rect = (java.awt.geom.Rectangle2D.Double) page.getBoundingBox();
			 Point2D.Double bl = new Point2D.Double(rect.getCenterX(), rect.getCenterY() + (rect.getHeight()/2));

			 if (startPoint == null) {
				 startPoint = new Point2D.Double(bl.getX(), bl.getY());
			 } else {
				 if (bl.getY() > startPoint.getY()) {
					 startPoint.setLocation(startPoint.getX(), bl.getY());
				 }
				 if (bl.getX() < startPoint.getX()) {
					 startPoint.setLocation(bl.getX(), startPoint.getY());
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

     public ArrayList<VisualComponent> getPageVertices(VisualPage p) {
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
}
