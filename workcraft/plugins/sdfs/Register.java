package org.workcraft.plugins.sdfs;
import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.framework.exceptions.DeserialisationException;
import org.workcraft.framework.serialisation.ReferenceProducer;
import org.workcraft.framework.serialisation.ReferenceResolver;
import org.workcraft.util.XmlUtil;


@DisplayName("Register")
@VisualClass("org.workcraft.plugins.sdfs.VisualRegister")
public class Register extends Component {

	protected boolean marked = false;
	protected boolean enabled = false;

	public Register() {
		addXMLSerialisable();
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	private void addXMLSerialisable() {
		addXMLSerialiser(new XMLSerialiser(){
			public String getTagName() {
				return Register.class.getSimpleName();
			}

			public void deserialise(Element element,
					ReferenceResolver refResolver) throws DeserialisationException {
				marked = XmlUtil.readBoolAttr(element, "marked");
				enabled = XmlUtil.readBoolAttr(element, "enabled");

			}

			public void serialise(Element element,
					ReferenceProducer refResolver) {
				XmlUtil.writeBoolAttr(element, "enabled", enabled);
				XmlUtil.writeBoolAttr(element, "marked", marked);
			}
		});
	}
}
