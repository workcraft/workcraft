package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import org.workcraft.Framework;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.ToolboxPanel;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;

public class CommentGeneratorTool extends NodeGeneratorTool {

    public CommentGeneratorTool() {
        super(new DefaultNodeGenerator(CommentNode.class) {
            @Override
            public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
                VisualComment component = (VisualComment) super.generate(model, where);
                Framework framework = Framework.getInstance();
                MainWindow mainWindow = framework.getMainWindow();
                ToolboxPanel toolbox = mainWindow.getCurrentToolbox();
                GraphEditor editor = mainWindow.getCurrentEditor();
                GraphEditorTool defaultTool = toolbox.getDefaultTool();
                toolbox.selectTool(defaultTool);
                if (defaultTool instanceof SelectionTool) {
                    SelectionTool selectionTool = (SelectionTool) defaultTool;
                    selectionTool.editLabelInPlace(editor, component, "");
                }
                return component;
            }
        });
    }

    @Override
    public String getHintText() {
        return "Click to create a text label.";
    }

}
