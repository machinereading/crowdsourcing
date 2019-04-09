package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.cedarsoftware.util.io.JsonWriter;

public class REMines {
	private JSONParser mJsonParser = new JSONParser();

	private Path inputPath;
	private Path outputPath;
	private ArrayList<Path> filePathList;
	private Set<String> exceptFileSet;

	public void loadCorpus() throws Exception {
		this.filePathList = Files.walk(this.inputPath).filter(p -> Files.isRegularFile(p))
				.collect(Collectors.toCollection(ArrayList::new));
		System.out.println("done!");
		System.out.println("Number of file paths: " + filePathList.size());
	}

	private synchronized Path extractInputPath() {
		if (this.filePathList.isEmpty()) {
			return null;
		} else {
			return this.filePathList.remove(this.filePathList.size() - 1);
		}
	}

	public REMines setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	public REMines setOutputPath(Path outputPath) {
		this.outputPath = outputPath;
		return this;
	}

	public void transform() throws Exception {
		int remainCount = 0;
		int exceptCount = 0;
		BufferedReader br = null;
		BufferedWriter bw = null;
		while ((inputPath = REMines.this.extractInputPath()) != null) {

			String fileName = inputPath.getFileName().toString();
			bw = Files.newBufferedWriter(Paths.get(outputPath.toString(), fileName));

			if (fileName.contains("DS_Store")) {
				continue;
			}

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			StringBuilder str = new StringBuilder();
			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			JSONObject result = new JSONObject();
			JSONArray resultParagraphs = new JSONArray();

			JSONObject object = (JSONObject) mJsonParser.parse(str.toString().trim());
			JSONArray paragraphs = (JSONArray) object.get("paragraphs");
			Iterator<JSONObject> paraIter = paragraphs.iterator();
			while (paraIter.hasNext()) {
				JSONObject resultParagraph = new JSONObject();
				JSONObject paragraph = paraIter.next();

				int docID = Integer.parseInt(paragraph.get("wikiPageId").toString());
				int parID = Integer.parseInt(paragraph.get("pIdx").toString());
				String globalSID = paragraph.get("globalSId").toString();
				String taggedText = paragraph.get("text").toString();
				JSONArray questions = (JSONArray) paragraph.get("questions");
				Iterator<JSONObject> quesIter = questions.iterator();
				while (quesIter.hasNext()) {
					JSONObject question = quesIter.next();
					String e1 = question.get("e1").toString();
					String e2 = question.get("e2").toString();
					String nlq = question.get("nlq").toString().replace("<", "[").replace(">", "]");
					e1 = e1.substring(1, e1.length() - 1);
					e2 = e2.substring(1, e2.length() - 1);
					question.put("e1", e1);
					question.put("e2", e2);
					question.put("nlq", nlq);
				}

				taggedText = taggedText.replaceAll(">", "]");
				taggedText = taggedText.replaceAll("<", "[");
				taggedText = taggedText.replaceAll("다. ", "다.[.<line>.]");
				
				
				resultParagraph.put("globalSID", globalSID.substring(1, globalSID.length() - 1));
				resultParagraph.put("parID", parID);
				resultParagraph.put("docID", docID);
				resultParagraph.put("questions", questions);
				resultParagraph.put("taggedText", taggedText);
				
				resultParagraphs.add(resultParagraph);

			}
			
			result.put("paragraphs", resultParagraphs);
			bw.write(JsonWriter.formatJson(result.toString()));
			bw.close();
		}
	}

	public static void main(String[] ar) throws Exception {

		REMines rem = new REMines();
		rem.setInputPath(Paths.get("data/annotated/pilot1/task4_RE/"));
		rem.setOutputPath(Paths.get("data/gs/task4/gold_mines/"));
		rem.loadCorpus();
		rem.transform();

	}
}
