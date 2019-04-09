package edu.kaist.mrlab.cw.data;

public class Entity {
	private int st;
	private int eIdx;
	private int en;
	private String neType;
	private String surface;
	private String eName;
	private String eType;
	
	public Entity(int eIdx, int st, int en, String surface, String eName, String eType) {
		this.eIdx = eIdx;
		this.st = st;
		this.en = en;
		this.surface = surface;
		this.eName = eName;
		this.eType = eType;
	}

//	public Entity(int st, int eIdx, int en, String neType, String surface, String eType) {
//		this.st = st;
//		this.eIdx = eIdx;
//		this.en = en;
//		this.neType = neType;
//		this.surface = surface;
//		this.eType = eType;
//	}

	public int getSt() {
		return st;
	}

	public void setSt(int st) {
		this.st = st;
	}

	public int geteIdx() {
		return eIdx;
	}

	public void seteIdx(int eIdx) {
		this.eIdx = eIdx;
	}

	public int getEn() {
		return en;
	}

	public void setEn(int en) {
		this.en = en;
	}

	public String getNeType() {
		return neType;
	}

	public void setNeType(String neType) {
		this.neType = neType;
	}

	public String geteType() {
		return eType;
	}

	public void seteType(String eType) {
		this.eType = eType;
	}

	public String getSurface() {
		return surface;
	}

	public void setSurface(String surface) {
		this.surface = surface;
	}

	public String geteName() {
		return eName;
	}

	public void seteName(String eName) {
		this.eName = eName;
	}

}
