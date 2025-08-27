package org.workcraft.dom.visual;

import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.Hotkey;
import org.workcraft.annotations.SVGIcon;
import org.workcraft.dom.math.CommentNode;
import org.workcraft.gui.properties.PropertyDeclaration;
import org.workcraft.gui.tools.Decoration;
import org.workcraft.observation.PropertyChangedEvent;
import org.workcraft.plugins.builtin.settings.VisualCommonSettings;
import org.workcraft.utils.ColorUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

@Hotkey(KeyEvent.VK_N)
@DisplayName("Text Note")
@SVGIcon("images/node-comment.svg")
public class VisualComment extends VisualComponent {
    public static final String PROPERTY_TEXT_ALIGNMENT = "Text alignment";

    protected double size = VisualCommonSettings.getCommentBaseSize();
    protected double strokeWidth = VisualCommonSettings.getCommentStrokeWidth();
    protected Alignment textAlignment = VisualCommonSettings.getCommentTextAlignment();
    private RenderedParagraph labelRenderedParagraph = new RenderedParagraph("", getLabelFont(), getLabelPositioning(), getLabelOffset());

    public VisualComment(CommentNode note) {
        super(note);
        setLabelPositioning(Positioning.CENTER);
        setForegroundColor(VisualCommonSettings.getCommentBorderColor());
        setFillColor(VisualCommonSettings.getCommentFillColor());
        setLabelColor(VisualCommonSettings.getCommentTextColor());
        removePropertyDeclarationByName(PROPERTY_NAME_POSITIONING);
        removePropertyDeclarationByName(PROPERTY_NAME_COLOR);
        removePropertyDeclarationByName(PROPERTY_LABEL_POSITIONING);
        addPropertyDeclarations();
    }

    private void addPropertyDeclarations() {
        addPropertyDeclaration(new PropertyDeclaration<>(Alignment.class, PROPERTY_TEXT_ALIGNMENT,
                this::setTextAlignment, this::getTextAlignment).setCombinable().setTemplatable());
    }

    public Alignment getTextAlignment() {
        return textAlignment;
    }

    public void setTextAlignment(Alignment value) {
        if (textAlignment != value) {
            textAlignment = value;
            sendNotification(new PropertyChangedEvent(this, PROPERTY_TEXT_ALIGNMENT));
        }
    }

    @Override
    public Font getLabelFont() {
        return LABEL_FONT.deriveFont((float) VisualCommonSettings.getCommentFontSize());
    }

    @Override
    public boolean getLabelVisibility() {
        return true;
    }

    @Override
    public Point2D getLabelOffset() {
        return new Point2D.Double(0.0, 0.0);
    }

    @Override
    public boolean getNameVisibility() {
        return false;
    }

    @Override
    public Point2D getNameOffset() {
        return new Point2D.Double(0.0, 0.0);
    }

    @Override
    public Alignment getLabelAlignment() {
        return getTextAlignment();
    }

    @Override
    protected void cacheLabelRenderedText(String text, Font font, Positioning positioning, Point2D offset) {
        if (labelRenderedParagraph.isDifferent(text, font, positioning, offset)) {
            labelRenderedParagraph = new RenderedParagraph(text, font, positioning, offset);
        }
    }

    @Override
    protected void drawLabelInLocalSpace(DrawRequest r) {
        if (getLabelVisibility() && (labelRenderedParagraph != null) && !labelRenderedParagraph.isEmpty()) {
            cacheLabelRenderedText(r);
            Graphics2D g = r.getGraphics();
            Decoration d = r.getDecoration();
            g.setColor(ColorUtils.colorise(getLabelColor(), d.getColorisation()));
            labelRenderedParagraph.draw(g, getLabelAlignment());
        }
    }
    @Override
    public Rectangle2D getLabelBoundingBox() {
        if ((labelRenderedParagraph != null) && !labelRenderedParagraph.isEmpty()) {
            return labelRenderedParagraph.getBoundingBox();
        } else {
            return new Rectangle2D.Double();
        }
    }

    @Override
    public void draw(DrawRequest r) {
        Graphics2D g = r.getGraphics();
        cacheRenderedText(r); // needed to better estimate the bounding box
        Rectangle2D shape = getBoundingBoxInLocalSpace();
        //shape.setRect(shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
        shape.setRect(shape.getX() - 0.1, shape.getY() - 0.1, shape.getWidth() + 0.2, shape.getHeight() + 0.2);
        g.setColor(ColorUtils.colorise(getFillColor(), r.getDecoration().getBackground()));
        g.fill(shape);
        g.setColor(ColorUtils.colorise(getForegroundColor(), r.getDecoration().getColorisation()));
        float w = (float) strokeWidth;
        float[] pattern = {10.0f * w, 10.0f * w};
        g.setStroke(new BasicStroke(w, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.0f, pattern, 0.0f));
        g.draw(shape);
        drawLabelInLocalSpace(r);
    }

    @Override
    public boolean hitTestInLocalSpace(Point2D pointInLocalSpace) {
        return getBoundingBoxInLocalSpace().contains(pointInLocalSpace);
    }
    @Override
    public void copyStyle(Stylable src) {
        super.copyStyle(src);
        if (src instanceof VisualComment srcComment) {
            setTextAlignment(srcComment.getTextAlignment());
        }
    }

    @Override
    public void mixStyle(Stylable... srcs) {
        super.mixStyle(srcs);
        LinkedList<Alignment> textAlignments = new LinkedList<>();
        for (Stylable src: srcs) {
            if (src instanceof VisualComment srcComment) {
                textAlignments.add(srcComment.getTextAlignment());
            }
        }
        setTextAlignment(MixUtils.vote(textAlignments, Alignment.CENTER));
    }

}
