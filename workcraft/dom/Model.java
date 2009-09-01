package org.workcraft.dom;

import org.workcraft.dom.visual.VisualModel;

/**
 * A convenience type that integrates a MathModel and a VisualModel.
 * @author Ivan Poliakov
 *
 */
public interface Model {
	/**
	 * @return the mathematical model. This value cannot be null.
	 */
	public MathModel getMathModel();

	/**
	 * @return the associated visual model. This value may be null if the underlying
	 * mathematical model does not have an associated visual model.
	 */
	public VisualModel getVisualModel();

	/**
	 * @return the title of the model as specified by the user.
	 */
	public String getTitle();

	/**
	 * @return a user-friendly display name for this model, which is either
	 * read from <type>DisplayName</type> annotation, or, if the annotation
	 * is missing, taken from the name of the model class.
	 */
	public String getDisplayName();

	public HierarchyNode getRoot();
	public void setRoot(HierarchyNode root);
}