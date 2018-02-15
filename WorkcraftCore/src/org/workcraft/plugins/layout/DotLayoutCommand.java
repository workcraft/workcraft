package org.workcraft.plugins.layout;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Connection;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.ConnectionHelper;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.dom.visual.connections.VisualConnection.ConnectionType;
import org.workcraft.dom.visual.connections.VisualConnection.ScaleMode;
import org.workcraft.exceptions.LayoutException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.interop.DotFormat;
import org.workcraft.plugins.layout.jj.DotParser;
import org.workcraft.plugins.layout.jj.ParseException;
import org.workcraft.plugins.shared.tasks.ExternalProcessOutput;
import org.workcraft.plugins.shared.tasks.ExternalProcessTask;
import org.workcraft.tasks.Result;
import org.workcraft.tasks.Result.Outcome;
import org.workcraft.tasks.Task;
import org.workcraft.tasks.TaskManager;
import org.workcraft.util.DialogUtils;
import org.workcraft.util.Export;
import org.workcraft.util.FileUtils;
import org.workcraft.util.ToolUtils;

public class DotLayoutCommand extends AbstractLayoutCommand {

    private void saveGraph(VisualModel model, File file) throws IOException, ModelValidationException, SerialisationException {
        final Framework framework = Framework.getInstance();
        Exporter exporter = Export.chooseBestExporter(framework.getPluginManager(), model, DotFormat.getInstance());
        if (exporter == null) {
            throw new RuntimeException("Cannot find a .dot exporter for the model " + model);
        }
        FileOutputStream out = new FileOutputStream(file);
        exporter.export(model, out);
        out.close();
    }

    private List<Point2D> parseConnectionSpline(String pos) throws ParseException {
        try {
            ArrayList<Point2D> result = new ArrayList<>();
            Point2D end = null;
            String[] split = pos.split(" ");
            for (String s : split) {
                String[] ss = s.split(",");
                if ((ss.length < 2) || (ss.length > 3)) {
                    throw new ParseException("Bad connection position format.");
                }
                double pointsToInches = 1.0 / 72;
                if (ss.length == 3) {
                    double x = +Double.parseDouble(ss[1]) * pointsToInches;
                    double y = -Double.parseDouble(ss[2]) * pointsToInches;
                    Point2D p = new Point2D.Double(x, y);
                    if (ss[0].equals("s")) {
                        result.add(0, p);
                    } else {
                        if (ss[0].equals("e")) {
                            end = p;
                        } else {
                            throw new ParseException("Bad connection position format.");
                        }
                    }
                } else {
                    double x = +Double.parseDouble(ss[0]) * pointsToInches;
                    double y = -Double.parseDouble(ss[1]) * pointsToInches;
                    result.add(0, new Point2D.Double(x, y));
                }
            }

            if (end != null) {
                result.add(0, end);
            }
            return result;
        } catch (NumberFormatException ex) {
            throw new ParseException(ex.getMessage());
        }
    }

    private void applyLayout(String in, final VisualModel model) {
        DotParser parser = new DotParser(new StringReader(in.replace("\\\n", "")));
        try {
            parser.graph(new DotListener() {
                @Override
                public void node(String id, Map<String, String> properties) {
                    Node node = model.getNodeByReference(id);
                    if (node instanceof VisualTransformableNode) {
                        VisualTransformableNode m = (VisualTransformableNode) node;
                        String posStr = properties.get("pos");
                        if (posStr != null) {
                            String[] posParts = posStr.split(",");
                            if (posParts.length == 2) {
                                // FIXME: MovableHelper.translate does not work with groups and pages. setRootSpacePosition may be a solution.
                                double x = Double.parseDouble(posParts[0]) / 72.0;
                                double y = -Double.parseDouble(posParts[1]) / 72.0;
                                m.setRootSpacePosition(new Point2D.Double(x, y));
//                                MovableHelper.resetTransform(m);
//                                MovableHelper.translate(m, x, y);
                            } else {
                                System.err.println("Dot graph parse error: node 'pos' attribute has value '"
                                        + posStr + "', which is not a comma-separated pair of integers");
                            }
                        }
                    }
                }

                @Override
                public void arc(String from, String to, Map<String, String> properties) {
                    if (DotLayoutSettings.getImportConnectionsShape()) {
                        Node comp1 = model.getNodeByReference(from);
                        Node comp2 = model.getNodeByReference(to);
                        Set<Connection> connections = model.getConnections(comp1);
                        Connection con = null;
                        for (Connection c : connections) {
                            if (c.getSecond() == comp2) {
                                con = c;
                            }
                        }
                        if (con != null) {
                            VisualConnection vc = (VisualConnection) con;
                            vc.setConnectionType(ConnectionType.POLYLINE);
                            vc.setScaleMode(ScaleMode.ADAPTIVE);

                            Polyline poly = (Polyline) vc.getGraphic();
                            poly.remove(poly.getChildren());
                            try {
                                List<Point2D> points = parseConnectionSpline(properties.get("pos"));
                                for (int i = points.size() - 1; i >= 0; i--) {
                                    Point2D p = points.get(i);
                                    ControlPoint cp = new ControlPoint();
                                    cp.setPosition(p);
                                    poly.add(cp);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            ConnectionHelper.filterControlPoints(poly, 1.0, 1.0);
                        } else {
                            System.err.println(String.format("Unable to find a connection from %s to %s", from, to));
                        }
                    }
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDisplayName() {
        return "Graphviz DOT";
    }

    @Override
    public void layout(VisualModel model) {
        String prefix = FileUtils.getTempPrefix(model.getTitle());
        File directory = FileUtils.createTempDirectory(prefix);
        try {
            File original = new File(directory, "original.dot");
            original.deleteOnExit();
            File layout = new File(directory, "layout.dot");
            layout.deleteOnExit();

            saveGraph(model, original);

            List<String> args = new ArrayList<>();
            String toolName = ToolUtils.getAbsoluteCommandPath(DotLayoutSettings.getCommand());
            args.add(toolName);
            args.add("-Tdot");
            args.add("-o");
            args.add(layout.getAbsolutePath());
            args.add(original.getAbsolutePath());

            Task<ExternalProcessOutput> task = new ExternalProcessTask(args, directory);
            final Framework framework = Framework.getInstance();
            final TaskManager taskManager = framework.getTaskManager();
            Result<? extends ExternalProcessOutput> res = taskManager.execute(task, "Laying out the graph...");

            if (res.getOutcome() == Outcome.CANCEL) {
                return;
            }
            if (res.getOutcome() == Outcome.FAILURE) {
                throw new LayoutException("Failed to execute external process:\n" + res.getCause());
            }
            if (res.getPayload().getReturnCode() == 0) {
                String in = FileUtils.readAllText(layout);
                applyLayout(in, model);
            } else {
                throw new LayoutException("External process (dot) failed (code " +
                    res.getPayload().getReturnCode() + ")\n\n" +
                    new String(res.getPayload().getStdout()) + "\n\n" +
                    new String(res.getPayload().getStderr()));
            }
        } catch (IOException | ModelValidationException | SerialisationException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

}
