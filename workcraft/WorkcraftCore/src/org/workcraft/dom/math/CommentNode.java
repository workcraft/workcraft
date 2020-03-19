package org.workcraft.dom.math;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.visual.VisualComment;

@DisplayName("Text Note")
@IdentifierPrefix(value = "comment", isInternal = true)
@VisualClass(VisualComment.class)
public class CommentNode extends MathNode {

}
