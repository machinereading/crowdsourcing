package edu.kaist.mrlab.cw.data;

public class Question {
	int id;
	String e1surf;
	String e2surf;
	
	public Question(int id, String e1surf, String e2surf) {
		this.id = id;
		this.e1surf = e1surf;
		this.e2surf = e2surf;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getE1surf() {
		return e1surf;
	}

	public void setE1surf(String e1surf) {
		this.e1surf = e1surf;
	}

	public String getE2surf() {
		return e2surf;
	}

	public void setE2surf(String e2surf) {
		this.e2surf = e2surf;
	}
	
	
}
