package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cedarsoftware.util.io.JsonWriter;

import edu.kaist.mrlab.cw.data.Entity;

public class NewsInitial {
	
	public static Set<String> parEntitySet = new HashSet<>();

	public static void main(String[] ar) throws Exception {

		BufferedWriter bw = null;

		HashMap<String, String> IDMap = new HashMap<>();
		HashMap<String, List<String>> kboxEntityTypes = new HashMap<>();
		
		Set<String> titleSet = new HashSet<>();
		
		BufferedReader br = Files.newBufferedReader(Paths.get("data/entity_type_kbox"));
		String input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			if (st.countTokens() == 2) {
				String entity = st.nextToken();
				String type = st.nextToken();
				List<String> types = null;
				if (kboxEntityTypes.containsKey(entity)) {
					types = kboxEntityTypes.get(entity);
					types.add(type);
				} else {
					types = new ArrayList<>();
					types.add(type);
				}
				kboxEntityTypes.put(entity, types);
			}
		}

		boolean firstFlag = true;

		br = Files.newBufferedReader(Paths.get("data/news/result4.txt"));
		String prevWikiID = null;
		int parIDC = 0;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			if (st.countTokens() == 4) {
				String xbID = st.nextToken();
				String wikiID = st.nextToken();
				String wikiTitle = st.nextToken();
				String par = st.nextToken();
				
				titleSet.add(wikiTitle);

				if (firstFlag) {
					prevWikiID = wikiID;
					firstFlag = false;
				}

				if (prevWikiID.equals(wikiID)) {
					String id = wikiID + "\t" + parIDC;
					IDMap.put(id, par);
					parIDC++;
				} else {
					parIDC = 0;
				}
				prevWikiID = wikiID;
			}
		}

		String root = "data/annotated/news-10000-part1/task1/input_190215/";
		File f = new File(root);
		if (!f.exists()) {
			f.mkdirs();
		}

		for (String id : IDMap.keySet()) {
			StringTokenizer st = new StringTokenizer(id, "\t");
			String docID = st.nextToken();
			String parID = st.nextToken();
			
			String values = IDMap.get(docID + "\t" + parID);
			if (values != null) {

				File fd = new File(root + docID + "_" + parID + ".json");
				bw = Files.newBufferedWriter(Paths.get(root, docID + "_" + parID + ".json"));

				System.out.println(docID + "\t" + parID);

				String paragraph = values.trim();

				String plainText = getPlainText(paragraph).trim();

				JSONObject result = new JSONObject();
				result.put("plainText", plainText);
				result.put("docID", docID);
				result.put("parID", parID);
				result.put("globalSID", "null");

				JSONArray entityArr = new JSONArray();
				String entityListStr = "";
				for (Entity entity : entities) {
					JSONObject entityObj = new JSONObject();
					entityObj.put("id", entity.geteIdx());
					entityObj.put("surface", entity.getSurface());
					entityObj.put("entityName", entity.geteName());
					entityObj.put("st", entity.getSt());
					entityObj.put("en", entity.getEn());
					entityObj.put("eType", entity.geteType());
					entityArr.add(entityObj);

					entityListStr += entity.getSurface();
					
					String keyword = entity.geteName();
					JSONArray kboxTypeArray = new JSONArray();
					List<String> types = kboxEntityTypes.get(keyword.replace(" ", "_"));
					if (types != null) {
						for (String type : types) {
							kboxTypeArray.add(type);
						}
					} else {
						kboxTypeArray.add("NULL");
					}

					String net = null;

					if (kboxTypeArray.contains("Person")) {
						net = "PERSON";
					} else if (kboxTypeArray.contains("Organisation")) {
						net = "ORGANIZATION";
					} else if (kboxTypeArray.contains("Place")) {
						net = "LOCATION";
					} else {
						net = "ETC";
					}

					Matcher m = Pattern.compile("^([0-9]+?)년$").matcher(keyword);
					if (m.find()) {
						net = "TIME";
					}
					m = Pattern.compile("^([0-9]+?)월_([0-9]+?)일$").matcher(keyword);
					if (m.find()) {
						net = "TIME";
					}
					m = Pattern.compile("^([0-9]+?)월 ([0-9]+?)일$").matcher(keyword);
					if (m.find()) {
						net = "TIME";
					}
					m = Pattern.compile("^([0-9]+?)월$").matcher(keyword);
					if (m.find()) {
						net = "TIME";
					}
					m = Pattern.compile("^([0-9]+?)일$").matcher(keyword);
					if (m.find()) {
						net = "TIME";
					}

					entityObj.put("kbox_types", kboxTypeArray);
					entityObj.put("ne_type", net);
				}
				
				if(parEntitySet.contains(entityListStr)) {
					bw.close();
					fd.delete();
					entities.clear();
					continue;
				} else {
					parEntitySet.add(entityListStr);
				}

				result.put("entities", entityArr);

				bw.write(JsonWriter.formatJson(result.toString()));
				// bw.write(new GsonBuilder().setPrettyPrinting().create().toJson(new
				// JsonParser().parse(result.toJSONString())));
				bw.close();
				entities.clear();
			}

		}
		
		System.out.println(titleSet.size());

	}
	
	public static List<Entity> entities = new ArrayList<>();

	public static String getPlainText(String input) {
		String result = input;
		int linkOpen = result.indexOf("<link>");
		int eluOpen = result.indexOf("<eld>");

		int close = -1;
		int bar = -1;
		String first = null;
		String second = null;
		String surface = null;
		String entity = null;
		int id = 0;

		while (linkOpen >= 0 || eluOpen >= 0) {
			if ((linkOpen >= 0 && linkOpen < eluOpen) || (linkOpen >= 0 && eluOpen == -1)) {
				bar = result.indexOf("|", linkOpen);
				close = result.indexOf("</link>", bar);
				surface = result.substring(linkOpen + 6, bar);
				entity = result.substring(bar + 1, close);
				first = result.substring(0, linkOpen);
				second = result.substring(close + 7, result.length());

				result = first + surface + second;

				int st = first.length();
				int en = st + surface.length();
				entities.add(new Entity(id, st, en, surface, entity, "WIKILINK"));
				id++;

				linkOpen = result.indexOf("<link>");
				eluOpen = result.indexOf("<eld>");
			} else if ((eluOpen >= 0 && eluOpen < linkOpen) || (eluOpen >= 0 && linkOpen == -1)) {
				bar = result.indexOf("|", eluOpen);
				close = result.indexOf("</eld>", bar);
				surface = result.substring(eluOpen + 5, bar);
				entity = result.substring(bar + 1, close);
				first = result.substring(0, eluOpen);
				second = result.substring(close + 6, result.length());

				result = first + surface + second;

				int st = first.length();
				int en = st + surface.length();
				entities.add(new Entity(id, st, en, surface, entity, "ELU"));

				linkOpen = result.indexOf("<link>");
				eluOpen = result.indexOf("<eld>");
			}
		}

		// while (linkOpen >= 0) {
		//
		// }
		//
		// linkOpen = result.indexOf("<elu>");
		// while (linkOpen >= 0) {
		//
		// }

		return result;
	}

}
