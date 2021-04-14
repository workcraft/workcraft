package org.workcraft.plugins.builtin.commands;

import org.workcraft.Framework;
import org.workcraft.commands.AbstractLayoutCommand;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.*;
import org.workcraft.dom.visual.connections.ConnectionGraphic;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.LayoutException;
import org.workcraft.exceptions.ModelValidationException;
import org.workcraft.exceptions.NoExporterException;
import org.workcraft.exceptions.SerialisationException;
import org.workcraft.interop.Exporter;
import org.workcraft.plugins.builtin.interop.DotFormat;
import org.workcraft.plugins.builtin.settings.DotLayoutSettings;
import org.workcraft.plugins.layout.jj.DotParser;
import org.workcraft.plugins.layout.jj.ParseException;
import org.workcraft.tasks.*;
import org.workcraft.types.Pair;
import org.workcraft.utils.*;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DotLayoutCommand extends AbstractLayoutCommand {

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
            String toolName = ExecutableUtils.getAbsoluteCommandPath(DotLayoutSettings.getCommand());
            args.add(toolName);
            args.add("-Tdot");
            args.add("-o");
            args.add(layout.getAbsolutePath());
            args.add(original.getAbsolutePath());

            Task<ExternalProcessOutput> task = new ExternalProcessTask(args, directory);
            TaskManager taskManager = Framework.getInstance().getTaskManager();
            Result<? extends ExternalProcessOutput> res = taskManager.execute(task, "Laying out the graph...");

            if (res.isCancel()) {
                return;
            }
            if (res.isFailure()) {
                throw new LayoutException("Failed to execute external process:\n" + res.getCause());
            }
            ExternalProcessOutput output = res.getPayload();
            if (output.getReturnCode() == 0) {
                String in = FileUtils.readAllText(layout);
                applyLayout(in, model);
            } else {
                throw new LayoutException("External process (dot) failed (code " +
                    output.getReturnCode() + ")\n\n" + output.getStdoutString() + "\n\n" + output.getStderrString());
            }
        } catch (IOException | ModelValidationException | SerialisationException e) {
            DialogUtils.showError(e.getMessage());
        }
    }

    private void saveGraph(VisualModel model, File file)
            throws IOException, ModelValidationException, SerialisationException {

        DotFormat format = DotFormat.getInstance();
        Exporter exporter = ExportUtils.chooseBestExporter(model, format);
        if (exporter == null) {
            throw new NoExporterException(model, format);
        }
        FileOutputStream out = new FileOutputStream(file);
        exporter.export(model, out);
        out.close();
    }

    private void applyLayout(String text, VisualModel model) {
        DotParser parser = new DotParser(text);
        try {
            parser.parseGraph();
            for (String node : parser.getNodes()) {
                applyNodePosition(model, node, parser.getNodeAttributes(node));
            }
            straightenConnections(model);
            for (Pair<String, String> edge : parser.getEdges()) {
                applyEdgeShape(model, edge, parser.getEdgeAttributes(edge));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        for (VisualPage page : Hierarchy.getDescendantsOfType(model.getRoot(), VisualPage.class)) {
            page.centerPivotPoint(true, true);
        }
    }

    private void straightenConnections(VisualModel model) {
        for (VisualConnection connection : Hierarchy.getDescendantsOfType(model.getRoot(), VisualConnection.class)) {
            connection.setConnectionType(VisualConnection.ConnectionType.BEZIER);
            connection.setConnectionType(VisualConnection.ConnectionType.POLYLINE);
        }
    }

    private void applyNodePosition(VisualModel model, String id, DotParser.Attributes attributes)
            throws ParseException {

        Node node = model.getNodeByReference(id);
        String s = attributes.get("pos");
        if ((node instanceof VisualTransformableNode) && (s != null))  {
            Point2D pos = parseNodePosition(s);
            ((VisualTransformableNode) node).setRootSpacePosition(pos);
        }
    }

    private Point2D parseNodePosition(String str) throws ParseException {
        String[] posParts = str.split(",");
        if (posParts.length == 2) {
            double x = parseCoord(posParts[0]);
            double y = -parseCoord(posParts[1]);
            return new Point2D.Double(x, y);
        }
        throw new ParseException("Bad node position format.");
    }

    private void applyEdgeShape(VisualModel model, Pair<String, String> edge, DotParser.Attributes attributes)
            throws ParseException {

        if (DotLayoutSettings.getImportConnectionsShape()) {
            VisualNode fromNode = model.getNodeByReference(edge.getFirst());
            VisualNode toNode = model.getNodeByReference(edge.getSecond());
            VisualConnection connection = model.getConnection(fromNode, toNode);
            String s = attributes.get("pos");
            if ((connection != null) && (s != null)) {
                connection.setScaleMode(VisualConnection.ScaleMode.ADAPTIVE);
                List<Point2D> points = parseEdgeShape(s);
                Collections.reverse(points);
                ConnectionHelper.addControlPoints(connection, points);
                ConnectionGraphic graphic = connection.getGraphic();
                if (graphic instanceof Polyline) {
                    ConnectionHelper.filterControlPoints((Polyline) graphic, 1.0, 1.0);
                }
            }
        }
    }

    private List<Point2D> parseEdgeShape(String pos) throws ParseException {
        ArrayList<Point2D> result = new ArrayList<>();
        Point2D end = null;
        String[] split = pos.split(" ");
        for (String s : split) {
            String[] ss = s.split(",");
            if ((ss.length < 2) || (ss.length > 3)) {
                throw new ParseException("Bad connection position format.");
            }
            if (ss.length == 3) {
                double x = +parseCoord(ss[1]);
                double y = -parseCoord(ss[2]);
                Point2D p = new Point2D.Double(x, y);
                if ("s".equals(ss[0])) {
                    result.add(0, p);
                } else {
                    if ("e".equals(ss[0])) {
                        end = p;
                    } else {
                        throw new ParseException("Bad connection position format.");
                    }
                }
            } else {
                double x = +parseCoord(ss[0]);
                double y = -parseCoord(ss[1]);
                result.add(0, new Point2D.Double(x, y));
            }
        }

        if (end != null) {
            result.add(0, end);
        }
        return result;
    }

    private double parseCoord(String s) {
        return Double.parseDouble(s) / 72.0;
    }

}
