package org.workcraft;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompatibilityManager {
	private static final Pattern modelNamePattern = Pattern.compile("<model class=\"(.+?)\" ref=\"\">");
	private static final Pattern classNamePattern = Pattern.compile("<([A-Z]\\S*).*>");

	@SuppressWarnings("serial")
	private class CompatibilityMap extends HashMap<String, String> { };

	private final CompatibilityMap metaCompatibilityMap = new CompatibilityMap();
	private final CompatibilityMap modelCompatibilityMap = new CompatibilityMap();
	private final HashMap<String, CompatibilityMap> globalReplacementMap = new HashMap<String, CompatibilityMap>();
	private final HashMap<String, HashMap<String, CompatibilityMap>> contextualReplacementMap = new HashMap<String, HashMap<String, CompatibilityMap>>();

	public void registerMetaReplacement(String oldMetaData, String metaData) {
		metaCompatibilityMap.put(oldMetaData, metaData);
	}

	public void registerModelReplacement(String oldModelName, String modelName) {
		modelCompatibilityMap.put(oldModelName, modelName);
	}

	public void registerGlobalReplacement(String modelName, String pattern, String replacement) {
		CompatibilityMap replacementMap = globalReplacementMap.get(modelName);
		if (replacementMap == null) {
			replacementMap = new CompatibilityMap();
			globalReplacementMap.put(modelName, replacementMap);
		}
		replacementMap.put(pattern, replacement);
	}

	public void registerContextualReplacement(String modelName, String className, String pattern, String replacement) {
		HashMap<String, CompatibilityMap> contextualMap = contextualReplacementMap.get(modelName);
		if (contextualMap == null) {
			contextualMap = new HashMap<String, CompatibilityMap>();
			contextualReplacementMap.put(modelName, contextualMap);
		}
		CompatibilityMap replacementMap = contextualMap.get(className);
		if (replacementMap == null) {
			replacementMap = new CompatibilityMap();
			contextualMap.put(className, replacementMap);
		}
		replacementMap.put(pattern, replacement);
	}

	private String replace(String line, Map.Entry<String, String> replacement, String message) {
		String newline = line.replaceAll(replacement.getKey(), replacement.getValue());
		if ((message != null) && !line.equals(newline)) {
			System.out.println(message);
			System.out.println("  old: " + replacement.getKey());
			System.out.println("  new: " + replacement.getValue());
		}
		return newline;
	}

	private String replaceMetaData(String line) {
		for (Map.Entry<String, String> replacement: metaCompatibilityMap.entrySet()) {
			if (line.contains(replacement.getKey())) {
				line = replace(line, replacement, "Compatibility management: legacy meta data");
			}
		}
		return line;
	}

	private String replaceModelName(String line) {
		for (Map.Entry<String, String> replacement: modelCompatibilityMap.entrySet()) {
			if (line.contains(replacement.getKey())) {
				line = replace(line, replacement, "Compatibility management: legacy model class");
			}
		}
		return line;
	}

	private String replaceGlobalEntry(String modelName, String line) {
		CompatibilityMap replacementMap = globalReplacementMap.get(modelName);
		if (replacementMap != null) {
			for (Map.Entry<String, String> replacement: replacementMap.entrySet()) {
				line = replace(line, replacement, "Compatibility management: global replacement");
			}
		}
		return line;
	}

	private String replaceContextualEntry(String modelName, String className, String line) {
		HashMap<String, CompatibilityMap> contextualMap = contextualReplacementMap.get(modelName);
		if (contextualMap != null) {
			CompatibilityMap replacementMap = contextualMap.get(className);
			if (replacementMap != null) {
				for (Map.Entry<String, String> replacement: replacementMap.entrySet()) {
					line = replace(line, replacement, "Compatibility management: contextual replacement for " + className);
				}
			}
		}
		return line;
	}

	private String replaceEntry(String modelName, String className, String line) {
		line = replaceGlobalEntry(modelName, line);
		line = replaceContextualEntry(modelName, className, line);
		return line;
	}

	private String extractModelName(String line) {
		String result = null;
		Matcher matcher = modelNamePattern.matcher(line);
		if (matcher.find()) {
			result = matcher.group(1);
		}
		return result;
	}

	private String extractClassName(String line) {
		String result = null;
		Matcher matcher = classNamePattern.matcher(line);
		if (matcher.find()) {
			result = matcher.group(1);
		}
		return result;
	}

	public ByteArrayInputStream process(InputStream is) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ZipInputStream zis = new ZipInputStream(is);
		ZipOutputStream zos = new ZipOutputStream(out);
		ZipEntry zei;
	    BufferedReader br = new BufferedReader(new InputStreamReader(zis));
		try {
			while ((zei = zis.getNextEntry()) != null)	{
				ZipEntry zeo = new ZipEntry(zei.getName());
				boolean isMetaEntry = "meta".equals(zei.getName());
			    zos.putNextEntry(zeo);
			    String modelName = null;
			    String className = null;
			    String line = null;
			    while ((line = br.readLine()) != null) {
			    	if (isMetaEntry) {
			    		byte[] data = replaceMetaData(line).getBytes();
			    		zos.write(data, 0, data.length);
			    	} else if (modelName == null) {
			    		byte[] data = replaceModelName(line).getBytes();
			    		zos.write(data, 0, data.length);
			    		modelName = extractModelName(line);
			    	} else {
			    		String s = extractClassName(line);
			    		if (s != null) {
			    			className = s;
			    		}
			    		byte[] data = replaceEntry(modelName, className, line).getBytes();
			    		zos.write(data, 0, data.length);
			    	}
			    }
				zis.closeEntry();
				zos.closeEntry();
			}
			zos.close();
			out.close();
		} catch (IOException e) {
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

}
