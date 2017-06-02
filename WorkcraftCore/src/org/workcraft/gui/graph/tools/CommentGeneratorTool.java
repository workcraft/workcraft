package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import org.workcraft.Framework;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.graph.editors.AbstractInplaceEditor;
import org.workcraft.gui.graph.editors.LabelInplaceEditor;
import org.workcraft.gui.graph.generators.DefaultNodeGenerator;

public class CommentGeneratorTool extends NodeGeneratorTool {

    public CommentGeneratorTool() {
        super(new DefaultNodeGenerator(CommentNode.class) {
            @Override
            public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
                final VisualComment comment = (VisualComment) super.generate(model, where);
                Framework framework = Framework.getInstance();
                MainWindow mainWindow = framework.getMainWindow();
                Toolbox toolbox = mainWindow.getCurrentToolbox();
                GraphEditor editor = mainWindow.getCurrentEditor();
                GraphEditorTool defaultTool = toolbox.getDefaultTool();
                toolbox.selectTool(defaultTool);
                if (defaultTool instanceof SelectionTool) {
                    AbstractInplaceEditor textEditor = new LabelInplaceEditor(editor, comment);
                    textEditor.edit(comment.getLabel(), comment.getLabelFont(),
                            comment.getLabelOffset(), comment.getLabelAlignment(), true);
                }
                return comment;
            }
        });
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create a text label.";
    }

}
