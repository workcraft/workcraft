package org.workcraft.dom.math;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.IdentifierPrefix;
import org.workcraft.annotations.VisualClass;

@DisplayName("Text Note")
@IdentifierPrefix(value = "comment", isInternal = true)
@VisualClass(org.workcraft.dom.visual.VisualComment.class)
public class CommentNode extends MathNode {

}
