package org.workcraft.gui.tools;

import org.workcraft.dom.generators.DefaultNodeGenerator;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.dom.visual.VisualComment;
import org.workcraft.dom.visual.VisualNode;
import org.workcraft.gui.Toolbox;
import org.workcraft.gui.events.GraphEditorMouseEvent;
import org.workcraft.gui.tools.editors.AbstractInplaceEditor;
import org.workcraft.gui.tools.editors.LabelInplaceEditor;

public class CommentGeneratorTool extends NodeGeneratorTool {

    public CommentGeneratorTool() {
        super(new DefaultNodeGenerator(CommentNode.class));
    }

    @Override
    public void mouseReleased(GraphEditorMouseEvent mouseEvent) {
        VisualNode generatedNode = getGeneratedNode();
        if (generatedNode instanceof VisualComment comment) {
            Toolbox toolbox = mouseEvent.getEditor().getToolBox();
            toolbox.selectDefaultTool();
            if (toolbox.getSelectedTool() instanceof SelectionTool) {
                AbstractInplaceEditor textEditor = new LabelInplaceEditor(mouseEvent.getEditor(), comment);
                textEditor.edit(comment.getLabel(), comment.getLabelFont(),
                        comment.getLabelOffset(), comment.getLabelAlignment(), true);
            }
        }
        super.mouseReleased(mouseEvent);
    }

    @Override
    public String getHintText(final GraphEditor editor) {
        return "Click to create a text label.";
    }

}
