package org.workcraft.plugins.sdfs;
import org.workcraft.annotations.DisplayName;
import org.workcraft.annotations.VisualClass;
import org.workcraft.dom.math.MathNode;


@DisplayName("Register")
@VisualClass("org.workcraft.plugins.sdfs.VisualRegister")
public class Register extends MathNode {

	protected boolean marked = false;
	protected boolean enabled = false;

	public Register() {
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
}
