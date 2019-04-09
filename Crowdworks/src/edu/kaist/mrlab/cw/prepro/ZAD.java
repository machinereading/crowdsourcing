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
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import edu.kaist.mrlab.cw.script.ZAMention;

public class ZAD {
	
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

	public ZAD setInputPath(Path inputPath) {
		this.inputPath = inputPath;
		return this;
	}
	
	public JSONArray adjustEntities(JSONArray entities, JSONObject item, int count) {
		
		JSONArray result = new JSONArray();
		
		int itemStart = Integer.parseInt(item.get("st").toString());
		int itemEnd = Integer.parseInt(item.get("en").toString());
		String keyword = item.get("keyword").toString().replace(" ", "_");
		keyword = "[" + keyword + "]";
		
		String left = text.substring(0, itemStart + (count * 2));
		String right = text.substring(itemEnd + (count * 2), text.length());
		text = left + keyword + right;
		
		itemEnd += (2 + (count * 2));
		item.put("en", itemEnd);
		item.put("keyword", keyword);
		
		int itemEIdx = -1;
		
		boolean isStarting = true;
		
		Iterator<JSONObject> it = entities.iterator();
		while(it.hasNext()) {
			JSONObject entity = it.next();
			int st = Integer.parseInt(entity.get("st").toString());
			if(st < itemStart) {
				result.add(entity);
				continue;
			} else {
				int eIdx = Integer.parseInt(entity.get("eIdx").toString());
				int en = Integer.parseInt(entity.get("en").toString());
				
				if(isStarting) {
					itemEIdx = eIdx;
					
					item.put("eIdx", itemEIdx);
					item.put("e_type", "PRON");
					item.put("ne_type", "");
					
					result.add(item);
					
					isStarting = false;
				}

				st += 2;
				en += 2;
				eIdx++;
				
				entity.put("eIdx", eIdx);
				entity.put("st", st);
				entity.put("en", en);
				result.add(entity);
			}
			
		}
		
		
		
		return result;
	}
	
	String text = null;
	
	BufferedReader br = null;
	
	public void parsing() throws Exception {
		
		ZAMention zam = new ZAMention();
		
		while ((inputPath = ZAD.this.extractInputPath()) != null) {
			
			boolean isWritable = false;

			StringBuilder str = new StringBuilder();

			File f = new File(inputPath.toString());
			char[] c = new char[(int) f.length()];

			System.out.println("doing: " + inputPath.toString());

			br = new BufferedReader(new FileReader(f));
			br.read(c);
			str.append(c);
			
			if(str.length() == 0 || str.length() > 5000) {
				
				String fileName = inputPath.getFileName().toString();
				fileName = fileName.replace("task2_input", "task3_prev_paragraph");
				f = new File("data/annotated/task3_prev_paragraph/" + fileName);
				if(f.exists()) {
					f.delete();
				}
				
				continue;
			}
			
			JSONObject result = new JSONObject();

			JSONParser parser = new JSONParser();
			JSONObject object = (JSONObject) parser.parse(str.toString().trim());
			
			int wikiPageID = Integer.parseInt(object.get("wikiPageId").toString());
			int pIdx = Integer.parseInt(object.get("pIdx").toString());
			text = object.get("text").toString();
			JSONArray globalSId = (JSONArray) object.get("globalSId");
			JSONArray entities = (JSONArray) object.get("entities");
			int numOfEntity = entities.size();
			
			
//			JSONArray pronouns = (JSONArray) object.get("pronouns");
//			Iterator<JSONObject> it = pronouns.iterator();
//			int count = 0;
//			while(it.hasNext()) {
//				JSONObject item = it.next();
//				String ancestor = item.get("ancestor").toString();
//				if(ancestor.equals("-1") || ancestor.equals("-2")) {
//					continue;
//				}
//				
//				entities = adjustEntities(entities, item, count);
//				count++;
//				
//			}
			JSONObject zar = zam.getZARelations(text);
			JSONArray zaList = (JSONArray) zar.get("result");
			Iterator<JSONObject> it = zaList.iterator();
			while(it.hasNext()) {
				JSONObject item = it.next();
				JSONArray verbs = (JSONArray) item.get("verbs");
				if(verbs.size() > 0) {
					isWritable = true;
					break;
				}
			}
			
			if(!isWritable) {
				
				String fileName = inputPath.getFileName().toString();
				fileName = fileName.replace("task2_input", "task3_prev_paragraph");
				f = new File("data/annotated/task3_prev_paragraph/" + fileName);
				if(f.exists()) {
					f.delete();
				}
				
				continue;
			}
			
			result.put("wikiPageId", wikiPageID);
			result.put("pIdx", pIdx);
			result.put("globalSId", globalSId);
			result.put("text", text);
			result.put("entities", entities);
			result.put("result", zaList);
			
			String fileName = inputPath.getFileName().toString();
			fileName = fileName.replace("task2_input", "task3_input");
			BufferedWriter bw = Files.newBufferedWriter(Paths.get("data/annotated/task3_input", fileName));
			bw.write(result.toJSONString());
			bw.close();
			
		}
		
	}
	
	public static void main(String[] ar) throws Exception {
		
		ZAD zad = new ZAD();
		zad.setInputPath(Paths.get("data/annotated/task2_output"));
		zad.loadCorpus();
		zad.parsing();
		
	}
}
