package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ExternalReferenceResolver;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.util.XmlUtil;

/**
 * <p>Base class for all mathematical objects that act
 * as a graph arc.</p>
 * @author Ivan Poliakov
 *
 */
@VisualClass("org.workcraft.dom.visual.connections.VisualConnection")
public class Connection extends MathNode {
	private Component first;
	private Component second;

	private void addSerialisationObjects() {
		addXMLSerialiser(new XMLSerialiser() {
			public void serialise(Element element, ExternalReferenceResolver refResolver) {
				XmlUtil.writeIntAttr(element, "first", first.getID());
				XmlUtil.writeIntAttr(element, "second", second.getID());
			}
			public void deserialise (Element element, ReferenceResolver refResolver) throws DeserialisationException {
				first = (Component) refResolver.getObject(element.getAttribute("first"));
				second = (Component) refResolver.getObject(element.getAttribute("second"));
			}
			public String getTagName() {
				return Connection.class.getSimpleName();
			}
		});
	}

	/**
	 * <p>Creates a connection between the specified components.</p>
	 * <p><b>Note: </b> this only creates the connection object. This will not automatically
	 * add the connection to the model. If you want to add the newly created connection to the model,
	 * use
	 * <code>MathModel.connect()</code> instead. Alternatively, call
	 * <code>MathModell.addConnection()</code> to add the connection to the model.
	 * @param first -- a component that the connection starts from.
	 * @param second -- a component that the connection goes to.
	 */
	public Connection (Component first, Component second) {
		super();

		this.first = first;
		this.second = second;

		addSerialisationObjects();
	}

	/**
	 * @return the component that this connection starts from.
	 */
	final public Component getFirst() {
		return first;
	}

	/**
	 * @return the component that this connection goes to.
	 */
	final public Component getSecond() {
		return second;
	}
}