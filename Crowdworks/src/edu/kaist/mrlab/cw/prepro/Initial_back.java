package edu.kaist.mrlab.cw.prepro;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cedarsoftware.util.io.JsonWriter;

import edu.kaist.mrlab.cw.data.Entity;

public class Initial_back {

	public static void main(String[] ar) throws Exception {

		BufferedWriter bw = null;

		Map<String, List<String>> targetMap = new HashMap<>();
		HashMap<String, String> IDMap = new HashMap<>();
		HashMap<String, List<String>> kboxEntityTypes = new HashMap<>();

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

		br = Files.newBufferedReader(Paths.get("data/target/target_pargraph_list_part1.txt"));
		input = null;
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			String docID = st.nextToken();
			String parID = st.nextToken();

			List<String> parList = null;
			if (targetMap.containsKey(docID)) {
				parList = targetMap.get(docID);
			} else {
				parList = new ArrayList<String>();
			}
			parList.add(parID);
			targetMap.put(docID, parList);

		}

		br = Files.newBufferedReader(Paths.get("data/wiki-entity-tagged-corpus.tsv"));
		while ((input = br.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(input, "\t");
			if (st.countTokens() == 4) {
				String docID = st.nextToken();
				String parID = st.nextToken();
				String stcIDs = st.nextToken();
				String par = st.nextToken();

				IDMap.put(docID + "\t" + parID, par + "\t" + stcIDs);
			}
		}

		for (String docID : targetMap.keySet()) {
			List<String> parList = targetMap.get(docID);
			for (String parID : parList) {
				String values = IDMap.get(docID + "\t" + parID);
				if (values != null) {

					bw = Files.newBufferedWriter(
							Paths.get("data/annotated/wiki-10000-part2/task1_input/", docID + "_" + parID + ".json"));

					System.out.println(docID + "\t" + parID);

					StringTokenizer st = new StringTokenizer(values, "\t");
					String paragraph = st.nextToken();
					String gsIDs = st.nextToken();

					String plainText = getPlainText(paragraph).trim();

					JSONObject result = new JSONObject();
					result.put("plainText", plainText);
					result.put("docID", docID);
					result.put("parID", parID);
					result.put("globalSID", gsIDs);

					JSONArray entityArr = new JSONArray();
					for (Entity entity : entities) {
						JSONObject entityObj = new JSONObject();
						entityObj.put("id", entity.geteIdx());
						entityObj.put("surface", entity.getSurface());
						entityObj.put("entityName", entity.geteName());
						entityObj.put("st", entity.getSt());
						entityObj.put("en", entity.getEn());
						entityObj.put("eType", entity.geteType());
						entityArr.add(entityObj);

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

					result.put("entities", entityArr);

					bw.write(JsonWriter.formatJson(result.toString()));
					bw.close();
					entities.clear();
				}

			}
		}

	}

	public static List<Entity> entities = new ArrayList<>();

	public static String getPlainText(String input) {
		String result = input;
		int linkOpen = result.indexOf("<link>");
		int eluOpen = result.indexOf("<elu>");

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
				eluOpen = result.indexOf("<elu>");
			} else if ((eluOpen >= 0 && eluOpen < linkOpen) || (eluOpen >= 0 && linkOpen == -1)) {
				bar = result.indexOf("|", eluOpen);
				close = result.indexOf("</elu>", bar);
				surface = result.substring(eluOpen + 5, bar);
				entity = result.substring(bar + 1, close);
				first = result.substring(0, eluOpen);
				second = result.substring(close + 6, result.length());

				if (surface.length() < 3) {

					result = first + surface + second;

				} else {

					result = first + surface + second;

//					int st = first.length();
//					int en = st + surface.length();
//					entities.add(new Entity(id, st, en, surface, entity, "ELU"));
				}

				linkOpen = result.indexOf("<link>");
				eluOpen = result.indexOf("<elu>");
			}
		}

//		while (linkOpen >= 0) {
//
//		}
//
//		linkOpen = result.indexOf("<elu>");
//		while (linkOpen >= 0) {
//
//		}

		return result;
	}

}
