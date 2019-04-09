package edu.kaist.mrlab.cw.data;

public class PropertyDR {
	private String property = null;
	private String domain = null;
	private String range = null;
	
	public PropertyDR(String property) {
		this.property = property;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

}
