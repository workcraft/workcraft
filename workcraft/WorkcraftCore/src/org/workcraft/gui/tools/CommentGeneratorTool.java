package org.workcraft.gui.tools;

import org.workcraft.Framework;
import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.exceptions.NodeCreationException;
import org.workcraft.gui.MainWindow;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.tools.editors.AbstractInplaceEditor;
import org.workcraft.gui.tools.editors.LabelInplaceEditor;

import java.awt.geom.Point2D;

public class CommentGeneratorTool extends NodeGeneratorTool {

    public CommentGeneratorTool() {
        super(new DefaultNodeGenerator(CommentNode.class) {
            @Override
            public VisualNode generate(VisualModel model, Point2D where) throws NodeCreationException {
                final VisualComment comment = (VisualComment) super.generate(model, where);
                final Framework framework = Framework.getInstance();
                final MainWindow mainWindow = framework.getMainWindow();
                final Toolbox toolbox = mainWindow.getCurrentToolbox();
                final GraphEditor editor = mainWindow.getCurrentEditor();
                final GraphEditorTool defaultTool = toolbox.getDefaultTool();
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
