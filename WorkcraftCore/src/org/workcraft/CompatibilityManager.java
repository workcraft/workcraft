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
	private static final Pattern modelNamePattern = Pattern.compile("<model class=\"\\s*(.+?)\\s*\" ref=\"\">");

	@SuppressWarnings("serial")
	private class CompatibilityMap extends HashMap<String, String> { };

	private final CompatibilityMap modelReplacementMap = new CompatibilityMap();
	private final Map<String, CompatibilityMap> entryReplacementMap = new HashMap<String, CompatibilityMap>();

	public void registerModelReplacement(String oldModelName, String modelName) {
		modelReplacementMap.put(oldModelName, modelName);
	}

	public void registerEntryReplacement(String modelName, String oldEntry, String entry) {
		CompatibilityMap replacementMap = entryReplacementMap.get(modelName);
		if (replacementMap == null) {
			replacementMap = new CompatibilityMap();
			entryReplacementMap.put(modelName, replacementMap);
		}
		replacementMap.put(oldEntry, entry);
	}

	private String replace(String line, Map.Entry<String, String> replacement, String message) {
		if (line.contains(replacement.getKey())) {
			if (message != null) {
				System.out.println(message);
				System.out.println("  old: " + replacement.getKey());
				System.out.println("  new: " + replacement.getValue());
			}
			line = line.replace(replacement.getKey(), replacement.getValue());
		}
		return line;
	}

	private String processModelLine(String line) {
		for (Map.Entry<String, String> replacement: modelReplacementMap.entrySet()) {
			if (line.contains(replacement.getKey())) {
				replace(line, replacement, "Compatibility management of legacy model class:");
			}
		}
		return line;
	}

	private String processEntryLine(String modelName, String line) {
		CompatibilityMap replacementMap = entryReplacementMap.get(modelName);
		if (replacementMap != null) {
			for (Map.Entry<String, String> replacement: replacementMap.entrySet()) {
				line = replace(line, replacement, "Compatibility management of legacy entry:");
			}
		}
		return line;
	}

	private String parseModelLine(String line) {
		String result = null;
		Matcher matcher = modelNamePattern.matcher(line);
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
				ZipEntry zeo = new ZipEntry(zei);
			    zos.putNextEntry(zeo);
			    String modelName = null;
			    String line = null;
			    while ((line = br.readLine()) != null) {
			    	if (modelName == null) {
			    		byte[] data = processModelLine(line).getBytes();
			    		zos.write(data, 0, data.length);
			    		modelName = parseModelLine(line);
			    	} else {
			    		byte[] data = processEntryLine(modelName, line).getBytes();
			    		zos.write(data, 0, data.length);
			    	}
			    }
				zis.closeEntry();
				zos.closeEntry();
			}
			zos.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ByteArrayInputStream(out.toByteArray());
	}

}
