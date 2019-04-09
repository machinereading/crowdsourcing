package edu.kaist.mrlab.cw.postpro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class CR {

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

	public CR setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}

	BufferedReader br;

	public void generate() throws Exception {

		while ((inputPath = CR.this.extractInputPath()) != null) {
			
			JSONObject result = new JSONObject();
			
			String fileName = inputPath.getFileName().toString();

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			System.out.println(inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);

			JSONParser parser = new JSONParser();
			if(str.toString().trim().length() == 0) {
				continue;
			}
			JSONObject object = (JSONObject) parser.parse(str.toString().trim());
			JSONArray pronouns = (JSONArray) object.get("pronouns");

			JSONArray entities = (JSONArray) object.get("entities");
			
			StringBuilder eld = new StringBuilder();
			
			fileName = fileName.replace("task2_input", "task1_ELD");
			
			f = new File(Paths.get("data/annotated/task1_ELD", fileName).toString());
			if(!f.exists()) {
				continue;
			}
			c = new char[(int) f.length()];

			System.out.println(inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			eld.append(c);
			JSONObject eldObject = (JSONObject) parser.parse(eld.toString().trim());
			String eldPlainText = eldObject.get("plainText").toString();
			String eldMentionTagged = eldObject.get("mentionTagged").toString();
			String eldEntityTagged = eldObject.get("entityTagged").toString();
			int eldWikiPageID = Integer.parseInt(eldObject.get("wikiPageId").toString());
			int eldPIdx = Integer.parseInt(eldObject.get("pIdx").toString());
			JSONArray eldEntities = (JSONArray) eldObject.get("entities");
			JSONArray eldGlobalSID = (JSONArray) eldObject.get("globalSId");
			
			Iterator<JSONObject> eldIt = eldEntities.iterator();
			
			Iterator<JSONObject> it = entities.iterator();
			while(it.hasNext()) {
				JSONObject entity = it.next();
				JSONObject eldEntity = eldIt.next();
				if(!entity.containsKey("ancestor")) {
					continue;
				}
				String ancestor = entity.get("ancestor").toString();
				eldEntity.put("ancestor", ancestor);
				
			}
			
			result.put("globalSId", eldGlobalSID);
			result.put("pIdx", eldPIdx);
			result.put("plainText", eldPlainText);
			result.put("mentionTagged", eldMentionTagged);
			result.put("entityTagged", eldEntityTagged);
			result.put("wikiPageId", eldWikiPageID);
			result.put("entities", eldEntities);
			result.put("pronouns", pronouns);
			
			fileName = fileName.replace("task1_ELD", "task2_CR");
			
			BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/annotated/task2_CR", fileName));
			bw.write(result.toString());
			bw.close();
		}

	}

	public static void main(String[] ar) throws Exception {

		CR cr = new CR();
		cr.setInputPath(Paths.get("data/annotated/task2_output/")).loadCorpus();
		cr.generate();

	}
}
