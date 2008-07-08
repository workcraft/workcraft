package org.workcraft.framework;

import java.lang.reflect.Field;

import org.w3c.dom.Element;
import org.workcraft.framework.exceptions.InvalidPluginException;


public class PluginInfo {
	public enum Type {
		MODEL,
		COMPONENT,
		DOCUMENT_TOOL,
		FILE_TOOL
	}

	private String caption;
	private String className;
	private String[] appliedTo;
	private Type type;

	public PluginInfo(Class<?> cls) throws InvalidPluginException {
		className = cls.getName();
		try {
			Field srcCaption = cls.getField("CAPTION");
			caption = (String) srcCaption.get(null);
			Field srcAppliedTo = cls.getField("APPLIED_TO");
			appliedTo = (String[]) srcAppliedTo.get(null);
		} catch(Exception e) {
			throw(new InvalidPluginException(cls));
		}
		if(caption==null) {
			caption = className.substring(className.lastIndexOf('.')+1);
		}
	}

	public PluginInfo(Element element) throws InvalidPluginException {
		// TODO
	}

	public void toXml(Element element) {
		// TODO
	}

	/**
	 * @return the menu caption
	 */
	public String getCaption() {
		return caption;
	}
	/**
	 * @return the plugin class name
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * @return the array of file extensions or plugin class names to which this plugin is applied to
	 */
	public String[] getAppliedTo() {
		return appliedTo;
	}
	/**
	 * @return the plugin type
	 */
	public Type getType() {
		return type;
	}

}
