package edu.kaist.mrlab.cw.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class P1vsP2 {
	
	private Path inputPath;
	private ArrayList<Path> filePathList;

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

	public P1vsP2 setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}
	
	Set<String> idSet = new HashSet<>();
	
	public void loadPilot1() throws Exception {
		while ((inputPath = P1vsP2.this.extractInputPath()) != null) {
			BufferedReader br = Files.newBufferedReader(inputPath);

			if (inputPath.toString().contains(".DS_Store")) {
				continue;
			}

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			JSONParser parser = new JSONParser();
			JSONObject result = (JSONObject) parser.parse(str.toString().trim());
			String wikiPageId = result.get("wikiPageId").toString();
			String pIdx = result.get("pIdx").toString();
			
			idSet.add(wikiPageId + "\t" + pIdx);
			
		}
		
	}
	
	Set<String> commonSet = new HashSet<>();
	
	public void getSameData() throws Exception {
		
		while ((inputPath = P1vsP2.this.extractInputPath()) != null) {
			BufferedReader br = Files.newBufferedReader(inputPath);

			if (inputPath.toString().contains(".DS_Store")) {
				continue;
			}

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			JSONParser parser = new JSONParser();
			JSONObject result = (JSONObject) parser.parse(str.toString().trim());
			
			String docID = result.get("docID").toString();
			String parID = result.get("parID").toString();
			
			if(idSet.contains(docID + "\t" + parID)) {
				
				System.out.println(result.get("fileName").toString());
				commonSet.add(docID + "\t" + parID);
				
			}
			
			
		}
		
	}
	
	public void getCommonInP1() throws Exception {
		
		while ((inputPath = P1vsP2.this.extractInputPath()) != null) {
			BufferedReader br = Files.newBufferedReader(inputPath);

			if (inputPath.toString().contains(".DS_Store")) {
				continue;
			}

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			JSONParser parser = new JSONParser();
			JSONObject result = (JSONObject) parser.parse(str.toString().trim());
			String wikiPageId = result.get("wikiPageId").toString();
			String pIdx = result.get("pIdx").toString();
			
			if(commonSet.contains(wikiPageId + "\t" + pIdx)) {
				System.out.println(inputPath.getFileName());
			}
			
		}
		
	}
	
	
	public static void main(String[] ar) throws Exception {
		
		P1vsP2 pp = new P1vsP2();
		pp.setInputPath(Paths.get("data/annotated/pilot1/task1_ELD"));
		pp.loadCorpus();
		pp.loadPilot1();
		
		pp.setInputPath(Paths.get("data/annotated/pilot2/task1_output"));
		pp.loadCorpus();
		pp.getSameData();
		
		pp.setInputPath(Paths.get("data/annotated/pilot1/task1_ELD"));
		pp.loadCorpus();
		pp.getCommonInP1();
		
	}
}
