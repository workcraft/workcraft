package org.workcraft.plugins.sdfs;
import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.DisplayName;
import org.workcraft.dom.VisualClass;
import org.workcraft.dom.XMLSerialiser;
import org.workcraft.util.XmlUtil;


@DisplayName("Register")
@VisualClass("org.workcraft.plugins.sdfs.VisualRegister")
public class Register extends Component {

	protected boolean marked = false;
	protected boolean enabled = false;

	public Register(Element componentElement) {
		super(componentElement);

		Element e = XmlUtil.getChildElement(Register.class.getSimpleName(), componentElement);
		marked = XmlUtil.readBoolAttr(e, "marked");
		enabled = XmlUtil.readBoolAttr(e, "enabled");

		addXMLSerialisable();
	}

	public Register() {
		super();

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
			public void serialise(Element element) {
				XmlUtil.writeBoolAttr(element, "enabled", enabled);
				XmlUtil.writeBoolAttr(element, "marked", marked);
			}
		});
	}


}
