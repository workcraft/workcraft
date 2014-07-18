package org.workcraft.plugins.cpog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.workcraft.util.FileUtils;

public class EncoderSettings {

	public enum generationMode{
		OPTIMAL_ENCODING("Simulated annealing"),
		RECURSIVE("Exhaustive search"),
		RANDOM("Random search"),
		SCENCO("Old tool SCENCO"),
		OLD_SYNT("Old synthesise"),
		SEQUENTIAL("Sequential");

		public final String name;

		public static final generationMode[] modes =
			{
				OPTIMAL_ENCODING,
				RECURSIVE,
				RANDOM,
				SCENCO,
				OLD_SYNT,
				SEQUENTIAL
			};

		private generationMode(String name){
			this.name = name;
		}

		static public Map<String, generationMode> getChoice() {
			LinkedHashMap<String, generationMode> choice = new LinkedHashMap<String, generationMode>();
			for (generationMode item : generationMode.values()) {
				choice.put(item.name, item);
			}
			return choice;
		}
	}

	public EncoderSettings(String espressoPath, String abcPath, String libPath) {
		this.espressoPath = espressoPath;
		this.abcPath = abcPath;
		this.libPath = libPath;
	}

	private int solutionNumber = 10, numPO,bits;
	private generationMode genMode = generationMode.OPTIMAL_ENCODING;
	private boolean verboseMode, customEncMode, effort, contMode, cpogSize, costFunc, abcFlag;
	private String[] customEnc;
	private String espressoPath,abcPath,libPath;

	public boolean isCpogSize() {
		return cpogSize;
	}

	public void setCpogSize(boolean cpogSize) {
		this.cpogSize = cpogSize;
	}



	public boolean isAbcFlag() {
		return abcFlag;
	}

	public void setAbcFlag(boolean abcFlag) {
		this.abcFlag = abcFlag;
	}

	public boolean isCostFunc() {
		return costFunc;
	}

	public void setCostFunc(boolean costFunc) {
		this.costFunc = costFunc;
	}

	public boolean isContMode() {
		return contMode;
	}

	public void setContMode(boolean contMode) {
		this.contMode = contMode;
	}

	public boolean isEffort() {
		return effort;
	}

	public void setEffort(boolean effort) {
		this.effort = effort;
	}
	public int getBits() {
		return bits;
	}

	public void setBits(int bits) {
		this.bits = bits;
	}

	public String getEspressoPath() {
		return espressoPath;
	}

	public void setEspressoPath(String espressoPath) {
		this.espressoPath = espressoPath;
	}

	public String getAbcPath() {
		return abcPath;
	}

	public void setAbcPath(String abcPath) {
		this.abcPath = abcPath;
	}

	public EncoderSettings(int solutionNumber, generationMode genMode, boolean verboseMode,
			boolean customEncMode, String[] customEnc, int numPO) {
		this.solutionNumber = solutionNumber;
		this.genMode = genMode;
		this.verboseMode = verboseMode;
		this.customEncMode = customEncMode;
		this.numPO = numPO;
		this.customEnc = customEnc;
	}

	public EncoderSettings(int solutionNumber, generationMode genMode, boolean verboseMode,
			boolean customEncMode) {
		this.solutionNumber = solutionNumber;
		this.genMode = genMode;
		this.verboseMode = verboseMode;
		this.customEncMode = customEncMode;
	}

	public int getSolutionNumber() {
		return solutionNumber;
	}

	public void setSolutionNumber(int number){
		solutionNumber = number;
	}

	public void setGenerationModeInt(int index){
		switch(index){
		case 0:
			genMode = generationMode.OPTIMAL_ENCODING;
			break;
		case 1:
			genMode = generationMode.RECURSIVE;
			break;
		case 2:
			genMode = generationMode.RANDOM;
			break;
		case 3:
			genMode = generationMode.SCENCO;
			break;
		case 4:
			genMode = generationMode.OLD_SYNT;
			break;
		case 5:
			genMode = generationMode.SEQUENTIAL;
			break;
		default:
			System.out.println("Error.");
		}
	}

	public int getNumPO() {
		return numPO;
	}

	public void setNumPO(int numPO) {
		this.numPO = numPO;
	}

	public generationMode getGenMode() {
		return genMode;
	}

	public void setGenMode(generationMode genMode) {
		this.genMode = genMode;
	}

	public boolean isVerboseMode() {
		return verboseMode;
	}

	public void setVerboseMode(boolean verboseMode) {
		this.verboseMode = verboseMode;
	}

	public boolean isCustomEncMode() {
		return customEncMode;
	}

	public void setCustomEncMode(boolean customEncMode) {
		this.customEncMode = customEncMode;
	}

	public String[] getCustomEnc() {
		return customEnc;
	}

	public void setCustomEnc(String[] customEnc) {
		this.customEnc = customEnc;
	}

	public String getLibPath() {
		return libPath;
	}

	public void setLibPath(String libPath) {
		this.libPath = libPath;
	}


}
