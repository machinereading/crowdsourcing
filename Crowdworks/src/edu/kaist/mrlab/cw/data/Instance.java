package edu.kaist.mrlab.cw.data;

public class Instance {
	String sbj;
	String pred;
	String obj;
	double score;
	String module;
	String stc;

	public Instance(String sbj, String pred, String obj, String score, String module, String stc) {
		this.sbj = sbj;
		this.pred = pred;
		this.obj = obj;
		this.score = Double.parseDouble(score);
		this.module = module;
		this.stc = stc;
	}

	public String getSbj() {
		return sbj;
	}

	public void setSbj(String sbj) {
		this.sbj = sbj;
	}

	public String getPred() {
		return pred;
	}

	public void setPred(String pred) {
		this.pred = pred;
	}

	public String getObj() {
		return obj;
	}

	public void setObj(String obj) {
		this.obj = obj;
	}

	public double getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = Double.parseDouble(score);
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getStc() {
		return stc;
	}

}
