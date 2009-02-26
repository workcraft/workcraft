package org.workcraft.dom;

import org.w3c.dom.Element;
import org.workcraft.util.XmlUtil;

/**
 * Base type for mathematical objects -- components (graph nodes)
 * and connections (graph arcs).
 * @author Ivan Poliakov
 *
 */
public abstract class MathNode {
	private int ID = -1;
	private String label = "";
	private XMLSerialisation serialisation = new XMLSerialisation();

	private void addSerialisationObjects() {
		addXMLSerialiser(new XMLSerialiser() {
			public void serialise(Element element) {
				XmlUtil.writeIntAttr(element, "ID", getID());
				XmlUtil.writeStringAttr(element, "label", getLabel());
			}
			public String getTagName() {
				return MathNode.class.getSimpleName();
			}
		});
	}

	public MathNode() {
		addSerialisationObjects();
	}

	public MathNode(Element nodeElement) {
		Element element = XmlUtil.getChildElement(MathNode.class.getSimpleName(), nodeElement);
		setID(XmlUtil.readIntAttr(element, "ID", -1));
		setLabel(XmlUtil.readStringAttr(element, "label"));

		addSerialisationObjects();
	}

	/**
	 * <p>Adds an <type>XMLSerialiser</type> object to the serialisers list.
	 * All added serialisers will be called sequentially upon invocation of
	 * <code>serialiseToXML</code> to generate the resulting XML
	 * segment. </p>
	 * <p>This method provides the ability for all types inherited from
	 * <type>MathNode</type> to store serialisation information in
	 * an XML element, without interfering with other types in the
	 * inheritance chain.</p>
	 * <p><b>Note: should only be called inside the constructor of a type
	 * derived from <type>MathNode</type>. </b></p>
	 * <p><i>Example: </i></p>
	 * <pre>
	 * class A extends MathNode {
	 *     private int a;
	 *
	 *     public class A() {
	 *     	addXMLSerialiser(new XMLSerialiser() {
	 *       public String getTagName() {
	 *     	    return A.class.getSimpleName();
	 *     	  }
	 *       public void serialise(Element element) {
	 *       	XmlUtil.writeIntAttr(element, "a", a);
	 *       }
	 *      });
	 *    ...
	 *   }
	 * }
	 * 	 *
	 * class B extends A {
	 *     private int b;
	 *
	 *     public class B() {
	 *     super();
	 *     	addXMLSerialiser(new XMLSerialiser() {
	 *       public String getTagName() {
	 *     	    return B.class.getSimpleName();
	 *     	  }
	 *       public void serialise(Element element) {
	 *       	XmlUtil.writeIntAttr(element, "b", b);
	 *       }
	 *      });
	 *    ...
	 *   }
	 * }
	 * </pre>
	 * In this manner, when <code>serialiseToXML</code> is called, following
	 * XML will be produced:<br/>
	 *<pre>
	 *&lt;MathNode&gt;
	 *  &lt;A a="..."/&gt;
	 *  &lt;B b="..."/&gt;
	 *&lt;/MathNode&gt;
	 *</pre>
	 *
	 * @param serialiser - an XMLSerialiser object that can serialise the
	 * type to XML.
	 */
	final protected void addXMLSerialiser(XMLSerialiser serialiser) {
		serialisation.addSerialiser(serialiser);
	}

	/**
	 * Serialises the node into an XML element using <type>XMLSerialiser</type>
	 * objects registered by all the types in hierarchy.
	 * @param componentElement
	 */
	final protected void serialiseToXML(Element componentElement) {
		serialisation.serialise(componentElement);
	}


	/**
	 * @return an optional node label.
	 */
	final public String getLabel() {
		return label;
	}

	/**
	 * <p>Sets the node label.</p>
	 * @param label -- the new label.
	 */
	final public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Sets the node ID.
	 * @param id -- the new ID.
	 */
	final protected void setID(int id) {
		ID = id;
	}

	/**
	 * <p>Returns the node's unique ID number.</p>
	 * <p><b>Note:</b> the IDs are not persistent across
	 * serialisation/deserialisation operation, and can only
	 * be reliably used for an in-memory model.
	 *
	 * @return -- a unique integer ID.
	 */
	final public int getID() {
		return ID;
	}
}