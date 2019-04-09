package edu.kaist.mrlab.cw.data;

import java.util.List;

public class QParagraph {
	int docID;
	int parID;
	String mentionText;
	public List<Question> questions;

	public QParagraph(int wikiPageId, int pIdx, String mentionText) {
		this.docID = wikiPageId;
		this.parID = pIdx;
		this.mentionText = mentionText;
	}

	public int getDocID() {
		return docID;
	}

	public void setDocID(int docID) {
		this.docID = docID;
	}

	public int getParID() {
		return parID;
	}

	public void setParID(int parID) {
		this.parID = parID;
	}

	public String getMentionText() {
		return mentionText;
	}

	public void setMentionText(String mentionText) {
		this.mentionText = mentionText;
	}

}
